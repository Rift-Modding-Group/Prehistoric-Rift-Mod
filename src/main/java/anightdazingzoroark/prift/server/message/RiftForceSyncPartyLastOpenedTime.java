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

public class RiftForceSyncPartyLastOpenedTime implements IMessage {
    private int playerId;
    private int partyLastOpenedTime;

    public RiftForceSyncPartyLastOpenedTime() {}

    public RiftForceSyncPartyLastOpenedTime(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncPartyLastOpenedTime(EntityPlayer player, int partyLastOpenedTime) {
        this.playerId = player.getEntityId();
        this.partyLastOpenedTime = partyLastOpenedTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.partyLastOpenedTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partyLastOpenedTime);
    }

    public static class Handler implements IMessageHandler<RiftForceSyncPartyLastOpenedTime, IMessage> {
        @Override
        public IMessage onMessage(RiftForceSyncPartyLastOpenedTime message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceSyncPartyLastOpenedTime message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.partyLastOpenedTime < 0) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncPartyLastOpenedTime(player, playerTamedCreatures.getPartyLastOpenedTime()));
                else playerTamedCreatures.setPartyLastOpenedTime(message.partyLastOpenedTime);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.partyLastOpenedTime < 0) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncPartyLastOpenedTime(player, playerTamedCreatures.getPartyLastOpenedTime()));
                else playerTamedCreatures.setPartyLastOpenedTime(message.partyLastOpenedTime);
            }
        }
    }
}
