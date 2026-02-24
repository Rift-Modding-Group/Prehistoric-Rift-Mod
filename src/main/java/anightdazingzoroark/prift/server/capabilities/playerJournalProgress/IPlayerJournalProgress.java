package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.nbt.NBTTagList;

import java.util.List;
import java.util.Map;

public interface IPlayerJournalProgress {
    Map<RiftCreatureType, Boolean> getEncounteredCreatures();
    void setEncounteredCreatures(Map<RiftCreatureType, Boolean> value);
    void discoverCreature(RiftCreatureType creatureType);
    void unlockCreature(RiftCreatureType creatureType);
    void clearCreature(RiftCreatureType creatureType);
    void unlockAllEntries();
    void resetEntries();
    List<RiftCreatureType.CreatureCategory> getUnlockedCategories();

    //-----for parsing and exporting this into nbt-----
    NBTTagList getProgressAsNBTList();
    void parseNBTListToProgress(NBTTagList nbtTagList);
}
