package org.phrenzy.moneycrew.discord.scrim.storage;

import org.phrenzy.moneycrew.discord.scrim.model.Scrim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class InMemoryScrimStorage implements ScrimStorage {

    private UUID lastAccessedScrim;
    private Map<UUID, Scrim> inMemoryMap = new HashMap<>();

    @Override
    public Scrim createScrim() {
        lastAccessedScrim = UUID.randomUUID();
        return Scrim.builder()
                .identifier(lastAccessedScrim)
                .participants(new HashSet<>(10))
                .status(true)
                .build();
    }

    @Override
    public Scrim fetchCurrentScrim() {
        return fetchScrim(lastAccessedScrim);
    }

    @Override
    public Scrim fetchScrim(final UUID identifier) {
        return inMemoryMap.get(identifier);
    }

    @Override
    public Scrim updateScrim(final Scrim scrim) {
        inMemoryMap.put(scrim.getIdentifier(), scrim);
        return scrim;
    }

    @Override
    public void deleteScrim(final Scrim scrim) {
        inMemoryMap.remove(scrim.getIdentifier());
    }

}
