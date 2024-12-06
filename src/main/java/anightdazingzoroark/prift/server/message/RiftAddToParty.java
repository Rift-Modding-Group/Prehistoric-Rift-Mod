package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftAddToParty extends AbstractMessage<RiftAddToParty> {
    private int playerId;
    private UUID uuid;

    public RiftAddToParty() {}

    public RiftAddToParty(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.uuid = creature.getUniqueID();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.uuid = new UUID(mostSigBits, leastSigBits);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);

        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftAddToParty message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

        boolean creatureInList = playerTamedCreatures.getPartyNBT().stream()
                .noneMatch(nbt -> nbt.hasKey("UniqueIDMost") && nbt.hasKey("UniqueIDLeast") && nbt.getUniqueId("UniqueID").equals(message.uuid));
        if (creatureInList && creature != null) playerTamedCreatures.addToPartyCreatures(creature);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftAddToParty message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

        boolean creatureInList = playerTamedCreatures.getPartyNBT().stream()
                .noneMatch(nbt -> nbt.hasKey("UniqueIDMost") && nbt.hasKey("UniqueIDLeast") && nbt.getUniqueId("UniqueID").equals(message.uuid));
        if (creatureInList && creature != null) playerTamedCreatures.addToPartyCreatures(creature);
    }
}
