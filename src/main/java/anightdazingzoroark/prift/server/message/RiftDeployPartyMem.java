package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

        PlayerPartyProperties playerParty = PlayerPartyHelper.getPlayerParty(player);
        if (playerParty == null) return;

        playerParty.deployPartyMember(message.position, message.deploy);
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftDeployPartyMem message, EntityPlayer entityPlayer, MessageContext messageContext) {}
}
