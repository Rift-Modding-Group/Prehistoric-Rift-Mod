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
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class RiftUpdateIndividualPartyCreatureHealthClient extends RiftLibMessage<RiftUpdateIndividualPartyCreatureHealthClient> {
    private int playerId;
    private int creatureId;
    private int pos;
    private float newHealth;

    public RiftUpdateIndividualPartyCreatureHealthClient() {}

    public RiftUpdateIndividualPartyCreatureHealthClient(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.creatureId = creature.getEntityId();
        this.pos = -1;
        this.newHealth = -1f;
    }

    public RiftUpdateIndividualPartyCreatureHealthClient(EntityPlayer player, int pos, float newHealth) {
        this.playerId = player.getEntityId();
        this.creatureId = -1;
        this.pos = pos;
        this.newHealth = newHealth;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureId = buf.readInt();
        this.pos = buf.readInt();
        this.newHealth = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.creatureId);
        buf.writeInt(this.pos);
        buf.writeFloat(this.newHealth);
    }

    @Override
    public void executeOnServer(MinecraftServer minecraftServer, RiftUpdateIndividualPartyCreatureHealthClient message, EntityPlayer messagePlayer, MessageContext messageContext) {
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
            RiftMessages.WRAPPER.sendTo(new RiftUpdateIndividualPartyCreatureHealthClient(player, pos, creature.getHealth()), (EntityPlayerMP) player);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft minecraft, RiftUpdateIndividualPartyCreatureHealthClient message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
        if (player == null) return;
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
        if (playerTamedCreatures != null) {
            CreatureNBT creatureNBT = playerTamedCreatures.getPartyNBT().get(message.pos);
            creatureNBT.setCreatureHealth(message.newHealth);
            playerTamedCreatures.setPartyMemNBT(message.pos, creatureNBT);
        }
    }
}
