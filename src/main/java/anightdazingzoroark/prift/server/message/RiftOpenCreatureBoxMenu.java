package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.ServerProxy;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenCreatureBoxMenu extends AbstractMessage<RiftOpenCreatureBoxMenu> {
    private int playerId;
    private int posX;
    private int posY;
    private int posZ;

    public RiftOpenCreatureBoxMenu() {}

    public RiftOpenCreatureBoxMenu(EntityPlayer player, BlockPos pos) {
        this.playerId = player.getEntityId();
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftOpenCreatureBoxMenu message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        ClientProxy.creatureBoxBlockPos = new BlockPos(message.posX, message.posY, message.posZ);
        player.openGui(RiftInitialize.instance, ServerProxy.GUI_CREATURE_BOX, messagePlayer.world, 0, 0, 0);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftOpenCreatureBoxMenu message, EntityPlayer messagePlayer, MessageContext messageContext) {

    }
}
