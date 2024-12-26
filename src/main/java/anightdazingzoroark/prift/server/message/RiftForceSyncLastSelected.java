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

public class RiftForceSyncLastSelected implements IMessage {
    private int playerId;
    private int lastSelected;

    public RiftForceSyncLastSelected() {}

    public RiftForceSyncLastSelected(EntityPlayer player) {
        this(player, -1);
    }

    public RiftForceSyncLastSelected(EntityPlayer player, int lastSelected) {
        this.playerId = player.getEntityId();
        this.lastSelected = lastSelected;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.lastSelected = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.lastSelected);
    }

    public static class Handler implements IMessageHandler<RiftForceSyncLastSelected, IMessage> {
        @Override
        public IMessage onMessage(RiftForceSyncLastSelected message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceSyncLastSelected message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.lastSelected < 0) RiftMessages.WRAPPER.sendToAll(new RiftForceSyncLastSelected(player, playerTamedCreatures.getLastSelected()));
                else playerTamedCreatures.setLastSelected(message.lastSelected);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.lastSelected < 0) RiftMessages.WRAPPER.sendToServer(new RiftForceSyncLastSelected(player, playerTamedCreatures.getLastSelected()));
                else playerTamedCreatures.setLastSelected(message.lastSelected);
            }
        }
    }
}
