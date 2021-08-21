package org.phrenzy.moneycrew.discord;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.phrenzy.moneycrew.discord.di.DiscordModule;
import org.phrenzy.moneycrew.discord.scrim.service.MessageListener;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class MoneyCrew {

    private static final char ARG_SEPARATOR = '=';
    private static final String ENV_TOKEN = "TOKEN";

    public static void main(final String[] args) {

        final Injector injector = Guice.createInjector(Stage.PRODUCTION, ImmutableList.of(new DiscordModule()));
        final Set<MessageListener<DiscordApi>> listeners = injector.getInstance(Key.get(new TypeLiteral<Set<MessageListener<DiscordApi>>>() {}));

        // Read the token from env (or the first program parameter) when invoking the bot.
        final String token = getTokenFromEnvironment()
                .or(() -> getTokenFromArguments(args))
                .orElseThrow(() -> new IllegalArgumentException("DiscordApi token missing"));

        new DiscordApiBuilder()
                .setToken(token)
                .login()
                .thenAccept(api -> listeners.forEach(listener -> listener.bindListeners(api)))
                .exceptionally(ExceptionLogger.get());
    }

    private static Optional<String> getTokenFromEnvironment() {
        return Optional.ofNullable(System.getenv(ENV_TOKEN));
    }

    private static Optional<String> getTokenFromArguments(final String[] args) {

        if (args == null || args.length == 0) {
            return Optional.empty();
        }

        return Arrays.stream(args)
                .filter(arg -> arg.indexOf(ARG_SEPARATOR) != -1)
                .map(arg -> arg.substring(arg.indexOf(ARG_SEPARATOR) + 1))
                .findFirst();
    }

}
