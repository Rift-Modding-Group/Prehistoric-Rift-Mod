package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.info.CreatureNBTKeywordNew;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.inventory.CreatureInventoryHandler;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import net.minecraft.nbt.NBTTagCompound;

public interface IRiftCreature {
    default CreatureNBTKeywordNew<?>[] getNBTKeywords() {
        return new CreatureNBTKeywordNew[]{
                CreatureNBTKeywordNew.CREATURE_TYPE,
                CreatureNBTKeywordNew.LEVEL,
                CreatureNBTKeywordNew.NATURE,
                CreatureNBTKeywordNew.AGE_IN_TICKS,
                CreatureNBTKeywordNew.STAMINA,
                CreatureNBTKeywordNew.CREATURE_STATS,
                CreatureNBTKeywordNew.CREATURE_MOVES
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
    RiftCreatureEnums.Nature getNature();
    void setNature(RiftCreatureEnums.Nature value);
    int getAgeInTicks();
    void setAgeInTicks(int value);
    float getHealth();
    float getMaxHealth();
    float getStamina();
    void setStamina(float value);
    float getMaxStamina();
    RiftInventoryHandler getCreatureInventory();
    CreatureStatsStorage getCreatureStats();
    void setCreatureStats(CreatureStatsStorage value);
    CreatureMoveStorage getCreatureMoves();
    void setCreatureMoves(CreatureMoveStorage value);
}
