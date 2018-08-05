package org.phrenzy.moneycrew.discord.scrim.model;

import lombok.Builder;
import lombok.Data;
import org.javacord.api.entity.user.User;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class Scrim {

    private UUID identifier;
    private boolean status;
    private Set<User> participants;

    public boolean isFull() {
        return participants.size() == 10;
    }
}
