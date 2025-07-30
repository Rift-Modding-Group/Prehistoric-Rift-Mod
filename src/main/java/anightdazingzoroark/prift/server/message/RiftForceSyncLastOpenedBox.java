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

public class RiftForceSyncLastOpenedBox implements IMessage {
    private int playerId;
    private int lastOpenedBox;

    public RiftForceSyncLastOpenedBox() {}

    public RiftForceSyncLastOpenedBox(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncLastOpenedBox(EntityPlayer player, int lastOpenedBox) {
        this.playerId = player.getEntityId();
        this.lastOpenedBox = lastOpenedBox;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.lastOpenedBox = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.lastOpenedBox);
    }

    public static class Handler implements IMessageHandler<RiftForceSyncLastOpenedBox, IMessage> {
        @Override
        public IMessage onMessage(RiftForceSyncLastOpenedBox message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceSyncLastOpenedBox message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    int value = playerTamedCreatures.getLastOpenedBox();
                    RiftMessages.WRAPPER.sendTo(new RiftForceSyncLastOpenedBox(player, value), (EntityPlayerMP) player);
                }
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    playerTamedCreatures.setLastOpenedBox(message.lastOpenedBox);
                }
            }
        }
    }
}
