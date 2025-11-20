package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftApatosaurusManagePassengers extends RiftLibMessage<RiftApatosaurusManagePassengers> {
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
    public void executeOnServer(MinecraftServer minecraftServer, RiftApatosaurusManagePassengers message, EntityPlayer entityPlayer, MessageContext messageContext) {
        World world = entityPlayer.world;
        Apatosaurus apatosaurus = (Apatosaurus) world.getEntityByID(message.apatoId);
        if (apatosaurus != null) apatosaurus.addPassengersManual();
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftApatosaurusManagePassengers message, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
