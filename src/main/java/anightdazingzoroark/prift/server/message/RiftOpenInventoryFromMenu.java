package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.data.CreatureGuiFactory;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import com.cleanroommc.modularui.factory.GuiFactories;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenInventoryFromMenu extends RiftLibMessage<RiftOpenInventoryFromMenu> {
    private int playerId;
    private int creatureId;

    public RiftOpenInventoryFromMenu() {}

    public RiftOpenInventoryFromMenu(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.creatureId = creature.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.creatureId);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftOpenInventoryFromMenu message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) messagePlayer.getEntityWorld().getEntityByID(message.creatureId);
        EntityPlayer player = (EntityPlayer) messagePlayer.getEntityWorld().getEntityByID(message.playerId);
        if (creature == null || player == null) return;
        CreatureGuiFactory.INSTANCE.open(player, creature);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenInventoryFromMenu riftOpenInventoryFromMenu, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
