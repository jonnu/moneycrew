package org.phrenzy.moneycrew.discord.core.observer;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;

public class ShutdownLifecycleObserver implements MessageEventObserver<DiscordApi> {

    @Override
    public boolean canHandle(final String message) {
        return message.startsWith("!quit");
    }

    @Override
    public void observe(final DiscordApi api, final MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotOwner() || event.getMessageAuthor().isServerAdmin()) {
            event.getChannel().sendMessage("I'm outtie...");
            api.disconnect();
        }
    }
}
