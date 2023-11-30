package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenPopupFromRadial extends AbstractMessage<RiftOpenPopupFromRadial> {
    private int creatureId;

    public RiftOpenPopupFromRadial() {}

    public RiftOpenPopupFromRadial(RiftCreature creature) {
        this.creatureId = creature.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftOpenPopupFromRadial message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftOpenPopupFromRadial message, EntityPlayer player, MessageContext messageContext) {
        World world = player.world;
        player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_RADIAL, world, message.creatureId, 0, 0);
    }
}
