package org.phrenzy.moneycrew.discord.di;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.javacord.api.DiscordApi;
import org.phrenzy.moneycrew.discord.core.observer.ShutdownLifecycleObserver;
import org.phrenzy.moneycrew.discord.core.observer.StatusObserver;
import org.phrenzy.moneycrew.discord.core.observer.UptimeObserver;
import org.phrenzy.moneycrew.discord.faceit.observer.FaceItObserver;
import org.phrenzy.moneycrew.discord.scrim.service.MessageEventObserver;
import org.phrenzy.moneycrew.discord.core.observer.PingObserver;
import org.phrenzy.moneycrew.discord.scrim.observer.ScrimObserver;
import org.phrenzy.moneycrew.discord.scrim.service.MessageListener;
import org.phrenzy.moneycrew.discord.scrim.service.StartupMessageListener;
import org.phrenzy.moneycrew.discord.scrim.service.TextMessageListener;
import org.phrenzy.moneycrew.discord.scrim.storage.InMemoryScrimStorage;
import org.phrenzy.moneycrew.discord.scrim.storage.ScrimStorage;

import java.util.Set;

public class DiscordModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ScrimStorage.class).to(InMemoryScrimStorage.class);

        Multibinder<MessageListener<DiscordApi>> listeners = Multibinder.newSetBinder(binder(), new TypeLiteral<MessageListener<DiscordApi>>() {});
        listeners.addBinding().to(TextMessageListener.class);
        listeners.addBinding().to(StartupMessageListener.class);
    }

    @Provides
    public Set<MessageEventObserver<DiscordApi>> bindAllMessageEventObservers(final ScrimStorage scrimStorage) {
        return ImmutableSet.of(
                new ShutdownLifecycleObserver(),
                new PingObserver(),
                new StatusObserver(),
                new UptimeObserver(),
                new ScrimObserver(scrimStorage),
                new FaceItObserver()
        );
    }
}
