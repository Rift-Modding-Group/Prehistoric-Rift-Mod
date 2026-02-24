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
        if (instance == null) return new NBTTagList();
        return instance.getProgressAsNBTList();
    }

    @Override
    public void readNBT(Capability<IPlayerJournalProgress> capability, IPlayerJournalProgress instance, EnumFacing side, NBTBase nbt) {
        if (instance == null || nbt == null) return;

        if (nbt instanceof NBTTagList entryNBTList) instance.parseNBTListToProgress(entryNBTList);
    }
}
