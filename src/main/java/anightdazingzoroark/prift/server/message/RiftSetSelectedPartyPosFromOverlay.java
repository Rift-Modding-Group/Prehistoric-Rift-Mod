package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftSetSelectedPartyPosFromOverlay implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftSetSelectedPartyPosFromOverlay, IMessage> {
        @Override
        public IMessage onMessage(RiftSetSelectedPartyPosFromOverlay message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSetSelectedPartyPosFromOverlay message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    playerTamedCreatures.setSelectedPosInOverlay(message.newPos);
                }
            }
        }
    }
}
