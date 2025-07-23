package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftUpdatePartyWhenPartyScreenOpen implements IMessage {
    private int playerId;
    private boolean isSingleplayer;

    public RiftUpdatePartyWhenPartyScreenOpen() {}

    public RiftUpdatePartyWhenPartyScreenOpen(EntityPlayer player, boolean isSingleplayer) {
        this.playerId = player.getEntityId();
        this.isSingleplayer = isSingleplayer;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.isSingleplayer = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeBoolean(this.isSingleplayer);
    }

    public static class Handler implements IMessageHandler<RiftUpdatePartyWhenPartyScreenOpen, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdatePartyWhenPartyScreenOpen message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdatePartyWhenPartyScreenOpen message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    for (int x = 0; x < playerTamedCreatures.getPartyNBT().size(); x++) {
                        NBTTagCompound partyMemNBT = playerTamedCreatures.getPartyNBT().get(x);

                        if (partyMemNBT.isEmpty()) continue; //ignore any empty spaces

                        //get deployment type of creature
                        PlayerTamedCreatures.DeploymentType deploymentType = PlayerTamedCreatures.DeploymentType.values()[partyMemNBT.getByte("DeploymentType")];

                        //update the creature if it is deployed and if it is singleplayer
                        //singleplayer check is there because the screen gets paused by that time
                        //as if it were multiplayer they will be updated as usual
                        if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY && message.isSingleplayer) {
                            UUID creatureUUID = partyMemNBT.getUniqueId("UniqueID");
                            RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, creatureUUID);
                            if (creature != null) {

                            }
                        }
                        //otherwise just edit nbt
                        else if (deploymentType != PlayerTamedCreatures.DeploymentType.PARTY) {
                            RiftCreatureType creatureType = RiftCreatureType.values()[partyMemNBT.getByte("CreatureType")];
                        }
                    }
                }
            }
        }
    }
}
