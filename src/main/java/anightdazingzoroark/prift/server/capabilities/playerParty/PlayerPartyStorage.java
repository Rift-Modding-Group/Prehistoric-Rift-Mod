package anightdazingzoroark.prift.server.capabilities.playerParty;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

public class PlayerPartyStorage implements Capability.IStorage<IPlayerParty> {
    @Override
    public @Nullable NBTBase writeNBT(Capability<IPlayerParty> capability, IPlayerParty instance, EnumFacing side) {
        if (instance == null) return null;
        return instance.getPartyAsNBTList();
    }

    @Override
    public void readNBT(Capability<IPlayerParty> capability, IPlayerParty instance, EnumFacing side, NBTBase nbt) {
        if (instance == null || nbt == null) return;
        if (!(nbt instanceof NBTTagList tagList)) return;
        instance.parseNBTListToParty(tagList);
    }
}
