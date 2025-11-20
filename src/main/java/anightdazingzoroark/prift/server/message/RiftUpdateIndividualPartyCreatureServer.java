package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftUpdateIndividualPartyCreatureServer extends RiftLibMessage<RiftUpdateIndividualPartyCreatureServer> {
    private int playerId;
    private int creatureId;

    public RiftUpdateIndividualPartyCreatureServer() {}

    public RiftUpdateIndividualPartyCreatureServer(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.creatureId = creature.getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.creatureId);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftUpdateIndividualPartyCreatureServer message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        RiftCreature creature = (RiftCreature) messagePlayer.world.getEntityByID(message.creatureId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null && creature != null) {
            //first of all, check if creature is in the party
            int pos = -1;
            UUID creatureUUID = creature.getUniqueID();
            for (int x = 0; x < playerTamedCreatures.getPartyNBT().size(); x++) {
                CreatureNBT partyMemNBT = playerTamedCreatures.getPartyNBT().get(x);
                if (!partyMemNBT.nbtIsEmpty()
                        && partyMemNBT.getUniqueID() != null
                        && partyMemNBT.getUniqueID().equals(creatureUUID)) {
                    pos = x;
                    break;
                }
            }
            if (pos < 0) return;

            //if creature is in the party, go on and update
            CreatureNBT creatureNBT = new CreatureNBT(creature);
            playerTamedCreatures.setPartyMemNBT(pos, creatureNBT);
        }
    }

    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdateIndividualPartyCreatureServer message, EntityPlayer messagePlayer, MessageContext messageContext) {}
}
