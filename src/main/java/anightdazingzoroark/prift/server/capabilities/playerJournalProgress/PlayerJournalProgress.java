package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.enums.CreatureCategory;

import java.util.ArrayList;
import java.util.List;

public class PlayerJournalProgress implements IPlayerJournalProgress {
    private List<RiftCreatureType> unlockedCreatures = new ArrayList<>();

    @Override
    public List<RiftCreatureType> getUnlockedCreatures() {
        return this.unlockedCreatures;
    }

    @Override
    public void setUnlockedCreatures(List<RiftCreatureType> value) {
        this.unlockedCreatures = value;
    }

    @Override
    public void unlockCreature(RiftCreatureType creatureType) {
        this.unlockedCreatures.add(creatureType);
    }

    @Override
    public void clearCreature(RiftCreatureType creatureType) {
        this.unlockedCreatures.remove(creatureType);
    }

    @Override
    public void resetEntries() {
        this.unlockedCreatures.clear();
    }

    @Override
    public List<CreatureCategory> getUnlockedCategories() {
        List<CreatureCategory> categoryList = new ArrayList<>();
        for (CreatureCategory category : CreatureCategory.values()) {
            if (!category.equals(CreatureCategory.ALL)) {
                for (RiftCreatureType creatureType : this.unlockedCreatures) {
                    if (creatureType.getCreatureCategory().equals(category) && !categoryList.contains(creatureType.getCreatureCategory())) {
                        categoryList.add(creatureType.getCreatureCategory());
                    }
                }
            }
            else if (!this.unlockedCreatures.isEmpty()) categoryList.add(category);
        }
        return categoryList;
    }
}
