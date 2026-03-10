package anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue;

import anightdazingzoroark.prift.helper.FixedSizeList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Function;

public class FixedSizeListPropertyValue<I> extends AbstractPropertyValue<FixedSizeList<I>> {
    private final Function<FixedSizeList<I>, NBTBase> listWriter;
    private final Function<NBTBase, FixedSizeList<I>> listReader;

    public FixedSizeListPropertyValue(
            String key, I defaultValue, int maxSize,
            Function<FixedSizeList<I>, NBTBase> listWriter,
            Function<NBTBase, FixedSizeList<I>> listReader
    ) {
        super(key, new FixedSizeList<>(maxSize, defaultValue));
        this.listWriter = listWriter;
        this.listReader = listReader;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setTag(this.getKey(), this.listWriter.apply(this.value));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        this.value = this.listReader.apply(nbtTagCompound.getTag(this.getKey()));
    }

    @Override
    public Class<FixedSizeList<I>> getHeldClass() {
        return (Class<FixedSizeList<I>>) (Class<?>) FixedSizeList.class;
    }
}
