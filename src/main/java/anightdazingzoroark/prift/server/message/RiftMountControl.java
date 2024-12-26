package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftMountControl implements IMessage {
    private int creatureId;
    private int control; //0 is for left click, 1 is for right click, 2 is for spacebar
    private int tick; //tick num
    private int targetId;
    private int targetPosX;
    private int targetPosY;
    private int targetPosZ;
    private boolean usingSSR = false;

    public RiftMountControl() {}

    public RiftMountControl(RiftCreature creature, int control, int tick) {
        this(creature, control, tick, null, null);
        this.usingSSR = true;
    }

    public RiftMountControl(RiftCreature creature, int control, int tick, Entity targetEntity, BlockPos targetBlockPos) {
        this.creatureId = creature.getEntityId();
        this.control = control;
        this.tick = tick;
        this.targetId = targetEntity != null ? targetEntity.getEntityId() : -1;
        this.targetPosX = targetBlockPos != null ? targetBlockPos.getX() : 0;
        this.targetPosY = targetBlockPos != null ? targetBlockPos.getY() : -1;
        this.targetPosZ = targetBlockPos != null ? targetBlockPos.getZ() : 0;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
        this.tick = buf.readInt();
        this.targetId = buf.readInt();
        this.targetPosX = buf.readInt();
        this.targetPosY = buf.readInt();
        this.targetPosZ = buf.readInt();
        this.usingSSR = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.control);
        buf.writeInt(this.tick);
        buf.writeInt(this.targetId);
        buf.writeInt(this.targetPosX);
        buf.writeInt(this.targetPosY);
        buf.writeInt(this.targetPosZ);
        buf.writeBoolean(this.usingSSR);
    }

    public static class Handler implements IMessageHandler<RiftMountControl, IMessage> {
        @Override
        public IMessage onMessage(RiftMountControl message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftMountControl message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            World world = playerEntity.getEntityWorld();
            RiftCreature creature = (RiftCreature)world.getEntityByID(message.creatureId);

            if (message.usingSSR) creature.controlInput(message.control, message.tick);
            else {
                Entity target = world.getEntityByID(message.targetId);
                BlockPos pos = new BlockPos(message.targetPosX, message.targetPosY, message.targetPosZ);
                creature.controlInput(message.control, message.tick, target, pos);
            }
        }
    }
}
