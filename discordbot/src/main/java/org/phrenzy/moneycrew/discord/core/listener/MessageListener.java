package org.phrenzy.moneycrew.discord.core.listener;

public interface MessageListener<T> {
    void bindListeners(T obj);
}
