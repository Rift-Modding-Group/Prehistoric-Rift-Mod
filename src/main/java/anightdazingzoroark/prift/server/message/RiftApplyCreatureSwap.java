package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.BlockPosUtil;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.CreatureBoxStorage;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxHelper;
import anightdazingzoroark.prift.server.properties.playerCreatureBox.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;

public class RiftApplyCreatureSwap extends RiftLibMessage<RiftApplyCreatureSwap> {
    private int playerId;
    private NBTTagCompound swapInfoNBT;
    private BlockPos creatureBoxPos;

    public RiftApplyCreatureSwap() {}

    public RiftApplyCreatureSwap(EntityPlayer player, SelectedCreatureInfo.SwapInfo swapInfo) {
        this(player, swapInfo, BlockPos.ORIGIN);
    }

    public RiftApplyCreatureSwap(EntityPlayer player, SelectedCreatureInfo.SwapInfo swapInfo, BlockPos creatureBoxPos) {
        this.playerId = player.getEntityId();
        this.swapInfoNBT = swapInfo.getNBT();
        this.creatureBoxPos = creatureBoxPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        NBTTagCompound preSwapInfoNBT = ByteBufUtils.readTag(buf);
        if (preSwapInfoNBT != null) this.swapInfoNBT = preSwapInfoNBT;

        NBTTagCompound preCreatureBoxPosNBT = ByteBufUtils.readTag(buf);
        if (preCreatureBoxPosNBT != null) this.creatureBoxPos = BlockPosUtil.getBlockPosFromNBT(preCreatureBoxPosNBT);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeTag(buf, this.swapInfoNBT);
        ByteBufUtils.writeTag(buf, BlockPosUtil.getBlockPosAsNBT(this.creatureBoxPos));
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftApplyCreatureSwap message, EntityPlayer messagePlayer, MessageContext messageContext) {
        //prepare player and swap info
        EntityPlayer player = (EntityPlayer) server.getEntityWorld().getEntityByID(message.playerId);
        SelectedCreatureInfo.SwapInfo swapInfo = new SelectedCreatureInfo.SwapInfo(message.swapInfoNBT);
        if (player == null || !swapInfo.canSwap()) return;

        //prepare player properties for party and box
        PlayerPartyProperties playerPartyProperties = PlayerPartyHelper.getPlayerParty(player);
        PlayerCreatureBoxProperties playerCreatureBoxProperties = PlayerCreatureBoxHelper.getPlayerCreatureBox(player);

        //prepare creature box
        TileEntity tileEntity = server.getEntityWorld().getTileEntity(message.creatureBoxPos);
        RiftTileEntityCreatureBox teCreatureBox = (RiftTileEntityCreatureBox) tileEntity;

        //get creature nbts
        CreatureNBT nbtOne = this.getCreatureNBT(
                swapInfo.getCreatureOne(),
                playerPartyProperties,
                playerCreatureBoxProperties,
                teCreatureBox
        );
        CreatureNBT nbtTwo = this.getCreatureNBT(
                swapInfo.getCreatureTwo(),
                playerPartyProperties,
                playerCreatureBoxProperties,
                teCreatureBox
        );

        //prepare each for swap, not necessary when both are in same position
        if (swapInfo.getCreatureOne().selectedPosType != swapInfo.getCreatureTwo().selectedPosType) {
            this.prepareNBTForSwap(
                    nbtOne, swapInfo.getCreatureOne(), swapInfo.getCreatureTwo(),
                    server, player.getPosition(), playerPartyProperties
            );
            this.prepareNBTForSwap(
                    nbtTwo, swapInfo.getCreatureTwo(), swapInfo.getCreatureOne(),
                    server, player.getPosition(), playerPartyProperties
            );
        }

        //execute swap
        //for if swap positions are distinct, prevent syncing problems
        if (swapInfo.getCreatureOne().selectedPosType != swapInfo.getCreatureTwo().selectedPosType) {
            this.performSwap(
                    nbtOne, swapInfo.getCreatureTwo(),
                    playerPartyProperties,
                    playerCreatureBoxProperties,
                    teCreatureBox
            );
            this.performSwap(
                    nbtTwo, swapInfo.getCreatureOne(),
                    playerPartyProperties,
                    playerCreatureBoxProperties,
                    teCreatureBox
            );
        }
        //for if swap positions are in the same pos
        else this.performSwap(
                nbtOne, nbtTwo, swapInfo,
                playerPartyProperties,
                playerCreatureBoxProperties,
                teCreatureBox
        );
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftApplyCreatureSwap message, EntityPlayer messagePlayer, MessageContext messageContext) {}

