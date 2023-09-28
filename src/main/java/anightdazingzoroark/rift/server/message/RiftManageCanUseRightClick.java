package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftManageCanUseRightClick extends AbstractMessage<RiftManageCanUseRightClick> {
    private int creatureId;
    private boolean canUseRightClick;

    public RiftManageCanUseRightClick() {}

    public RiftManageCanUseRightClick(RiftCreature creature, boolean canUseRightClick) {
        this.creatureId = creature.getEntityId();
        this.canUseRightClick = canUseRightClick;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.canUseRightClick = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.canUseRightClick);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftManageCanUseRightClick message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftManageCanUseRightClick message, EntityPlayer player, MessageContext messageContext) {
        World world = player.getEntityWorld();
        RiftCreature interacted = (RiftCreature) player.world.getEntityByID(message.creatureId);

        if (!world.isRemote) interacted.setCanUseRightClick(message.canUseRightClick);
    }
}
