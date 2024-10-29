package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerJournalProgressStorage implements Capability.IStorage<IPlayerJournalProgress> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IPlayerJournalProgress> capability, IPlayerJournalProgress instance, EnumFacing side) {
        if (instance == null) return null;

        NBTTagCompound compound = new NBTTagCompound();

        NBTTagList entryList = new NBTTagList();
        if (!instance.getUnlockedCreatures().isEmpty()) {
            Set<Integer> uniqueCreatureOrdinals = new HashSet<>();
            for (RiftCreatureType creatureType : instance.getUnlockedCreatures()) {
                uniqueCreatureOrdinals.add(creatureType.ordinal());
            }
            for (int ordinal : uniqueCreatureOrdinals) {
                entryList.appendTag(new NBTTagInt(ordinal));
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
                NBTTagList entryList = compound.getTagList("UnlockedCreatures", 3);
                if (!entryList.isEmpty()) {
                    List<RiftCreatureType> finalUnlockedList = new ArrayList<>();
                    for (int i = 0; i < entryList.tagCount(); i++) {
                        finalUnlockedList.add(RiftCreatureType.values()[entryList.getIntAt(i)]);
                    }
                    instance.setUnlockedCreatures(finalUnlockedList);
                }
            }
        }
    }
}
