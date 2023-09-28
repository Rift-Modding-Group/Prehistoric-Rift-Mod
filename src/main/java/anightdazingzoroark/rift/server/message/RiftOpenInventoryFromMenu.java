package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.ServerProxy;
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
