package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSpawnDetectParticle extends AbstractMessage<RiftSpawnDetectParticle> {
    private int playerId;
    private int xPos;
    private int yPos;
    private int zPos;

    public RiftSpawnDetectParticle() {}

    public RiftSpawnDetectParticle(EntityPlayer player, int xPos, int yPos, int zPos) {
        this.playerId = player.getEntityId();
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.xPos = buf.readInt();
        this.yPos = buf.readInt();
        this.zPos = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.xPos);
        buf.writeInt(this.yPos);
        buf.writeInt(this.zPos);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftSpawnDetectParticle message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftInitialize.PROXY.spawnParticle("detect", message.xPos + 0.5D, message.yPos + 1D, message.zPos + 0.5D, 0, 0,0);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSpawnDetectParticle message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
