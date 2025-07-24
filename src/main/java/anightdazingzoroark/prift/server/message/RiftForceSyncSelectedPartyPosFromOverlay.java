package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftForceSyncSelectedPartyPosFromOverlay implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftForceSyncSelectedPartyPosFromOverlay, IMessage> {
        @Override
        public IMessage onMessage(RiftForceSyncSelectedPartyPosFromOverlay message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceSyncSelectedPartyPosFromOverlay message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);

                if (player == null) return;

                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    RiftMessages.WRAPPER.sendTo(new RiftForceSyncSelectedPartyPosFromOverlay(player, playerTamedCreatures.getSelectedPosInOverlay()), (EntityPlayerMP) player);
                }
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    playerTamedCreatures.setSelectedPosInOverlay(message.newPos);
                }
            }
        }
    }
}
