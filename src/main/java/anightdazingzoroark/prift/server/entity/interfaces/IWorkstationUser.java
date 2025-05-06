package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Map;

public interface IWorkstationUser {
    Map<String, Boolean> getWorkstations();
    default boolean isWorkstation(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        for (String workstationName : this.getWorkstations().keySet()) {
            Block blockFromString = Block.getBlockFromName(workstationName);
            if (blockFromString != null && blockFromString.equals(block)) return true;
        }
        return false;
    }
    BlockPos workstationUseFromPos();
    SoundEvent useAnimSound();
    default void writeWorkstationDataToNBT(NBTTagCompound compound) {
        compound.setBoolean("HasWorkstation", this.hasWorkstation());
        if (compound.getBoolean("HasWorkstation")) {
            compound.setInteger("WorkstationX", this.getWorkstationPos().getX());
            compound.setInteger("WorkstationY", this.getWorkstationPos().getY());
            compound.setInteger("WorkstationZ", this.getWorkstationPos().getZ());
        }

    }
    default void readWorkstationDataFromNBT(NBTTagCompound compound) {
        if (compound.getBoolean("HasWorkstation")) this.setUseWorkstation(compound.getInteger("WorkstationX"), compound.getInteger("WorkstationY"), compound.getInteger("WorkstationZ"));
    }
    void setUseWorkstation(double x, double y, double z);
    void clearWorkstation(boolean destroyed);
    default void clearWorkstationMessage(boolean destroyed, EntityPlayer owner) {
        if (destroyed) owner.sendStatusMessage(new TextComponentTranslation("action.creature_workstation_destroyed"), false);
        else owner.sendStatusMessage(new TextComponentTranslation("action.clear_creature_workstation"), false);
    }
    boolean hasWorkstation();
    BlockPos getWorkstationPos();
}
