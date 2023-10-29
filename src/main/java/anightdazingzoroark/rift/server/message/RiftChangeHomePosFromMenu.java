package anightdazingzoroark.rift.server.message;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.ServerProxy;
import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeHomePosFromMenu extends AbstractMessage<RiftChangeHomePosFromMenu> {
    private int creatureId;
    private boolean setHome;

    public RiftChangeHomePosFromMenu() {}

    public RiftChangeHomePosFromMenu(RiftCreature creature, boolean setHome) {
        this.creatureId = creature.getEntityId();
        this.setHome = setHome;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.setHome = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.setHome);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftChangeHomePosFromMenu message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer server, RiftChangeHomePosFromMenu message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) player.world.getEntityByID(message.creatureId);
        World world = player.world;
        if (message.setHome) {
            creature.setHomePos();
            player.sendStatusMessage(new TextComponentTranslation("tameupdate.set_home", creature.getName(), creature.getHomePos().getX(), creature.getHomePos().getY(), creature.getHomePos().getZ()), false);
        }
        else player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_RADIAL, world, message.creatureId, 0, 0);
    }
}
