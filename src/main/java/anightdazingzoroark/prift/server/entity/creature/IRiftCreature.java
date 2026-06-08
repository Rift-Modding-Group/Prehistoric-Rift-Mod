package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.server.entity.creature.info.RiftCreatureEnums;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import net.minecraft.nbt.NBTTagCompound;

/**
 * This is more or less a helper interface for creature information.
 * */
public interface IRiftCreature {
    default CreatureNBTKeyword<?>[] getNBTKeywords() {
        return new CreatureNBTKeyword[]{
                CreatureNBTKeyword.CREATURE_TYPE,
                CreatureNBTKeyword.LEVEL,
                CreatureNBTKeyword.NATURE,
                CreatureNBTKeyword.AGE_IN_TICKS,
                CreatureNBTKeyword.STAMINA,
                CreatureNBTKeyword.CREATURE_STATS,
                CreatureNBTKeyword.CREATURE_MOVES
        };
    }
    default void writeCreatureNBT(NBTTagCompound nbtTagCompound) {
        for (CreatureNBTKeyword<?> keyword : this.getNBTKeywords()) {
            keyword.writeToNBT(nbtTagCompound, this);
        }
    }
    default void readCreatureNBT(NBTTagCompound nbtTagCompound) {
        for (CreatureNBTKeyword<?> keyword : this.getNBTKeywords()) {
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
    RiftLibInventoryHandler getCreatureInventory();
    CreatureStatsStorage getCreatureStats();
    void setCreatureStats(CreatureStatsStorage value);
    CreatureMoveStorage getCreatureMoves();
    void setCreatureMoves(CreatureMoveStorage value);
}
