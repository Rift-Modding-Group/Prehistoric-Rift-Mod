package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftChangeNameFromBox extends AbstractMessage<RiftChangeNameFromBox> {
    private UUID creatureUUID;
    private String newName;

    public RiftChangeNameFromBox() {}

    public RiftChangeNameFromBox(UUID uuid, String newName) {
        this.creatureUUID = uuid;
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.creatureUUID = new UUID(mostSigBits, leastSigBits);

        int stringLength = buf.readInt();
        byte[] stringBytes = new byte[stringLength];
        buf.readBytes(stringBytes);
        this.newName = new String(stringBytes);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.creatureUUID.getMostSignificantBits());
        buf.writeLong(this.creatureUUID.getLeastSignificantBits());

        byte[] stringBytes = this.newName.getBytes();
        buf.writeInt(stringBytes.length);
        buf.writeBytes(stringBytes);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftChangeNameFromBox message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature)RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
        if (creature != null) {
            creature.setCustomNameTag(message.newName);
            creature.setAlwaysRenderNameTag(true);
            PlayerTamedCreaturesHelper.updatePartyMem(creature);
        }
        else {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("CustomName", message.newName);

            IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            playerTamedCreatures.modifyCreature(message.creatureUUID, compound);
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftChangeNameFromBox message, EntityPlayer player, MessageContext messageContext) {
        RiftCreature creature = (RiftCreature)RiftUtil.getEntityFromUUID(player.world, message.creatureUUID);
        if (creature != null) {
            creature.setCustomNameTag(message.newName);
            creature.setAlwaysRenderNameTag(true);
            PlayerTamedCreaturesHelper.updatePartyMem(creature);
        }
        else {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("CustomName", message.newName);

            IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            playerTamedCreatures.modifyCreature(message.creatureUUID, compound);
        }
    }
}
