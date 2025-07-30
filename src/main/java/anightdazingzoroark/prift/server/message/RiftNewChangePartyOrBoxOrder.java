package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftNewChangePartyOrBoxOrder implements IMessage {
    private byte boxSwapTypeBit;
    private int playerId;
    private int posSelected;
    private int posToSwap;

    //only relevant when dealing with boxes
    private int boxSelected;
    private int boxToSwap;

    public RiftNewChangePartyOrBoxOrder() {}

    public RiftNewChangePartyOrBoxOrder(EntityPlayer player, int posSelected, int posToSwap) {
        this.boxSwapTypeBit = -1;
        this.playerId = player.getEntityId();
        this.posSelected = posSelected;
        this.posToSwap = posToSwap;
        this.boxSelected = -1;
        this.boxToSwap = -1;
    }

    public RiftNewChangePartyOrBoxOrder(BoxSwapType swapType, EntityPlayer player, int boxSelected, int posSelected, int boxToSwap, int posToSwap) {
        this.boxSwapTypeBit = (byte) swapType.ordinal();
        this.playerId = player.getEntityId();
        this.posSelected = posSelected;
        this.posToSwap = posToSwap;
        this.boxSelected = boxSelected;
        this.boxToSwap = boxToSwap;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.boxSwapTypeBit = buf.readByte();
        this.playerId = buf.readInt();
        this.posSelected = buf.readInt();
        this.posToSwap = buf.readInt();
        this.boxSelected = buf.readInt();
        this.boxToSwap = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.boxSwapTypeBit);
        buf.writeInt(this.playerId);
        buf.writeInt(this.posSelected);
        buf.writeInt(this.posToSwap);
        buf.writeInt(this.boxSelected);
        buf.writeInt(this.boxToSwap);
    }

    public static class Handler implements IMessageHandler<RiftNewChangePartyOrBoxOrder, IMessage> {
        @Override
        public IMessage onMessage(RiftNewChangePartyOrBoxOrder message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftNewChangePartyOrBoxOrder message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures == null) return;

                //what to do depends on if swapType or boxSwapType exist
                //this ones for rearranging within party
                if (message.boxSwapTypeBit < 0) playerTamedCreatures.rearrangePartyCreatures(message.posSelected, message.posToSwap);
                //this ones for swapping with anything related to box
                else {
                    BoxSwapType boxSwapType = BoxSwapType.values()[message.boxSwapTypeBit];
                    if (boxSwapType == BoxSwapType.REARRANGE_BOX) {
                        playerTamedCreatures.rearrangeBoxCreatures(message.boxSelected, message.posSelected, message.boxToSwap, message.posToSwap);
                    }
                    else if (boxSwapType == BoxSwapType.BOX_TO_PARTY) {
                        playerTamedCreatures.boxPartySwap(message.boxSelected, message.posSelected, message.posToSwap);
                    }
                }
            }
        }
    }

    public enum BoxDeployedSwapType {
        REARRANGE_BOX_DEPLOYED,
        BOX_DEPLOYED_TO_PARTY,
        BOX_DEPLOYED_TO_BOX;
    }

    public enum BoxSwapType {
        REARRANGE_BOX,
        BOX_TO_PARTY
    }
}
