package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftMountControl extends AbstractMessage<RiftMountControl> {
    private int creatureId;
    private int control; //0 is for left click, 1 is for right click
    private boolean execute; //execute regardless of tick
    private int tick; //tick num

    public RiftMountControl() {}

    public RiftMountControl(RiftCreature creature, int control) {
        this(creature, control, 0);
    }

    public RiftMountControl(RiftCreature creature, int control, int tick) {
        this(creature, control, false, tick);
    }

    public RiftMountControl(RiftCreature creature, int control, boolean execute, int tick) {
        this.creatureId = creature.getEntityId();
        this.control = control;
        this.execute = execute;
        this.tick = tick;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.control = buf.readInt();
        this.execute = buf.readBoolean();
        this.tick = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
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

        creature.controlInput(message.control, message.tick, null);
    }
}
