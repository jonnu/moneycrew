package org.phrenzy.moneycrew.discord.core.observer;

import org.javacord.api.event.message.MessageCreateEvent;

public interface MessageEventObserver<T> {
    boolean canHandle(final String message);
    void observe(final T service, final MessageCreateEvent event);

}
