package anightdazingzoroark.prift.client.newui.custom;

import anightdazingzoroark.prift.client.newui.function.MoveSwapInfoConsumer;
import anightdazingzoroark.prift.client.newui.function.MoveSwapInfoSupplier;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.BooleanConsumer;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.Contract;
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

    public MoveSwapInfoSyncValue(@NotNull MoveSwapInfoSupplier getter) {
        this(getter, (MoveSwapInfoConsumer) null);
    }

    @Contract("null, null -> fail")
    public MoveSwapInfoSyncValue(@Nullable MoveSwapInfoSupplier clientGetter,
                            @Nullable MoveSwapInfoSupplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Contract("null, _, null, _ -> fail")
    public MoveSwapInfoSyncValue(@Nullable MoveSwapInfoSupplier clientGetter, @Nullable MoveSwapInfoConsumer clientSetter,
                            @Nullable MoveSwapInfoSupplier serverGetter, @Nullable MoveSwapInfoConsumer serverSetter) {
        if (clientGetter == null && serverGetter == null) {
            throw new NullPointerException("Client or server getter must not be null!");
        }
        if (NetworkUtils.isClient()) {
            this.getter = clientGetter != null ? clientGetter : serverGetter;
            this.setter = clientSetter != null ? clientSetter : serverSetter;
        }
        else {
            this.getter = serverGetter != null ? serverGetter : clientGetter;
            this.setter = serverSetter != null ? serverSetter : clientSetter;
        }
        this.cache = this.getter.getSwapInfo();
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
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getSwapInfo() != this.cache) {
            setValue(this.getter.getSwapInfo(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        if (this.cache == null) return;
        ByteBufUtils.writeTag(buffer, this.cache.getNBT());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        SelectedMoveInfo.SwapInfo readSwapInfo = new SelectedMoveInfo.SwapInfo(ByteBufUtils.readTag(buffer));
        this.setValue(readSwapInfo, true, false);
    }
}
