package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public interface IWorkstationUser {
    boolean canUseWorkstation();
    boolean isWorkstation(BlockPos pos);
    BlockPos workstationUseFromPos();
    boolean isUsingWorkAnim();
    void setUsingWorkAnim(boolean value);
    SoundEvent useAnimSound();
    default void writeWorkstationDataToNBT(NBTTagCompound compound) {
        compound.setBoolean("UsingWorkstation", this.isUsingWorkstation());
        if (compound.getBoolean("UsingWorkstation")) {
            compound.setInteger("WorkstationX", this.getWorkstationPos().getX());
            compound.setInteger("WorkstationY", this.getWorkstationPos().getY());
            compound.setInteger("WorkstationZ", this.getWorkstationPos().getZ());
        }

    }
    default void readWorkstationDataFromNBT(NBTTagCompound compound) {
        if (compound.getBoolean("UsingWorkstation")) this.setUseWorkstation(compound.getInteger("WorkstationX"), compound.getInteger("WorkstationY"), compound.getInteger("WorkstationZ"));
    }
    void setUseWorkstation(double x, double y, double z);
    void clearWorkstation(boolean destroyed);
    default void clearWorkstationMessage(boolean destroyed, EntityPlayer owner) {
        if (destroyed) owner.sendStatusMessage(new TextComponentTranslation("action.creature_workstation_destroyed"), false);
        else owner.sendStatusMessage(new TextComponentTranslation("action.clear_creature_workstation"), false);
    }
    boolean isUsingWorkstation();
    BlockPos getWorkstationPos();
}
