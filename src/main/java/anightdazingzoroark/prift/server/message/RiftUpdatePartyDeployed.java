package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
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

public class RiftUpdatePartyDeployed extends AbstractMessage<RiftUpdatePartyDeployed> {
    private int playerId;
    private UUID uuid;
    private NBTTagCompound tagCompound;

    public RiftUpdatePartyDeployed() {}

    public RiftUpdatePartyDeployed(EntityPlayer player, RiftCreature creature) {
        this(player, creature, new NBTTagCompound());
    }

    public RiftUpdatePartyDeployed(EntityPlayer player, RiftCreature creature, NBTTagCompound tagCompound) {
        this.playerId = player.getEntityId();
        this.uuid = creature != null ? creature.getUniqueID() : RiftUtil.nilUUID;
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.uuid = new UUID(mostSigBits, leastSigBits);

        this.tagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);

        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());

        ByteBufUtils.writeTag(buf, this.tagCompound);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftUpdatePartyDeployed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

        if (message.tagCompound.isEmpty()) {
            if (creature == null) return;

            if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                playerTamedCreatures.modifyCreature(message.uuid, newCompound);
                RiftMessages.WRAPPER.sendToServer(new RiftUpdatePartyDeployed(player, creature, newCompound));
            }
            else if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                playerTamedCreatures.modifyCreature(message.uuid, newCompound);
                RiftMessages.WRAPPER.sendToAll(new RiftUpdatePartyDeployed(player, creature, newCompound));

                //for removing creature and hitboxes
                RiftUtil.removeCreature(creature);
            }
        }
        else {
            playerTamedCreatures.modifyCreature(message.uuid, message.tagCompound);
            if (creature != null
                    && PlayerTamedCreatures.DeploymentType.values()[message.tagCompound.getByte("DeploymentType")] == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE
                    && creature.isEntityAlive()) {
                //for removing creature and hitboxes
                RiftUtil.removeCreature(creature);
            }
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftUpdatePartyDeployed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

        if (message.tagCompound.isEmpty()) {
            if (creature == null) return;

            if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                playerTamedCreatures.modifyCreature(message.uuid, newCompound);
                RiftMessages.WRAPPER.sendToAll(new RiftUpdatePartyDeployed(player, creature, newCompound));
            }
            else if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                playerTamedCreatures.modifyCreature(message.uuid, newCompound);
                RiftMessages.WRAPPER.sendToAll(new RiftUpdatePartyDeployed(player, creature, newCompound));

                //for removing creature and hitboxes
                RiftUtil.removeCreature(creature);
            }
        }
        else {
            playerTamedCreatures.modifyCreature(message.uuid, message.tagCompound);
            if (creature != null
                    && PlayerTamedCreatures.DeploymentType.values()[message.tagCompound.getByte("DeploymentType")] == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE
                    && creature.isEntityAlive()) {
                //for removing creature and hitboxes
                RiftUtil.removeCreature(creature);
            }
        }
    }
}
