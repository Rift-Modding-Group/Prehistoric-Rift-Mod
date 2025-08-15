package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.UUID;

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
                    CreatureNBT partyMemNBT = playerTamedCreatures.getPartyNBT().get(message.partyMemPos);

                    //if creature is deployed, edit the creature itself
                    //otherwise, edit its nbt
                    if (partyMemNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                        UUID creatureUUID = partyMemNBT.getUniqueID();
                        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, creatureUUID);
                        if (creature != null) {
                            //get moves
                            CreatureMove moveSelected = creature.getLearnedMoves().get(message.movePosSelected);
                            CreatureMove moveToMove = creature.getLearnedMoves().get(message.movePosToMove);

                            //swap moves and finalize
                            List<CreatureMove> newLearntMoves = creature.getLearnedMoves();
                            newLearntMoves.set(message.movePosSelected, moveToMove);
                            newLearntMoves.set(message.movePosToMove, moveSelected);
                            creature.setLearnedMoves(newLearntMoves);

                            //swap associated cooldowns so that people will never exploit this
                            //to try skip move cooldowns
                            int selectedCooldown = creature.getMoveCooldown(message.movePosSelected);
                            int toMoveCooldown = creature.getMoveCooldown(message.movePosToMove);
                            creature.setMoveCooldown(message.movePosSelected, toMoveCooldown);
                            creature.setMoveCooldown(message.movePosToMove, selectedCooldown);
                        }
                    }
                    else {
                        //get moves
                        NBTTagList movesNBT = partyMemNBT.getMovesListNBT();
                        NBTTagCompound moveSelected = movesNBT.getCompoundTagAt(message.movePosSelected);
                        NBTTagCompound moveToMove = movesNBT.getCompoundTagAt(message.movePosToMove);

                        //swap moves
                        movesNBT.set(message.movePosSelected, moveToMove);
                        movesNBT.set(message.movePosToMove, moveSelected);

                        //swap associated cooldowns so that people will never exploit this
                        //to try skip move cooldowns
                        int selectedCooldown = partyMemNBT.getMoveCooldown(message.movePosSelected);
                        int toMoveCooldown = partyMemNBT.getMoveCooldown(message.movePosToMove);
                        partyMemNBT.setMoveCooldown(message.movePosSelected, toMoveCooldown);
                        partyMemNBT.setMoveCooldown(message.movePosToMove, selectedCooldown);

                        //update
                        partyMemNBT.setMovesListNBT(movesNBT);
                        playerTamedCreatures.setPartyMemNBT(message.partyMemPos, partyMemNBT);
                    }
                }
            }
        }

        private NBTTagCompound setMoveCooldownNBT(NBTTagCompound creatureNBT, int moveIndex, int cooldown) {
            switch (moveIndex) {
                case 0:
                    creatureNBT.setInteger("CooldownMoveOne", cooldown);
                    break;
                case 1:
                    creatureNBT.setInteger("CooldownMoveTwo", cooldown);
                    break;
                case 2:
                    creatureNBT.setInteger("CooldownMoveThree", cooldown);
                    break;
            }
            return creatureNBT;
        }
    }
}
