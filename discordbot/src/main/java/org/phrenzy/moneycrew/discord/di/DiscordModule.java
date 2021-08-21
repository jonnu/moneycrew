package org.phrenzy.moneycrew.discord.di;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import org.javacord.api.DiscordApi;
import org.phrenzy.moneycrew.discord.faceit.observer.FaceItObserver;
import org.phrenzy.moneycrew.discord.scrim.service.MessageEventObserver;
import org.phrenzy.moneycrew.discord.scrim.observer.PingObserver;
import org.phrenzy.moneycrew.discord.scrim.observer.ScrimObserver;
import org.phrenzy.moneycrew.discord.scrim.service.MessageListener;
import org.phrenzy.moneycrew.discord.scrim.service.TextMessageListener;
import org.phrenzy.moneycrew.discord.scrim.storage.InMemoryScrimStorage;
import org.phrenzy.moneycrew.discord.scrim.storage.ScrimStorage;

import java.util.Set;

public class DiscordModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ScrimStorage.class).to(InMemoryScrimStorage.class);
        bind(new TypeLiteral<MessageListener<DiscordApi>>() {}).to(TextMessageListener.class);
    }

    @Provides
    public Set<MessageEventObserver<DiscordApi>> bindAllMessageEventObservers(final ScrimStorage scrimStorage) {
        return ImmutableSet.of(
                new PingObserver(),
                new ScrimObserver(scrimStorage),
                new FaceItObserver()
        );
    }
}
