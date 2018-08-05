package org.phrenzy.moneycrew.discord.scrim.observer;

import com.google.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.phrenzy.moneycrew.discord.scrim.service.MessageEventObserver;
import org.phrenzy.moneycrew.discord.scrim.model.Scrim;
import org.phrenzy.moneycrew.discord.scrim.storage.ScrimStorage;

import java.util.stream.Collectors;

@Slf4j
public class ScrimObserver implements MessageEventObserver<DiscordApi> {

    private final ScrimStorage storage;

    @Inject
    public ScrimObserver(@NonNull final ScrimStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean canHandle(String message) {
        return message.startsWith("!scrim");
    }

    @Override
    public void observe(final DiscordApi api, final MessageCreateEvent event) {

        Server server = event.getServer().orElse(null);
        User user = event.getMessage().getAuthor().asUser().orElse(null);

        if (server == null || user == null) {
            log.warn("Event received that I could not obtain server/user information from: {}", event);
            return;
        }

        Scrim scrim = storage.fetchCurrentScrim();
        if (scrim == null) {
            scrim = storage.createScrim();
        }

        if (scrim.getParticipants().contains(user)) {
            scrim.getParticipants().remove(user);
        } else {
            scrim.getParticipants().add(user);
        }

        event.getChannel().sendMessage(String.format("Scrim: %s. Currently got: %d players! Joined:", scrim.getIdentifier(), scrim.getParticipants().size()));
        event.getChannel().sendMessage(scrim.getParticipants()
                .stream().map(participant -> participant.getDisplayName(server))
                .collect(Collectors.joining(", ")));

        if (scrim.isFull()) {
            event.getChannel().sendMessage("Scrim is full ya big JESSIE.");
        }
    }
}
