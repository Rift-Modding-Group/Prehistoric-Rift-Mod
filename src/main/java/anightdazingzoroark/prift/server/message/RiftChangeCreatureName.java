package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeCreatureName extends RiftLibMessage<RiftChangeCreatureName> {
    private int creatureId;
    private String newName;

    public RiftChangeCreatureName() {}

    public RiftChangeCreatureName(RiftCreature creature, String newName) {
        this.creatureId = creature.getEntityId();
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.newName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        ByteBufUtils.writeUTF8String(buf, this.newName);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftChangeCreatureName message, EntityPlayer entityPlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) server.getEntityWorld().getEntityByID(message.creatureId);
        if (creature == null) return;

        if (message.newName == null) return;

        creature.setCustomNameTag(message.newName);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftChangeCreatureName message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
