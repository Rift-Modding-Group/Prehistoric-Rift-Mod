package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxDataProvider;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxInfo;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.ICreatureBoxData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftAddCreatureBoxData implements IMessage {
    private int posX;
    private int posY;
    private int posZ;
    private int playerId;

    public RiftAddCreatureBoxData() {}

    public RiftAddCreatureBoxData(BlockPos pos, EntityPlayer player) {
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.playerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeInt(this.playerId);
    }

    public static class Handler implements IMessageHandler<RiftAddCreatureBoxData, IMessage> {
        @Override
        public IMessage onMessage(RiftAddCreatureBoxData message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftAddCreatureBoxData message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                ICreatureBoxData creatureBoxData = messagePlayer.world.getCapability(CreatureBoxDataProvider.CREATURE_BOX_DATA_CAPABILITY, null);
                BlockPos blockPos = new BlockPos(message.posX, message.posY, message.posZ);

                if (player != null && creatureBoxData != null) {
                    CreatureBoxInfo info = new CreatureBoxInfo(blockPos, player);
                    creatureBoxData.getCreatureBoxInformation().add(info);
                }
            }
        }
    }
}
