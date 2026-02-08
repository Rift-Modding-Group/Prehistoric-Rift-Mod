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

public class RiftSetCreatureNBTParam extends RiftLibMessage<RiftSetCreatureNBTParam> {
    private int playerId;
    private NBTTagCompound param;
    private NBTTagCompound selectedCreatureInfoNBT;

    public RiftSetCreatureNBTParam() {}

    public RiftSetCreatureNBTParam(EntityPlayer player, NBTTagCompound param, SelectedCreatureInfo selectedCreatureInfo) {
        this.playerId = player.getEntityId();
        this.param = param;
        this.selectedCreatureInfoNBT = selectedCreatureInfo.getNBT();
    }

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        this.playerId = byteBuf.readInt();
        this.param = ByteBufUtils.readTag(byteBuf);
        this.selectedCreatureInfoNBT = ByteBufUtils.readTag(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(this.playerId);
        ByteBufUtils.writeTag(byteBuf, this.param);
        ByteBufUtils.writeTag(byteBuf, this.selectedCreatureInfoNBT);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftSetCreatureNBTParam message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraftServer.getEntityWorld().getEntityByID(message.playerId);
        SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);
        if (player == null) return;

        IPlayerTamedCreatures playerTamedCreatures = PlayerTamedCreaturesHelper.getPlayerTamedCreatures(player);
        if (playerTamedCreatures == null) return;

        switch (selectedCreatureInfo.selectedPosType) {
            case PARTY: {
                CreatureNBT oldCreatureNBT = playerTamedCreatures.getPartyNBT().get(selectedCreatureInfo.pos[0]);
                CreatureNBT mergedCreatureNBT = this.combineNBTs(oldCreatureNBT, message.param);

                playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], mergedCreatureNBT);
                CreatureNBT newCreatureNBT = playerTamedCreatures.getPartyNBT().get(selectedCreatureInfo.pos[0]);
                break;
            }
            case BOX: {
                CreatureNBT oldCreatureNBT = playerTamedCreatures.getBoxNBT().getBoxContents(selectedCreatureInfo.pos[0]).get(selectedCreatureInfo.pos[1]);
                CreatureNBT mergedCreatureNBT = this.combineNBTs(oldCreatureNBT, message.param);
                playerTamedCreatures.setBoxMemNBT(selectedCreatureInfo.pos[0], selectedCreatureInfo.pos[1], mergedCreatureNBT);
                break;
            }
        }

        //send to client
        RiftMessages.WRAPPER.sendTo(new RiftSetCreatureNBTParam(player, message.param, selectedCreatureInfo), (EntityPlayerMP) player);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftSetCreatureNBTParam message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraft.world.getEntityByID(message.playerId);
        SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);
        if (player == null) return;

        IPlayerTamedCreatures playerTamedCreatures = PlayerTamedCreaturesHelper.getPlayerTamedCreatures(player);
        if (playerTamedCreatures == null) return;

        switch (selectedCreatureInfo.selectedPosType) {
            case PARTY: {
                CreatureNBT oldCreatureNBT = playerTamedCreatures.getPartyNBT().get(selectedCreatureInfo.pos[0]);
                CreatureNBT mergedCreatureNBT = this.combineNBTs(oldCreatureNBT, message.param);

                playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], mergedCreatureNBT);
                CreatureNBT newCreatureNBT = playerTamedCreatures.getPartyNBT().get(selectedCreatureInfo.pos[0]);
                break;
            }
            case BOX: {
                CreatureNBT oldCreatureNBT = playerTamedCreatures.getBoxNBT().getBoxContents(selectedCreatureInfo.pos[0]).get(selectedCreatureInfo.pos[1]);
                CreatureNBT mergedCreatureNBT = this.combineNBTs(oldCreatureNBT, message.param);
                playerTamedCreatures.setBoxMemNBT(selectedCreatureInfo.pos[0], selectedCreatureInfo.pos[1], mergedCreatureNBT);
                break;
            }
        }
    }

    private CreatureNBT combineNBTs(CreatureNBT creatureNBT, NBTTagCompound nbtParam) {
        NBTTagCompound toReturn = creatureNBT.getCreatureNBT();
        toReturn.merge(nbtParam);
        return new CreatureNBT(toReturn);
    }
}
