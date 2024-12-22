package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftDeployPartyMem extends AbstractMessage<RiftDeployPartyMem> {
    private int playerId;
    private int position;
    public boolean deploy;

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
    public void onClientReceived(Minecraft minecraft, RiftDeployPartyMem message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        UUID uuid = playerTamedCreatures.getPartyCreatures(messagePlayer.world).get(message.position).getUniqueID();

        if (message.deploy) {
            //edit nbt
            NBTTagCompound compound = new NBTTagCompound();
            compound.setByte("DeploymentType", (byte) PlayerTamedCreatures.DeploymentType.PARTY.ordinal());
            playerTamedCreatures.modifyCreature(uuid, compound);
        }
        else {
            //edit nbt
            NBTTagCompound compound = new NBTTagCompound();
            compound.setByte("DeploymentType", (byte) PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE.ordinal());
            //compound.setInteger("PartyReenergizeTime", 1200);
            playerTamedCreatures.modifyCreature(uuid, compound);

            //update remove creature
            RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, uuid);
            partyMember.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
            PlayerTamedCreaturesHelper.updatePartyMem(partyMember);
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftDeployPartyMem message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        UUID uuid = playerTamedCreatures.getPartyCreatures(messagePlayer.world).get(message.position).getUniqueID();

        if (message.deploy) {
            //edit nbt
            NBTTagCompound compound = new NBTTagCompound();
            compound.setByte("DeploymentType", (byte) PlayerTamedCreatures.DeploymentType.PARTY.ordinal());
            playerTamedCreatures.modifyCreature(uuid, compound);

            //create creature
            NBTTagCompound creatureCompound = playerTamedCreatures.getPartyNBT().get(message.position);
            RiftCreature creature = PlayerTamedCreaturesHelper.createCreatureFromNBT(messagePlayer.world, creatureCompound);

            if (creature != null) {
                creature.setPosition(player.posX, player.posY, player.posZ);
                player.world.spawnEntity(creature);
            }
        }
        else {
            //edit nbt
            NBTTagCompound compound = new NBTTagCompound();
            compound.setByte("DeploymentType", (byte) PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE.ordinal());
            //compound.setInteger("PartyReenergizeTime", 1200);
            playerTamedCreatures.modifyCreature(uuid, compound);

            //update remove creature
            RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, uuid);
            partyMember.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
            PlayerTamedCreaturesHelper.updatePartyMem(partyMember);
        }
    }
}