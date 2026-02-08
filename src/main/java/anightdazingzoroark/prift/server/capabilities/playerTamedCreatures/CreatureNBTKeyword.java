package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

//helper class where strings corresponding to nbt values are to be placed in
public class CreatureNBTKeyword<T> {
    public static final CreatureNBTKeyword<Integer> LEVEL = new CreatureNBTKeyword<>("Level", Integer.class);
    public static final CreatureNBTKeyword<Integer> XP = new CreatureNBTKeyword<>("XP", Integer.class);
    public static final CreatureNBTKeyword<Integer> LOVE_COOLDOWN = new CreatureNBTKeyword<>("LoveCooldown", Integer.class);
    public static final CreatureNBTKeyword<Integer> VARIANT = new CreatureNBTKeyword<>("Variant", Integer.class);
    public static final CreatureNBTKeyword<Byte> CREATURE_TYPE = new CreatureNBTKeyword<>("CreatureType", Byte.class);
    public static final CreatureNBTKeyword<Byte> TAME_BEHAVIOR = new CreatureNBTKeyword<>("TameBehavior", Byte.class);
    public static final CreatureNBTKeyword<Integer> ENERGY = new CreatureNBTKeyword<>("Energy", Integer.class);
    public static final CreatureNBTKeyword<Boolean> HAS_TARGET = new CreatureNBTKeyword<>("HasTarget", Boolean.class);
    public static final CreatureNBTKeyword<Integer> AGE_TICKS = new CreatureNBTKeyword<>("AgeTicks", Integer.class);
    public static final CreatureNBTKeyword<Boolean> JUST_SPAWNED = new CreatureNBTKeyword<>("JustSpawned", Boolean.class);
    public static final CreatureNBTKeyword<Boolean> HAS_HOME_POS = new CreatureNBTKeyword<>("HasHomePos", Boolean.class);
    public static final CreatureNBTKeyword<Boolean> SADDLED = new CreatureNBTKeyword<>("Saddled", Boolean.class);
    public static final CreatureNBTKeyword<Byte> LARGE_WEAPON_TYPE = new CreatureNBTKeyword<>("LargeWeapon", Byte.class);
    public static final CreatureNBTKeyword<NBTTagCompound> LEARNED_MOVES = new CreatureNBTKeyword<>("LearnedMoves", NBTTagCompound.class);
    public static final CreatureNBTKeyword<NBTTagCompound> LEARNABLE_MOVES = new CreatureNBTKeyword<>("LearnableMoves", NBTTagCompound.class);
    public static final CreatureNBTKeyword<NBTTagCompound> GEAR = new CreatureNBTKeyword<>("Gear", NBTTagCompound.class);
    public static final CreatureNBTKeyword<NBTTagCompound> INVENTORY = new CreatureNBTKeyword<>("Inventory", NBTTagCompound.class);

    public static <T> NBTTagCompound mergeResult(NBTTagCompound tagCompound, CreatureNBTKeyword<T> keyword, T keywordValue) {
        if (tagCompound.hasKey(keyword.name)) tagCompound.merge(keyword.setValue(keywordValue));
        else return keyword.setValue(tagCompound, keywordValue);
        return tagCompound;
    }

    public final String name;
    private final Class<T> clazz;

    private CreatureNBTKeyword(String name, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public boolean existsInTagCompound(NBTTagCompound nbtTagCompound) {
        return nbtTagCompound.hasKey(this.name);
    }

    public NBTTagCompound setValue(T value) {
        return this.setValue(new NBTTagCompound(), value);
    }

    private NBTTagCompound setValue(NBTTagCompound nbtToSetTo, T value) {
        if (value instanceof Integer) nbtToSetTo.setInteger(this.name, (Integer) value);
        else if (value instanceof Boolean) nbtToSetTo.setBoolean(this.name, (Boolean) value);
        else if (value instanceof Byte) nbtToSetTo.setByte(this.name, (Byte) value);
        else if (value instanceof NBTTagCompound) nbtToSetTo.setTag(this.name, (NBTTagCompound) value);
        return nbtToSetTo;
    }

    public T parseValue(NBTTagCompound nbtTagCompound) {
        NBTBase filteredNBTBase = nbtTagCompound.getTag(this.name);
        if (filteredNBTBase.isEmpty()) return null;

        if (this.clazz == Integer.class) return this.clazz.cast(nbtTagCompound.getInteger(this.name));
        else if (this.clazz == Boolean.class) return this.clazz.cast(nbtTagCompound.getBoolean(this.name));
        else if (this.clazz == Byte.class) return this.clazz.cast(nbtTagCompound.getByte(this.name));
        else if (this.clazz == NBTTagCompound.class) return this.clazz.cast(nbtTagCompound.getCompoundTag(this.name));
        return null;
    }
}
