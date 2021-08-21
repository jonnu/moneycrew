package org.phrenzy.moneycrew.discord.faceit.model;

import lombok.Value;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Value
public class Player {
    String nickname;
    String country;
    Map<String, Game> games;
    Set<UUID> friendsIds;
}
