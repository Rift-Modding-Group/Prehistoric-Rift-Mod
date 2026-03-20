package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.holder.HolderHelper;
import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.entity.CreatureDeployment;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftReleaseCreature extends RiftLibMessage<RiftReleaseCreature> {
    private int playerId;
    private SelectedCreatureInfo selectedCreatureInfo;

    public RiftReleaseCreature() {}

    public RiftReleaseCreature(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo) {
        this.playerId = player.getEntityId();
        this.selectedCreatureInfo = selectedCreatureInfo;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        NBTTagCompound selectedCreatureInfoNBT = ByteBufUtils.readTag(buf);
        if (selectedCreatureInfoNBT == null) return;
        this.selectedCreatureInfo = SelectedCreatureInfo.createFromNBT(selectedCreatureInfoNBT);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.selectedCreatureInfo.getNBT());
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftReleaseCreature message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        if (player == null) return;

        SelectedCreatureInfo selectedCreatureInfo = message.selectedCreatureInfo;
        if (selectedCreatureInfo == null) return;

        //find creature in the world
        CreatureNBT creatureNBT = HolderHelper.getSelectedCreature(player, selectedCreatureInfo);
        if (creatureNBT.nbtIsEmpty()) return;
        RiftCreature corresponded = creatureNBT.findCorrespondingCreature(server.getEntityWorld());

        //if creature found, drop its items and gear and despawn it
        if (corresponded != null) {
            corresponded.creatureInventory.dropAllItems(server.getEntityWorld(), player.getPosition());
            corresponded.creatureGear.dropAllItems(server.getEntityWorld(), player.getPosition());

            //shouldn't matter what deployment type makes it despawn
            corresponded.setDeploymentType(CreatureDeployment.PARTY_INACTIVE);
        }
        //otherwise, drop its items and gear on player location
        else {
            creatureNBT.dropInventory(server.getEntityWorld(), player.getPosition());
            creatureNBT.dropGear(server.getEntityWorld(), player.getPosition());
        }

        //remove forever
        HolderHelper.setSelectedCreature(player, selectedCreatureInfo, new CreatureNBT());
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftReleaseCreature message, EntityPlayer entityPlayer, MessageContext messageContext) {

    }
}
