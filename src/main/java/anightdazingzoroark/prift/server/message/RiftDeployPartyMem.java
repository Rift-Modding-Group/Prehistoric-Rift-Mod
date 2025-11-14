package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftDeployPartyMem implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftDeployPartyMem, IMessage> {
        @Override
        public IMessage onMessage(RiftDeployPartyMem message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftDeployPartyMem message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;
                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                CreatureNBT partyMemNBT = playerTamedCreatures.getPartyNBT().get(message.position);
                UUID creatureUUID = partyMemNBT.getUniqueID();

                //if true, deploy the creature
                if (message.deploy) {
                    //due to the way creature dismissal works, there's a delay in when a creature gets dismissed
                    //so if the deploy button is activated during that small delay, the process must stop and
                    //the creature stays
                    //hence, there should be a check on whether or not the creature exists in the world
                    //when dealing with this
                    if (RiftUtil.checkForEntityWithUUID(messagePlayer.world, creatureUUID)) {
                        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, creatureUUID);
                        if (creature != null) creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                    }
                    else {
                        partyMemNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                        playerTamedCreatures.setPartyMemNBT(message.position, partyMemNBT);
                        PlayerTamedCreaturesHelper.forceSyncPartyNBT(player);

                        //create creature
                        RiftCreature creature = partyMemNBT.getCreatureAsNBT(messagePlayer.world);

                        if (creature != null) {
                            creature.setPosition(player.posX, player.posY, player.posZ);
                            player.world.spawnEntity(creature);
                        }
                    }
                }
                //if false, dismiss creature back to party
                else {
                    //find creature in the world first
                    RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, creatureUUID);

                    //if not null, get its nbt, change its nbt to inactive, then remove the creature
                    if (partyMember != null) {
                        CreatureNBT partyMemCurrentNBT = new CreatureNBT(partyMember);
                        partyMemCurrentNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
                        playerTamedCreatures.setPartyMemNBT(message.position, partyMemCurrentNBT);
                        PlayerTamedCreaturesHelper.forceSyncPartyNBT(player);

                        partyMember.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE); //creature and its hitboxes disappear once this is done
                    }
                    //otherwise just change the nbt
                    else {
                        partyMemNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
                        playerTamedCreatures.setPartyMemNBT(message.position, partyMemNBT);
                        PlayerTamedCreaturesHelper.forceSyncPartyNBT(player);
                    }
                }
            }
        }
    }
}
