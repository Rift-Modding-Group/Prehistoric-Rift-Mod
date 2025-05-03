package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageUtilizingControl implements IMessage {
    private int entityId;
    private int control; //0 is left click, 1 is right click, 2 is spacebar, 3 is middle click
    private boolean isUsing;

    public RiftManageUtilizingControl() {}

    public RiftManageUtilizingControl(EntityLivingBase entity, boolean isUsing) {
        this(entity, -1, isUsing);
    }

    public RiftManageUtilizingControl(EntityLivingBase entity, int control, boolean isUsing) {
        this.entityId = entity.getEntityId();
        this.control = control;
        this.isUsing = isUsing;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.control = buf.readInt();
        this.isUsing = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.control);
        buf.writeBoolean(this.isUsing);
    }

    public static class Handler implements IMessageHandler<RiftManageUtilizingControl, IMessage> {
        @Override
        public IMessage onMessage(RiftManageUtilizingControl message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftManageUtilizingControl message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            EntityLivingBase entity = (EntityLivingBase) playerEntity.world.getEntityByID(message.entityId);
            if (entity instanceof RiftLargeWeapon) {
                RiftLargeWeapon weapon = (RiftLargeWeapon) entity;
                weapon.setUsingLeftClick(message.isUsing);
            }
        }
    }
}
