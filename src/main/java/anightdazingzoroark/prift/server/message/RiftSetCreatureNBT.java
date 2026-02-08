package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftSetCreatureNBT extends RiftLibMessage<RiftSetCreatureNBT> {
    private int playerId;
    private NBTTagCompound creatureNBTTagCompound;
    private NBTTagCompound selectedCreatureInfoNBT;

    public RiftSetCreatureNBT() {}

    public RiftSetCreatureNBT(EntityPlayer player, CreatureNBT creatureNBT, SelectedCreatureInfo selectedCreatureInfo) {
        this.playerId = player.getEntityId();
        this.creatureNBTTagCompound = creatureNBT.getCreatureNBT();
        this.selectedCreatureInfoNBT = selectedCreatureInfo.getNBT();
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        this.playerId = byteBuf.readInt();
        this.creatureNBTTagCompound = ByteBufUtils.readTag(byteBuf);
        this.selectedCreatureInfoNBT = ByteBufUtils.readTag(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(this.playerId);
        ByteBufUtils.writeTag(byteBuf, this.creatureNBTTagCompound);
        ByteBufUtils.writeTag(byteBuf, this.selectedCreatureInfoNBT);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetCreatureNBT message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraftServer.getEntityWorld().getEntityByID(message.playerId);
        CreatureNBT newCreatureNBT = new CreatureNBT(message.creatureNBTTagCompound);
        SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);
        if (player == null) return;

        IPlayerTamedCreatures playerTamedCreatures = PlayerTamedCreaturesHelper.getPlayerTamedCreatures(player);
        if (playerTamedCreatures == null) return;

        switch (selectedCreatureInfo.selectedPosType) {
            case PARTY: {
                playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], newCreatureNBT);
                break;
            }
            case BOX: {
                playerTamedCreatures.setBoxMemNBT(selectedCreatureInfo.pos[0], selectedCreatureInfo.pos[1], newCreatureNBT);
                break;
            }
        }

        System.out.println("set saddled when set and sync: "+newCreatureNBT.isSaddled());
        //send to client
        RiftMessages.WRAPPER.sendTo(new RiftSetCreatureNBT(player, newCreatureNBT, selectedCreatureInfo), (EntityPlayerMP) player);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetCreatureNBT message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraft.world.getEntityByID(message.playerId);
        CreatureNBT newCreatureNBT = new CreatureNBT(message.creatureNBTTagCompound);
        SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);
        if (player == null) return;

        IPlayerTamedCreatures playerTamedCreatures = PlayerTamedCreaturesHelper.getPlayerTamedCreatures(player);
        if (playerTamedCreatures == null) return;

        switch (selectedCreatureInfo.selectedPosType) {
            case PARTY: {
                playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], newCreatureNBT);
                break;
            }
            case BOX: {
                playerTamedCreatures.setBoxMemNBT(selectedCreatureInfo.pos[0], selectedCreatureInfo.pos[1], newCreatureNBT);
                break;
            }
        }
        System.out.println("set saddled when set and sync: "+newCreatureNBT.isSaddled());
    }
}
