package anightdazingzoroark.prift.server.entity.creaturenew.info;

import anightdazingzoroark.prift.server.entity.creaturenew.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creaturenew.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureRegistry;
import anightdazingzoroark.prift.server.entity.creaturenew.builder.RiftCreatureBuilder;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CreatureNBTKeywordNew<T> {
    //every single keyword is defined here
    public static final CreatureNBTKeywordNew<RiftCreatureBuilder> CREATURE_TYPE = new CreatureNBTKeywordNew<>(
            "CreatureType", RiftCreatureBuilder.class,
            IRiftCreature::getCreatureType,
            //CreatureType is meant to be static, so it cannot be edited in nbt no matter what
            null
    );
    public static final CreatureNBTKeywordNew<Integer> LEVEL = new CreatureNBTKeywordNew<>(
            "Level", Integer.class,
            IRiftCreature::getLevel,
            IRiftCreature::setLevel
    );
    public static final CreatureNBTKeywordNew<Integer> AGE_IN_TICKS = new CreatureNBTKeywordNew<>(
            "AgeInTicks", Integer.class,
            IRiftCreature::getAgeInTicks,
            IRiftCreature::setAgeInTicks
    );
    public static final CreatureNBTKeywordNew<Float> STAMINA = new CreatureNBTKeywordNew<>(
            "Stamina", Float.class,
            IRiftCreature::getStamina,
            IRiftCreature::setStamina
    );
    public static final CreatureNBTKeywordNew<CreatureMoveStorage> CREATURE_MOVES = new CreatureNBTKeywordNew<>(
            "CreatureMoves", CreatureMoveStorage.class,
            IRiftCreature::getCreatureMoves,
            IRiftCreature::setCreatureMoves
    );

    //normal class operations here
    private final String name;
    private final Class<T> typeClass;
    private final Function<IRiftCreature, T> writeValue;
    private final BiConsumer<IRiftCreature, T> readValue;

    private CreatureNBTKeywordNew(
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
        else if (this.typeClass == CreatureMoveStorage.class) {
            CreatureMoveStorage moveStorage = new CreatureMoveStorage();
            moveStorage.readFromNBT(nbtTagCompound);
            return this.typeClass.cast(moveStorage);
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
        else if (this.typeClass == CreatureMoveStorage.class) {
            nbtTagCompound.setTag(this.name, ((CreatureMoveStorage) value).getAsNBT());
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
        else if (this.typeClass == CreatureMoveStorage.class) {
            CreatureMoveStorage creatureMoveStorage = creature.getCreatureMoves();
            if (creatureMoveStorage == null) return;
            nbtTagCompound.setTag(this.name, creatureMoveStorage.getAsNBT());
        }
    }

    public void readToNBT(NBTTagCompound nbtTagCompound, IRiftCreature creature) {
        if (this.readValue == null) return;
        this.readValue.accept(creature, this.getValueFromNBT(nbtTagCompound));
    }
}
