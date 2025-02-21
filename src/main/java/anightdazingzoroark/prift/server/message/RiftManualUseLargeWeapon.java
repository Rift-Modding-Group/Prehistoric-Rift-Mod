package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftManualUseLargeWeapon implements IMessage {
    private int creatureId;
    private boolean using;

    public RiftManualUseLargeWeapon() {}

    public RiftManualUseLargeWeapon(RiftCreature creature, boolean using) {
        this.creatureId = creature.getEntityId();
        this.using = using;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.using = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.using);
    }


    public static class Handler implements IMessageHandler<RiftManualUseLargeWeapon, IMessage> {
        @Override
        public IMessage onMessage(RiftManualUseLargeWeapon message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftManualUseLargeWeapon message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);

                if (creature != null) {
                    if (message.using) {
                        creature.setUsingLargeWeapon(true);
                        if (creature.getLargeWeapon().maxCooldown > 0 && creature.canFireLargeWeapon()) creature.setLargeWeaponUse(Math.min(creature.getLargeWeaponUse() + 1, creature.getLargeWeapon().maxUse));
                    }
                    else creature.setUsingLargeWeapon(false);
                }
            }
        }
    }
}
