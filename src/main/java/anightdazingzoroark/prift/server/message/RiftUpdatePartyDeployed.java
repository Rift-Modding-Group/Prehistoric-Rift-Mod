package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftUpdatePartyDeployed implements IMessage {
    private int playerId;
    private UUID uuid;
    private NBTTagCompound tagCompound;

    public RiftUpdatePartyDeployed() {}

    public RiftUpdatePartyDeployed(EntityPlayer player, RiftCreature creature) {
        this(player, creature, new NBTTagCompound());
    }

    public RiftUpdatePartyDeployed(EntityPlayer player, RiftCreature creature, NBTTagCompound tagCompound) {
        this.playerId = player.getEntityId();
        this.uuid = creature != null ? creature.getUniqueID() : RiftUtil.nilUUID;
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();

        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.uuid = new UUID(mostSigBits, leastSigBits);

        this.tagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);

        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());

        ByteBufUtils.writeTag(buf, this.tagCompound);
    }

    public static class Handler implements IMessageHandler<RiftUpdatePartyDeployed, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdatePartyDeployed message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdatePartyDeployed message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);
                if (message.tagCompound.isEmpty()) {
                    if (creature == null) return;
                    //System.out.println("update nbt server");

                    if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                        NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                        playerTamedCreatures.modifyCreature(message.uuid, newCompound);
                        //System.out.println("(party) server nbt: "+playerTamedCreatures.getPartyMemberTag(message.uuid));
                        RiftMessages.WRAPPER.sendToAll(new RiftUpdatePartyDeployed(player, creature, newCompound));
                    }
                    else if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                        NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                        playerTamedCreatures.modifyCreature(message.uuid, newCompound);
                        //System.out.println("(party inactive) server nbt: "+playerTamedCreatures.getPartyMemberTag(message.uuid));
                        RiftMessages.WRAPPER.sendToAll(new RiftUpdatePartyDeployed(player, creature, newCompound));
                    }
                }
                else {
                    playerTamedCreatures.modifyCreature(message.uuid, message.tagCompound);
                    if (creature != null
                            && PlayerTamedCreatures.DeploymentType.values()[message.tagCompound.getByte("DeploymentType")] == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE
                            && creature.isEntityAlive()) {
                        //for removing creature and hitboxes
                        RiftUtil.removeCreature(creature);
                    }
                }
            }
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
                RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);
                if (message.tagCompound.isEmpty()) {
                    if (creature == null) return;

                    if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                        NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                        playerTamedCreatures.modifyCreature(message.uuid, newCompound);
                        RiftMessages.WRAPPER.sendToServer(new RiftUpdatePartyDeployed(player, creature, newCompound));
                    }
                    else if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                        NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                        playerTamedCreatures.modifyCreature(message.uuid, newCompound);
                        RiftMessages.WRAPPER.sendToServer(new RiftUpdatePartyDeployed(player, creature, newCompound));
                    }
                }
                else {
                    //System.out.println("update nbt client");
                    playerTamedCreatures.modifyCreature(message.uuid, message.tagCompound);
                    //System.out.println("client nbt: "+playerTamedCreatures.getPartyMemberTag(message.uuid));
                    if (creature != null
                            && PlayerTamedCreatures.DeploymentType.values()[message.tagCompound.getByte("DeploymentType")] == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE
                            && creature.isEntityAlive()) {
                        //for removing creature and hitboxes
                        RiftUtil.removeCreature(creature);
                    }
                }
            }
        }
    }
}
