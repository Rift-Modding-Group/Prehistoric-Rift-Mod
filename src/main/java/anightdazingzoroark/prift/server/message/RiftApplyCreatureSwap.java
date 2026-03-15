package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftApplyCreatureSwap extends RiftLibMessage<RiftApplyCreatureSwap> {
    private int playerId;
    private NBTTagCompound swapInfoNBT;

    public RiftApplyCreatureSwap() {}

    public RiftApplyCreatureSwap(EntityPlayer player, SelectedCreatureInfo.SwapInfo swapInfo) {
        this.playerId = player.getEntityId();
        this.swapInfoNBT = swapInfo.getNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.swapInfoNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.swapInfoNBT);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftApplyCreatureSwap message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        SelectedCreatureInfo.SwapInfo swapInfo = new SelectedCreatureInfo.SwapInfo(message.swapInfoNBT);
        if (player == null || !swapInfo.canSwap()) return;

        PlayerPartyProperties playerPartyProperties = PlayerPartyHelper.getPlayerParty(player);
        PlayerCreatureBoxProperties playerCreatureBoxProperties = PlayerCreatureBoxHelper.getPlayerCreatureBox(player);
        if (playerPartyProperties == null || playerCreatureBoxProperties == null) return;

        FixedSizeList<CreatureNBT> playerParty = playerPartyProperties.getPlayerParty();
        CreatureBoxStorage playerCreatureBox = playerCreatureBoxProperties.getCreatureBoxStorage();

        //party first in swap
        if (swapInfo.getCreatureOne().selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            //get nbt for 1st party position
            CreatureNBT nbtOne = playerParty.get(swapInfo.getCreatureOne().getIndex());

            //party -> party
            if (swapInfo.getCreatureTwo().selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                //get nbt for second position
                CreatureNBT nbtTwo = playerParty.get(swapInfo.getCreatureTwo().getIndex());

                //set nbts in each position
                playerParty.set(swapInfo.getCreatureOne().getIndex(), nbtTwo);
                playerParty.set(swapInfo.getCreatureTwo().getIndex(), nbtOne);
            }
            //party -> box
            else if (swapInfo.getCreatureTwo().selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                //get nbt for second position
                FixedSizeList<CreatureNBT> currentPlayerBox = playerCreatureBox.getBoxContents(
                        swapInfo.getCreatureTwo().getBoxIndex()
                );
                CreatureNBT nbtTwo = currentPlayerBox.get(swapInfo.getCreatureTwo().getIndex());

                //set nbts in each position
                playerParty.set(swapInfo.getCreatureOne().getIndex(), nbtTwo);
                playerCreatureBox.setBoxCreature(
                        swapInfo.getCreatureTwo().getBoxIndex(),
                        swapInfo.getCreatureTwo().getIndex(),
                        nbtOne
                );

                //save to creature box
                playerCreatureBoxProperties.setCreatureBoxStorage(playerCreatureBox);
            }

            //save to party
            playerPartyProperties.setPlayerParty(playerParty);
        }
        //box first in swap
        else if (swapInfo.getCreatureOne().selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
            //list at box index to base from
            FixedSizeList<CreatureNBT> currentPlayerBox = playerCreatureBox.getBoxContents(
                    swapInfo.getCreatureOne().getBoxIndex()
            );

            //get nbt for 1st box position
            CreatureNBT nbtOne = currentPlayerBox.get(swapInfo.getCreatureOne().getIndex());

            //box -> party
            if (swapInfo.getCreatureTwo().selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                //get nbts for each position
                CreatureNBT nbtTwo = playerParty.get(swapInfo.getCreatureTwo().getIndex());

                //set nbts in each position
                playerCreatureBox.setBoxCreature(
                        swapInfo.getCreatureOne().getBoxIndex(),
                        swapInfo.getCreatureOne().getIndex(),
                        nbtTwo
                );
                playerParty.set(swapInfo.getCreatureTwo().getIndex(), nbtOne);

                //save to party
                playerPartyProperties.setPlayerParty(playerParty);
            }
            //box -> box
            else if (swapInfo.getCreatureTwo().selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                //get nbts for each position
                CreatureNBT nbtTwo = currentPlayerBox.get(swapInfo.getCreatureTwo().getIndex());

                //set nbts in each position
                playerCreatureBox.setBoxCreature(
                        swapInfo.getCreatureOne().getBoxIndex(),
                        swapInfo.getCreatureOne().getIndex(),
                        nbtTwo
                );
                playerCreatureBox.setBoxCreature(
                        swapInfo.getCreatureTwo().getBoxIndex(),
                        swapInfo.getCreatureTwo().getIndex(),
                        nbtOne
                );
            }

            //save to creature box
            playerCreatureBoxProperties.setCreatureBoxStorage(playerCreatureBox);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftApplyCreatureSwap message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
