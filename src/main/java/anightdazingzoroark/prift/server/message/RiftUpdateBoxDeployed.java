package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class RiftUpdateBoxDeployed extends AbstractMessage<RiftUpdateBoxDeployed> {
    private int posX;
    private int posY;
    private int posZ;
    private UUID uuid;
    private NBTTagCompound tagCompound;

    public RiftUpdateBoxDeployed() {}

    public RiftUpdateBoxDeployed(BlockPos pos, RiftCreature creature) {
        this(pos, creature, new NBTTagCompound());
    }

    public RiftUpdateBoxDeployed(BlockPos pos, RiftCreature creature, NBTTagCompound tagCompound) {
        this.posX = pos.getX();
        this.posY = pos.getY();
        this.posZ = pos.getZ();
        this.uuid = creature != null ? creature.getUniqueID() : RiftUtil.nilUUID;
        this.tagCompound = tagCompound;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();

        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        this.uuid = new UUID(mostSigBits, leastSigBits);

        this.tagCompound = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);

        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());

        ByteBufUtils.writeTag(buf, this.tagCompound);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftUpdateBoxDeployed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        BlockPos creatureBoxPos = new BlockPos(message.posX, message.posY, message.posZ);
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) messagePlayer.world.getTileEntity(creatureBoxPos);
        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

        if (message.tagCompound.isEmpty()) {
            if (creature == null || creatureBox == null) return;

            if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) {
                System.out.println(PlayerTamedCreaturesHelper.createNBTFromCreature(creature));
                NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                creatureBox.replaceInCreatureList(message.uuid, newCompound);
                RiftMessages.WRAPPER.sendToServer(new RiftUpdateBoxDeployed(creatureBoxPos, creature, newCompound));
            }
            else if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE_INACTIVE) {
                NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                creatureBox.replaceInCreatureList(message.uuid, newCompound);
                RiftMessages.WRAPPER.sendToServer(new RiftUpdateBoxDeployed(creatureBoxPos, creature, newCompound));

                //for removing creature and hitboxes
                RiftUtil.removeCreature(creature);
            }
        }
        else {
            if (creatureBox == null) return;

            creatureBox.replaceInCreatureList(message.uuid, message.tagCompound);
            if (creature != null
                    && PlayerTamedCreatures.DeploymentType.values()[message.tagCompound.getByte("DeploymentType")] == PlayerTamedCreatures.DeploymentType.BASE_INACTIVE
                    && creature.isEntityAlive()) {
                //for removing creature and hitboxes
                RiftUtil.removeCreature(creature);
                System.out.println("remove pt 2");
            }
        }
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftUpdateBoxDeployed message, EntityPlayer messagePlayer, MessageContext messageContext) {
        BlockPos creatureBoxPos = new BlockPos(message.posX, message.posY, message.posZ);
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) messagePlayer.world.getTileEntity(creatureBoxPos);
        RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

        if (message.tagCompound.isEmpty()) {
            if (creature == null || creatureBox == null) return;

            if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) {
                NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                creatureBox.replaceInCreatureList(message.uuid, newCompound);
                RiftMessages.WRAPPER.sendToAll(new RiftUpdateBoxDeployed(creatureBoxPos, creature, newCompound));
            }
            else if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE_INACTIVE) {
                NBTTagCompound newCompound = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);
                creatureBox.replaceInCreatureList(message.uuid, newCompound);
                RiftMessages.WRAPPER.sendToAll(new RiftUpdateBoxDeployed(creatureBoxPos, creature, newCompound));

                //for removing creature and hitboxes
                RiftUtil.removeCreature(creature);
                System.out.println("remove pt 1");
            }
        }
        else {
            if (creatureBox == null) return;

            creatureBox.replaceInCreatureList(message.uuid, message.tagCompound);
            if (creature != null
                    && PlayerTamedCreatures.DeploymentType.values()[message.tagCompound.getByte("DeploymentType")] == PlayerTamedCreatures.DeploymentType.BASE_INACTIVE
                    && creature.isEntityAlive()) {
                //for removing creature and hitboxes
                RiftUtil.removeCreature(creature);
            }
        }
    }
}
