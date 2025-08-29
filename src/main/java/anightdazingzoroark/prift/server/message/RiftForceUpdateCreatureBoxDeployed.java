package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.tileentities.RiftNewTileEntityCreatureBox;
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

public class RiftForceUpdateCreatureBoxDeployed implements IMessage {
    private int playerId;
    private int posX, posY, posZ;
    private int listSize;
    private FixedSizeList<CreatureNBT> tagCompounds;

    public RiftForceUpdateCreatureBoxDeployed() {}

    public RiftForceUpdateCreatureBoxDeployed(EntityPlayer player, BlockPos pos) {
        this(player, pos, new FixedSizeList<>(0));
    }

    public RiftForceUpdateCreatureBoxDeployed(EntityPlayer player, BlockPos pos, FixedSizeList<CreatureNBT> tagCompounds) {
        this.playerId = player.getEntityId();
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.listSize = tagCompounds.size();
        this.tagCompounds = tagCompounds;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();

        this.listSize = buf.readInt();
        NBTTagCompound compound = ByteBufUtils.readTag(buf);
        this.tagCompounds = this.setNBTTagListToNBTList(compound.getTagList("List", 10), this.listSize);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);

        buf.writeInt(this.listSize);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("List", this.setNBTListToNBTTagList(this.tagCompounds));
        ByteBufUtils.writeTag(buf, compound);
    }

    private NBTTagList setNBTListToNBTTagList(FixedSizeList<CreatureNBT> tagCompounds) {
        NBTTagList tagList = new NBTTagList();
        for (CreatureNBT tagCompound : tagCompounds.getList()) tagList.appendTag(tagCompound.getCreatureNBT());
        return tagList;
    }

    private FixedSizeList<CreatureNBT> setNBTTagListToNBTList(NBTTagList tagList, int size) {
        FixedSizeList<CreatureNBT> compoundList = new FixedSizeList<>(size, new CreatureNBT());
        for (int x = 0; x < tagList.tagCount(); x++) {
            CreatureNBT tagCompound = new CreatureNBT((NBTTagCompound) tagList.get(x));
            compoundList.set(x, tagCompound);
        }
        return compoundList;
    }

    public static class Handler implements IMessageHandler<RiftForceUpdateCreatureBoxDeployed, IMessage> {
        @Override
        public IMessage onMessage(RiftForceUpdateCreatureBoxDeployed message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftForceUpdateCreatureBoxDeployed message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                //get player who opened the box
                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                if (player == null) return;

                //check if block pos has creature box tile entity
                TileEntity tileEntity = messagePlayer.world.getTileEntity(new BlockPos(message.posX, message.posY, message.posZ));
                if (!(tileEntity instanceof RiftNewTileEntityCreatureBox)) return;

                //get creature box tile entity
                RiftNewTileEntityCreatureBox teCreatureBox = (RiftNewTileEntityCreatureBox) tileEntity;

                RiftMessages.WRAPPER.sendTo(
                        new RiftForceUpdateCreatureBoxDeployed(
                                player,
                                new BlockPos(message.posX, message.posY, message.posZ),
                                teCreatureBox.getDeployedCreatures()
                        ),
                        (EntityPlayerMP) player
                );
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                if (player == null) return;

                //check if block pos has creature box tile entity
                TileEntity tileEntity = messagePlayer.world.getTileEntity(new BlockPos(message.posX, message.posY, message.posZ));
                if (!(tileEntity instanceof RiftNewTileEntityCreatureBox)) return;

                //get creature box tile entity
                RiftNewTileEntityCreatureBox teCreatureBox = (RiftNewTileEntityCreatureBox) tileEntity;

                //now set the transmitted data
                teCreatureBox.setCreatureListNBT(message.tagCompounds);
            }
        }
    }
}
