package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import net.minecraft.nbt.NBTTagCompound;

public interface ITurretModeUser {
    boolean isTurretMode();
    void setTurretMode(boolean value);
    TurretModeTargeting getTurretTargeting();
    void setTurretModeTargeting(TurretModeTargeting turretModeTargeting);
    default void writeTurretModeDataToNBT(NBTTagCompound compound) {
        compound.setBoolean("TurretMode", this.isTurretMode());
        compound.setByte("TurretTargeting", (byte) this.getTurretTargeting().ordinal());
    }
    default void readTurretModeDataFromNBT(NBTTagCompound compound) {
        this.setTurretMode(compound.getBoolean("TurretMode"));
        this.setTurretModeTargeting(TurretModeTargeting.values()[compound.getByte("TurretTargeting")]);
    }
}
