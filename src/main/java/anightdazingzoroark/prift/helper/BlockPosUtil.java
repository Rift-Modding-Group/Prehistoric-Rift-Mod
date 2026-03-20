package anightdazingzoroark.prift.helper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class BlockPosUtil {
    public static NBTTagCompound getBlockPosAsNBT(@NotNull BlockPos pos) {
        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setIntArray("BlockPos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        return toReturn;
    }

    public static BlockPos getBlockPosFromNBT(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound == null) return BlockPos.ORIGIN;
        int[] array = nbtTagCompound.getIntArray("BlockPos");
        return new BlockPos(array[0], array[1], array[2]);
    }
}
