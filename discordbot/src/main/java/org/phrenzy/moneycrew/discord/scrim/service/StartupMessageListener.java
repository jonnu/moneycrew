package org.phrenzy.moneycrew.discord.scrim.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.permission.Role;
import org.phrenzy.moneycrew.discord.meta.listener.EmojiRoleReactionAddListener;
import org.phrenzy.moneycrew.discord.meta.listener.EmojiRoleReactionRemoveListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class StartupMessageListener implements MessageListener<DiscordApi> {

    // todo: make dynamic.
    // todo: move to di'd dependency.
    private static final Map<String, String> EMOJI_ROLE_STRING_MAPPING = ImmutableMap.<String, String>builder()
            .put("jmc_hs", "CS:GO")
            .put("jmc_monster", "ROCKET LEAGUE")
            .put("jmc_malding", "VALORANT")
            .put("jmc_mightneck", "VALHEIM")
            .build();

    private final Map<Emoji, Role> emojiToRoleMap = new HashMap<>();

    @Override
    public void bindListeners(final DiscordApi api) {

        final Collection<Role> roles = api.getRoles();
        api.getCustomEmojis().forEach(emoji -> {
            if (EMOJI_ROLE_STRING_MAPPING.containsKey(emoji.getName())) {
                roles.stream()
                        .filter(role -> role.getName().toLowerCase().contains(EMOJI_ROLE_STRING_MAPPING.get(emoji.getName()).toLowerCase()))
                        .findFirst()
                        .ifPresent(role -> emojiToRoleMap.put(emoji, role));
            }
        });

        log.info("Mapping Created: {}", emojiToRoleMap);

        // start to build the message.
        final MessageBuilder builder = new MessageBuilder()
                .append("Role Administration", MessageDecoration.UNDERLINE)
                .appendNewLine()
                .appendNewLine()
                .append(emojiToRoleMap.entrySet().stream().map(e -> e.getKey().getMentionTag() + " " + e.getValue().getName()).collect(Collectors.joining("\n")))
                .appendNewLine()
                .appendNewLine();

        // empty the old bois
        api.getChannelById(878719940310499328L)
                .flatMap(Channel::asTextChannel)
                .map(x -> x.getMessages(25).thenAccept(messages -> {

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
                                    message.addReactions(emojiToRoleMap.keySet().toArray(new Emoji[0]));
                                    message.addReactionAddListener(new EmojiRoleReactionAddListener(message.getId(), emojiToRoleMap));
                                    message.addReactionRemoveListener(new EmojiRoleReactionRemoveListener(message.getId(), emojiToRoleMap));
                                }));
                    }
                }));
    }

}
