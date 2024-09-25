package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.ServerProxy;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenInventoryFromMenu extends AbstractMessage<RiftOpenInventoryFromMenu> {
    private int creatureId;

    public RiftOpenInventoryFromMenu() {}

    public RiftOpenInventoryFromMenu(int creatureId) {
        this.creatureId = creatureId;
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
    public void onClientReceived(Minecraft client, RiftOpenInventoryFromMenu message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftOpenInventoryFromMenu message, EntityPlayer player, MessageContext messageContext) {
        World world = player.getEntityWorld();
        player.openGui(RiftInitialize.instance, ServerProxy.GUI_CREATURE_INVENTORY, world, message.creatureId, 0, 0);
    }
}
