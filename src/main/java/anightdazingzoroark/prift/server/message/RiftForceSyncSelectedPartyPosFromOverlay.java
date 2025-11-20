package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftForceSyncSelectedPartyPosFromOverlay extends RiftLibMessage<RiftForceSyncSelectedPartyPosFromOverlay> {
    private int playerId;
    private int newPos;

    public RiftForceSyncSelectedPartyPosFromOverlay() {}

    public RiftForceSyncSelectedPartyPosFromOverlay(EntityPlayer player) {
        this(player, 0);
    }

    public RiftForceSyncSelectedPartyPosFromOverlay(EntityPlayer player, int newPos) {
        this.playerId = player.getEntityId();
        this.newPos = newPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.newPos = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.newPos);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftForceSyncSelectedPartyPosFromOverlay message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null) {
            RiftMessages.WRAPPER.sendTo(new RiftForceSyncSelectedPartyPosFromOverlay(player, playerTamedCreatures.getSelectedPosInOverlay()), (EntityPlayerMP) player);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftForceSyncSelectedPartyPosFromOverlay message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null) playerTamedCreatures.setSelectedPosInOverlay(message.newPos);
    }
}
