package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

        PlayerPartyProperties playerParty = PlayerPartyHelper.getPlayerParty(player);
        if (playerParty == null) return;

        playerParty.teleportPartyMember(message.partyMemPos, player);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftTeleportPartyMemToPlayer message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
