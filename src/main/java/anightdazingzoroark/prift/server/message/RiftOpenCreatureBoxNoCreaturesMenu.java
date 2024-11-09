package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenCreatureBoxNoCreaturesMenu extends AbstractMessage<RiftOpenCreatureBoxNoCreaturesMenu> {
    private int playerId;

    public RiftOpenCreatureBoxNoCreaturesMenu() {}

    public RiftOpenCreatureBoxNoCreaturesMenu(EntityPlayer player) {
        this.playerId = player.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftOpenCreatureBoxNoCreaturesMenu message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        ClientProxy.popupFromRadial = PopupFromCreatureBox.NO_CREATURES;
        player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, messagePlayer.world, 0, 0, 0);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftOpenCreatureBoxNoCreaturesMenu message, EntityPlayer messagePlayer, MessageContext messageContext) {

    }
}
