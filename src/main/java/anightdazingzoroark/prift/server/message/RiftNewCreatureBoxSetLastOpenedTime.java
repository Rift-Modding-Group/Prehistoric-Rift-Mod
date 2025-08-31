package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftNewCreatureBoxSetLastOpenedTime implements IMessage {
    private int playerId;
    private int lastOpenedTime;

    public RiftNewCreatureBoxSetLastOpenedTime() {}

    public RiftNewCreatureBoxSetLastOpenedTime(EntityPlayer player, int lastOpenedTime) {
        this.playerId = player.getEntityId();
        this.lastOpenedTime = lastOpenedTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.lastOpenedTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.lastOpenedTime);
    }

    public static class Handler implements IMessageHandler<RiftNewCreatureBoxSetLastOpenedTime, IMessage> {
        @Override
        public IMessage onMessage(RiftNewCreatureBoxSetLastOpenedTime message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftNewCreatureBoxSetLastOpenedTime message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    int timeToSubtract = message.lastOpenedTime - playerTamedCreatures.getBoxLastOpenedTime();

                    //deal with countdown for creatures
                    playerTamedCreatures.getBoxNBT().countdownCreatureRevival(timeToSubtract);

                    //now set the time
                    playerTamedCreatures.setBoxLastOpenedTime(message.lastOpenedTime);
                }
            }
        }
    }
}
