package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

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
                        playerTamedCreatures.setPartyMemNBT(selectedCreatureInfo.pos[0], this.swapCreatureMoves(
                                selectedCreatureNBT,
                                moveSelectedInfo,
                                moveToSwapInfo
                        ));
                    }
                }
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
