package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftUpdateBoxDeployed extends AbstractMessage<RiftUpdateBoxDeployed> {
    private int posX;
    private int posY;
    private int posZ;
    private int index;
    private NBTTagCompound tagCompound;

    public RiftUpdateBoxDeployed() {}

    public RiftUpdateBoxDeployed(BlockPos pos, int index, NBTTagCompound tagCompound) {
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.index = index;
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.index = buf.readInt();
        this.tagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeInt(this.index);
        ByteBufUtils.writeTag(buf, this.tagCompound);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftUpdateBoxDeployed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        BlockPos blockPos = new BlockPos(message.posX, message.posY, message.posZ);
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) messagePlayer.world.getTileEntity(blockPos);

        if (creatureBox != null) creatureBox.replaceInCreatureList(message.index, message.tagCompound);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftUpdateBoxDeployed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        BlockPos blockPos = new BlockPos(message.posX, message.posY, message.posZ);
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) messagePlayer.world.getTileEntity(blockPos);

        if (creatureBox != null) creatureBox.replaceInCreatureList(message.index, message.tagCompound);
    }
}
