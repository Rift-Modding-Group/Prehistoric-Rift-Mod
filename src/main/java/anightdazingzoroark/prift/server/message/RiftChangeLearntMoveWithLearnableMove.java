package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftChangeLearntMoveWithLearnableMove implements IMessage {
    private int playerId;
    private int partyMemPos;
    private int learntMovePos;
    private String learnableMove;

    public RiftChangeLearntMoveWithLearnableMove() {}

    public RiftChangeLearntMoveWithLearnableMove(EntityPlayer player, int partyMemPos, int learntMovePos, String learnableMove) {
        this.playerId = player.getEntityId();
        this.partyMemPos = partyMemPos;
        this.learntMovePos = learntMovePos;
        this.learnableMove = learnableMove;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.partyMemPos = buf.readInt();
        this.learntMovePos = buf.readInt();
        this.learnableMove = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partyMemPos);
        buf.writeInt(this.learntMovePos);
        ByteBufUtils.writeUTF8String(buf, this.learnableMove);
    }

    public static class Handler implements IMessageHandler<RiftChangeLearntMoveWithLearnableMove, IMessage> {
        @Override
        public IMessage onMessage(RiftChangeLearntMoveWithLearnableMove message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftChangeLearntMoveWithLearnableMove message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    NBTTagCompound partyMemNBT = playerTamedCreatures.getPartyNBT().get(message.partyMemPos);

                    //get learnt moves and learnable moves
                    NBTTagList learntMovesNBT = partyMemNBT.getTagList("LearnedMoves", 10);
                    NBTTagList learnableMovesNBT = partyMemNBT.getTagList("LearnableMoves", 10);

                    //get learnt move
                    NBTTagCompound learntMoveToSwitch = learntMovesNBT.getCompoundTagAt(message.learntMovePos);

                    //get learnable move and its int position
                    int learnableMovePos = -1;
                    NBTTagCompound learnableMoveToSwitch = new NBTTagCompound();
                    for (int x = 0; x < learnableMovesNBT.tagCount(); x++) {
                        NBTTagCompound nbtToTest = learnableMovesNBT.getCompoundTagAt(x);
                        CreatureMove moveToTest = CreatureMove.values()[nbtToTest.getInteger("Move")];
                        if (moveToTest != null && moveToTest.toString().equals(message.learnableMove)) {
                            learnableMovePos = x;
                            learnableMoveToSwitch = nbtToTest;
                            break;
                        }
                    }

                    if (learnableMoveToSwitch.isEmpty() || learnableMovePos < 0) return;

                    //switch
                    learntMovesNBT.set(message.learntMovePos, learnableMoveToSwitch);
                    learnableMovesNBT.set(learnableMovePos, learntMoveToSwitch);

                    //update nbt
                    partyMemNBT.setTag("LearnedMoves", learntMovesNBT);
                    partyMemNBT.setTag("LearnableMoves", learnableMovesNBT);
                    playerTamedCreatures.setPartyMemNBT(message.partyMemPos, partyMemNBT);
                }
            }
        }
    }
}
