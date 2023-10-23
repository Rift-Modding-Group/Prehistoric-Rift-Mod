package anightdazingzoroark.rift.compat.shouldersurfingreloaded.messages;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SSRMountControl implements IMessage {
    private int creatureId;
    private int targetId;
    private int control; //0 is for left click, 1 is for right click
    private boolean execute; //execute regardless of tick
    private int tick; //tick num

    public SSRMountControl() {}

    public SSRMountControl(RiftCreature creature, int targetId, int control) {
        this(creature, targetId, control, 0);
    }

    public SSRMountControl(RiftCreature creature, int targetId, int control, int tick) {
        this(creature, targetId, control, false, tick);
    }

    public SSRMountControl(RiftCreature creature, int targetId,  int control, boolean execute, int tick) {
        this.creatureId = creature.getEntityId();
        this.targetId = targetId;
        this.control = control;
        this.execute = execute;
        this.tick = tick;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.targetId = buf.readInt();
        this.control = buf.readInt();
        this.execute = buf.readBoolean();
        this.tick = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.targetId);
        buf.writeInt(this.control);
        buf.writeBoolean(this.execute);
        buf.writeInt(this.tick);
    }

    public static class Handler implements IMessageHandler<SSRMountControl, IMessage> {
        @Override
        public IMessage onMessage(SSRMountControl message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(SSRMountControl message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            RiftCreature creature = (RiftCreature)world.getEntityByID(message.creatureId);
            EntityLivingBase target;

            if (message.targetId == -1) target = null;
            else target = (EntityLivingBase)world.getEntityByID(message.targetId);

            if (!world.isRemote) creature.controlInput(message.control, message.tick, target);
        }
    }
}
