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

public class RiftSetSelectedPartyPosFromOverlay extends RiftLibMessage<RiftSetSelectedPartyPosFromOverlay> {
    private int playerId;
    private int newPos;

    public RiftSetSelectedPartyPosFromOverlay() {}

    public RiftSetSelectedPartyPosFromOverlay(EntityPlayer player, int value) {
        this.playerId = player.getEntityId();
        this.newPos = value;
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetSelectedPartyPosFromOverlay message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        if (playerTamedCreatures != null) playerTamedCreatures.setSelectedPosInOverlay(message.newPos);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetSelectedPartyPosFromOverlay message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
