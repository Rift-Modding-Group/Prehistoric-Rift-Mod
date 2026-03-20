package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.holder.HolderHelper;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetSelectedCreature extends RiftLibMessage<RiftSetSelectedCreature> {
    private int playerId;
    private SelectedCreatureInfo selectedCreatureInfo;
    private CreatureNBT creatureNBT;

    public RiftSetSelectedCreature() {}

    public RiftSetSelectedCreature(EntityPlayer player, SelectedCreatureInfo selectedCreature, CreatureNBT creatureNBT) {
        this.playerId = player.getEntityId();
        this.selectedCreatureInfo = selectedCreature;
        this.creatureNBT = creatureNBT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        NBTTagCompound selectedCreatureInfoNBT = ByteBufUtils.readTag(buf);
        if (selectedCreatureInfoNBT == null) return;
        this.selectedCreatureInfo = SelectedCreatureInfo.createFromNBT(selectedCreatureInfoNBT);

        NBTTagCompound creatureNBTAsNBT = ByteBufUtils.readTag(buf);
        if (creatureNBTAsNBT == null) return;
        this.creatureNBT = new CreatureNBT(creatureNBTAsNBT);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.selectedCreatureInfo.getNBT());
        ByteBufUtils.writeTag(buf, this.creatureNBT.getCreatureNBT());
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftSetSelectedCreature message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;

        SelectedCreatureInfo selectedCreatureInfo = message.selectedCreatureInfo;
        if (selectedCreatureInfo == null) return;

        HolderHelper.setSelectedCreature(player, selectedCreatureInfo, message.creatureNBT);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetSelectedCreature message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
