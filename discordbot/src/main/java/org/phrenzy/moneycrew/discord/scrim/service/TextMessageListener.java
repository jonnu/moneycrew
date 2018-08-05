package org.phrenzy.moneycrew.discord.scrim.service;

import com.google.inject.Inject;
import lombok.NonNull;
import org.javacord.api.DiscordApi;

import java.util.Set;

public class TextMessageListener implements MessageListener<DiscordApi> {

    private Set<MessageEventObserver<DiscordApi>> observers;
    private static final String COMMAND_TRIGGER = "!";

    @Inject
    TextMessageListener(@NonNull Set<MessageEventObserver<DiscordApi>> observers) {
        this.observers = observers;
    }

    @Override
    public void bindListeners(final DiscordApi api) {

        api.addMessageCreateListener(event -> {

            final String message = event.getMessage().getContent().toLowerCase();

            // Ignore messages that do not start with a command trigger (for now).
            if (!message.substring(0, 1).equals(COMMAND_TRIGGER)) {
                return;
            }

            observers.stream()
                    .filter(observer -> observer.canHandle(message))
                    .forEach(observer -> observer.observe(api, event));
        });

        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }
}
