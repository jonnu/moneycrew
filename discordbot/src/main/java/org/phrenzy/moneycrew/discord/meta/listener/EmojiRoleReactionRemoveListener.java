package org.phrenzy.moneycrew.discord.meta.listener;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

import java.util.Map;
import java.util.Optional;

@Log4j2
@AllArgsConstructor
public class EmojiRoleReactionRemoveListener implements ReactionRemoveListener {

    private final long id;
    private final Map<KnownCustomEmoji, Role> map;

    @Override
    public void onReactionRemove(final ReactionRemoveEvent event) {
        event.requestUser().thenAccept(user -> {
            if (user.isYourself()) {
                log.info("I will not react to myself.");
                return;
            }

            event.requestMessage().thenAccept(message -> {
                if (message.getId() == id) {
                    Optional.ofNullable(map.get(event.getEmoji()))
                            .ifPresent(user::removeRole);
                }
            });
        });
    }
}
