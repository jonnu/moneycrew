package org.phrenzy.moneycrew.discord.core.observer;

import lombok.extern.log4j.Log4j2;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.phrenzy.moneycrew.discord.scrim.service.MessageEventObserver;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Log4j2
public class StatusObserver implements MessageEventObserver<DiscordApi> {

    @Override
    public boolean canHandle(final String message) {
        return message.startsWith("!status");
    }

    @Override
    public void observe(final DiscordApi api, final MessageCreateEvent event) {

        try {
            event.getChannel().sendMessage("Host: " + InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException exception) {
            log.error("Unable to fetch status", exception);
        }
    }
}
