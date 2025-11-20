package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenInventoryFromMenu extends RiftLibMessage<RiftOpenInventoryFromMenu> {
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftOpenInventoryFromMenu message, EntityPlayer messagePlayer, MessageContext messageContext) {
        World world = messagePlayer.getEntityWorld();
        messagePlayer.openGui(RiftInitialize.instance, RiftGui.GUI_CREATURE_INVENTORY, world, message.creatureId, 0, 0);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenInventoryFromMenu riftOpenInventoryFromMenu, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
