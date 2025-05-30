package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;

import java.util.*;

public class PlayerJournalProgress implements IPlayerJournalProgress {
    private Map<RiftCreatureType, Boolean> encounteredCreatures = new HashMap<>();

    @Override
    public Map<RiftCreatureType, Boolean> getEncounteredCreatures() {
        return this.encounteredCreatures;
    }

    @Override
    public void setEncounteredCreatures(Map<RiftCreatureType, Boolean> value) {
        this.encounteredCreatures = value;
    }

    @Override
    public void discoverCreature(RiftCreatureType creatureType) {
        if (!this.encounteredCreatures.containsKey(creatureType)) this.encounteredCreatures.put(creatureType, false);
    }

    @Override
    public void unlockCreature(RiftCreatureType creatureType) {
        if (this.encounteredCreatures.containsKey(creatureType)) this.encounteredCreatures.replace(creatureType, true);
        else this.encounteredCreatures.put(creatureType, true);
    }

    @Override
    public void clearCreature(RiftCreatureType creatureType) {
        this.encounteredCreatures.remove(creatureType);
    }

    @Override
    public void unlockAllEntries() {
        for (RiftCreatureType creatureType : RiftCreatureType.values()) this.unlockCreature(creatureType);
    }

    @Override
    public void resetEntries() {
        this.encounteredCreatures.clear();
    }

    @Override
    public List<RiftCreatureType.CreatureCategory> getUnlockedCategories() {
        List<RiftCreatureType.CreatureCategory> categoryList = new ArrayList<>();
        for (RiftCreatureType.CreatureCategory category : RiftCreatureType.CreatureCategory.values()) {
            if (!category.equals(RiftCreatureType.CreatureCategory.ALL)) {
                for (RiftCreatureType creatureType : this.encounteredCreatures.keySet()) {
                    if (creatureType.getCreatureCategory().equals(category) && !categoryList.contains(creatureType.getCreatureCategory())) {
                        categoryList.add(creatureType.getCreatureCategory());
                    }
                }
            }
            else if (!this.encounteredCreatures.isEmpty()) categoryList.add(category);
        }
        return categoryList;
    }
}
