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
    public boolean settingCreatureWorkstation;
    public int creatureIdForWorkstation;
    public boolean isCaptured;

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

        this.settingCreatureWorkstation = false;
        this.creatureIdForWorkstation = -1;
        this.isCaptured = false;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        compound.setBoolean("DismountedCreature", this.ridingCreature);

        compound.setInteger("LeftClickFill", this.leftClickFill);
        compound.setInteger("RightClickFill", this.rightClickFill);
        compound.setBoolean("RCTrigger", this.rCTrigger);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        this.ridingCreature = compound.getBoolean("DismountedCreature");

        this.leftClickFill = compound.getInteger("LeftClickFill");
        this.rightClickFill = compound.getInteger("RightClickFill");
        this.rCTrigger = compound.getBoolean("RCTrigger");
    }

    @Override
    public String getID() {
        return "Prehistoric Rift Property Tracker";
    }

    @Override
    public Class<EntityLivingBase> getEntityClass() {
        return EntityLivingBase.class;
    }
}
