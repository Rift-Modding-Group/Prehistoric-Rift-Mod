package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.Sarcosuchus;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSarcosuchusSpinTargeting extends AbstractMessage<RiftSarcosuchusSpinTargeting> {
    private int sarcoId;
    private int targetId;

    public RiftSarcosuchusSpinTargeting() {}

    public RiftSarcosuchusSpinTargeting(Sarcosuchus sarcosuchus, EntityLivingBase target) {
        this.sarcoId = sarcosuchus.getEntityId();
        this.targetId = target.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.sarcoId = buf.readInt();
        this.targetId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.sarcoId);
        buf.writeInt(this.targetId);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftSarcosuchusSpinTargeting message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer server, RiftSarcosuchusSpinTargeting message, EntityPlayer player, MessageContext messageContext) {
        Sarcosuchus sarcosuchus = (Sarcosuchus) player.world.getEntityByID(message.sarcoId);
        EntityLivingBase target = (EntityLivingBase) player.world.getEntityByID(message.targetId);

        double angleToTarget = Math.atan2(sarcosuchus.getLookVec().z, sarcosuchus.getLookVec().x);
        target.setPosition(2 * Math.cos(angleToTarget) + sarcosuchus.posX, sarcosuchus.posY, 2 * Math.sin(angleToTarget) + sarcosuchus.posZ);
        sarcosuchus.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        sarcosuchus.attackEntityUsingSpin(target);
        target.motionX = 0;
        target.motionY = 0;
        target.motionZ = 0;
    }
}
