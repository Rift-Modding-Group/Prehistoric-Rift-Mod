package anightdazingzoroark.prift.client.newui.value;

import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FixedSizeCreatureListSyncValue extends ValueSyncHandler<FixedSizeList<CreatureNBT>> {
    private final Supplier<FixedSizeList<CreatureNBT>> getter;
    private final Consumer<FixedSizeList<CreatureNBT>> setter;
    private FixedSizeList<CreatureNBT> cache;

    public FixedSizeCreatureListSyncValue(@NotNull Supplier<FixedSizeList<CreatureNBT>> getter, @NotNull Consumer<FixedSizeList<CreatureNBT>> setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public void setValue(FixedSizeList<CreatureNBT> value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) this.setter.accept(value);
        if (sync) sync(0, this::write);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.get() != this.cache) {
            setValue(this.getter.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        this.setValue(this.getter.get(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        if (this.cache == null) return;
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        RiftTileEntityCreatureBox.getNBTFromDeployedList(nbtTagCompound, this.cache);
        ByteBufUtils.writeTag(buffer, nbtTagCompound);
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        NBTTagCompound readNBT = ByteBufUtils.readTag(buffer);
        if (readNBT == null) return;
        FixedSizeList<CreatureNBT> value = RiftTileEntityCreatureBox.getDeployedListFromNBT(readNBT);
        this.setValue(value, true, false);
    }

    @Override
    public FixedSizeList<CreatureNBT> getValue() {
        return this.cache;
    }

    @Override
    public Class<FixedSizeList<CreatureNBT>> getValueType() {
        return (Class<FixedSizeList<CreatureNBT>>) (Class<?>) FixedSizeList.class;
    }
}
