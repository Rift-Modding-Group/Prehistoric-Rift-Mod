package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangePartyOrBoxOrder extends AbstractMessage<RiftChangePartyOrBoxOrder> {
    private byte swapTypeBit;
    private int posSelected;
    private int posToSwap;

    public RiftChangePartyOrBoxOrder() {}

    public RiftChangePartyOrBoxOrder(SwapType swapType, int posSelected) {
        this.swapTypeBit = (byte) swapType.ordinal();
        this.posSelected = posSelected;
        this.posToSwap = -1;
    }

    public RiftChangePartyOrBoxOrder(SwapType swapType, int posSelected, int posToSwap) {
        this.swapTypeBit = (byte) swapType.ordinal();
        this.posSelected = posSelected;
        this.posToSwap = posToSwap;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.swapTypeBit = buf.readByte();
        this.posSelected = buf.readInt();
        this.posToSwap = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.swapTypeBit);
        buf.writeInt(this.posSelected);
        buf.writeInt(this.posToSwap);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftChangePartyOrBoxOrder message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftChangePartyOrBoxOrder message, EntityPlayer player, MessageContext messageContext) {
        SwapType swapType = SwapType.values()[message.swapTypeBit];
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (message.posToSwap != -1) {
            switch (swapType) {
                case REARRANGE_PARTY:
                    playerTamedCreatures.rearrangePartyCreatures(message.posSelected, message.posToSwap);
                    break;
                case REARRANGE_BOX:
                    playerTamedCreatures.rearrangeBoxCreatures(message.posSelected, message.posToSwap);
                    break;
                case PARTY_BOX_SWAP:
                    playerTamedCreatures.partyCreatureToBoxCreature(message.posSelected, message.posToSwap);
                    break;
                case BOX_PARTY_SWAP:
                    playerTamedCreatures.boxCreatureToPartyCreature(message.posSelected, message.posToSwap);
                    break;
            }
        }
        else {
            switch (swapType) {
                case PARTY_TO_BOX:
                    playerTamedCreatures.partyCreatureToBox(message.posSelected);
                    break;
                case BOX_TO_PARTY:
                    playerTamedCreatures.boxCreatureToParty(message.posSelected);
                    break;
            }
        }
    }

    public enum SwapType {
        REARRANGE_PARTY,
        REARRANGE_BOX,
        REARRANGE_BOX_DEPLOYED,

        PARTY_BOX_SWAP,
        PARTY_TO_BOX,
        PARTY_BOX_DEPLOYED_SWAP,
        PARTY_TO_BOX_DEPLOYED,

        BOX_PARTY_SWAP,
        BOX_TO_PARTY,
        BOX_BOX_DEPLOYED_SWAP,
        BOX_TO_BOX_DEPLOYED,

        BOX_DEPLOYED_PARTY_SWAP,
        BOX_DEPLOYED_TO_PARTY,
        BOX_DEPLOYED_BOX_SWAP,
        BOX_DEPLOYED_TO_BOX;
    }
}
