package anightdazingzoroark.prift.client.newui.sync;

import anightdazingzoroark.prift.client.newui.function.PlayerPartyConsumer;
import anightdazingzoroark.prift.client.newui.function.PlayerPartySupplier;
import anightdazingzoroark.prift.server.capabilities.playerParty.IPlayerParty;
import anightdazingzoroark.prift.server.capabilities.playerParty.PlayerPartyHelper;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class PlayerPartySyncValue extends ValueSyncHandler<IPlayerParty> {
    private final EntityPlayer player;
    private final PlayerPartySupplier getter;
    private final PlayerPartyConsumer setter;
    private IPlayerParty cache;
    private boolean clientSyncInitialized;

    public PlayerPartySyncValue(@NotNull EntityPlayer player, @NotNull PlayerPartySupplier getter, @NotNull PlayerPartyConsumer setter) {
        this.player = Objects.requireNonNull(player);
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.get();
    }

    public void deployAtIndex(int index, boolean deploy) {
        if (this.cache == null) return;
        this.cache.deployPartyMember(index, deploy, this.player);
        this.notifyUpdate();
    }

    public void teleportAtIndex(int index) {
        if (this.cache == null) return;
        this.cache.teleportPartyMember(index, this.player);
        this.notifyUpdate();
    }

    //-----everything below is normal operation-----
    @Override
    public void setValue(IPlayerParty value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
            this.clientSyncInitialized = true;
            this.cache.applyDeploymentOrTeleportation(this.player);
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
        NBTTagCompound partyTagCompound = new NBTTagCompound();
        partyTagCompound.setTag("PlayerParty", this.cache.getPartyAsNBTList());
        partyTagCompound.setTag("TeleportationMarker", this.cache.getTeleportationMarkerAsNBT());
        ByteBufUtils.writeTag(buffer, partyTagCompound);
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        NBTTagCompound readNBT = ByteBufUtils.readTag(buffer);
        if (readNBT == null) return;
        NBTTagList tagList = readNBT.getTagList("PlayerParty", 10);
        NBTTagCompound teleportationMarker = readNBT.getCompoundTag("TeleportationMarker");

        IPlayerParty playerParty = Objects.requireNonNull(PlayerPartyHelper.getPlayerParty(this.player));
        playerParty.parseNBTListToParty(tagList);
        playerParty.parseNBTTeleportationMarker(teleportationMarker);
        this.setValue(playerParty, true, false);
    }

    @Override
    public IPlayerParty getValue() {
        return this.cache;
    }

    @Override
    public Class<IPlayerParty> getValueType() {
        return IPlayerParty.class;
    }

    public boolean isClientSyncInitialized() {
        return this.clientSyncInitialized;
    }
}
