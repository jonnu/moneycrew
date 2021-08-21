package org.phrenzy.moneycrew.discord.scrim.service;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import org.javacord.api.DiscordApi;

import java.util.Set;

@AllArgsConstructor(onConstructor = @__(@Inject))
public class TextMessageListener implements MessageListener<DiscordApi> {

    private final Set<MessageEventObserver<DiscordApi>> observers;
    private static final String COMMAND_TRIGGER = "!";

    @Override
    public void bindListeners(final DiscordApi api) {

        api.addMessageCreateListener(event -> {

            final String message = event.getMessage().getContent().toLowerCase();
            System.out.println("Rec: " + message);

            // Ignore messages that do not start with a command trigger (for now).
            if (!message.startsWith(COMMAND_TRIGGER)) {
                return;
            }

            observers.stream()
                    .filter(observer -> observer.canHandle(message))
                    .forEach(observer -> observer.observe(api, event));
        });

        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }
}
