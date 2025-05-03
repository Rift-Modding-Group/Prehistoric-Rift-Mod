package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerJournalProgressStorage implements Capability.IStorage<IPlayerJournalProgress> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IPlayerJournalProgress> capability, IPlayerJournalProgress instance, EnumFacing side) {
        if (instance == null) return null;

        NBTTagCompound compound = new NBTTagCompound();

        NBTTagList entryList = new NBTTagList();
        if (!instance.getEncounteredCreatures().isEmpty()) {
            for (Map.Entry<RiftCreatureType, Boolean> entry : instance.getEncounteredCreatures().entrySet()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Creature", (byte) entry.getKey().ordinal());
                tagCompound.setBoolean("IsUnlocked", entry.getValue());
                entryList.appendTag(tagCompound);
            }
            compound.setTag("UnlockedCreatures", entryList);
        }
        else compound.setTag("UnlockedCreatures", entryList);

        return compound;
    }

    @Override
    public void readNBT(Capability<IPlayerJournalProgress> capability, IPlayerJournalProgress instance, EnumFacing side, NBTBase nbt) {
        if (instance == null || nbt == null) return;

        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound)nbt;

            if (compound.hasKey("UnlockedCreatures")) {
                NBTTagList entryList = compound.getTagList("UnlockedCreatures", 10);
                if (!entryList.isEmpty()) {
                    Map<RiftCreatureType, Boolean> finalUnlockedList = new HashMap<>();
                    for (int i = 0; i < entryList.tagCount(); i++) {
                        NBTTagCompound nbtEntry = (NBTTagCompound) entryList.get(i);
                        finalUnlockedList.put(RiftCreatureType.values()[nbtEntry.getByte("Creature")], nbtEntry.getBoolean("IsUnlocked"));
                    }
                    instance.setEncounteredCreatures(finalUnlockedList);
                }
            }
        }
    }
}
