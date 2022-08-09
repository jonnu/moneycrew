package org.phrenzy.moneycrew.discord.core.listener;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.DiscordApi;
import org.phrenzy.moneycrew.discord.core.observer.MessageEventObserver;

import java.util.Set;

@Log4j2
@AllArgsConstructor(onConstructor = @__(@Inject))
public class TextMessageListener implements MessageListener<DiscordApi> {

    private final Set<MessageEventObserver<DiscordApi>> observers;
    private static final String COMMAND_TRIGGER = "!";

    @Override
    public void bindListeners(final DiscordApi api) {

        api.addMessageCreateListener(event -> {

            final String message = event.getMessage().getContent().toLowerCase();

            if (event.getMessageAuthor().isYourself()) {
                log.debug("message (from self): {}", message);
                return;
            }

            // Ignore messages that do not start with a command trigger (for now).
            if (!message.startsWith(COMMAND_TRIGGER)) {
                return;
            }

            log.info("received command: {}", message);

            observers.stream()
                    .filter(observer -> observer.canHandle(message))
                    .forEach(observer -> observer.observe(api, event));
        });

        log.info("You can invite the bot by using the following url: {}", api.createBotInvite());
    }
}
