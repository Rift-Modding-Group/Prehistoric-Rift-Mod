package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftDeployPartyMem extends RiftLibMessage<RiftDeployPartyMem> {
    private int playerId;
    private int position;
    private boolean deploy;

    public RiftDeployPartyMem() {}

    public RiftDeployPartyMem(EntityPlayer player, int position, boolean deploy) {
        this.playerId = player.getEntityId();
        this.position = position;
        this.deploy = deploy;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.position = buf.readInt();
        this.deploy = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.position);
        buf.writeBoolean(this.deploy);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftDeployPartyMem message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;

        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures == null) return;

        CreatureNBT partyMemNBT = playerTamedCreatures.getPartyNBT().get(message.position);
        UUID creatureUUID = partyMemNBT.getUniqueID();

        if (message.deploy) {
            //due to the way creature dismissal works, there's a delay in when a creature gets dismissed
            //so if the deploy button is activated during that small delay, the process must stop and
            //the creature stays
            //hence, there should be a check on whether or not the creature exists in the world
            //when dealing with this
            if (RiftUtil.checkForEntityWithUUID(messagePlayer.world, creatureUUID)) {
                RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, creatureUUID);
                if (creature != null) creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
            }
            else {
                //update to local partymemnbt
                partyMemNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);

                //update on player
                NBTTagCompound newNBTParam = CreatureNBTKeyword.DEPLOYMENT_TYPE.setValue((byte) PlayerTamedCreatures.DeploymentType.PARTY.ordinal());
                NBTTagCompound mergedNBT = partyMemNBT.getCreatureNBT();
                mergedNBT.merge(newNBTParam);
                CreatureNBT mergedCreatureNBT = new CreatureNBT(mergedNBT);
                playerTamedCreatures.setPartyMemNBT(message.position, mergedCreatureNBT);

                //create creature
                RiftCreature creature = partyMemNBT.getCreatureAsNBT(messagePlayer.world);

                if (creature != null) {
                    creature.setPosition(player.posX, player.posY, player.posZ);
                    player.world.spawnEntity(creature);
                }
            }
        }
        //if false, dismiss creature back to party
        else {
            //find creature in the world first
            RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, creatureUUID);

            //if not null, get its nbt, change its nbt to inactive, then remove the creature
            if (partyMember != null) {
                //update on player
                NBTTagCompound newNBTParam = CreatureNBTKeyword.DEPLOYMENT_TYPE.setValue((byte) PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE.ordinal());
                NBTTagCompound mergedNBT = partyMemNBT.getCreatureNBT();
                mergedNBT.merge(newNBTParam);
                CreatureNBT mergedCreatureNBT = new CreatureNBT(mergedNBT);
                playerTamedCreatures.setPartyMemNBT(message.position, mergedCreatureNBT);

                //creature and its hitboxes disappear once this is done
                partyMember.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
            }
            //otherwise just change the nbt
            else {
                NBTTagCompound newNBTParam = CreatureNBTKeyword.DEPLOYMENT_TYPE.setValue((byte) PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE.ordinal());
                NBTTagCompound mergedNBT = partyMemNBT.getCreatureNBT();
                mergedNBT.merge(newNBTParam);
                CreatureNBT mergedCreatureNBT = new CreatureNBT(mergedNBT);
                playerTamedCreatures.setPartyMemNBT(message.position, mergedCreatureNBT);
            }
        }

        //send to client
        RiftMessages.WRAPPER.sendTo(new RiftDeployPartyMem(player, message.position, message.deploy), (EntityPlayerMP) player);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftDeployPartyMem message, EntityPlayer entityPlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) minecraft.world.getEntityByID(message.playerId);
        if (player == null) return;

        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures == null) return;

        CreatureNBT partyMemNBT = playerTamedCreatures.getPartyNBT().get(message.position);
        UUID creatureUUID = partyMemNBT.getUniqueID();

        if (message.deploy) {
            //update to local partymemnbt
            partyMemNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);

            //update on player
            NBTTagCompound newNBTParam = CreatureNBTKeyword.DEPLOYMENT_TYPE.setValue((byte) PlayerTamedCreatures.DeploymentType.PARTY.ordinal());
            NBTTagCompound mergedNBT = partyMemNBT.getCreatureNBT();
            mergedNBT.merge(newNBTParam);
            CreatureNBT mergedCreatureNBT = new CreatureNBT(mergedNBT);
            playerTamedCreatures.setPartyMemNBT(message.position, mergedCreatureNBT);
        }
        //if false, dismiss creature back to party
        else {
            NBTTagCompound newNBTParam = CreatureNBTKeyword.DEPLOYMENT_TYPE.setValue((byte) PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE.ordinal());
            NBTTagCompound mergedNBT = partyMemNBT.getCreatureNBT();
            mergedNBT.merge(newNBTParam);
            CreatureNBT mergedCreatureNBT = new CreatureNBT(mergedNBT);
            playerTamedCreatures.setPartyMemNBT(message.position, mergedCreatureNBT);
        }
    }
}
