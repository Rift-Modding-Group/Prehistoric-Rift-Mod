package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.PlayerTamedCreatures.DeploymentType;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftManagePartyMem extends AbstractMessage<RiftManagePartyMem> {
    private UUID creatureUUID;
    public boolean deploy;
    private NBTTagCompound creatureNBT;

    public RiftManagePartyMem() {}

    public RiftManagePartyMem(RiftCreature creature, boolean deploy) {
        this.creatureUUID = creature.getUniqueID();
        this.deploy = deploy;
        if (deploy) {
            NBTTagCompound creatureInCompound = new NBTTagCompound();
            creature.writeEntityToNBT(creatureInCompound);
            creatureInCompound.setUniqueId("UniqueID", creature.getUniqueID());
            creatureInCompound.setString("CustomName", creature.getCustomNameTag());
            this.creatureNBT = creatureInCompound;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.creatureUUID = new UUID(mostSigBits, leastSigBits);
        this.deploy = buf.readBoolean();
        this.creatureNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.creatureUUID.getMostSignificantBits());
        buf.writeLong(this.creatureUUID.getLeastSignificantBits());
        buf.writeBoolean(this.deploy);
        ByteBufUtils.writeTag(buf, this.creatureNBT);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftManagePartyMem message, EntityPlayer player, MessageContext messageContext) {
        if (message.deploy) {
            RiftCreatureType creatureType = RiftCreatureType.values()[message.creatureNBT.getByte("CreatureType")];
            UUID uniqueID = message.creatureNBT.getUniqueId("UniqueID");
            String customName = message.creatureNBT.getString("CustomName");

            if (creatureType != null) {
                RiftCreature creature = creatureType.invokeClass(player.world);
                EntityPlayer owner = (EntityPlayer)RiftUtil.getEntityFromUUID(player.world, UUID.fromString(message.creatureNBT.getString("OwnerUUID")));

                creature.readEntityFromNBT(message.creatureNBT);
                creature.setUniqueId(uniqueID);
                creature.setCustomNameTag(customName);
                creature.setDeploymentType(DeploymentType.PARTY);
                creature.setPosition(owner.posX, owner.posY, owner.posZ);

                player.world.spawnEntity(creature);
                creature.updatePlayerTameList();
            }
        }
        else {
            RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
            partyMember.setDeploymentType(DeploymentType.PARTY_INACTIVE);
            partyMember.updatePlayerTameList();

            //for removing hitboxes
            partyMember.setDead();
            partyMember.setHealth(0);
            partyMember.updateParts();
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftManagePartyMem message, EntityPlayer player, MessageContext messageContext) {
        if (message.deploy) {
            RiftCreatureType creatureType = RiftCreatureType.values()[message.creatureNBT.getByte("CreatureType")];
            UUID uniqueID = message.creatureNBT.getUniqueId("UniqueID");
            String customName = message.creatureNBT.getString("CustomName");

            if (creatureType != null) {
                RiftCreature creature = creatureType.invokeClass(player.world);
                EntityPlayer owner = (EntityPlayer)RiftUtil.getEntityFromUUID(player.world, UUID.fromString(message.creatureNBT.getString("OwnerUUID")));

                creature.readEntityFromNBT(message.creatureNBT);
                creature.setUniqueId(uniqueID);
                creature.setCustomNameTag(customName);
                creature.setDeploymentType(DeploymentType.PARTY);
                creature.setPosition(owner.posX, owner.posY, owner.posZ);

                player.world.spawnEntity(creature);
                creature.updatePlayerTameList();
            }
        }
        else {
            RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
            partyMember.setDeploymentType(DeploymentType.PARTY_INACTIVE);
            partyMember.updatePlayerTameList();

            //for removing hitboxes
            partyMember.setDead();
            partyMember.setHealth(0);
            partyMember.updateParts();
        }
    }
}
