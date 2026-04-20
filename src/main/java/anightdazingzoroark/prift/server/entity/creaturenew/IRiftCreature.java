package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.server.entity.creaturenew.info.CreatureNBTKeywordNew;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureBuilder;
import net.minecraft.nbt.NBTTagCompound;

public interface IRiftCreature {
    default CreatureNBTKeywordNew<?>[] getNBTKeywords() {
        return new CreatureNBTKeywordNew[]{
                CreatureNBTKeywordNew.CREATURE_TYPE,
                CreatureNBTKeywordNew.LEVEL,
                CreatureNBTKeywordNew.AGE_IN_TICKS,
                CreatureNBTKeywordNew.STAMINA
        };
    }
    default void writeCreatureNBT(NBTTagCompound nbtTagCompound) {
        for (CreatureNBTKeywordNew<?> keyword : this.getNBTKeywords()) {
            keyword.writeToNBT(nbtTagCompound, this);
        }
    }
    default void readCreatureNBT(NBTTagCompound nbtTagCompound) {
        for (CreatureNBTKeywordNew<?> keyword : this.getNBTKeywords()) {
            keyword.readToNBT(nbtTagCompound, this);
        }
    }

    //-----boilerplate code incomin... omaga...-----
    RiftCreatureBuilder getCreatureType();
    int getLevel();
    void setLevel(int value);
    int getAgeInTicks();
    void setAgeInTicks(int value);
    float getHealth();
    float getMaxHealth();
    float getStamina();
    void setStamina(float value);
    float getMaxStamina();
}
