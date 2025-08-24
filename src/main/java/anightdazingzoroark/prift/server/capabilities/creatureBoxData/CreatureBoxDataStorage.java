package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class CreatureBoxDataStorage implements Capability.IStorage<ICreatureBoxData> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ICreatureBoxData> capability, ICreatureBoxData instance, EnumFacing side) {
        if (instance == null) return null;
        NBTTagCompound compound = new NBTTagCompound();

        NBTTagList tagList = new NBTTagList();
        for (BlockPos creatureBoxPos : instance.getCreatureBoxPositions()) {
            NBTTagCompound posNBT = new NBTTagCompound();
            posNBT.setInteger("BoxPosX", creatureBoxPos.getX());
            posNBT.setInteger("BoxPosY", creatureBoxPos.getY());
            posNBT.setInteger("BoxPosZ", creatureBoxPos.getZ());
            tagList.appendTag(posNBT);
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
                    NBTTagCompound posNBT = tagList.getCompoundTagAt(x);
                    BlockPos creatureBoxPos = new BlockPos(
                            posNBT.getInteger("BoxPosX"),
                            posNBT.getInteger("BoxPosY"),
                            posNBT.getInteger("BoxPosZ")
                    );
                    instance.getCreatureBoxPositions().add(creatureBoxPos);
                }
            }
        }
    }
}
