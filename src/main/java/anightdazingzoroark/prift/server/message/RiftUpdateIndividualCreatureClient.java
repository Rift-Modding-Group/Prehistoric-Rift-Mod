package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftUpdateIndividualCreatureClient implements IMessage {
    private int playerId;
    private int creatureId;
    private int pos;
    private CreatureNBT tagCompound;

    public RiftUpdateIndividualCreatureClient() {}

    public RiftUpdateIndividualCreatureClient(EntityPlayer player, RiftCreature creature) {
        this.playerId = player.getEntityId();
        this.creatureId = creature.getEntityId();
        this.pos = -1;
        this.tagCompound = new CreatureNBT();
    }

    public RiftUpdateIndividualCreatureClient(EntityPlayer player, int pos, CreatureNBT tagCompound) {
        this.playerId = player.getEntityId();
        this.creatureId = -1;
        this.pos = pos;
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.creatureId = buf.readInt();
        this.pos = buf.readInt();
        this.tagCompound = new CreatureNBT(ByteBufUtils.readTag(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.creatureId);
        buf.writeInt(this.pos);
        ByteBufUtils.writeTag(buf, this.tagCompound.getCreatureNBT());
    }

    public static class Handler implements IMessageHandler<RiftUpdateIndividualCreatureClient, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdateIndividualCreatureClient message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdateIndividualCreatureClient message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
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
                    RiftMessages.WRAPPER.sendTo(new RiftUpdateIndividualCreatureClient(player, pos, creatureNBT), (EntityPlayerMP) player);
                }
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                if (playerTamedCreatures != null) {
                    playerTamedCreatures.setPartyMemNBT(message.pos, message.tagCompound);
                }
            }
        }
    }
}
