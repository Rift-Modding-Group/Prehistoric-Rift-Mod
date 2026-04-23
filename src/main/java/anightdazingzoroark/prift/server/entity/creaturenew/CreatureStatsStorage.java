package anightdazingzoroark.prift.server.entity.creaturenew;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creaturenew.info.RiftCreatureEnums;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A creature's stats are to be stored here
 * */
public class CreatureStatsStorage {
    //the final stats of a creature
    private final Map<RiftCreatureEnums.Stats, Double> stats = new HashMap<>();
    //individual values are random stat incrementors, they are randomly generated for each creature
    private final Map<RiftCreatureEnums.Stats, Integer> individualValues = new HashMap<>();

    //individual values are to be created here
    public void initializeIndividualValues() {
        Random random = new Random();

        this.individualValues.put(RiftCreatureEnums.Stats.HEALTH, random.nextInt(0, 20));
        this.individualValues.put(RiftCreatureEnums.Stats.MELEE_DAMAGE, random.nextInt(0, 20));
        this.individualValues.put(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE, random.nextInt(0, 20));
        this.individualValues.put(RiftCreatureEnums.Stats.STAMINA, random.nextInt(0, 20));
        this.individualValues.put(RiftCreatureEnums.Stats.SPEED, random.nextInt(0, 20));
    }

    //is to be run once, no need to repeatedly execute
    public void parseStats(RiftCreatureBuilder creatureType, int creatureLevel, RiftCreatureEnums.Nature nature) {
        Map<RiftCreatureEnums.Stats, Double> baseStats = creatureType.getStats();

        //-----parse health-----
        double baseHealth = baseStats.get(RiftCreatureEnums.Stats.HEALTH);
        double parsedHealth = RiftUtil.slopeResult(baseHealth, false, 0, 10, 0, 200);
        parsedHealth += parsedHealth * 0.1D * (creatureLevel - 1);
        parsedHealth += parsedHealth * nature.getStatModifier(RiftCreatureEnums.Stats.HEALTH);
        parsedHealth = Math.round(parsedHealth);
        parsedHealth += individualValues.get(RiftCreatureEnums.Stats.HEALTH);
        this.stats.put(RiftCreatureEnums.Stats.HEALTH, parsedHealth);

        //-----parse melee damage-----
        //note: dont worry, this isnt its real melee damage. the base power of the move it will use will
        //be the final determinant in the melee damage this creature will deal
        double baseMeleeDamage = baseStats.get(RiftCreatureEnums.Stats.MELEE_DAMAGE);
        double parsedMeleeDamage = RiftUtil.slopeResult(baseMeleeDamage, false, 0, 10, 0, 200);
        parsedMeleeDamage += parsedMeleeDamage * 0.1D * (creatureLevel - 1);
        parsedMeleeDamage += parsedMeleeDamage * nature.getStatModifier(RiftCreatureEnums.Stats.MELEE_DAMAGE);
        parsedMeleeDamage = Math.round(parsedMeleeDamage);
        parsedMeleeDamage += individualValues.get(RiftCreatureEnums.Stats.MELEE_DAMAGE);
        this.stats.put(RiftCreatureEnums.Stats.MELEE_DAMAGE, parsedMeleeDamage);

        //-----parse elemental damage-----
        //similar to melee damage, the base power of the move it will use will be
        //the final determinant in the elemental damage this creature will deal
        double baseElementalDamage = baseStats.get(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE);
        double parsedElementalDamage = RiftUtil.slopeResult(baseElementalDamage, false, 0, 10, 0, 200);
        parsedElementalDamage += parsedElementalDamage * 0.1D * (creatureLevel - 1);
        parsedElementalDamage += parsedElementalDamage * nature.getStatModifier(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE);
        parsedElementalDamage = Math.round(parsedElementalDamage);
        parsedElementalDamage += individualValues.get(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE);
        this.stats.put(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE, parsedElementalDamage);

        //-----parse stamina-----
        double baseStamina = baseStats.get(RiftCreatureEnums.Stats.STAMINA);
        double parsedStamina = RiftUtil.slopeResult(baseStamina, false, 0, 10, 0, 80);
        parsedStamina += parsedStamina * 0.1 * (parsedStamina - 1);
        parsedStamina += parsedStamina * nature.getStatModifier(RiftCreatureEnums.Stats.STAMINA);
        parsedStamina += Math.floor(individualValues.get(RiftCreatureEnums.Stats.STAMINA) / 2D);
        parsedStamina = Math.round(parsedStamina);
        this.stats.put(RiftCreatureEnums.Stats.STAMINA, parsedStamina);

        //-----parse speed-----
        double baseSpeed = baseStats.get(RiftCreatureEnums.Stats.SPEED);
        //speed isn't affected by leveling but other factors can manipulate it
        double parsedSpeed = RiftUtil.slopeResult(baseSpeed, false, 1, 5, 20, 100);
        parsedSpeed += parsedSpeed * nature.getStatModifier(RiftCreatureEnums.Stats.SPEED);
        parsedSpeed += Math.floor(individualValues.get(RiftCreatureEnums.Stats.SPEED) / 2D);
        parsedSpeed = Math.round(parsedSpeed);
        parsedSpeed = RiftUtil.slopeResult(parsedSpeed, false, 20, 100, 0.15, 0.35);
        this.stats.put(RiftCreatureEnums.Stats.SPEED, parsedSpeed);
    }

