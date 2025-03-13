package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftGrabbedEntitySetPos implements IMessage {
    private int grabberCreatureId;
    private int grabbedEntityId;

    public RiftGrabbedEntitySetPos() {}

    public RiftGrabbedEntitySetPos(RiftCreature creature, Entity grabbedEntity) {
        this.grabberCreatureId = creature.getEntityId();
        this.grabbedEntityId = grabbedEntity.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.grabberCreatureId = buf.readInt();
        this.grabbedEntityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.grabberCreatureId);
        buf.writeInt(this.grabbedEntityId);
    }

    public static class Handler implements IMessageHandler<RiftGrabbedEntitySetPos, IMessage> {
        @Override
        public IMessage onMessage(RiftGrabbedEntitySetPos message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftGrabbedEntitySetPos message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            RiftCreature grabberCreature = (RiftCreature) playerEntity.world.getEntityByID(message.grabberCreatureId);
            Entity grabbedEntity = playerEntity.world.getEntityByID(message.grabbedEntityId);

            grabbedEntity.setPosition(grabberCreature.grabLocation().x, grabberCreature.grabLocation().y, grabberCreature.grabLocation().z);
            grabbedEntity.motionX = 0;
            grabbedEntity.motionY = 0;
            grabbedEntity.motionZ = 0;
        }
    }
}
