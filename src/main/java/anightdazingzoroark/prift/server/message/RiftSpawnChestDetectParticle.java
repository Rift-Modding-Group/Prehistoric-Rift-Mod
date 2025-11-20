package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.message.RiftLibMessageSide;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftSpawnChestDetectParticle extends RiftLibMessage<RiftSpawnChestDetectParticle> {
    private int xPos;
    private int yPos;
    private int zPos;

    public RiftSpawnChestDetectParticle() {}

    public RiftSpawnChestDetectParticle(int xPos, int yPos, int zPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.xPos = buf.readInt();
        this.yPos = buf.readInt();
        this.zPos = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.xPos);
        buf.writeInt(this.yPos);
        buf.writeInt(this.zPos);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSpawnChestDetectParticle message, EntityPlayer entityPlayer, MessageContext messageContext) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftSpawnChestDetectParticle message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftInitialize.PROXY.spawnParticle("chest_detect", message.xPos + 0.5D, message.yPos + 1D, message.zPos + 0.5D, 0, 0,0);
    }
}
