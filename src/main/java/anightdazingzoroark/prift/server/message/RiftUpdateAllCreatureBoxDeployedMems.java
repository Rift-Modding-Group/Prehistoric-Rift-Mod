package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftNewTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftUpdateAllCreatureBoxDeployedMems implements IMessage {
    private int x, y, z;

    public RiftUpdateAllCreatureBoxDeployedMems() {}

    public RiftUpdateAllCreatureBoxDeployedMems(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    public static class Handler implements IMessageHandler<RiftUpdateAllCreatureBoxDeployedMems, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdateAllCreatureBoxDeployedMems message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdateAllCreatureBoxDeployedMems message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                BlockPos creatureBoxPos = new BlockPos(message.x, message.y, message.z);

                //find creature box
                TileEntity te = messagePlayer.world.getTileEntity(creatureBoxPos);
                if (!(te instanceof RiftNewTileEntityCreatureBox)) return;
                RiftNewTileEntityCreatureBox teCreatureBox = (RiftNewTileEntityCreatureBox) te;

                //create entity list
                for (int i = 0; i < teCreatureBox.getDeployedCreatures().size(); i++) {
                    //get creature in the world
                    CreatureNBT creatureNBT = teCreatureBox.getDeployedCreatures().get(i);
                    RiftCreature creatureInWorld = creatureNBT.findCorrespondingCreature(messagePlayer.world);
                    if (creatureInWorld == null) continue;

                    //get nbt from said creature
                    CreatureNBT newCreatureNBT = new CreatureNBT(creatureInWorld);

                    //now replace the old data
                    teCreatureBox.setCreatureInPos(i, newCreatureNBT);
                }
            }
        }
    }
}
