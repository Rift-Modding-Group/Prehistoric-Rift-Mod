package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftTeleportPartyMemToPlayer extends RiftLibMessage<RiftTeleportPartyMemToPlayer> {
    private int playerId;
    private int partyMemPos;

    public RiftTeleportPartyMemToPlayer() {}

    public RiftTeleportPartyMemToPlayer(EntityPlayer player, int partyMemPos) {
        this.playerId = player.getEntityId();
        this.partyMemPos = partyMemPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.partyMemPos = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.partyMemPos);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftTeleportPartyMemToPlayer message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;;

        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures == null) return;

        UUID creatureUUID = playerTamedCreatures.getPartyNBT().get(message.partyMemPos).getUniqueID();

        RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, creatureUUID);

        if (partyMember != null) partyMember.setPosition(player.posX, player.posY, player.posZ);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftTeleportPartyMemToPlayer message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
