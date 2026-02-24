package anightdazingzoroark.prift.client.newui.sync;

import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlayerJournalProgressSyncValue extends ValueSyncHandler<IPlayerJournalProgress> {
    private final EntityPlayer player;
    private final Supplier<IPlayerJournalProgress> getter;
    private final Consumer<IPlayerJournalProgress> setter;
    private IPlayerJournalProgress cache;

    public PlayerJournalProgressSyncValue(@NotNull EntityPlayer player, @NotNull Supplier<IPlayerJournalProgress> getter, @NotNull Consumer<IPlayerJournalProgress> setter) {
        this.player = Objects.requireNonNull(player);
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public void setValue(IPlayerJournalProgress value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
            //this.clientSyncInitialized = true;
        }
        if (sync) this.sync(0, this::write);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.get() != this.cache) {
            this.setValue(this.getter.get(), false, false);
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
        NBTTagCompound toWrite = new NBTTagCompound();
        NBTTagList tagList = this.cache.getProgressAsNBTList();
        toWrite.setTag("JournalProgress", tagList);
        ByteBufUtils.writeTag(buffer, toWrite);
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        NBTTagCompound readNBT = ByteBufUtils.readTag(buffer);
        if (readNBT == null) return;
        NBTTagList tagList = readNBT.getTagList("JournalProgress", 10);

        IPlayerJournalProgress journalProgress = PlayerJournalProgressHelper.getPlayerJournalProgress(this.player);
        journalProgress.parseNBTListToProgress(tagList);
        this.setValue(journalProgress, true, false);
    }

    @Override
    public IPlayerJournalProgress getValue() {
        return this.cache;
    }

    @Override
    public Class<IPlayerJournalProgress> getValueType() {
        return IPlayerJournalProgress.class;
    }
}