    @NotNull
    private CreatureNBT getCreatureNBT(
            SelectedCreatureInfo selectedCreatureInfo,
            PlayerPartyProperties playerPartyProperties,
            PlayerCreatureBoxProperties playerCreatureBoxProperties,
            RiftTileEntityCreatureBox teCreatureBox
    ) {
        if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
            return playerPartyProperties.getPlayerParty().get(selectedCreatureInfo.getIndex());
        }
        else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
            if (playerCreatureBoxProperties != null) {
                return playerCreatureBoxProperties.getCreatureBoxStorage()
                        .getBoxContents(selectedCreatureInfo.getBoxIndex())
                        .get(selectedCreatureInfo.getIndex());
            }
        }
        else if (selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
            if (teCreatureBox != null) {
                return teCreatureBox.getDeployedCreatures().get(selectedCreatureInfo.getIndex());
            }
        }

        return new CreatureNBT();
    }

    private void prepareNBTForSwap(
            CreatureNBT nbtToSwap,
            SelectedCreatureInfo posToSwap, SelectedCreatureInfo posToSwapTo,
            MinecraftServer server,
            BlockPos playerPos,
            PlayerPartyProperties playerPartyProperties
    ) {
        //skip all this if nbt is empty
        if (nbtToSwap.nbtIsEmpty()) return;

        //normal operation
        if (nbtToSwap.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY
                || nbtToSwap.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE
        ) {
            this.preparePartyCreatureForSwap(
                    server, playerPartyProperties, nbtToSwap,
                    playerPos, posToSwap.getIndex(),
                    posToSwapTo.selectedPosType
            );
        }
        else if (nbtToSwap.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE_INACTIVE) {
            this.prepareBoxCreatureForSwap(nbtToSwap, posToSwapTo.selectedPosType);
        }
        else if (nbtToSwap.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) {
            this.prepareBoxDeployedCreatureForSwap(server, nbtToSwap, posToSwapTo.selectedPosType);
        }
    }

    private void performSwap(
            CreatureNBT nbtToSwap, SelectedCreatureInfo posToSwapTo,
            PlayerPartyProperties playerPartyProperties,
            PlayerCreatureBoxProperties playerCreatureBoxProperties,
            RiftTileEntityCreatureBox teCreatureBox
    ) {
        if (posToSwapTo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY
                && playerPartyProperties != null
        ) {
            FixedSizeList<CreatureNBT> playerParty = playerPartyProperties.getPlayerParty();
            playerParty.set(posToSwapTo.getIndex(), nbtToSwap);
            playerPartyProperties.setPlayerParty(playerParty);
        }
        else if (posToSwapTo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX
                && playerCreatureBoxProperties != null
        ) {
            CreatureBoxStorage playerBoxStorage = playerCreatureBoxProperties.getCreatureBoxStorage();
            playerBoxStorage.setBoxCreature(
                    posToSwapTo.getBoxIndex(),
                    posToSwapTo.getIndex(),
                    nbtToSwap
            );
            playerCreatureBoxProperties.setCreatureBoxStorage(playerBoxStorage);
        }
        else if (posToSwapTo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED
                && teCreatureBox != null
        ) {
            FixedSizeList<CreatureNBT> boxDeployedCreatures = teCreatureBox.getDeployedCreatures();
            boxDeployedCreatures.set(posToSwapTo.getIndex(), nbtToSwap);
            teCreatureBox.setDeployedCreatures(boxDeployedCreatures);
        }
    }

    private void performSwap(
            CreatureNBT nbtOne, CreatureNBT nbtTwo, SelectedCreatureInfo.SwapInfo swapInfo,
            PlayerPartyProperties playerPartyProperties,
            PlayerCreatureBoxProperties playerCreatureBoxProperties,
            RiftTileEntityCreatureBox teCreatureBox
    ) {
        //ensure that selectedPosType for this condition is the same
        if (swapInfo.getCreatureOne().selectedPosType != swapInfo.getCreatureTwo().selectedPosType) return;

        if (swapInfo.getCreatureOne().selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY
                && playerPartyProperties != null
        ) {
            FixedSizeList<CreatureNBT> playerParty = playerPartyProperties.getPlayerParty();
            playerParty.set(swapInfo.getCreatureOne().getIndex(), nbtTwo);
            playerParty.set(swapInfo.getCreatureTwo().getIndex(), nbtOne);
            playerPartyProperties.setPlayerParty(playerParty);
        }
        else if (swapInfo.getCreatureOne().selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX
                && playerCreatureBoxProperties != null
        ) {
            CreatureBoxStorage playerBoxStorage = playerCreatureBoxProperties.getCreatureBoxStorage();
            playerBoxStorage.setBoxCreature(
                    swapInfo.getCreatureOne().getBoxIndex(),
                    swapInfo.getCreatureOne().getIndex(),
                    nbtTwo
            );
            playerBoxStorage.setBoxCreature(
                    swapInfo.getCreatureTwo().getBoxIndex(),
                    swapInfo.getCreatureTwo().getIndex(),
                    nbtOne
            );
            playerCreatureBoxProperties.setCreatureBoxStorage(playerBoxStorage);
        }
        else if (swapInfo.getCreatureOne().selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED
                && teCreatureBox != null
        ) {
            FixedSizeList<CreatureNBT> boxDeployedCreatures = teCreatureBox.getDeployedCreatures();
            boxDeployedCreatures.set(swapInfo.getCreatureOne().getIndex(), nbtTwo);
            boxDeployedCreatures.set(swapInfo.getCreatureTwo().getIndex(), nbtOne);
            teCreatureBox.setDeployedCreatures(boxDeployedCreatures);
        }
    }

    private void preparePartyCreatureForSwap(
            MinecraftServer server,
            PlayerPartyProperties playerPartyProperties,
            CreatureNBT creatureNBT,
            BlockPos playerPos,
            int origPartyIndex,
            SelectedCreatureInfo.SelectedPosType posToSwapTo
    ) {
        //find creature deployed in the world first
        //no need to check deployment explicitly, as null means its already party_inactive or whatever
        RiftCreature corresponded = creatureNBT.findCorrespondingCreature(server.getEntityWorld());

        //dropping inventory, only for moving to box
        if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.BOX) {
            if (corresponded != null) {
                corresponded.creatureInventory.dropAllItems(server.getEntityWorld(), corresponded.getPosition());
            }
            else creatureNBT.dropInventory(server.getEntityWorld(), playerPos);
        }

        //override the stored nbt with the deployed creature's nbt
        //already takes into consideration if corresponded is null
        creatureNBT.overrideCreature(corresponded);

        //change creature nbt based on where to move to
        if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.BOX) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);

        }
        else if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE);
        }

        //remove creature from player party map
        if (playerPartyProperties != null) {
            playerPartyProperties.removeCreatureFromDeployMap(origPartyIndex);
        }

        //change deployment of already existing creature if it exists
        if (corresponded != null) {
            //setting to base inactive despawns it
            if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.BOX) {
                corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
            }
            else if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE);
            }
        }
    }

    //very minimalistic, only here for consistency... ye idk :pensive:
    private void prepareBoxCreatureForSwap(CreatureNBT creatureNBT, SelectedCreatureInfo.SelectedPosType posToSwapTo) {
        if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.PARTY) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
        }
        else if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE);
        }
    }

    private void prepareBoxDeployedCreatureForSwap(
            MinecraftServer server,
            CreatureNBT creatureNBT,
            SelectedCreatureInfo.SelectedPosType posToSwapTo
    ) {
        //find creature deployed in the world first
        RiftCreature corresponded = creatureNBT.findCorrespondingCreature(server.getEntityWorld());

        //override the stored nbt with the deployed creature's nbt
        //already takes into consideration if corresponded is null
        creatureNBT.overrideCreature(corresponded);

        //change creature nbt based on where to move to
        if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.PARTY) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);

        }
        else if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.BOX) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
        }

        //change deployment of already existing creature if it exists
        if (corresponded != null) {
            if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.PARTY) {
                corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
            }
            //setting it to base inactive despawns it
            else if (posToSwapTo == SelectedCreatureInfo.SelectedPosType.BOX) {
                corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
            }
        }
    }
}
