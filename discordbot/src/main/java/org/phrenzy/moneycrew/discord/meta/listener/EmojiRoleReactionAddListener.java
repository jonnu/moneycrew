package org.phrenzy.moneycrew.discord.meta.listener;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import java.util.Map;
import java.util.Optional;

@Log4j2
@AllArgsConstructor
public class EmojiRoleReactionAddListener implements ReactionAddListener {

    private final long id;
    private final Map<KnownCustomEmoji, Role> map;

    @Override
    public void onReactionAdd(final ReactionAddEvent event) {
        event.requestUser().thenAccept(user -> {
            if (user.isYourself()) {
                log.info("I will not react to myself.");
                return;
            }

            event.requestMessage().thenAccept(message -> {
                if (message.getId() == id) {
                    Optional.ofNullable(map.get(event.getEmoji()))
                            .ifPresent(user::addRole);
                }
            });
        });
    }
}
