package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class RiftUpdateBoxDeployed implements IMessage {
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

    public static class Handler implements IMessageHandler<RiftUpdateBoxDeployed, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdateBoxDeployed message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdateBoxDeployed message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

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
            if (ctx.side == Side.CLIENT) {
                EntityPlayer messagePlayer = Minecraft.getMinecraft().player;

                BlockPos creatureBoxPos = new BlockPos(message.posX, message.posY, message.posZ);
                RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox) messagePlayer.world.getTileEntity(creatureBoxPos);
                RiftCreature creature = (RiftCreature) RiftUtil.getEntityFromUUID(messagePlayer.world, message.uuid);

                if (message.tagCompound.isEmpty()) {
                    if (creature == null || creatureBox == null) return;

                    if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) {
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
                    }
                }
            }
        }
    }
}
