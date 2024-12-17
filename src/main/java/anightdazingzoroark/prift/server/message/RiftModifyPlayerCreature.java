package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;
import java.util.stream.Collectors;

public class RiftModifyPlayerCreature extends AbstractMessage<RiftModifyPlayerCreature> {
    private int playerID;
    private UUID creatureUUID;
    private NBTTagCompound tagCompound;

    public RiftModifyPlayerCreature() {}

    public RiftModifyPlayerCreature(EntityPlayer player, UUID creatureUUID, NBTTagCompound tagCompound) {
        this.playerID = player.getEntityId();
        this.creatureUUID = creatureUUID;
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerID = buf.readInt();

        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.creatureUUID = new UUID(mostSigBits, leastSigBits);

        this.tagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerID);

        buf.writeLong(this.creatureUUID.getMostSignificantBits());
        buf.writeLong(this.creatureUUID.getLeastSignificantBits());

        ByteBufUtils.writeTag(buf, this.tagCompound);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftModifyPlayerCreature message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerID);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        playerTamedCreatures.modifyCreature(message.creatureUUID, message.tagCompound);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftModifyPlayerCreature message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerID);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        playerTamedCreatures.modifyCreature(message.creatureUUID, message.tagCompound);
    }
}
