package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CreatureBoxDataStorage implements Capability.IStorage<ICreatureBoxData> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ICreatureBoxData> capability, ICreatureBoxData instance, EnumFacing side) {
        if (instance == null) return null;
        NBTTagCompound compound = new NBTTagCompound();

        NBTTagList tagList = new NBTTagList();
        for (CreatureBoxInfo creatureBoxInfo : instance.getCreatureBoxInformation()) {
            NBTTagCompound infoNBT = creatureBoxInfo.toNBT();
            tagList.appendTag(infoNBT);
        }
        compound.setTag("BoxPositions", tagList);

        return compound;
    }

    @Override
    public void readNBT(Capability<ICreatureBoxData> capability, ICreatureBoxData instance, EnumFacing side, NBTBase nbt) {
        if (instance == null || nbt == null) return;
        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) nbt;

            if (compound.hasKey("BoxPositions")) {
                NBTTagList tagList = compound.getTagList("BoxPositions", 10);
                for (int x = 0; x < tagList.tagCount(); x++) {
                    NBTTagCompound infoNBT = tagList.getCompoundTagAt(x);
                    CreatureBoxInfo info = new CreatureBoxInfo(infoNBT);
                    instance.getCreatureBoxInformation().add(info);
                }
            }
        }
    }
}
