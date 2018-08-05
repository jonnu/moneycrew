package org.phrenzy.moneycrew.discord.scrim.service;

import org.javacord.api.event.message.MessageCreateEvent;

public interface MessageEventObserver<T> {
    boolean canHandle(final String message);
    void observe(final T service, final MessageCreateEvent event);

}
