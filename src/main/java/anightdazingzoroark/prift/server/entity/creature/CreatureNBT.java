package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.server.entity.creature.info.RiftCreatureEnums;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * a wrapper for NBTTagCompound for creatures meant for use in UIs and packets
 * */
public class CreatureNBT implements IRiftCreature {
    public final NBTTagCompound nbtTagCompound;

    public CreatureNBT(NBTTagCompound nbtTagCompound) {
        this.nbtTagCompound = nbtTagCompound;
    }

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
        return null;
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
        return CreatureNBTKeyword.CREATURE_MOVES.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setCreatureMoves(CreatureMoveStorage value) {
        CreatureNBTKeyword.CREATURE_MOVES.setValueInNBT(this.nbtTagCompound, value);
    }
}
