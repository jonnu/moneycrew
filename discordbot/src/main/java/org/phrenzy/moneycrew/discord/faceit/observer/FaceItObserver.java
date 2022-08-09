package org.phrenzy.moneycrew.discord.faceit.observer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.phrenzy.moneycrew.discord.faceit.model.Player;
import org.phrenzy.moneycrew.discord.core.observer.MessageEventObserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

@Slf4j
public class FaceItObserver implements MessageEventObserver<DiscordApi> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    private static final String COMMAND = "!faceit";

    @Override
    public boolean canHandle(final String message) {
        return message.startsWith("!faceit");
    }

    @Override
    public void observe(final DiscordApi service, final MessageCreateEvent event) {

        String username = event.getMessage().getContent().substring(COMMAND.length() + 1);

        OkHttpClient client = new OkHttpClient.Builder().build();
        try {

            log.info("Got username {}", username);

            Response response = client.newCall(new Request.Builder()
                    .addHeader("Authorization", "Bearer 026be46b-5b60-41a8-a725-23f04b3befa0")
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .url("https://open.faceit.com/data/v4/players?game=csgo&nickname=" + username)
                    .get()
                    .build()).execute();

            if (response.isSuccessful()) {

                log.info("Successful ({}) API call to: {}", response.code(), response.request().url());

                Player player = Optional.ofNullable(response.body())
                        .map(body -> safelyMapStreamToClass(body.byteStream(), Player.class))
                        .orElse(null);

                log.info("Player: {}", MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(player));

                if (player == null) {
                    return;
                }

                event.getChannel().sendMessage(String.format("%s :flag_%s: (faceit level: %d, faceit ELO: %s)",
                        player.getNickname(),
                        player.getCountry().toLowerCase(Locale.getDefault()),
                        player.getGames().get("csgo").getSkillLevel(),
                        player.getGames().get("csgo").getFaceitElo())
                );
            }
            else {
                log.info("failed: {} {}", response.code(), response.message());
            }
        }
        catch (Exception e) {
            event.getChannel().sendMessage(e.getMessage());
        }
    }

    private static <T> T safelyMapStreamToClass(final InputStream stream, final Class<T> clazz) {
        try {
            return MAPPER.readValue(stream, clazz);
        } catch (IOException exception) {
            log.error("Failed to map to {}", clazz, exception);
            return null;
        }
    }
}
