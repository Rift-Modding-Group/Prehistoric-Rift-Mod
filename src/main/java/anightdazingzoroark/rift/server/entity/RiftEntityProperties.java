package anightdazingzoroark.rift.server.entity;

import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public class RiftEntityProperties extends EntityProperties<EntityLivingBase> {
    public boolean ridingCreature;
    public int leftClickFill;
    public int rightClickFill;
    public boolean rCTrigger;
    public boolean isBleeding;
    public int bleedingStrength;
    public int ticksUntilStopBleeding;

    @Override
    public int getTrackingTime() {
        return 20;
    }

    @Override
    public void init() {
        this.ridingCreature = false;

        this.leftClickFill = 0;
        this.rightClickFill = 0;
        this.rCTrigger = false;

        this.isBleeding = false;
        this.bleedingStrength = -1;
        this.ticksUntilStopBleeding = 0;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setBoolean("DismountedCreature", ridingCreature);

        compound.setInteger("LeftClickFill", leftClickFill);
        compound.setInteger("RightClickFill", rightClickFill);
        compound.setBoolean("RCTrigger", rCTrigger);

        compound.setBoolean("IsBleeding", isBleeding);
        compound.setInteger("BleedingStrength", bleedingStrength);
        compound.setInteger("TicksUntilStopBleeding", ticksUntilStopBleeding);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        this.ridingCreature = compound.getBoolean("DismountedCreature");

        this.leftClickFill = compound.getInteger("LeftClickFill");
        this.rightClickFill = compound.getInteger("RightClickFill");
        this.rCTrigger = compound.getBoolean("RCTrigger");

        this.isBleeding = compound.getBoolean("IsBleeding");
        this.bleedingStrength = compound.getInteger("BleedingStrength");
        this.ticksUntilStopBleeding = compound.getInteger("TicksUntilStopBleeding");
    }

    @Override
    public String getID() {
        return "Prehistoric Rift Property Tracker";
    }

    @Override
    public Class<EntityLivingBase> getEntityClass() {
        return EntityLivingBase.class;
    }

    public void setBleeding(int strength, int ticks) {
        this.isBleeding = true;
        this.bleedingStrength = strength;
        this.ticksUntilStopBleeding = ticks;
    }

    public void resetBleeding() {
        this.isBleeding = false;
        this.bleedingStrength = -1;
        this.ticksUntilStopBleeding = 0;
    }
}
