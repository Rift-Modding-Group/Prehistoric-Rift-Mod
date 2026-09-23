package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.player.PlayerPartyProperties;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftPartyActionMessage extends RiftLibMessage<RiftPartyActionMessage> {
    private byte action;

    public RiftPartyActionMessage() {}

    public RiftPartyActionMessage(Action action) {
        this.action = (byte) action.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.action = buffer.readByte();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(this.action);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftPartyActionMessage message, EntityPlayer player, MessageContext messageContext) {
        if (message.action < 0 || message.action >= Action.values().length) return;

        PlayerPartyProperties playerParty = PlayerPartyProperties.get(player);
        if (playerParty == null) return;

        switch (Action.values()[message.action]) {
            case SELECT_PREVIOUS -> playerParty.selectPreviousPartyMember();
            case SELECT_NEXT -> playerParty.selectNextPartyMember();
            case DEPLOY_OR_DISMISS -> playerParty.toggleSelectedPartyMember();
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftPartyActionMessage message, EntityPlayer player, MessageContext messageContext) {}

    public enum Action {
        SELECT_PREVIOUS,
        SELECT_NEXT,
        DEPLOY_OR_DISMISS
    }
}
