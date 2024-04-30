package anightdazingzoroark.prift.server.entity;

import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.List;

public class RiftEntityProperties extends EntityProperties<EntityLivingBase> {
    public boolean ridingCreature;
    public int leftClickFill;
    public int rightClickFill;
    public boolean rCTrigger;
    public boolean isBleeding;
    public int bleedingStrength;
    public int ticksUntilStopBleeding;
    public boolean settingCreatureWorkstation;
    public int creatureIdForWorkstation;
    public boolean isCaptured;
    public boolean isTiedByBola;
    public int bolaTiedCountdown;

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

        this.settingCreatureWorkstation = false;
        this.creatureIdForWorkstation = -1;
        this.isCaptured = false;

        this.isTiedByBola = false;
        this.bolaTiedCountdown = 0;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setBoolean("DismountedCreature", this.ridingCreature);

        compound.setInteger("LeftClickFill", this.leftClickFill);
        compound.setInteger("RightClickFill", this.rightClickFill);
        compound.setBoolean("RCTrigger", this.rCTrigger);

        compound.setBoolean("IsBleeding", this.isBleeding);
        compound.setInteger("BleedingStrength", this.bleedingStrength);
        compound.setInteger("TicksUntilStopBleeding", this.ticksUntilStopBleeding);

        compound.setBoolean("IsTiedByBola", this.isTiedByBola);
        compound.setInteger("BolaCountdown", this.bolaTiedCountdown);
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

        this.isTiedByBola = compound.getBoolean("IsTiedByBola");
        this.bolaTiedCountdown = compound.getInteger("BolaCountdown");
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

    public void setBolaCapture(int ticks) {
        this.isTiedByBola = true;
        this.bolaTiedCountdown = ticks;
    }

    public void resetBolaCapture() {
        this.isTiedByBola = false;
        this.bolaTiedCountdown = 0;
    }
}
