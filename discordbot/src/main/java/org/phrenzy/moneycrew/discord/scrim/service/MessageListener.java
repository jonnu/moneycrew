package org.phrenzy.moneycrew.discord.scrim.service;

public interface MessageListener<T> {
    void bindListeners(T obj);
}
