package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.PlayerTamedCreatures;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangePartyOrder extends AbstractMessage<RiftChangePartyOrder> {
    private int posSelected;
    private int posToSwap;

    public RiftChangePartyOrder() {}

    public RiftChangePartyOrder(int posSelected, int posToSwap) {
        this.posSelected = posSelected;
        this.posToSwap = posToSwap;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.posSelected = buf.readInt();
        this.posToSwap = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.posSelected);
        buf.writeInt(this.posToSwap);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftChangePartyOrder message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftChangePartyOrder message, EntityPlayer player, MessageContext messageContext) {
        PlayerTamedCreatures playerTamedCreatures = EntityPropertiesHandler.INSTANCE.getProperties(player, PlayerTamedCreatures.class);
        playerTamedCreatures.rearrangePartyCreatures(message.posSelected, message.posToSwap);
    }
}
