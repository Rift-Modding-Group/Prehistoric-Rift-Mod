package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.tileentities.RiftNewTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.UUID;

public class RiftSwapCreatureMoves implements IMessage {
    private int playerId;
    private NBTTagCompound selectedCreatureInfoNBT;
    private NBTTagCompound moveSelectedInfoNBT;
    private NBTTagCompound moveToSwapInfoNBT;

    public RiftSwapCreatureMoves() {}

    public RiftSwapCreatureMoves(EntityPlayer player, SelectedCreatureInfo selectedCreatureInfo, SelectedMoveInfo moveSelectedInfo, SelectedMoveInfo moveToSwapInfo) {
        this.playerId = player.getEntityId();
        this.selectedCreatureInfoNBT = selectedCreatureInfo.getNBT();
        this.moveSelectedInfoNBT = moveSelectedInfo.getNBT();
        this.moveToSwapInfoNBT = moveToSwapInfo.getNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.selectedCreatureInfoNBT = ByteBufUtils.readTag(buf);
        this.moveSelectedInfoNBT = ByteBufUtils.readTag(buf);
        this.moveToSwapInfoNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.selectedCreatureInfoNBT);
        ByteBufUtils.writeTag(buf, this.moveSelectedInfoNBT);
        ByteBufUtils.writeTag(buf, this.moveToSwapInfoNBT);
    }

    public static class Handler implements IMessageHandler<RiftSwapCreatureMoves, IMessage> {
        @Override
        public IMessage onMessage(RiftSwapCreatureMoves message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSwapCreatureMoves message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayerMP messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                SelectedCreatureInfo selectedCreatureInfo = new SelectedCreatureInfo(message.selectedCreatureInfoNBT);
                CreatureNBT selectedCreatureNBT = selectedCreatureInfo.getCreatureNBT(player);
                SelectedMoveInfo moveSelectedInfo = new SelectedMoveInfo(message.moveSelectedInfoNBT);
                SelectedMoveInfo moveToSwapInfo = new SelectedMoveInfo(message.moveToSwapInfoNBT);

                if (playerTamedCreatures != null) {
                    if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                        //now here's where things get tricky
                        //if creature is deployed, edit the creature itself
                        //otherwise, edit its nbt
                        if (selectedCreatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                            UUID selectedCreatureUUID = selectedCreatureNBT.getUniqueID();
                            RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, selectedCreatureUUID);
                            if (creature != null) {
                                this.swapCreatureMoves(creature, moveSelectedInfo, moveToSwapInfo);
                                playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], this.swapCreatureMoves(
                                        selectedCreatureNBT,
                                        moveSelectedInfo,
                                        moveToSwapInfo
                                ));
                            }
                        }
                        else {
                            playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], this.swapCreatureMoves(
                                    selectedCreatureNBT,
                                    moveSelectedInfo,
                                    moveToSwapInfo
                            ));
                        }
                    }
                    else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                        playerTamedCreatures.getBoxNBT().setBoxCreature(
                                selectedCreatureInfo.pos[0],
                                selectedCreatureInfo.pos[1],
                                this.swapCreatureMoves(
                                        selectedCreatureNBT,
                                        moveSelectedInfo,
                                        moveToSwapInfo
                                )
                        );
                    }
                    else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                        TileEntity tileEntity = messagePlayer.world.getTileEntity(selectedCreatureInfo.getCreatureBoxOpenedFrom());
                        if (!(tileEntity instanceof RiftNewTileEntityCreatureBox)) return;
                        RiftNewTileEntityCreatureBox teCreatureBox = (RiftNewTileEntityCreatureBox) tileEntity;
                        CreatureNBT creatureNBT = teCreatureBox.getDeployedCreatures().get(selectedCreatureInfo.pos[0]);

                        //if creature exists in the world, edit the creature itself
                        //otherwise, edit its nbt
                        UUID selectedCreatureUUID = creatureNBT.getUniqueID();
                        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, selectedCreatureUUID);
                        if (creature != null) {
                            this.swapCreatureMoves(creature, moveSelectedInfo, moveToSwapInfo);
                            teCreatureBox.setCreatureInPos(selectedCreatureInfo.pos[0], this.swapCreatureMoves(
                                    selectedCreatureNBT,
                                    moveSelectedInfo,
                                    moveToSwapInfo
                            ));
                        }
                        else {
                            teCreatureBox.setCreatureInPos(selectedCreatureInfo.pos[0], this.swapCreatureMoves(
                                    selectedCreatureNBT,
                                    moveSelectedInfo,
                                    moveToSwapInfo
                            ));
                        }
                    }
                }
            }
        }

        private void swapCreatureMoves(RiftCreature creature, SelectedMoveInfo moveSelectedInfo, SelectedMoveInfo moveToSwapInfo) {
            if (creature == null || moveSelectedInfo == null || moveToSwapInfo == null) return;
            CreatureNBT creatureNBT = new CreatureNBT(creature);
            CreatureMove moveSelected = moveSelectedInfo.getMoveUsingNBT(creatureNBT);
            CreatureMove moveToSwap = moveToSwapInfo.getMoveUsingNBT(creatureNBT);
            List<CreatureMove> newLearntMoves = creatureNBT.getMovesList();
            List<CreatureMove> newLearnableMoves = creatureNBT.getLearnableMovesList();

            if (moveSelectedInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT && moveToSwapInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT) {
                newLearntMoves.set(moveSelectedInfo.movePos, moveToSwap);
                newLearntMoves.set(moveToSwapInfo.movePos, moveSelected);
                creature.setLearnedMoves(newLearntMoves);
            }
            else if (moveSelectedInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE && moveToSwapInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT) {
                newLearnableMoves.set(moveSelectedInfo.movePos, moveToSwap);
                newLearntMoves.set(moveToSwapInfo.movePos, moveSelected);
                creature.setLearnedMoves(newLearntMoves);
                creature.setLearnableMoves(newLearnableMoves);
            }
            else if (moveSelectedInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT && moveToSwapInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE) {
                newLearntMoves.set(moveSelectedInfo.movePos, moveToSwap);
                newLearnableMoves.set(moveToSwapInfo.movePos, moveSelected);
                creature.setLearnedMoves(newLearntMoves);
                creature.setLearnableMoves(newLearnableMoves);
            }
            else if (moveSelectedInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE && moveToSwapInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE) {
                newLearnableMoves.set(moveSelectedInfo.movePos, moveToSwap);
                newLearnableMoves.set(moveToSwapInfo.movePos, moveSelected);
                creature.setLearnableMoves(newLearnableMoves);
            }
        }

        private CreatureNBT swapCreatureMoves(CreatureNBT creatureNBT, SelectedMoveInfo moveSelectedInfo, SelectedMoveInfo moveToSwapInfo) {
            if (creatureNBT == null || moveSelectedInfo == null || moveToSwapInfo == null) return creatureNBT;
            CreatureMove moveSelected = moveSelectedInfo.getMoveUsingNBT(creatureNBT);
            CreatureMove moveToSwap = moveToSwapInfo.getMoveUsingNBT(creatureNBT);

            if (moveSelectedInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT && moveToSwapInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT) {
                creatureNBT.setMove(moveSelectedInfo.movePos, moveToSwap);
                creatureNBT.setMove(moveToSwapInfo.movePos, moveSelected);
            }
            else if (moveSelectedInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE && moveToSwapInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT) {
                creatureNBT.setLearnableMove(moveSelectedInfo.movePos, moveToSwap);
                creatureNBT.setMove(moveToSwapInfo.movePos, moveSelected);
            }
            else if (moveSelectedInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT && moveToSwapInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE) {
                creatureNBT.setMove(moveSelectedInfo.movePos, moveToSwap);
                creatureNBT.setLearnableMove(moveToSwapInfo.movePos, moveSelected);
            }
            else if (moveSelectedInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE && moveToSwapInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE) {
                creatureNBT.setLearnableMove(moveSelectedInfo.movePos, moveToSwap);
                creatureNBT.setLearnableMove(moveToSwapInfo.movePos, moveSelected);
            }
            return creatureNBT;
        }
    }
}
