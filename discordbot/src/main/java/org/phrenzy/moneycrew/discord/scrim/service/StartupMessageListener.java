package org.phrenzy.moneycrew.discord.scrim.service;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class StartupMessageListener implements MessageListener<DiscordApi> {

    // todo: make dynamic.
    private static Map<String, String> EMOJI_ROLE_STRING_MAPPING = ImmutableMap.<String, String>builder()
            .put("jmc_hs", "CS:GO")
            .put("jmc_monster", "ROCKET LEAGUE")
            .put("jmc_malding", "VALORANT")
            .put("jmc_mightneck", "VALHEIM")
            .build();

    private final Map<Emoji, Role> emojiToRoleMap = new HashMap<>();

    @Override
    public void bindListeners(final DiscordApi api) {

        log.info("StartupMessageListener bound");

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

        // empty the old bois
        api.getChannelById(878719940310499328L)
                .flatMap(Channel::asTextChannel)
                .map(x -> x.getMessages(100).thenAccept(MessageSet::deleteAll));

        api.getChannelById(878719940310499328L)
                .flatMap(Channel::asTextChannel)
                .map(channel -> new MessageBuilder()
                        .append("Role Administration", MessageDecoration.UNDERLINE)
                        .appendNewLine()
                        .appendNewLine()
                        .append(emojiToRoleMap.entrySet().stream().map(e -> e.getKey().getMentionTag() + " " + e.getValue().getName()).collect(Collectors.joining("\n")))
                        .appendNewLine()
                        .appendNewLine()
                        .send(channel)
                        .thenAccept(message -> {
                            message.addReactions(emojiToRoleMap.keySet().toArray(new Emoji[0]));
                            message.addReactionAddListener(new EmojiRoleReactionAddListener(message.getId(), emojiToRoleMap));
                            message.addReactionRemoveListener(new EmojiRoleReactionRemoveListener(message.getId(), emojiToRoleMap));
                        }));

        // Debugging.
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().toLowerCase().startsWith("!roles")) {
                event.getChannel().sendMessage(event.getMessage().getUserAuthor().map(x -> new HashSet<>(x.getRoles(event.getServer().orElse(null)))).toString());
            }
        });

        api.addServerBecomesAvailableListener(event -> {

            log.info("ServerBecomesAvailable triggered.");

            event.getServer()
                    .getTextChannels()
                    .stream()
                    .filter(channel -> channel.getName().toLowerCase().contains(api.getYourself().getName().toLowerCase()))
                    .collect(Collectors.toList())
                    .forEach(channel -> channel.sendMessage("Hello."));
        });
    }

    @AllArgsConstructor
    class EmojiRoleReactionAddListener implements ReactionAddListener {

        private final long id;
        private final Map<Emoji, Role> map;

        @Override
        public void onReactionAdd(final ReactionAddEvent event) {

            if (event.getUser().map(User::isYourself).orElse(false)) {
                log.info("I will not react to myself.");
                return;
            }

            if (event.getMessageId() == id && map.containsKey(event.getEmoji())) {
                event.requestUser().thenAccept(user -> {
                    user.addRole(map.get(event.getEmoji()));
                    log.info("Added role {} to user {}", map.get(event.getEmoji()), user);
                });
            }
        }
    }

    @AllArgsConstructor
    class EmojiRoleReactionRemoveListener implements ReactionRemoveListener {

        private final long id;
        private final Map<Emoji, Role> map;

        @Override
        public void onReactionRemove(final ReactionRemoveEvent event) {

            if (event.getUser().map(User::isYourself).orElse(false)) {
                log.info("I will not react to myself.");
                return;
            }

            if (event.getMessageId() == id && map.containsKey(event.getEmoji())) {
                log.info("Removing role {} from user {}", map.get(event.getEmoji()), event.getUser());
                event.requestUser().thenAccept(user -> {
                    user.removeRole(map.get(event.getEmoji()));
                    log.info("Removed role {} from user {}", map.get(event.getEmoji()), user);
                });
            }
        }
    }
}
