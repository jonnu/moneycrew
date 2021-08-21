package org.phrenzy.moneycrew.discord.faceit.model;

import lombok.Value;

import java.util.Map;

@Value
public class Game {
    String gameProfileId;
    String gamePlayerId;
    String gamePlayerName;
    String region;
    Map<String, Region> regions;
    Integer skillLevel;
    String skillLevelLabel;
    Integer faceitElo;
}
