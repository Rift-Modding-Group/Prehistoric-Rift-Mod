package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftRemoveCreatureFromBox extends AbstractMessage<RiftRemoveCreatureFromBox> {
    private UUID creatureUUID;

    public RiftRemoveCreatureFromBox() {}

    public RiftRemoveCreatureFromBox(UUID uuid) {
        this.creatureUUID = uuid;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.creatureUUID = new UUID(mostSigBits, leastSigBits);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.creatureUUID.getMostSignificantBits());
        buf.writeLong(this.creatureUUID.getLeastSignificantBits());
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftRemoveCreatureFromBox message, EntityPlayer player, MessageContext messageContext) {
        //remove from world
        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
        if (creature != null) {
            creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.NONE);

            //for removing hitboxes
            creature.setDead();
            creature.setHealth(0);
            creature.updateParts();
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftRemoveCreatureFromBox message, EntityPlayer player, MessageContext messageContext) {
        //remove from world
        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
        if (creature != null) {
            player.sendStatusMessage(new TextComponentTranslation("reminder.creature_released", creature.getName(false), creature.getName(false)), false);
            creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.NONE);

            //for removing hitboxes
            creature.setDead();
            creature.setHealth(0);
            creature.updateParts();
        }

        //remove from party and box
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        playerTamedCreatures.removeCreature(message.creatureUUID);
        playerTamedCreatures.removeCreatureFromBoxDeployed(player.world, ClientProxy.creatureBoxBlockPos, message.creatureUUID);
    }
}
