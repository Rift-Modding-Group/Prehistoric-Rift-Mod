package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftMountControl extends AbstractMessage<RiftMountControl> {
    private int creatureId;
    private int targetId;
    private int control; //0 is for left click, 1 is for right click
    private boolean execute; //execute regardless of tick
    private int tick; //tick num

    public RiftMountControl() {}

    public RiftMountControl(RiftCreature creature, int targetId, int control) {
        this(creature, targetId, control, 0);
    }

    public RiftMountControl(RiftCreature creature, int targetId, int control, int tick) {
        this(creature, targetId, control, false, tick);
    }

    public RiftMountControl(RiftCreature creature, int targetId, int control, boolean execute, int tick) {
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

    @Override
    public void onClientReceived(Minecraft client, RiftMountControl message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftMountControl message, EntityPlayer player, MessageContext messageContext) {
        World world = player.getEntityWorld();
        RiftCreature creature = (RiftCreature)world.getEntityByID(message.creatureId);
        EntityLivingBase target;

        if (message.targetId == -1) target = null;
        else target = (EntityLivingBase)world.getEntityByID(message.targetId);
        creature.controlInput(message.control, message.tick, target);
    }
}
