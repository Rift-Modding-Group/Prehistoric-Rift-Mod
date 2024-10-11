package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftChangeBoxDeployedOrder extends AbstractMessage<RiftChangeBoxDeployedOrder> {
    private byte swapTypeBit;
    private int creatureBoxXPos;
    private int creatureBoxYPos;
    private int creatureBoxZPos;
    private int posSelected;
    private int posToSwap;

    public RiftChangeBoxDeployedOrder() {}

    public RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType swapType, BlockPos creatureBoxPos, int posSelected) {
        this.swapTypeBit = (byte) swapType.ordinal();
        this.creatureBoxXPos = creatureBoxPos.getX();
        this.creatureBoxYPos = creatureBoxPos.getY();
        this.creatureBoxZPos = creatureBoxPos.getZ();
        this.posSelected = posSelected;
        this.posToSwap = -1;
    }

    public RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType swapType, BlockPos creatureBoxPos, int posSelected, int posToSwap) {
        this.swapTypeBit = (byte) swapType.ordinal();
        this.creatureBoxXPos = creatureBoxPos.getX();
        this.creatureBoxYPos = creatureBoxPos.getY();
        this.creatureBoxZPos = creatureBoxPos.getZ();
        this.posSelected = posSelected;
        this.posToSwap = posToSwap;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.swapTypeBit = buf.readByte();
        this.creatureBoxXPos = buf.readInt();
        this.creatureBoxYPos = buf.readInt();
        this.creatureBoxZPos = buf.readInt();
        this.posSelected = buf.readInt();
        this.posToSwap = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.swapTypeBit);
        buf.writeInt(this.creatureBoxXPos);
        buf.writeInt(this.creatureBoxYPos);
        buf.writeInt(this.creatureBoxZPos);
        buf.writeInt(this.posSelected);
        buf.writeInt(this.posToSwap);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftChangeBoxDeployedOrder message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftChangeBoxDeployedOrder message, EntityPlayer player, MessageContext messageContext) {
        RiftChangePartyOrBoxOrder.SwapType swapType = RiftChangePartyOrBoxOrder.SwapType.values()[message.swapTypeBit];
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        BlockPos creatureBoxPos = new BlockPos(message.creatureBoxXPos, message.creatureBoxYPos, message.creatureBoxZPos);
        if (message.posToSwap != -1) {
            switch (swapType) {
                case REARRANGE_BOX_DEPLOYED:
                    playerTamedCreatures.rearrangeDeployedBoxCreatures(player.world, creatureBoxPos, message.posSelected, message.posToSwap);
                    break;
                case PARTY_BOX_DEPLOYED_SWAP:
                    playerTamedCreatures.partyCreatureToBoxCreatureDeployed(player.world, creatureBoxPos, message.posSelected, message.posToSwap);
                    break;
                case BOX_BOX_DEPLOYED_SWAP:
                    playerTamedCreatures.boxCreatureToBoxCreatureDeployed(player.world, creatureBoxPos, message.posSelected, message.posToSwap);
                    break;
                case BOX_DEPLOYED_PARTY_SWAP:
                    playerTamedCreatures.boxCreatureDeployedToPartyCreature(player.world, creatureBoxPos, message.posSelected, message.posToSwap);
                    break;
                case BOX_DEPLOYED_BOX_SWAP:
                    playerTamedCreatures.boxCreatureDeployedToBoxCreature(player.world, creatureBoxPos, message.posSelected, message.posToSwap);
                    break;
            }
        }
        else {
            switch (swapType) {
                case PARTY_TO_BOX_DEPLOYED:
                    playerTamedCreatures.partyCreatureToBoxDeployed(player.world, creatureBoxPos, message.posSelected);
                    break;
                case BOX_TO_BOX_DEPLOYED:
                    playerTamedCreatures.boxCreatureToBoxDeployed(player.world, creatureBoxPos, message.posSelected);
                    break;
                case BOX_DEPLOYED_TO_PARTY:
                    playerTamedCreatures.boxCreatureDeployedToParty(player.world, creatureBoxPos, message.posSelected);
                    break;
                case BOX_DEPLOYED_TO_BOX:
                    playerTamedCreatures.boxCreatureDeployedToBox(player.world, creatureBoxPos, message.posSelected);
                    break;
            }
        }
    }
}
