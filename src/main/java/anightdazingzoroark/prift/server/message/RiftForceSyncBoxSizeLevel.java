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

public class RiftForceSyncBoxSizeLevel implements IMessage {
    private int playerId;
    private int boxSizeLevel;

    public RiftForceSyncBoxSizeLevel() {}

    public RiftForceSyncBoxSizeLevel(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncBoxSizeLevel(EntityPlayer player, int boxSizeLevel) {
        this.playerId = player.getEntityId();
        this.boxSizeLevel = boxSizeLevel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.boxSizeLevel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.boxSizeLevel);
    }

    public static class Handler implements IMessageHandler<RiftForceSyncBoxSizeLevel, IMessage> {
        @Override
        public IMessage onMessage(RiftForceSyncBoxSizeLevel message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceSyncBoxSizeLevel message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.boxSizeLevel < 0) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncBoxSizeLevel(player, playerTamedCreatures.getBoxSizeLevel()));
                else playerTamedCreatures.setBoxSizeLevel(message.boxSizeLevel);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.boxSizeLevel < 0) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncBoxSizeLevel(player, playerTamedCreatures.getBoxSizeLevel()));
                else playerTamedCreatures.setBoxSizeLevel(message.boxSizeLevel);
            }
        }
    }
}
