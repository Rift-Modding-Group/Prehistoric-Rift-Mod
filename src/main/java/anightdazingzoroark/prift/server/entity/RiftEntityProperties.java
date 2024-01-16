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
    private String entityUUID;

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

        this.entityUUID = "";
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

        compound.setString("EntityUUID", this.entityUUID);
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

        this.entityUUID = compound.getString("EntityUUID");
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

    public void setUUID(UUID uuid) {
        this.entityUUID = uuid.toString();
    }

    public UUID getUUID() {
        return UUID.fromString(this.entityUUID);
    }

    public EntityLivingBase getEntityFromUUID(World world, UUID uuid) {
        if (!world.isRemote) {
            List<EntityLivingBase> entities = world.getEntities(EntityLivingBase.class, null);
            for (EntityLivingBase entity : entities) {
                RiftEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(entity, RiftEntityProperties.class);
                if (properties.getUUID().equals(uuid)) return entity;
            }
        }
        return null;
    }
}
