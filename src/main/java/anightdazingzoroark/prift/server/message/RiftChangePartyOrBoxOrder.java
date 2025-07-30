package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftChangePartyOrBoxOrder implements IMessage {
    private byte swapTypeBit;
    private int playerId;
    private int posSelected;
    private int posToSwap;

    public RiftChangePartyOrBoxOrder() {}

    public RiftChangePartyOrBoxOrder(SwapType swapType, EntityPlayer player, int posSelected) {
        this.swapTypeBit = (byte) swapType.ordinal();
        this.playerId = player.getEntityId();
        this.posSelected = posSelected;
        this.posToSwap = -1;
    }

    public RiftChangePartyOrBoxOrder(SwapType swapType, EntityPlayer player, int posSelected, int posToSwap) {
        this.swapTypeBit = (byte) swapType.ordinal();
        this.playerId = player.getEntityId();
        this.posSelected = posSelected;
        this.posToSwap = posToSwap;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.swapTypeBit = buf.readByte();
        this.playerId = buf.readInt();
        this.posSelected = buf.readInt();
        this.posToSwap = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.swapTypeBit);
        buf.writeInt(this.playerId);
        buf.writeInt(this.posSelected);
        buf.writeInt(this.posToSwap);
    }

    public static class Handler implements IMessageHandler<RiftChangePartyOrBoxOrder, IMessage> {
        @Override
        public IMessage onMessage(RiftChangePartyOrBoxOrder message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangePartyOrBoxOrder message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                SwapType swapType = SwapType.values()[message.swapTypeBit];
                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (message.posToSwap != -1) {
                    switch (swapType) {
                        case REARRANGE_PARTY:
                            playerTamedCreatures.rearrangePartyCreatures(message.posSelected, message.posToSwap);
                            break;
                        case REARRANGE_BOX:
                            //playerTamedCreatures.rearrangeBoxCreatures(message.posSelected, message.posToSwap);
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
