package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.*;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
    private boolean interMessage;

    public RiftDeployPartyMem() {}

    public RiftDeployPartyMem(EntityPlayer player, int position, boolean deploy) {
        this(player, position, deploy, false);
    }

    public RiftDeployPartyMem(EntityPlayer player, int position, boolean deploy, boolean interMessage) {
        this.playerId = player.getEntityId();
        this.position = position;
        this.deploy = deploy;
        this.interMessage = interMessage;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.position = buf.readInt();
        this.deploy = buf.readBoolean();
        this.interMessage = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.position);
        buf.writeBoolean(this.deploy);
        buf.writeBoolean(this.interMessage);
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
                UUID uuid = playerTamedCreatures.getPartyCreatures(messagePlayer.world).get(message.position).getUniqueID();

                if (message.deploy) {
                    //edit nbt
                    NBTTagCompound creatureCompound = playerTamedCreatures.getPartyNBT().get(message.position);

                    //due to the way creature dismissal works, there's a delay in when a creature gets dismissed
                    //so if the deploy button is activated during that small delay, the process must stop and
                    //the creature stays
                    //hence, there should be a check on whether or not the creature exists in the world
                    //when dealing with this
                    UUID creatureUUID = creatureCompound.getUniqueId("UniqueID");
                    if (RiftUtil.checkForEntityWithUUID(messagePlayer.world, creatureUUID)) {
                        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, creatureUUID);
                        if (creature != null) creature.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                    }
                    else {
                        creatureCompound.setByte("DeploymentType", (byte) PlayerTamedCreatures.DeploymentType.PARTY.ordinal());
                        playerTamedCreatures.setPartyMemNBT(message.position, creatureCompound);

                        //create creature
                        RiftCreature creature = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(messagePlayer.world, creatureCompound);

                        if (creature != null) {
                            creature.setPosition(player.posX, player.posY, player.posZ);
                            player.world.spawnEntity(creature);
                        }
                    }
                }
                else {
                    //edit nbt
                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setByte("DeploymentType", (byte) PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE.ordinal());
                    playerTamedCreatures.modifyCreature(uuid, compound);

                    //update creature
                    RiftCreature partyMember = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, uuid);
                    if (partyMember != null) {
                        partyMember.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
                        //creature removal is already handled in RiftCreature
                    }
                }

                //repeat on the client side
                //if (!message.interMessage) RiftMessages.WRAPPER.sendToAll(new RiftDeployPartyMem(player, message.position, message.deploy, true));
            }
        }
    }
}
