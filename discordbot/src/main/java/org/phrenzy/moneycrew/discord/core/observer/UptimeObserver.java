package org.phrenzy.moneycrew.discord.core.observer;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.phrenzy.moneycrew.discord.scrim.service.MessageEventObserver;
import org.threeten.extra.AmountFormats;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class UptimeObserver implements MessageEventObserver<DiscordApi> {

    private final LocalDateTime initialised;

    public UptimeObserver() {
        initialised = LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public boolean canHandle(final String message) {
        return message.startsWith("!uptime");
    }

    @Override
    public void observe(final DiscordApi api, final MessageCreateEvent event) {
        final LocalDateTime eventTime = LocalDateTime.ofInstant(event.getMessage().getCreationTimestamp(), ZoneOffset.UTC);
        final Duration timeDelta = Duration.between(initialised, eventTime).truncatedTo(ChronoUnit.SECONDS);
        event.getChannel().sendMessage(AmountFormats.wordBased(timeDelta, Locale.ENGLISH));
    }
}
