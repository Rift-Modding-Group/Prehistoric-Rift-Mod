package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftChangeLearntMovesOrder implements IMessage {
    private int playerId;
    private int partyMemPos;
    private int movePosSelected;
    private int movePosToMove;

    public RiftChangeLearntMovesOrder() {}

    public RiftChangeLearntMovesOrder(EntityPlayer player, int partyMemPos, int movePosSelected, int movePosToMove) {
        this.playerId = player.getEntityId();
        this.partyMemPos = partyMemPos;
        this.movePosSelected = movePosSelected;
        this.movePosToMove = movePosToMove;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.partyMemPos = buf.readInt();
        this.movePosSelected = buf.readInt();
        this.movePosToMove = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partyMemPos);
        buf.writeInt(this.movePosSelected);
        buf.writeInt(this.movePosToMove);
    }

    public static class Handler implements IMessageHandler<RiftChangeLearntMovesOrder, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeLearntMovesOrder message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeLearntMovesOrder message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    NBTTagCompound partyMemNBT = playerTamedCreatures.getPartyNBT().get(message.partyMemPos);

                    //get moves
                    NBTTagList movesNBT = partyMemNBT.getTagList("LearnedMoves", 10);
                    NBTTagCompound moveSelected = movesNBT.getCompoundTagAt(message.movePosSelected);
                    NBTTagCompound moveToMove = movesNBT.getCompoundTagAt(message.movePosToMove);

                    //swap moves
                    movesNBT.set(message.movePosSelected, moveToMove);
                    movesNBT.set(message.movePosToMove, moveSelected);

                    //update
                    partyMemNBT.setTag("LearnedMoves", movesNBT);
                    playerTamedCreatures.setPartyMemNBT(message.partyMemPos, partyMemNBT);
                }
            }
        }
    }
}
