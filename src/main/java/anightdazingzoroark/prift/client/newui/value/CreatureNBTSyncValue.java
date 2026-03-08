package anightdazingzoroark.prift.client.newui.value;

import anightdazingzoroark.prift.helper.CreatureNBT;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreatureNBTSyncValue extends ValueSyncHandler<CreatureNBT> {
    private final Supplier<CreatureNBT> getter;
    private final Consumer<CreatureNBT> setter;
    private CreatureNBT cache;

    public CreatureNBTSyncValue(@NotNull Supplier<CreatureNBT> getter, @NotNull Consumer<CreatureNBT> setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
    }

    public void setCreatureNBT(CreatureNBT creatureNBT) {
        this.cache = creatureNBT;
    }

    @Override
    public void setValue(CreatureNBT value, boolean setSource, boolean sync) {
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
        ByteBufUtils.writeTag(buffer, this.cache.getCreatureNBT());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        NBTTagCompound readNBT = ByteBufUtils.readTag(buffer);
        if (readNBT == null) return;
        CreatureNBT nbtToSet = new CreatureNBT(readNBT);
        this.setValue(nbtToSet, true, false);
    }

    @Override
    public CreatureNBT getValue() {
        return this.cache;
    }

    @Override
    public Class<CreatureNBT> getValueType() {
        return CreatureNBT.class;
    }
}
