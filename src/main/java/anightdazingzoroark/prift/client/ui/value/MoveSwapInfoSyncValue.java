package anightdazingzoroark.prift.client.ui.value;

import anightdazingzoroark.prift.client.ui.function.MoveSwapInfoConsumer;
import anightdazingzoroark.prift.client.ui.function.MoveSwapInfoSupplier;
import anightdazingzoroark.prift.client.ui.holder.SelectedMoveInfo;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class MoveSwapInfoSyncValue extends ValueSyncHandler<SelectedMoveInfo.SwapInfo> {
    private final MoveSwapInfoSupplier getter;
    private final MoveSwapInfoConsumer setter;
    private SelectedMoveInfo.SwapInfo cache;

    public MoveSwapInfoSyncValue(@NotNull MoveSwapInfoSupplier getter, @Nullable MoveSwapInfoConsumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getSwapInfo();
    }

    @Override
    public void setValue(SelectedMoveInfo.SwapInfo value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) this.setter.accept(value);
        if (sync) sync(0, this::write);
    }

    @Override
    public SelectedMoveInfo.SwapInfo getValue() {
        return this.cache;
    }

    @Override
    public Class<SelectedMoveInfo.SwapInfo> getValueType() {
        return SelectedMoveInfo.SwapInfo.class;
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
        SelectedMoveInfo.SwapInfo readSwapInfo = new SelectedMoveInfo.SwapInfo(readNBT);
        this.setValue(readSwapInfo, true, false);
    }
}
