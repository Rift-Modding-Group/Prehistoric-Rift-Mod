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

public class RiftForceSyncPartySizeLevel implements IMessage {
    private int playerId;
    private int partySizeLevel;

    public RiftForceSyncPartySizeLevel() {}

    public RiftForceSyncPartySizeLevel(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncPartySizeLevel(EntityPlayer player, int partySizeLevel) {
        this.playerId = player.getEntityId();
        this.partySizeLevel = partySizeLevel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.partySizeLevel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partySizeLevel);
    }

    public static class Handler implements IMessageHandler<RiftForceSyncPartySizeLevel, IMessage> {
        @Override
        public IMessage onMessage(RiftForceSyncPartySizeLevel message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceSyncPartySizeLevel message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            }
        }
    }
}
