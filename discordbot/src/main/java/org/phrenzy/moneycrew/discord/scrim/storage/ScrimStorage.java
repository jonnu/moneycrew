package org.phrenzy.moneycrew.discord.scrim.storage;

import org.phrenzy.moneycrew.discord.scrim.model.Scrim;

import java.util.UUID;

public interface ScrimStorage {
    Scrim createScrim();
    Scrim fetchCurrentScrim();
    Scrim fetchScrim(final UUID identifier);
    Scrim updateScrim(final Scrim scrim);
    void deleteScrim(final Scrim scrim);
}
