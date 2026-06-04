package anightdazingzoroark.prift.server.entity.creature.info;

import anightdazingzoroark.prift.server.entity.creature.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.CreatureStatsStorage;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CreatureNBTKeyword<T> {
    //every single keyword is defined here
    public static final CreatureNBTKeyword<RiftCreatureBuilder> CREATURE_TYPE = new CreatureNBTKeyword<>(
            "CreatureType", RiftCreatureBuilder.class,
            IRiftCreature::getCreatureType,
            //CreatureType is meant to be static, so it cannot be edited in nbt no matter what
            null
    );
    public static final CreatureNBTKeyword<Integer> LEVEL = new CreatureNBTKeyword<>(
            "Level", Integer.class,
            IRiftCreature::getLevel,
            IRiftCreature::setLevel
    );
    public static final CreatureNBTKeyword<RiftCreatureEnums.Nature> NATURE = new CreatureNBTKeyword<>(
            "Nature", RiftCreatureEnums.Nature.class,
            IRiftCreature::getNature,
            IRiftCreature::setNature
    );
    public static final CreatureNBTKeyword<Integer> AGE_IN_TICKS = new CreatureNBTKeyword<>(
            "AgeInTicks", Integer.class,
            IRiftCreature::getAgeInTicks,
            IRiftCreature::setAgeInTicks
    );
    public static final CreatureNBTKeyword<Float> STAMINA = new CreatureNBTKeyword<>(
            "Stamina", Float.class,
            IRiftCreature::getStamina,
            IRiftCreature::setStamina
    );
    public static final CreatureNBTKeyword<CreatureStatsStorage> CREATURE_STATS = new CreatureNBTKeyword<>(
            "CreatureStats", CreatureStatsStorage.class,
            IRiftCreature::getCreatureStats,
            IRiftCreature::setCreatureStats
    );
    public static final CreatureNBTKeyword<CreatureMoveStorage> CREATURE_MOVES = new CreatureNBTKeyword<>(
            "CreatureMoves", CreatureMoveStorage.class,
            IRiftCreature::getCreatureMoves,
            IRiftCreature::setCreatureMoves
    );

    //normal class operations here
    private final String name;
    private final Class<T> typeClass;
    private final Function<IRiftCreature, T> writeValue;
    private final BiConsumer<IRiftCreature, T> readValue;

    private CreatureNBTKeyword(
            String name, Class<T> typeClass,
            Function<IRiftCreature, T> writeValue,
            BiConsumer<IRiftCreature, T> readValue
    ) {
        this.name = name;
        this.typeClass = typeClass;
        this.writeValue = writeValue;
        this.readValue = readValue;
    }

    public String getName() {
        return this.name;
    }

    public T getValueFromCreature(IRiftCreature creature) {
        return this.writeValue.apply(creature);
    }

    public T getValueFromNBT(NBTTagCompound nbtTagCompound) {
        if (this.typeClass == Integer.class) {
            return this.typeClass.cast(nbtTagCompound.getInteger(this.name));
        }
        else if (this.typeClass == Float.class) {
            return this.typeClass.cast(nbtTagCompound.getFloat(this.name));
        }
        else if (this.typeClass == String.class) {
            return this.typeClass.cast(nbtTagCompound.getString(this.name));
        }
        else if (this.typeClass == RiftCreatureBuilder.class) {
            RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(nbtTagCompound.getString(this.name));
            return this.typeClass.cast(builder);
        }
        else if (this.typeClass == CreatureStatsStorage.class) {
            CreatureStatsStorage moveStorage = new CreatureStatsStorage();
            moveStorage.readFromNBT(nbtTagCompound.getCompoundTag(this.name));
            return this.typeClass.cast(moveStorage);
        }
        else if (this.typeClass == CreatureMoveStorage.class) {
            CreatureMoveStorage moveStorage = new CreatureMoveStorage();
            moveStorage.readFromNBT(nbtTagCompound.getCompoundTag(this.name));
            return this.typeClass.cast(moveStorage);
        }
        else if (this.typeClass.isEnum()) {
            if (!nbtTagCompound.hasKey(this.name)) return null;
            int ordinal = nbtTagCompound.getByte(this.name);
            Object[] enumConstants = this.typeClass.getEnumConstants();
            if (ordinal < 0 || ordinal >= enumConstants.length) return null;
            return this.typeClass.cast(enumConstants[ordinal]);
        }
        return null;
    }

    public void setValueInNBT(NBTTagCompound nbtTagCompound, T value) {
        if (this.typeClass == Integer.class) {
            nbtTagCompound.setInteger(this.name, (Integer) value);
        }
        else if (this.typeClass == Float.class) {
            nbtTagCompound.setFloat(this.name, (Float) value);
        }
        else if (this.typeClass == String.class) {
            nbtTagCompound.setString(this.name, (String) value);
        }
        else if (this.typeClass == RiftCreatureBuilder.class) {
            nbtTagCompound.setString(this.name, ((RiftCreatureBuilder) value).getName());
        }
        else if (this.typeClass == CreatureStatsStorage.class) {
            nbtTagCompound.setTag(this.name, ((CreatureStatsStorage) value).getAsNBT());
        }
        else if (this.typeClass == CreatureMoveStorage.class) {
            nbtTagCompound.setTag(this.name, ((CreatureMoveStorage) value).getAsNBT());
        }
        else if (this.typeClass.isEnum()) {
            nbtTagCompound.setByte(this.name, value == null ? (byte) -1 : (byte) ((Enum<?>) value).ordinal());
        }
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound, IRiftCreature creature) {
        if (this.writeValue == null || creature == null) return;
        if (this.typeClass == Integer.class) {
            nbtTagCompound.setInteger(this.name, (Integer) this.writeValue.apply(creature));
        }
        else if (this.typeClass == Float.class) {
            nbtTagCompound.setFloat(this.name, (Float) this.writeValue.apply(creature));
        }
        else if (this.typeClass == String.class) {
            nbtTagCompound.setString(this.name, (String) this.writeValue.apply(creature));
        }
        else if (this.typeClass == RiftCreatureBuilder.class) {
            nbtTagCompound.setString(this.name, ((RiftCreatureBuilder) this.writeValue.apply(creature)).getName());
        }
        else if (this.typeClass == CreatureStatsStorage.class) {
            CreatureStatsStorage creatureStatsStorage = (CreatureStatsStorage) this.writeValue.apply(creature);
            if (creatureStatsStorage == null) return;
            nbtTagCompound.setTag(this.name, creatureStatsStorage.getAsNBT());
        }
        else if (this.typeClass == CreatureMoveStorage.class) {
            CreatureMoveStorage creatureMoveStorage = (CreatureMoveStorage) this.writeValue.apply(creature);
            if (creatureMoveStorage == null) return;
            nbtTagCompound.setTag(this.name, creatureMoveStorage.getAsNBT());
        }
        else if (this.typeClass.isEnum()) {
            Enum<?> enumValue = (Enum<?>) this.writeValue.apply(creature);
            nbtTagCompound.setByte(this.name, enumValue == null ? (byte) -1 : (byte) enumValue.ordinal());
        }
    }

    public void readToNBT(NBTTagCompound nbtTagCompound, IRiftCreature creature) {
        if (this.readValue == null || creature == null) return;
        this.readValue.accept(creature, this.getValueFromNBT(nbtTagCompound));
    }
}
