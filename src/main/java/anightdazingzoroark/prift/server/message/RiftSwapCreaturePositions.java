package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftSwapCreaturePositions implements IMessage {
    private int playerId;
    private NBTTagCompound selectedPosNBT;
    private NBTTagCompound posToSwapNBT;

    public RiftSwapCreaturePositions() {}

    public RiftSwapCreaturePositions(EntityPlayer player, SelectedCreatureInfo selectedPos, SelectedCreatureInfo posToSwap) {
        this.playerId = player.getEntityId();
        this.selectedPosNBT = selectedPos.getNBT();
        this.posToSwapNBT = posToSwap.getNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.selectedPosNBT = ByteBufUtils.readTag(buf);
        this.posToSwapNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.selectedPosNBT);
        ByteBufUtils.writeTag(buf, this.posToSwapNBT);
    }

    public static class Handler implements IMessageHandler<RiftSwapCreaturePositions, IMessage> {
        @Override
        public IMessage onMessage(RiftSwapCreaturePositions message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftSwapCreaturePositions message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                SelectedCreatureInfo selectedPos = new SelectedCreatureInfo(message.selectedPosNBT);
                SelectedCreatureInfo posToSwap = new SelectedCreatureInfo(message.posToSwapNBT);

                if (playerTamedCreatures != null) {
                    if (selectedPos.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                        if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                            playerTamedCreatures.rearrangePartyCreatures(
                                    selectedPos.pos[0],
                                    posToSwap.pos[0]
                            );
                        }
                        else if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                            this.setSpawnedCreatureDeployment(player, selectedPos, PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                            playerTamedCreatures.boxPartySwap(
                                    posToSwap.pos[0],
                                    posToSwap.pos[1],
                                    selectedPos.pos[0]
                            );
                        }
                        else if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                            this.attachCreatureToCreatureBox(player, posToSwap.getCreatureBoxOpenedFrom(), selectedPos);
                            this.setSpawnedCreatureDeployment(player, posToSwap, PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
                            playerTamedCreatures.boxDeployedPartySwap(
                                    messagePlayer.world,
                                    posToSwap.getCreatureBoxOpenedFrom(),
                                    posToSwap.pos[0],
                                    selectedPos.pos[0]
                            );
                        }
                    }
                    else if (selectedPos.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                        if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                            this.setSpawnedCreatureDeployment(player, posToSwap, PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                            playerTamedCreatures.boxPartySwap(
                                    selectedPos.pos[0],
                                    selectedPos.pos[1],
                                    posToSwap.pos[0]
                            );
                        }
                        else if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                            playerTamedCreatures.rearrangeBoxCreatures(
                                    posToSwap.pos[0],
                                    posToSwap.pos[1],
                                    selectedPos.pos[0],
                                    selectedPos.pos[1]
                            );
                        }
                        else if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                            this.setSpawnedCreatureDeployment(player, posToSwap, PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                            playerTamedCreatures.boxDeployedBoxSwap(
                                    messagePlayer.world,
                                    posToSwap.getCreatureBoxOpenedFrom(),
                                    posToSwap.pos[0],
                                    selectedPos.pos[0],
                                    selectedPos.pos[1]
                            );
                        }
                    }
                    else if (selectedPos.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                        if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                            this.setSpawnedCreatureDeployment(player, selectedPos, PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
                            this.attachCreatureToCreatureBox(player, selectedPos.getCreatureBoxOpenedFrom(), posToSwap);
                            playerTamedCreatures.boxDeployedPartySwap(
                                    messagePlayer.world,
                                    selectedPos.getCreatureBoxOpenedFrom(),
                                    selectedPos.pos[0],
                                    posToSwap.pos[0]
                            );
                        }
                        else if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                            this.setSpawnedCreatureDeployment(player, selectedPos, PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
                            playerTamedCreatures.boxDeployedBoxSwap(
                                    messagePlayer.world,
                                    selectedPos.getCreatureBoxOpenedFrom(),
                                    selectedPos.pos[0],
                                    posToSwap.pos[0],
                                    posToSwap.pos[1]
                            );
                        }
                        else if (posToSwap.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                            playerTamedCreatures.rearrangeDeployedBoxCreatures(
                                    messagePlayer.world,
                                    selectedPos.getCreatureBoxOpenedFrom(),
                                    selectedPos.pos[0],
                                    posToSwap.pos[0]
                            );
                        }
                    }
                }
            }
        }

        private void setSpawnedCreatureDeployment(EntityPlayer player, SelectedCreatureInfo posToDespawn, PlayerTamedCreatures.DeploymentType deploymentType) {
            RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, posToDespawn.getCreatureNBT(player).getUniqueID());
            if (creature == null) return;
            creature.clearHomePos();
            creature.setDeploymentType(deploymentType);
        }

        private void attachCreatureToCreatureBox(EntityPlayer player, BlockPos creatureBlockPos, SelectedCreatureInfo creatureInfo) {
            RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, creatureInfo.getCreatureNBT(player).getUniqueID());
            if (creature == null) return;
            creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE);
            creature.setHomePos(creatureBlockPos.getX(), creatureBlockPos.getY(), creatureBlockPos.getZ());
        }
    }
}
