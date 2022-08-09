package org.phrenzy.moneycrew.discord.core.observer;

import com.google.inject.Inject;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;

public class PingObserver implements MessageEventObserver<DiscordApi> {

    private long inMemoryCounter;

    @Inject
    public PingObserver() {
        inMemoryCounter = 0;
    }

    @Override
    public boolean canHandle(String message) {
        return message.startsWith("!ping");
    }

    public void observe(final DiscordApi api, final MessageCreateEvent event) {
        event.getChannel().sendMessage(String.format("pong! (I've received %d ping%s...)", ++inMemoryCounter, inMemoryCounter == 1 ? "" : "s"));
    }

}
