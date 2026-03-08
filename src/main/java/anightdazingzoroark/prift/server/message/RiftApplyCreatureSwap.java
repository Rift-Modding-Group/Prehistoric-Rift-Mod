package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.CreatureNBT;
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
        if (playerPartyProperties == null) return;
        FixedSizeList<CreatureNBT> playerParty = playerPartyProperties.getPlayerParty();

        if (swapInfo.getCreatureOne().selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY && swapInfo.getCreatureTwo().selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            CreatureNBT nbtOne = playerParty.get(swapInfo.getCreatureOne().pos[0]);
            CreatureNBT nbtTwo = playerParty.get(swapInfo.getCreatureTwo().pos[0]);

            playerParty.set(swapInfo.getCreatureOne().pos[0], nbtTwo);
            playerParty.set(swapInfo.getCreatureTwo().pos[0], nbtOne);
        }

        playerPartyProperties.setPlayerParty(playerParty);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftApplyCreatureSwap message, EntityPlayer messagePlayer, MessageContext messageContext) {

    }
}
