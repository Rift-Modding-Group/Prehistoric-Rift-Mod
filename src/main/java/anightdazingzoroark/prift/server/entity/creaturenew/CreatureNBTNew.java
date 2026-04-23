package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.info.CreatureNBTKeywordNew;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.AbstractCreatureBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * a wrapper for NBTTagCompound for creatures meant for use in UIs and packets
 * */
public class CreatureNBTNew implements IRiftCreature {
    public final NBTTagCompound nbtTagCompound;

    public CreatureNBTNew(NBTTagCompound nbtTagCompound) {
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
        return CreatureNBTKeywordNew.CREATURE_TYPE.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public int getLevel() {
        if (this.nbtTagCompound.isEmpty()) return 0;
        return CreatureNBTKeywordNew.LEVEL.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setLevel(int value) {
        CreatureNBTKeywordNew.LEVEL.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public int getAgeInTicks() {
        if (this.nbtTagCompound.isEmpty()) return 0;
        return CreatureNBTKeywordNew.AGE_IN_TICKS.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setAgeInTicks(int value) {
        CreatureNBTKeywordNew.AGE_IN_TICKS.setValueInNBT(this.nbtTagCompound, value);
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
        return CreatureNBTKeywordNew.STAMINA.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setStamina(float value) {
        CreatureNBTKeywordNew.STAMINA.setValueInNBT(this.nbtTagCompound, value);
    }

    @Override
    public float getMaxStamina() {
        return this.getAttributeValue("rift.stamina");
    }

    @Override
    public CreatureMoveStorage getCreatureMoves() {
        if (this.nbtTagCompound.isEmpty()) return null;
        return CreatureNBTKeywordNew.CREATURE_MOVES.getValueFromNBT(this.nbtTagCompound);
    }

    @Override
    public void setCreatureMoves(CreatureMoveStorage value) {
        CreatureNBTKeywordNew.CREATURE_MOVES.setValueInNBT(this.nbtTagCompound, value);
    }
}
