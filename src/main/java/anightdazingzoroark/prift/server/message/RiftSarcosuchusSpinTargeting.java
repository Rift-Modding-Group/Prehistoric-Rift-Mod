package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.Sarcosuchus;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSarcosuchusSpinTargeting implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftSarcosuchusSpinTargeting, IMessage> {
        @Override
        public IMessage onMessage(RiftSarcosuchusSpinTargeting message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSarcosuchusSpinTargeting message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            Sarcosuchus sarcosuchus = (Sarcosuchus) playerEntity.world.getEntityByID(message.sarcoId);
            EntityLivingBase target = (EntityLivingBase) playerEntity.world.getEntityByID(message.targetId);

            double angleToTarget = Math.atan2(sarcosuchus.getLookVec().z, sarcosuchus.getLookVec().x);
            target.setPosition(2 * Math.cos(angleToTarget) + sarcosuchus.posX, sarcosuchus.posY, 2 * Math.sin(angleToTarget) + sarcosuchus.posZ);
            sarcosuchus.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            sarcosuchus.attackEntityUsingSpin(target);
            target.motionX = 0;
            target.motionY = 0;
            target.motionZ = 0;
        }
    }
}
