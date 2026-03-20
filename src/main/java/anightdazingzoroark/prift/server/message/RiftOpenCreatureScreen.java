package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.data.CreatureGuiFactory;
import anightdazingzoroark.prift.client.newui.holder.HolderHelper;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
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
    private int playerId;
    private NBTTagCompound selectedCreatureInfoNBT;
    private int pageToOpenTo;

    public RiftOpenCreatureScreen() {}

    public RiftOpenCreatureScreen(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo, int pageToOpenTo) {
        this.playerId = player.getEntityId();
        this.selectedCreatureInfoNBT = selectedCreatureInfo.getNBT();
        this.pageToOpenTo = pageToOpenTo;
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        this.playerId = byteBuf.readInt();
        this.selectedCreatureInfoNBT = ByteBufUtils.readTag(byteBuf);
        this.pageToOpenTo = byteBuf.readInt();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(this.playerId);
        ByteBufUtils.writeTag(byteBuf, this.selectedCreatureInfoNBT);
        byteBuf.writeInt(this.pageToOpenTo);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftOpenCreatureScreen message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraftServer.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;

        //define selected creature info
        SelectedCreatureInfo selectedCreatureInfo = SelectedCreatureInfo.createFromNBT(message.selectedCreatureInfoNBT);
        if (selectedCreatureInfo == null) return;

        //open box deployed creature
        if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
            RiftCreature creature = HolderHelper.getSelectedCreature(player, selectedCreatureInfo)
                    .findCorrespondingCreature(player.world);

            if (creature != null) CreatureGuiFactory.create()
                    .setPageToOpenTo(message.pageToOpenTo)
                    .setMenuOpenedFrom(selectedCreatureInfo.getMenuOpenedFrom())
                    .setLastCreatureBox(selectedCreatureInfo.getCreatureBoxPos())
                    .build().open(player, creature);
        }
        //open player party creature
        else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            RiftCreature creature = HolderHelper.getSelectedCreature(player, selectedCreatureInfo)
                    .findCorrespondingCreature(player.world);

            //found in the world means its already deployed
            if (creature != null) CreatureGuiFactory.create()
                        .setPageToOpenTo(message.pageToOpenTo)
                        .setMenuOpenedFrom(selectedCreatureInfo.getMenuOpenedFrom())
                        .setLastCreatureBox(selectedCreatureInfo.getCreatureBoxPos())
                        .build().open(player, creature);
            //otherwise it means its not
            else CreatureGuiFactory.create()
                        .setPageToOpenTo(message.pageToOpenTo)
                        .setMenuOpenedFrom(selectedCreatureInfo.getMenuOpenedFrom())
                        .setLastCreatureBox(selectedCreatureInfo.getCreatureBoxPos())
                        .build().open(player, selectedCreatureInfo);
        }
        //open box creature
        else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
            CreatureGuiFactory.create()
                    .setPageToOpenTo(message.pageToOpenTo)
                    .setMenuOpenedFrom(selectedCreatureInfo.getMenuOpenedFrom())
                    .setLastCreatureBox(selectedCreatureInfo.getCreatureBoxPos())
                    .build().open(player, selectedCreatureInfo);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftOpenCreatureScreen message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
