package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.api.creature.config.RiftCreatureConfig;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureAcquisitionInfo;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureNBTKeyword;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

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
                CreatureNBTKeyword.INVENTORY,
                CreatureNBTKeyword.CREATURE_STATS,
                CreatureNBTKeyword.CREATURE_MOVES,
                CreatureNBTKeyword.TAME_TARGETING,
                CreatureNBTKeyword.ACQUISITION_INFO,
                CreatureNBTKeyword.DEPLOYMENT_TYPE,
                CreatureNBTKeyword.EAT_FROM_INVENTORY
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

    @NotNull
    default RiftCreatureConfig getCreatureConfig() {
        RiftCreatureConfig config = ServerProxy.jsonConfigParser.getCreatureConfig(this.getCreatureType().getName());

        if (config == null) {
            throw new IllegalStateException("No creature config found for creature type " + this.getCreatureType().getName());
        }

        return config;
    }

    @NotNull
    default String getDisplayString(@NotNull RiftCreatureEnums.Stats stat) {
        return switch (stat) {
            case HEALTH -> Math.round(this.getHealth()) + " / " + Math.round(this.getMaxHealth());
            case MELEE_DAMAGE, ELEMENTAL_DAMAGE -> String.valueOf(this.getCreatureStats().getValueForStat(stat, this.getLevel(), this.getNature(), true));
            case STAMINA -> Math.round(this.getStamina()) + " / " + Math.round(this.getMaxStamina());
            case SPEED -> {
                double statValue = this.getCreatureStats().getValueForStat(stat, this.getLevel(), this.getNature(), true);
                yield BigDecimal.valueOf(statValue).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            }
        };
    }

    //-----boilerplate code incomin... omaga...-----
    String getName();
    String getName(boolean showLevel);
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
    void setCreatureInventory(RiftLibInventoryHandler value);
    CreatureStatsStorage getCreatureStats();
    void setCreatureStats(CreatureStatsStorage value);
    CreatureMoveStorage getCreatureMoves();
    void setCreatureMoves(CreatureMoveStorage value);
    RiftCreatureEnums.TameTargeting getTameTargeting();
    void setTameTargeting(@NotNull RiftCreatureEnums.TameTargeting value);
    CreatureAcquisitionInfo getAcquisitionInfo();
    void setAcquisitionInfo(@NotNull CreatureAcquisitionInfo value);
    RiftCreatureEnums.CreatureDeployment getDeploymentType();
    void setDeploymentType(@Nullable RiftCreatureEnums.CreatureDeployment value);
    boolean getEatFromInventory();
    void setEatFromInventory(boolean value);

    //-----same but these are helpers----
    UUID getUniqueID();
    boolean isTamed();
    boolean isOwner(EntityLivingBase entity);
    boolean hasCustomName();
    String getCustomNameTag();
    void setCustomNameTag(String value);
}
