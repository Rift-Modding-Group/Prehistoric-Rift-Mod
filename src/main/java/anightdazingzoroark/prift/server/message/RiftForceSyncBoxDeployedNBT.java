package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftForceSyncBoxDeployedNBT implements IMessage {
    private int creatureBoxPosX;
    private int creatureBoxPosY;
    private int creatureBoxPosZ;
    private int playerId;
    private NBTTagCompound creatureNBTTagCompound;

    public RiftForceSyncBoxDeployedNBT() {}

    public RiftForceSyncBoxDeployedNBT(BlockPos blockPos, EntityPlayer player) {
        this.creatureBoxPosX = blockPos.getX();
        this.creatureBoxPosY = blockPos.getY();
        this.creatureBoxPosZ = blockPos.getZ();
        this.playerId = player.getEntityId();
        this.creatureNBTTagCompound = new NBTTagCompound();
    }

    public RiftForceSyncBoxDeployedNBT(BlockPos blockPos, EntityPlayer player, NBTTagCompound listTagCompound) {
        this.creatureBoxPosX = blockPos.getX();
        this.creatureBoxPosY = blockPos.getY();
        this.creatureBoxPosZ = blockPos.getZ();
        this.playerId = player.getEntityId();
        this.creatureNBTTagCompound = listTagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureBoxPosX = buf.readInt();
        this.creatureBoxPosY = buf.readInt();
        this.creatureBoxPosZ = buf.readInt();
        this.playerId = buf.readInt();
        this.creatureNBTTagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureBoxPosX);
        buf.writeInt(this.creatureBoxPosY);
        buf.writeInt(this.creatureBoxPosZ);
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.creatureNBTTagCompound);
    }


    public static class Handler implements IMessageHandler<RiftForceSyncBoxDeployedNBT, IMessage> {
        @Override
        public IMessage onMessage(RiftForceSyncBoxDeployedNBT message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceSyncBoxDeployedNBT message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                BlockPos creatureBoxPos = new BlockPos(message.creatureBoxPosX, message.creatureBoxPosY, message.creatureBoxPosZ);

                //find creature box
                TileEntity te = messagePlayer.world.getTileEntity(creatureBoxPos);
                if (!(te instanceof RiftTileEntityCreatureBox)) return;
                RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) te;

                //create an nbt list
                NBTTagCompound tagCompound = new NBTTagCompound();
                NBTTagList creatureNBTTagList = new NBTTagList();
                for (int i = 0; i < teCreatureBox.getDeployedCreatures().size(); i++) {
                    creatureNBTTagList.appendTag(teCreatureBox.getDeployedCreatures().get(i).getCreatureNBT());
                }
                tagCompound.setTag("NewTagList", creatureNBTTagList);

                //now send to player
                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                RiftMessages.WRAPPER.sendTo(new RiftForceSyncBoxDeployedNBT(creatureBoxPos, player, tagCompound), (EntityPlayerMP) player);
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;
                BlockPos creatureBoxPos = new BlockPos(message.creatureBoxPosX, message.creatureBoxPosY, message.creatureBoxPosZ);

                //find creature box
                TileEntity te = messagePlayer.world.getTileEntity(creatureBoxPos);
                if (!(te instanceof RiftTileEntityCreatureBox)) return;
                RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) te;

                //get new list
                NBTTagList newCreatureNBTTagList = message.creatureNBTTagCompound.getTagList("NewTagList", 10);

                //update nbt list
                for (int i = 0; i < teCreatureBox.getDeployedCreatures().size(); i++) {
                    CreatureNBT newCreatureNBT = new CreatureNBT(newCreatureNBTTagList.getCompoundTagAt(i));
                    teCreatureBox.setCreatureInPos(i, newCreatureNBT);
                }
            }
        }
    }
}
