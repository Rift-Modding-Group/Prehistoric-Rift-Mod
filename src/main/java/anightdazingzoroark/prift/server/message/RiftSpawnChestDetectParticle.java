package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSpawnChestDetectParticle extends AbstractMessage<RiftSpawnChestDetectParticle> {
    private int playerId;
    private int xPos;
    private int yPos;
    private int zPos;

    public RiftSpawnChestDetectParticle() {}

    public RiftSpawnChestDetectParticle(EntityPlayer player, int xPos, int yPos, int zPos) {
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
    public void onClientReceived(Minecraft minecraft, RiftSpawnChestDetectParticle message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftInitialize.PROXY.spawnParticle("chest_detect", message.xPos + 0.5D, message.yPos + 1D, message.zPos + 0.5D, 0, 0,0);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftSpawnChestDetectParticle message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
