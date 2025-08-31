package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.creatureBoxData.CreatureBoxDataProvider;
import anightdazingzoroark.prift.server.capabilities.creatureBoxData.ICreatureBoxData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftAddOrRemoveCreatureBoxPosFromData implements IMessage {
    private int posX;
    private int posY;
    private int posZ;
    private boolean addOrRemove;

    public RiftAddOrRemoveCreatureBoxPosFromData() {}

    public RiftAddOrRemoveCreatureBoxPosFromData(BlockPos pos, boolean addOrRemove) {
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.addOrRemove = addOrRemove;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.addOrRemove = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeBoolean(this.addOrRemove);
    }


    public static class Handler implements IMessageHandler<RiftAddOrRemoveCreatureBoxPosFromData, IMessage> {
        @Override
        public IMessage onMessage(RiftAddOrRemoveCreatureBoxPosFromData message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftAddOrRemoveCreatureBoxPosFromData message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                ICreatureBoxData creatureBoxData = messagePlayer.world.getCapability(CreatureBoxDataProvider.CREATURE_BOX_DATA_CAPABILITY, null);
                BlockPos blockPos = new BlockPos(message.posX, message.posY, message.posZ);

                if (creatureBoxData != null) {
                    if (message.addOrRemove) {
                        if (!creatureBoxData.getCreatureBoxPositions().contains(blockPos)) creatureBoxData.getCreatureBoxPositions().add(blockPos);
                    }
                    else creatureBoxData.getCreatureBoxPositions().remove(blockPos);
                }
            }
        }
    }
}
