package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetWorkstation extends RiftLibMessage<RiftSetWorkstation> {
    private int creatureId;
    private boolean startUse;

    public RiftSetWorkstation() {}

    public RiftSetWorkstation(RiftCreature creature, boolean startUse) {
        this.creatureId = creature.getEntityId();
        this.startUse = startUse;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureId = buf.readInt();
        this.startUse = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureId);
        buf.writeBoolean(this.startUse);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetWorkstation message, EntityPlayer messagePlayer, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
        if (creature != null && creature.getOwner() != null && creature.getOwner().equals(messagePlayer)) {
            if (message.startUse) {
                creature.setSitting(false);
                ClientProxy.settingCreatureWorkstation = true;
                ClientProxy.creatureIdForWorkstation = message.creatureId;
                messagePlayer.sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_start"), false);
            }
            else ((IWorkstationUser) creature).clearWorkstation(false);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetWorkstation message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
