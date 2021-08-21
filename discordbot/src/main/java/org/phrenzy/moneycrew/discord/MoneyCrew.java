package org.phrenzy.moneycrew.discord;

import com.google.common.collect.ImmutableList;
import com.google.inject.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.phrenzy.moneycrew.discord.di.DiscordModule;
import org.phrenzy.moneycrew.discord.scrim.service.MessageListener;

public class MoneyCrew {

    public static void main(final String[] args) {

        Injector injector = Guice.createInjector(Stage.PRODUCTION, ImmutableList.of(
                new DiscordModule()
        ));

        MessageListener<DiscordApi> listener = injector.getInstance(Key.get(new TypeLiteral<MessageListener<DiscordApi>>() {}));

        // Read the token from the first program parameter when invoking the bot
        String token = args[0].substring(args[0].indexOf('=') + 1);

        new DiscordApiBuilder()
                .setToken(token)
                .login()
                .thenAccept(listener::bindListeners)
                .exceptionally(ExceptionLogger.get());
    }

}
