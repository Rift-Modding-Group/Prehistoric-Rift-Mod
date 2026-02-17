package anightdazingzoroark.prift.client.newui.sync;

import anightdazingzoroark.prift.client.newui.function.SelectedMoveInfoConsumer;
import anightdazingzoroark.prift.client.newui.function.SelectedMoveInfoSupplier;
import anightdazingzoroark.prift.client.newui.holder.SelectedMoveInfo;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SelectedMoveInfoSyncValue extends ValueSyncHandler<SelectedMoveInfo> {
    private final SelectedMoveInfoSupplier getter;
    private final SelectedMoveInfoConsumer setter;
    private SelectedMoveInfo cache = null;

    public SelectedMoveInfoSyncValue(@NotNull SelectedMoveInfoSupplier getter, @Nullable SelectedMoveInfoConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void setValue(SelectedMoveInfo value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) this.setter.accept(value);
        if (sync) sync(0, this::write);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getSelectedMoveInfo() != this.cache) {
            this.setValue(this.getter.getSelectedMoveInfo(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        this.setValue(this.getter.getSelectedMoveInfo(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        if (this.cache == null) {
            ByteBufUtils.writeTag(buffer, new NBTTagCompound());
            return;
        }
        ByteBufUtils.writeTag(buffer, this.cache.getNBT());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        NBTTagCompound readNBT = ByteBufUtils.readTag(buffer);
        if (readNBT == null || readNBT.isEmpty()) {
            this.setValue(null, true, false);
            return;
        }
        SelectedMoveInfo readSelectedInfo = new SelectedMoveInfo(readNBT);
        this.setValue(readSelectedInfo, true, false);
    }

    @Override
    public SelectedMoveInfo getValue() {
        return this.cache;
    }

    @Override
    public Class<SelectedMoveInfo> getValueType() {
        return SelectedMoveInfo.class;
    }
}
