package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftMortar;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftIncrementControlUse implements IMessage {
    private int entityId;
    private int control;

    public RiftIncrementControlUse() {}

    public RiftIncrementControlUse(EntityLivingBase entity) {
        this(entity, -1);
    }

    public RiftIncrementControlUse(EntityLivingBase entity, int control) {
        this.entityId = entity.getEntityId();
        this.control = control;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.control = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.control);
    }

    public static class Handler implements IMessageHandler<RiftIncrementControlUse, IMessage> {
        @Override
        public IMessage onMessage(RiftIncrementControlUse message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftIncrementControlUse message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            EntityLivingBase entity = (EntityLivingBase) playerEntity.world.getEntityByID(message.entityId);

            if (!playerEntity.world.isRemote) {
                if (entity instanceof RiftCreature) {
                    RiftCreature creature = (RiftCreature) entity;
                    switch (message.control) {
                        case 0:
                            creature.setLeftClickUse(creature.getLeftClickUse() + 1);
                            break;
                        case 1:
                            if (creature.getEnergy() > 6) creature.setRightClickUse(creature.getRightClickUse() + 1);
                            break;
                        case 2:
                            if (creature.getEnergy() > 6) creature.setSpacebarUse(creature.getSpacebarUse() + 1);
                            break;
                        case 3:
                            if (creature.getEnergy() > 6) creature.setMiddleClickUse(creature.getMiddleClickUse() + 1);
                            break;
                    }
                }
                else if (entity instanceof RiftCatapult) {
                    RiftCatapult catapult = (RiftCatapult) entity;
                    catapult.setLeftClickUse(catapult.getLeftClickUse() + 1);
                }
                else if (entity instanceof RiftMortar) {
                    RiftMortar mortar = (RiftMortar) entity;
                    mortar.setLeftClickUse(mortar.getLeftClickUse() + 1);
                }
            }
        }
    }
}
