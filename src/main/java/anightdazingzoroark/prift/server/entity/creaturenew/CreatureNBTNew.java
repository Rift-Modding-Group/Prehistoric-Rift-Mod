package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.server.entity.creaturenew.info.CreatureNBTKeywordNew;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureBuilder;
import net.minecraft.nbt.NBTTagCompound;

/**
 * a wrapper for NBTTagCompound for creatures meant for use in UIs and packets
 * */
public class CreatureNBTNew implements IRiftCreature {
    public final NBTTagCompound nbtTagCompound;

    public CreatureNBTNew(NBTTagCompound nbtTagCompound) {
        this.nbtTagCompound = nbtTagCompound;
    }

    //-----so much boilerplate code incoming-----
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
}
