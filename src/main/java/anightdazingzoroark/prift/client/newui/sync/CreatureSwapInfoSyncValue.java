package anightdazingzoroark.prift.client.newui.sync;

import anightdazingzoroark.prift.client.newui.function.CreatureSwapInfoConsumer;
import anightdazingzoroark.prift.client.newui.function.CreatureSwapInfoSupplier;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class CreatureSwapInfoSyncValue extends ValueSyncHandler<SelectedCreatureInfo.SwapInfo> {
    private final CreatureSwapInfoSupplier getter;
    private final CreatureSwapInfoConsumer setter;
    private SelectedCreatureInfo.SwapInfo cache;

    public CreatureSwapInfoSyncValue(@NotNull CreatureSwapInfoSupplier getter, @Nullable CreatureSwapInfoConsumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getSwapInfo();
    }

    @Override
    public void setValue(SelectedCreatureInfo.SwapInfo value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) this.setter.accept(value);
        if (sync) sync(0, this::write);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getSwapInfo() != this.cache) {
            setValue(this.getter.getSwapInfo(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        this.setValue(this.getter.getSwapInfo(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        if (this.cache == null) return;
        ByteBufUtils.writeTag(buffer, this.cache.getNBT());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        NBTTagCompound readNBT = ByteBufUtils.readTag(buffer);
        if (readNBT == null) return;
        SelectedCreatureInfo.SwapInfo readSwapInfo = new SelectedCreatureInfo.SwapInfo(readNBT);
        this.setValue(readSwapInfo, true, false);
    }

    @Override
    public SelectedCreatureInfo.SwapInfo getValue() {
        return this.cache;
    }

    @Override
    public Class<SelectedCreatureInfo.SwapInfo> getValueType() {
        return SelectedCreatureInfo.SwapInfo.class;
    }
}
