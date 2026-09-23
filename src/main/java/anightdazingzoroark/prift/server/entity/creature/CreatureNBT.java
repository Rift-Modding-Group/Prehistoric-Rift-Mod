package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * a wrapper for NBTTagCompound for creatures meant for use in UIs and packets
 */
public record CreatureNBT(@NotNull NBTTagCompound nbtTagCompound) implements IRiftCreature {
    public static final CreatureNBT EMPTY_NBT = new CreatureNBT(new NBTTagCompound());

    private float getAttributeValue(String value) {
        NBTTagList attributeList = this.nbtTagCompound.getTagList("Attributes", 10);
        for (int x = 0; x < attributeList.tagCount(); x++) {
            NBTTagCompound tagCompound = attributeList.getCompoundTagAt(x);
            if (!tagCompound.hasKey("Name") || !tagCompound.getString("Name").equals(value)) continue;
            return (float) tagCompound.getDouble("Base");
        }
        return 0;
    }

    //-----so much boilerplate code from IRiftCreature incoming-----
    @Override
    public String getName() {
        return this.getName(true);
    }

    @Override
    public String getName(boolean showLevel) {
        if (this.nbtTagCompound.isEmpty() || this.getCreatureType() == null) return "???";
        String toReturn = this.hasCustomName() ? this.getCustomNameTag() : this.getCreatureType().getLocalizedName();
        if (showLevel) toReturn = toReturn + " (" + I18n.format("info.level", this.getLevel()) + ")";
        return toReturn;
    }

    @Override
    public RiftCreatureBuilder getCreatureType() {
        if (this.nbtTagCompound.isEmpty()) return null;
        return CreatureNBTKeyword.CREATURE_TYPE.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public int getLevel() {
        if (this.nbtTagCompound.isEmpty()) return 0;
        return CreatureNBTKeyword.LEVEL.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setLevel(int value) {
        CreatureNBTKeyword.LEVEL.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public RiftCreatureEnums.Nature getNature() {
        if (this.nbtTagCompound.isEmpty()) return null;
        return CreatureNBTKeyword.NATURE.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setNature(RiftCreatureEnums.Nature value) {
        CreatureNBTKeyword.NATURE.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public int getAgeInTicks() {
        if (this.nbtTagCompound.isEmpty()) return 0;
        return CreatureNBTKeyword.AGE_IN_TICKS.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setAgeInTicks(int value) {
        CreatureNBTKeyword.AGE_IN_TICKS.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public float getHealth() {
        return this.nbtTagCompound.getFloat("Health");
    }

    @Override
    public float getMaxHealth() {
        return this.getAttributeValue("generic.maxHealth");
    }

    @Override
    public float getStamina() {
        if (this.nbtTagCompound.isEmpty()) return 0f;
        return CreatureNBTKeyword.STAMINA.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setStamina(float value) {
        CreatureNBTKeyword.STAMINA.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public float getMaxStamina() {
        return this.getAttributeValue("rift.stamina");
    }

    @Override
    public RiftLibInventoryHandler getCreatureInventory() {
        if (this.nbtTagCompound.isEmpty()) return null;
        return CreatureNBTKeyword.INVENTORY.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setCreatureInventory(RiftLibInventoryHandler value) {
        CreatureNBTKeyword.INVENTORY.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public CreatureStatsStorage getCreatureStats() {
        if (this.nbtTagCompound.isEmpty()) return null;
        return CreatureNBTKeyword.CREATURE_STATS.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setCreatureStats(CreatureStatsStorage value) {
        CreatureNBTKeyword.CREATURE_STATS.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public CreatureMoveStorage getCreatureMoves() {
        if (this.nbtTagCompound.isEmpty()) return null;
        CreatureMoveStorage creatureMoves = CreatureNBTKeyword.CREATURE_MOVES.getValueFromNBT(this.nbtTagCompound);
        creatureMoves.setCreatureUser(this.getCreatureType());
        return creatureMoves;
    }

    @Override
    public void setCreatureMoves(CreatureMoveStorage value) {
        CreatureNBTKeyword.CREATURE_MOVES.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public RiftCreatureEnums.TameTargeting getTameTargeting() {
        if (this.nbtTagCompound.isEmpty()) return null;
        return CreatureNBTKeyword.TAME_TARGETING.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setTameTargeting(@NotNull RiftCreatureEnums.TameTargeting value) {
        CreatureNBTKeyword.TAME_TARGETING.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public CreatureAcquisitionInfo getAcquisitionInfo() {
        return CreatureNBTKeyword.ACQUISITION_INFO.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setAcquisitionInfo(@NotNull CreatureAcquisitionInfo value) {
        CreatureNBTKeyword.ACQUISITION_INFO.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public RiftCreatureEnums.CreatureDeployment getDeploymentType() {
        if (this.nbtTagCompound.isEmpty()) return null;
        return CreatureNBTKeyword.DEPLOYMENT_TYPE.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setDeploymentType(RiftCreatureEnums.CreatureDeployment value) {
        CreatureNBTKeyword.DEPLOYMENT_TYPE.setValueInNBT(this.nbtTagCompound, value);
    }

    //-----helper nbt code incoming-----
    @Override
    public UUID getUniqueID() {
        return this.nbtTagCompound.getUniqueId("UUID");
    }

    @Override
    public boolean isTamed() {
        if (this.nbtTagCompound.isEmpty()) return false;
        return this.nbtTagCompound.hasKey("OwnerUUID");
    }

    @Override
    public boolean isOwner(EntityLivingBase entity) {
        if (this.nbtTagCompound.isEmpty() || entity == null) return false;
        if (!this.nbtTagCompound.hasKey("OwnerUUID", 8)) return false;
        return entity.getUniqueID().toString().equals(this.nbtTagCompound.getString("OwnerUUID"));
    }

    @Override
    public boolean hasCustomName() {
        if (this.nbtTagCompound.isEmpty()) return false;
        return this.nbtTagCompound.hasKey("CustomName") && !this.nbtTagCompound.getString("CustomName").isEmpty();
    }

    @Override
    public String getCustomNameTag() {
        if (this.nbtTagCompound.isEmpty()) return "???";
        return this.nbtTagCompound.getString("CustomName");
    }

    @Override
    public void setCustomNameTag(String value) {
        this.nbtTagCompound.setString("CustomName", value);
    }
}
