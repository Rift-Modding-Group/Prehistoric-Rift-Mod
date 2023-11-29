package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageCanUseClick extends AbstractMessage<RiftManageCanUseClick> {
    private int creatureId;
    private int mouse;
    private boolean canUseClick;

    public RiftManageCanUseClick() {}

    public RiftManageCanUseClick(RiftCreature creature, int mouse, boolean canUseClick) {
        this.creatureId = creature.getEntityId();
        this.mouse = mouse;
        this.canUseClick = canUseClick;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.mouse = buf.readInt();
        this.canUseClick = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeInt(this.mouse);
        buf.writeBoolean(this.canUseClick);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftManageCanUseClick message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftManageCanUseClick message, EntityPlayer player, MessageContext messageContext) {
        World world = player.world;
        RiftCreature interacted = (RiftCreature) player.world.getEntityByID(message.creatureId);

        if (!world.isRemote) {
            if (message.mouse == 0) interacted.setCanUseLeftClick(message.canUseClick);
            else if (message.mouse == 1) interacted.setCanUseRightClick(message.canUseClick);
        }
    }
}
