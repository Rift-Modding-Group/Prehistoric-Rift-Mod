package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.BlockPosUtil;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntity;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftUpdateTileEntityProperty extends RiftLibMessage<RiftUpdateTileEntityProperty> {
    private BlockPos tileEntityPos;
    private String key;
    private NBTTagCompound nbtTagCompound;

    public RiftUpdateTileEntityProperty() {}

    public RiftUpdateTileEntityProperty(BlockPos tileEntityPos, String key, NBTTagCompound nbtTagCompound) {
        this.tileEntityPos = tileEntityPos;
        this.key = key;

        NBTTagCompound nbtToSend = new NBTTagCompound();
        nbtToSend.setTag(key, nbtTagCompound);
        this.nbtTagCompound = nbtToSend;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound blockPosNBT = ByteBufUtils.readTag(buf);
        if (blockPosNBT == null) return;
        this.tileEntityPos = BlockPosUtil.getBlockPosFromNBT(blockPosNBT);

        this.key = ByteBufUtils.readUTF8String(buf);

        NBTTagCompound nbtTagCompound = ByteBufUtils.readTag(buf);
        if (nbtTagCompound == null) return;
        this.nbtTagCompound = nbtTagCompound;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, BlockPosUtil.getBlockPosAsNBT(this.tileEntityPos));
        ByteBufUtils.writeUTF8String(buf, this.key);
        ByteBufUtils.writeTag(buf, this.nbtTagCompound);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftUpdateTileEntityProperty message, EntityPlayer entityPlayer, MessageContext messageContext) {
        TileEntity tileEntity = server.getEntityWorld().getTileEntity(message.tileEntityPos);
        if (!(tileEntity instanceof RiftTileEntity riftTileEntity)) return;

        riftTileEntity.set(message.key, message.nbtTagCompound);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdateTileEntityProperty message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
