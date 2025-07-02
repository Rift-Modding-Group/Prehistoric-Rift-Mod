package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitbox;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftRemoveCreature implements IMessage {
    private int entityId;
    private boolean isCreature; //true if creature, false if hitbox

    public RiftRemoveCreature() {}

    public RiftRemoveCreature(Entity entity, boolean isCreature) {
        this.entityId = entity.getEntityId();
        this.isCreature = isCreature;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.isCreature = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.isCreature);
    }

    public static class Handler implements IMessageHandler<RiftRemoveCreature, IMessage> {
        @Override
        public IMessage onMessage(RiftRemoveCreature message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftRemoveCreature message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                if (message.isCreature) {
                    RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.entityId);
                    if (creature != null) messagePlayer.world.removeEntityDangerously(creature);
                }
                else {
                    EntityHitbox creaturePart = (EntityHitbox) messagePlayer.world.getEntityByID(message.entityId);
                    if (creaturePart != null) messagePlayer.world.removeEntityDangerously(creaturePart);
                }
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                if (message.isCreature) {
                    RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.entityId);
                    if (creature != null) messagePlayer.world.removeEntityDangerously(creature);
                }
                else {
                    EntityHitbox creaturePart = (EntityHitbox) messagePlayer.world.getEntityByID(message.entityId);
                    if (creaturePart != null) messagePlayer.world.removeEntityDangerously(creaturePart);
                }
            }
        }
    }
}
