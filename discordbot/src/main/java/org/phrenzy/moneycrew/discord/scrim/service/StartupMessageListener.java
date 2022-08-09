package org.phrenzy.moneycrew.discord.scrim.service;

import lombok.extern.log4j.Log4j2;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.util.logging.ExceptionLogger;
import org.phrenzy.moneycrew.discord.core.listener.MessageListener;
import org.phrenzy.moneycrew.discord.meta.listener.EmojiRoleReactionAddListener;
import org.phrenzy.moneycrew.discord.meta.listener.EmojiRoleReactionRemoveListener;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.phrenzy.moneycrew.discord.text.StringNormalisation.normalise;

@Log4j2
public class StartupMessageListener implements MessageListener<DiscordApi> {

    private static final long JMC_SERVER_ID = 474694402732851221L;
    private static final long ROLE_CHANNEL_ID = 878719940310499328L;
    private static final long WELCOME_CHANNEL_ID = 474694403156344833L;

    private final Map<KnownCustomEmoji, Role> emojiToRoleMap = new HashMap<>();

    @Override
    public void bindListeners(final DiscordApi api) {

        api.unsetActivity();
        api.updateActivity(ActivityType.WATCHING, "https://github.com/jonnu/moneycrew");

        final Map<String, Role> sanitisedRoles = api.getRoles().stream()
                .collect(Collectors.toMap(role -> normalise(role.getName()), Function.identity()));

        final Map<String, KnownCustomEmoji> sanitisedEmoji = api.getCustomEmojis().stream()
                .filter(emoji -> !emoji.isAnimated())
                .collect(Collectors.toMap(emoji -> normalise(emoji.getName()), Function.identity()));

        sanitisedEmoji.entrySet()
                .stream()
                .filter(ent -> sanitisedRoles.containsKey(ent.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(emoji -> emojiToRoleMap.put(emoji.getValue(), sanitisedRoles.get(emoji.getKey())));

        emojiToRoleMap.forEach((emoji, role) -> log.info("Bound {} --> {}", emoji.getName(), role.getName()));

        // start to build the message.
        final MessageBuilder builder = new MessageBuilder()
                .append("Role Administration", MessageDecoration.UNDERLINE)
                .appendNewLine()
                .appendNewLine()
                .append(emojiToRoleMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.comparing(Nameable::getName)))
                        .map(entry -> convertEmojiToMessageLine(entry.getKey()))
                        .collect(Collectors.joining("\n")))
                .appendNewLine()
                .appendNewLine()
                .append("React to this message to get access to game-specific channels.")
                .appendNewLine()
                .appendNewLine()
                .append(" ");

        // empty the old bois
        api.getChannelById(ROLE_CHANNEL_ID)
                .flatMap(Channel::asTextChannel)
                .map(c -> c.getMessages(25).thenAccept(messages -> {

                    final Optional<Message> existingMessage = messages.stream()
                            .filter(message -> message.getContent().trim().equals(builder.getStringBuilder().toString().trim()))
                            .findFirst();

                    existingMessage.ifPresent(message -> {
                        log.info("I found an existing post. I'll just clean the others I find.");
                        messages.stream().filter(m -> !m.equals(message)).forEach(Message::delete);
                    });

                    if (existingMessage.isEmpty()) {
                        log.info("I didn't see the message, so I'm going to post it once more.");
                        messages.getNewestMessage()
                                .map(Message::getChannel)
                                .map(channel -> builder.send(channel).thenAccept(message -> {
                                    log.info("Message: {}", message.getContent());
                                    message.addReactions(emojiToRoleMap.entrySet()
                                            .stream()
                                            .sorted(Map.Entry.comparingByValue(Comparator.comparing(Nameable::getName)))
                                            .map(Map.Entry::getKey)
                                            .toArray(KnownCustomEmoji[]::new));
                                    message.addReactionAddListener(new EmojiRoleReactionAddListener(message.getId(), emojiToRoleMap));
                                    message.addReactionRemoveListener(new EmojiRoleReactionRemoveListener(message.getId(), emojiToRoleMap));
                                }).exceptionally(ExceptionLogger.get()));
                        messages.deleteAll();
                    }
                }));

        final MessageBuilder welcomeMessage = new MessageBuilder()
                .append("Welcome to JONTE's MONEY CREW.")
                .appendNewLine()
                .appendNewLine()
                .append("**Rules**")
                .appendNewLine()
                .appendNewLine()
                .append(":money_with_wings: Feel free to invite others.")
                .appendNewLine()
                .append(":money_with_wings: Don't be a fucking dickhead.")
                .appendNewLine()
                .appendNewLine()
                .append("Invite link:")
                .appendNewLine()
                .append(getInviteLink(api).map(Invite::getUrl).orElse(null))
                .appendNewLine()
                .appendNewLine()
                .append("Faceit hub:")
                .appendNewLine()
                .append("https://www.faceit.com/en/inv/Q6f3Ygj");

        log.info("Welcome message going...");

        api.getChannelById(WELCOME_CHANNEL_ID)
                .flatMap(Channel::asTextChannel)
                .ifPresent(channel -> channel.getMessages(25)
                        .thenAccept(MessageSet::deleteAll)
                        .exceptionally(ExceptionLogger.get()));

        api.getChannelById(WELCOME_CHANNEL_ID)
                .flatMap(Channel::asTextChannel)
                .ifPresent(channel -> welcomeMessage.send(channel)
                        .exceptionally(ExceptionLogger.get()));
    }

    private Optional<Invite> getInviteLink(final DiscordApi api) {
        CompletableFuture<Invite> inviteFuture = new CompletableFuture<>();

        api.getServerById(JMC_SERVER_ID)
                .ifPresent(server -> server.getInvites()
                        .thenAccept(col -> col.stream()
                                .findFirst()
                                .ifPresent(inviteFuture::complete)));

        return Optional.ofNullable(inviteFuture.exceptionally(t -> null).join());
    }

    private static String convertEmojiToMessageLine(final KnownCustomEmoji emoji) {
        return new StringJoiner(" → ")
                .add(emoji.getMentionTag())
                .add(normalise(emoji.getName()))
                .toString();
    }

}
