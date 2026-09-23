package anightdazingzoroark.prift.server.entity.creature.info;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.util.MathUtil;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A creature's stats are to be stored here
 * */
public class CreatureStatsStorage {
    //the final stats of a creature
    @NotNull
    private final Map<RiftCreatureEnums.Stats, StatValueHolder> stats = Map.of(
            RiftCreatureEnums.Stats.HEALTH, new StatValueHolder(RiftCreatureEnums.Stats.HEALTH),
            RiftCreatureEnums.Stats.MELEE_DAMAGE, new StatValueHolder(RiftCreatureEnums.Stats.MELEE_DAMAGE),
            RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE, new StatValueHolder(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE),
            RiftCreatureEnums.Stats.STAMINA, new StatValueHolder(RiftCreatureEnums.Stats.STAMINA),
            RiftCreatureEnums.Stats.SPEED, new StatValueHolder(RiftCreatureEnums.Stats.SPEED)
    );

    //individual values are to be created here
    public void initializeIndividualValues(@NotNull Random random) {
        for (StatValueHolder statValueHolder : this.stats.values()) {
            statValueHolder.individualValue = random.nextInt(0, 20);
        }
    }

    //is to be run once, no need to repeatedly execute
    public void parseStats(@NotNull Map<RiftCreatureEnums.Stats, Double> creatureBaseStats) {
        for (Map.Entry<RiftCreatureEnums.Stats, Double> statBaseEntry : creatureBaseStats.entrySet()) {
            StatValueHolder valueHolder = this.stats.get(statBaseEntry.getKey());
            valueHolder.baseValue = statBaseEntry.getValue();
        }
    }

    //also to be run once, but for applying the stats to a creature
    public void applyStatsToCreature(@NotNull RiftCreature creature) {
        //-----apply health-----
        double finalHealth = this.getValueForStat(RiftCreatureEnums.Stats.HEALTH, creature.getLevel(), creature.getNature(), true);
        creature.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(finalHealth);
        creature.heal((float) finalHealth);

        //-----apply melee attack-----
        double finalMeleeAttack = this.getValueForStat(RiftCreatureEnums.Stats.MELEE_DAMAGE, creature.getLevel(), creature.getNature(), true);
        creature.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(finalMeleeAttack);

        //-----apply elemental attack-----
        double finalElementalAttack = this.getValueForStat(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE, creature.getLevel(), creature.getNature(), true);
        creature.getEntityAttribute(RiftCreature.ELEMENTAL_DAMAGE_ATTRIBUTE).setBaseValue(finalElementalAttack);

        //-----apply stamina-----
        double finalStamina = this.getValueForStat(RiftCreatureEnums.Stats.STAMINA, creature.getLevel(), creature.getNature(), true);
        creature.getEntityAttribute(RiftCreature.STAMINA_ATTRIBUTE).setBaseValue(finalStamina);
        creature.setStamina((float) finalStamina);

        //-----apply speed-----
        double finalSpeed = this.getValueForStat(RiftCreatureEnums.Stats.SPEED, creature.getLevel(), creature.getNature(), true);
        creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(finalSpeed);
    }

    public double getValueForStat(@NotNull RiftCreatureEnums.Stats stat, int creatureLevel, @Nullable RiftCreatureEnums.Nature nature, boolean includeIV) {
        return this.stats.get(stat).getValue(creatureLevel, nature, includeIV);
    }

    public double getValueForStatUnmodified(@NotNull RiftCreatureEnums.Stats stat) {
        return this.getValueForStat(stat, 0, null, false);
    }

    public double getIndividualValueForStat(@NotNull RiftCreatureEnums.Stats stat) {
        return this.stats.get(stat).individualValue;
    }

    @NotNull
    public NBTTagCompound getAsNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        //store stats
        NBTTagList statTagList = new NBTTagList();
        for (Map.Entry<RiftCreatureEnums.Stats, StatValueHolder> entry : this.stats.entrySet()) {
            NBTTagCompound toAppend = new NBTTagCompound();
            toAppend.setByte("Stat", (byte) entry.getKey().ordinal());
            toAppend.setDouble("BaseValue", entry.getValue().baseValue);
            toAppend.setInteger("IndividualValue", entry.getValue().individualValue);
            statTagList.appendTag(toAppend);
        }
        toReturn.setTag("Stats", statTagList);

        return toReturn;
    }

    public void readFromNBT(@NotNull NBTTagCompound nbtTagCompound) {
        NBTTagList statTagList = nbtTagCompound.getTagList("Stats", 10);
        for (int index = 0; index < statTagList.tagCount(); index++) {
            NBTTagCompound statNBT = statTagList.getCompoundTagAt(index);

            int statOrdinal = statNBT.getByte("Stat");
            if (statOrdinal < 0 || statOrdinal >= RiftCreatureEnums.Stats.values().length) continue;
            RiftCreatureEnums.Stats stat = RiftCreatureEnums.Stats.values()[statOrdinal];
            double baseValue = statNBT.getDouble("BaseValue");
            int individualValue = statNBT.getInteger("IndividualValue");

            CreatureStatsStorage.StatValueHolder statValueHolder = this.stats.get(stat);
            statValueHolder.baseValue = baseValue;
            statValueHolder.individualValue = individualValue;
        }
    }

    private static class StatValueHolder {
        @NotNull
        private final RiftCreatureEnums.Stats stat;
        private double baseValue;
        private int individualValue;

        private StatValueHolder(@NotNull RiftCreatureEnums.Stats stat) {
            this.stat = stat;
        }

        private double getValue(int level, @Nullable RiftCreatureEnums.Nature nature, boolean includeIV) {
            double toReturn = this.stat.parseBaseValue(this.baseValue);

            //level modification
            if (level > 0 && this.stat.getAffectedByLeveling()) {
                toReturn += toReturn * 0.1D * (level - 1);
            }

            //nature modification
            if (nature != null) toReturn += toReturn * nature.getStatModifier(this.stat);

            //rounding
            toReturn = Math.round(toReturn);

            //individual value
            if (includeIV) toReturn += this.individualValue;

            //special case for speed
            if (this.stat == RiftCreatureEnums.Stats.SPEED) {
                toReturn = MathUtil.slopeResult(toReturn, false, 20, 100, 0.15, 0.35);
            }

            return toReturn;
        }
    }
}
