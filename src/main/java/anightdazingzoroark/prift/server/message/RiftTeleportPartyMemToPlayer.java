package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftTeleportPartyMemToPlayer implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftTeleportPartyMemToPlayer, IMessage> {
        @Override
        public IMessage onMessage(RiftTeleportPartyMemToPlayer message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftTeleportPartyMemToPlayer message, MessageContext ctx) {
            EntityPlayer messagePlayer = ctx.getServerHandler().player;
            EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
            IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
            UUID creatureUUID = playerTamedCreatures.getPartyNBT().get(message.partyMemPos).getUniqueID();

            RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, creatureUUID);

            if (partyMember != null) partyMember.setPosition(player.posX, player.posY, player.posZ);
        }
    }
}
