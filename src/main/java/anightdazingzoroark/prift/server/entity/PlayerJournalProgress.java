package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.server.enums.CreatureCategory;
import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerJournalProgress extends EntityProperties<EntityPlayer> {
    private final List<RiftCreatureType> unlockedCreatures = new ArrayList<>();

    @Override
    public int getTrackingTime() {
        return 20;
    }

    @Override
    public void init() {}

    @Override
    public void saveNBTData(NBTTagCompound nbtTagCompound) {
        NBTTagList entryList = new NBTTagList();
        if (!this.unlockedCreatures.isEmpty()) {
            Set<Integer> uniqueCreatureOrdinals = new HashSet<>();
            for (RiftCreatureType creatureType : this.unlockedCreatures) {
                uniqueCreatureOrdinals.add(creatureType.ordinal());
            }
            for (int ordinal : uniqueCreatureOrdinals) {
                entryList.appendTag(new NBTTagInt(ordinal));
            }
            nbtTagCompound.setTag("UnlockedCreatures", entryList);
        }
        else nbtTagCompound.setTag("UnlockedCreatures", entryList);
    }

    @Override
    public void loadNBTData(NBTTagCompound nbtTagCompound) {
        this.unlockedCreatures.clear();
        if (nbtTagCompound.hasKey("UnlockedCreatures")) {
            NBTTagList entryList = nbtTagCompound.getTagList("UnlockedCreatures", 3);
            for (int i = 0; i < entryList.tagCount(); i++) {
                this.unlockedCreatures.add(RiftCreatureType.values()[entryList.getIntAt(i)]);
            }
        }
    }

    @Override
    public String getID() {
        return "Player Journal Progress";
    }

    @Override
    public Class<EntityPlayer> getEntityClass() {
        return EntityPlayer.class;
    }

    public List<RiftCreatureType> getUnlockedCreatures() {
        return this.unlockedCreatures;
    }

    public void unlockCreature(RiftCreatureType creatureType) {
        this.unlockedCreatures.add(creatureType);
    }

    public void clearCreature(RiftCreatureType creatureType) {
        this.unlockedCreatures.remove(creatureType);
    }

    public void resetEntries() {
        this.unlockedCreatures.clear();
    }

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
