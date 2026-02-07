package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.data.CreatureGuiFactory;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftOpenCreatureScreen extends RiftLibMessage<RiftOpenCreatureScreen> {
    private boolean useSelectedCreature;
    private int playerId;
    private int creatureId;
    private NBTTagCompound selectedCreatureInfoNBT;

    public RiftOpenCreatureScreen() {}

    public RiftOpenCreatureScreen(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.creatureId = creature.getEntityId();
        this.useSelectedCreature = false;
    }

    public RiftOpenCreatureScreen(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        this.playerId = player.getEntityId();
        this.selectedCreatureInfoNBT = selectedCreatureInfo.getNBT();
        this.useSelectedCreature = true;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        this.useSelectedCreature = byteBuf.readBoolean();
        this.playerId = byteBuf.readInt();
        this.creatureId = byteBuf.readInt();
        this.selectedCreatureInfoNBT = ByteBufUtils.readTag(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeBoolean(this.useSelectedCreature);
        byteBuf.writeInt(this.playerId);
        byteBuf.writeInt(this.creatureId);
        ByteBufUtils.writeTag(byteBuf, this.selectedCreatureInfoNBT);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftOpenCreatureScreen message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraftServer.getEntityWorld().getEntityByID(message.playerId);
        if (message.useSelectedCreature) {
            SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);
            CreatureGuiFactory.INSTANCE.open(player, selectedCreatureInfo);
        }
        else {
            RiftCreature creature = (RiftCreature) minecraftServer.getEntityWorld().getEntityByID(message.creatureId);
            CreatureGuiFactory.INSTANCE.open(player, creature);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenCreatureScreen message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
