package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.enums.CreatureCategory;

import java.util.List;

public interface IPlayerJournalProgress {
    List<RiftCreatureType> getUnlockedCreatures();
    void setUnlockedCreatures(List<RiftCreatureType> value);
    void unlockCreature(RiftCreatureType creatureType);
    void clearCreature(RiftCreatureType creatureType);
    void resetEntries();
    List<CreatureCategory> getUnlockedCategories();
}
