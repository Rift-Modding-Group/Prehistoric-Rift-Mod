package anightdazingzoroark.prift.server.entity.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public interface ILeadWorkstationUser {
    boolean canBeAttachedForWork();
    boolean isAttachableForWork(BlockPos pos);
    int pullPower();
    boolean isUsingLeadForWork();
    void setLeadAttachPos(double x, double y, double z);
    BlockPos getLeadWorkPos();
    void clearLeadAttachPos(boolean destroyed);
    default void clearLeadAttachPosMessage(boolean destroyed, EntityPlayer owner) {
        if (destroyed) owner.sendStatusMessage(new TextComponentTranslation("action.creature_workstation_destroyed"), false);
        else owner.sendStatusMessage(new TextComponentTranslation("action.clear_creature_workstation"), false);
    }
    default void writeLeadWorkDataToNBT(NBTTagCompound compound) {
        compound.setBoolean("UsingLeadForWork", this.isUsingLeadForWork());
        if (compound.getBoolean("UsingLeadForWork")) {
            compound.setInteger("LeadWorkPosX", this.getLeadWorkPos().getX());
            compound.setInteger("LeadWorkPosY", this.getLeadWorkPos().getY());
            compound.setInteger("LeadWorkPosZ", this.getLeadWorkPos().getZ());
        }

    }
    default void readLeadWorkDataFromNBT(NBTTagCompound compound) {
        if (compound.getBoolean("UsingLeadForWork")) this.setLeadAttachPos(compound.getInteger("LeadWorkPosX"), compound.getInteger("LeadWorkPosY"), compound.getInteger("LeadWorkPosZ"));
    }
}