    //also to be run once, but for applying the stats to a creature
    public void applyStatsToCreature(RiftCreatureNew creature) {
        //-----apply health-----
        double finalHealth = this.getValueForStat(RiftCreatureEnums.Stats.HEALTH);
        creature.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(finalHealth);
        creature.heal((float) finalHealth);

        //-----apply melee attack-----
        creature.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.getValueForStat(RiftCreatureEnums.Stats.MELEE_DAMAGE));

        //-----apply elemental attack-----
        creature.getEntityAttribute(RiftCreatureNew.ELEMENTAL_DAMAGE_ATTRIBUTE).setBaseValue(this.getValueForStat(RiftCreatureEnums.Stats.ELEMENTAL_DAMAGE));

        //-----apply stamina-----
        double finalStamina = this.getValueForStat(RiftCreatureEnums.Stats.STAMINA);
        creature.getEntityAttribute(RiftCreatureNew.STAMINA_ATTRIBUTE).setBaseValue(finalStamina);
        creature.setStamina((float) finalStamina);

        //-----apply speed-----
        creature.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getValueForStat(RiftCreatureEnums.Stats.SPEED));
    }

    public double getValueForStat(RiftCreatureEnums.Stats stat) {
        return this.stats.get(stat);
    }

    public double getIndividualValueForStat(RiftCreatureEnums.Stats stat) {
        return this.individualValues.get(stat);
    }

    public NBTTagCompound getAsNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        //store stats
        NBTTagList statTagList = new NBTTagList();
        for (Map.Entry<RiftCreatureEnums.Stats, Double> entry : this.stats.entrySet()) {
            NBTTagCompound toAppend = new NBTTagCompound();
            toAppend.setByte("Stat", (byte) entry.getKey().ordinal());
            toAppend.setDouble("Value", entry.getValue());
            statTagList.appendTag(toAppend);
        }
        toReturn.setTag("Stats", statTagList);

        //store individual values
        NBTTagList individualValuesTagList = new NBTTagList();
        for (Map.Entry<RiftCreatureEnums.Stats, Integer> entry : this.individualValues.entrySet()) {
            NBTTagCompound toAppend = new NBTTagCompound();
            toAppend.setByte("Stat", (byte) entry.getKey().ordinal());
            toAppend.setInteger("Value", entry.getValue());
            individualValuesTagList.appendTag(toAppend);
        }
        toReturn.setTag("IndividualValues", individualValuesTagList);

        return toReturn;
    }

    public void readFromNBT(@NotNull NBTTagCompound nbtTagCompound) {
        this.stats.clear();
        this.individualValues.clear();

        NBTTagList statTagList = nbtTagCompound.getTagList("Stats", 10);
        for (int index = 0; index < statTagList.tagCount(); index++) {
            NBTTagCompound statNBT = statTagList.getCompoundTagAt(index);
            int statOrdinal = statNBT.getByte("Stat");
            if (statOrdinal < 0 || statOrdinal >= RiftCreatureEnums.Stats.values().length) continue;

            RiftCreatureEnums.Stats stat = RiftCreatureEnums.Stats.values()[statOrdinal];
            this.stats.put(stat, statNBT.getDouble("Value"));
        }

        NBTTagList individualValuesTagList = nbtTagCompound.getTagList("IndividualValues", 10);
        for (int index = 0; index < individualValuesTagList.tagCount(); index++) {
            NBTTagCompound individualValueNBT = individualValuesTagList.getCompoundTagAt(index);
            int statOrdinal = individualValueNBT.getByte("Stat");
            if (statOrdinal < 0 || statOrdinal >= RiftCreatureEnums.Stats.values().length) continue;

            RiftCreatureEnums.Stats stat = RiftCreatureEnums.Stats.values()[statOrdinal];
            this.individualValues.put(stat, individualValueNBT.getInteger("Value"));
        }
    }
}
