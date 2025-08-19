package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftChangeCreatureBoxName implements IMessage {
    private int playerId;
    private int boxPos;
    private String newName;

    public RiftChangeCreatureBoxName() {}

    public RiftChangeCreatureBoxName(EntityPlayer player, int boxPos, String newName) {
        this.playerId = player.getEntityId();
        this.boxPos = boxPos;
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.boxPos = buf.readInt();
        this.newName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.boxPos);
        ByteBufUtils.writeUTF8String(buf, this.newName);
    }

    public static class Handler implements IMessageHandler<RiftChangeCreatureBoxName, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeCreatureBoxName message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeCreatureBoxName message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                if (playerTamedCreatures != null) {
                    playerTamedCreatures.getBoxNBT().setBoxName(message.boxPos, message.newName);
                }
            }
        }
    }
}
