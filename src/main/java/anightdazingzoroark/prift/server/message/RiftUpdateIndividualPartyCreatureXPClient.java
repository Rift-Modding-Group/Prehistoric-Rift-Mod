package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class RiftUpdateIndividualPartyCreatureXPClient extends RiftLibMessage<RiftUpdateIndividualPartyCreatureXPClient> {
    private int playerId;
    private int creatureId;
    private int pos;
    private int newXP;

    public RiftUpdateIndividualPartyCreatureXPClient() {}

    public RiftUpdateIndividualPartyCreatureXPClient(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.creatureId = creature.getEntityId();
        this.pos = -1;
        this.newXP = -1;
    }

    public RiftUpdateIndividualPartyCreatureXPClient(EntityPlayer player, int pos, int newXP) {
        this.playerId = player.getEntityId();
        this.creatureId = -1;
        this.pos = pos;
        this.newXP = newXP;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureId = buf.readInt();
        this.pos = buf.readInt();
        this.newXP = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.creatureId);
        buf.writeInt(this.pos);
        buf.writeInt(this.newXP);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftUpdateIndividualPartyCreatureXPClient message, EntityPlayer messagePlayer, MessageContext messageContext) {
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

            //if creature is in the party, get health, pos in party, and then update
            RiftMessages.WRAPPER.sendTo(new RiftUpdateIndividualPartyCreatureXPClient(player, pos, creature.getXP()), (EntityPlayerMP) player);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdateIndividualPartyCreatureXPClient message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null) {
            CreatureNBT creatureNBT = playerTamedCreatures.getPartyNBT().get(message.pos);
            creatureNBT.setCreatureXP(message.newXP);
            playerTamedCreatures.setPartyMemNBT(message.pos, creatureNBT);
        }
    }
}
