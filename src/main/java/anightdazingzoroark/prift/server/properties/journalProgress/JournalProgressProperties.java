package anightdazingzoroark.prift.server.properties.journalProgress;

import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.HashMapPropertyValue;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalProgressProperties extends AbstractEntityProperties<EntityPlayer> {
    public JournalProgressProperties(@NotNull String key, @NotNull EntityPlayer entityHolder) {
        super(key, entityHolder);
    }

    @Override
    protected void registerDefaults(EntityPlayer entity) {
        this.register(new HashMapPropertyValue<RiftCreatureType, Boolean> (
                "EncounteredCreatures",
                hashMap -> {
                    NBTTagList encounteredNBT = new NBTTagList();
                    for (Map.Entry<RiftCreatureType, Boolean> encounteredEntry : hashMap.entrySet()) {
                        NBTTagCompound entry = new NBTTagCompound();
                        entry.setByte("Creature", (byte) encounteredEntry.getKey().ordinal());
                        entry.setBoolean("IsUnlocked", encounteredEntry.getValue());
                        encounteredNBT.appendTag(entry);
                    }
                    return encounteredNBT;
                },
                nbtBase -> {
                    if (!(nbtBase instanceof NBTTagList encounteredNBT)) return new HashMap<>();
                    HashMap<RiftCreatureType, Boolean> toReturn = new HashMap<>();
                    for (int index = 0; index < encounteredNBT.tagCount(); index++) {
                        NBTTagCompound entry = encounteredNBT.getCompoundTagAt(index);
                        toReturn.put(
                                RiftCreatureType.values()[entry.getByte("Creature")],
                                entry.getBoolean("IsUnlocked")
                        );
                    }
                    return toReturn;
                }
        ));
    }

    //-----direct progress editing and getting-----
    public HashMap<RiftCreatureType, Boolean> getEncounteredCreatures() {
        return this.get("EncounteredCreatures");
    }

    public void setEncounteredCreatures(HashMap<RiftCreatureType, Boolean> encounteredCreatures) {
        this.set("EncounteredCreatures", encounteredCreatures);
    }

    //-----indirect editing and getting-----
    public void discoverCreature(RiftCreatureType creatureType) {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        encounteredCreatures.put(creatureType, false);
        this.setEncounteredCreatures(encounteredCreatures);
    }

    public void unlockCreature(RiftCreatureType creatureType) {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        encounteredCreatures.put(creatureType, true);
        this.setEncounteredCreatures(encounteredCreatures);
    }

    public void clearCreature(RiftCreatureType creatureType) {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        encounteredCreatures.remove(creatureType);
        this.setEncounteredCreatures(encounteredCreatures);
    }

    public List<RiftCreatureType.CreatureCategory> getUnlockedCategories() {
        List<RiftCreatureType.CreatureCategory> toReturn = new ArrayList<>();
        for (RiftCreatureType.CreatureCategory category : RiftCreatureType.CreatureCategory.values()) {
            if (!category.equals(RiftCreatureType.CreatureCategory.ALL)) {
                for (RiftCreatureType creatureType : this.getEncounteredCreatures().keySet()) {
                    if (creatureType.getCreatureCategory().equals(category) && !toReturn.contains(creatureType.getCreatureCategory())) {
                        toReturn.add(creatureType.getCreatureCategory());
                    }
                }
            }
            else if (!this.getEncounteredCreatures().isEmpty()) toReturn.add(category);
        }
        return toReturn;
    }

    public void resetEntries() {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        encounteredCreatures.clear();
        this.setEncounteredCreatures(encounteredCreatures);
    }

    public void unlockAllEntries() {
        if (this.getEntityHolder().world.isRemote) return;
        HashMap<RiftCreatureType, Boolean> encounteredCreatures = this.getEncounteredCreatures();
        for (RiftCreatureType creatureType : RiftCreatureType.values()) {
            encounteredCreatures.put(creatureType, true);
        }
        this.setEncounteredCreatures(encounteredCreatures);
    }
}
