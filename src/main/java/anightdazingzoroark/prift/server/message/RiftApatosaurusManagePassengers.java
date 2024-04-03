package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftApatosaurusManagePassengers extends AbstractMessage<RiftApatosaurusManagePassengers> {
    private int apatoId;

    public RiftApatosaurusManagePassengers() {}

    public RiftApatosaurusManagePassengers(Apatosaurus apatosaurus) {
        this.apatoId = apatosaurus.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.apatoId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.apatoId);
    }

    @Override
    public void onClientReceived(Minecraft client, RiftApatosaurusManagePassengers message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void onServerReceived(MinecraftServer server, RiftApatosaurusManagePassengers message, EntityPlayer player, MessageContext messageContext) {
        World world = player.world;
        Apatosaurus apatosaurus = (Apatosaurus) world.getEntityByID(message.apatoId);
        apatosaurus.addPassengersManual();
    }
}
