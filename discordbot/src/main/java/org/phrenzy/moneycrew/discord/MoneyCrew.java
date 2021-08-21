package org.phrenzy.moneycrew.discord;

import com.google.common.collect.ImmutableList;
import com.google.inject.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.phrenzy.moneycrew.discord.di.DiscordModule;
import org.phrenzy.moneycrew.discord.scrim.service.MessageListener;

import java.util.Optional;

public class MoneyCrew {

    private static final String ENV_TOKEN = "TOKEN";

    public static void main(final String[] args) {

        if (args.length == 0) {
            throw new IllegalStateException("DiscordApi token required, but was not passed.");
        }

        final Injector injector = Guice.createInjector(Stage.PRODUCTION, ImmutableList.of(new DiscordModule()));
        final MessageListener<DiscordApi> listener = injector.getInstance(Key.get(new TypeLiteral<MessageListener<DiscordApi>>() {}));

        // Read the token from env (or the first program parameter) when invoking the bot.
        final String token = Optional.ofNullable(System.getenv(ENV_TOKEN))
                .orElse(args[0].substring(args[0].indexOf('=') + 1));

        new DiscordApiBuilder()
                .setToken(token)
                .login()
                .thenAccept(listener::bindListeners)
                .exceptionally(ExceptionLogger.get());
    }

}
