package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftDropCreatureBoxDeployedMemberInventory implements IMessage {
    private int creatureBoxPosX;
    private int creatureBoxPosY;
    private int creatureBoxPosZ;
    private int creatureBoxDeployedPos;
    private NBTTagList inventoryTagCompound;

    public RiftDropCreatureBoxDeployedMemberInventory() {}

    public RiftDropCreatureBoxDeployedMemberInventory(BlockPos creatureBoxPos, int creatureBoxDeployedPos, NBTTagList inventoryTagCompound) {
        this.creatureBoxPosX = creatureBoxPos.getX();
        this.creatureBoxPosY = creatureBoxPos.getY();
        this.creatureBoxPosZ = creatureBoxPos.getZ();
        this.creatureBoxDeployedPos = creatureBoxDeployedPos;
        this.inventoryTagCompound = inventoryTagCompound; //for reasons only god knows gettin other inventory contents within here returns nothin so here this goes ffs
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureBoxPosX = buf.readInt();
        this.creatureBoxPosY = buf.readInt();
        this.creatureBoxPosZ = buf.readInt();
        this.creatureBoxDeployedPos = buf.readInt();
        this.inventoryTagCompound = ByteBufUtils.readTag(buf).getTagList("Items", 10);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureBoxPosX);
        buf.writeInt(this.creatureBoxPosY);
        buf.writeInt(this.creatureBoxPosZ);
        buf.writeInt(this.creatureBoxDeployedPos);
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("Items", this.inventoryTagCompound);
        ByteBufUtils.writeTag(buf, tagCompound);
    }

    public static class Handler implements IMessageHandler<RiftDropCreatureBoxDeployedMemberInventory, IMessage> {
        @Override
        public IMessage onMessage(RiftDropCreatureBoxDeployedMemberInventory message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftDropCreatureBoxDeployedMemberInventory message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;

            BlockPos pos = new BlockPos(message.creatureBoxPosX, message.creatureBoxPosY, message.creatureBoxPosZ);
            RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox)playerEntity.world.getTileEntity(pos);
            IPlayerTamedCreatures playerTamedCreatures = playerEntity.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

            NBTTagCompound partyMemNBT = creatureBox.getCreatureList().get(message.creatureBoxDeployedPos);
            NBTTagList nbtItemList = message.inventoryTagCompound;
            boolean canBeSaddled = RiftCreatureType.values()[partyMemNBT.getByte("CreatureType")].canBeSaddled;

            for (int x = 0; x < nbtItemList.tagCount(); x++) {
                //get the real creature first to make the item spawn in its location
                //otherwise it spawns at the owners location
                RiftCreature realCreature = (RiftCreature) RiftUtil.getEntityFromUUID(playerEntity.world, partyMemNBT.getUniqueId("UniqueID"));
                boolean inWorld = playerEntity.world.getLoadedEntityList().contains(realCreature);
                int itemPosX = realCreature == null ? (int)playerEntity.posX : (inWorld ? realCreature.getPosition().getX() : (int)playerEntity.posX);
                int itemPosY = realCreature == null ? (int)playerEntity.posY : (inWorld ? realCreature.getPosition().getY() : (int)playerEntity.posY);
                int itemPosZ = realCreature == null ? (int)playerEntity.posZ : (inWorld ? realCreature.getPosition().getZ() : (int)playerEntity.posZ);

                NBTTagCompound nbttagcompound = nbtItemList.getCompoundTagAt(x);
                int j = nbttagcompound.getByte("Slot") & 255;
                if ((canBeSaddled && j != 0) || !canBeSaddled) {
                    EntityItem entityItem = new EntityItem(playerEntity.world, itemPosX, itemPosY, itemPosZ);
                    entityItem.setItem(new ItemStack(nbttagcompound));
                    playerEntity.world.spawnEntity(entityItem);
                }
            }

            playerTamedCreatures.removeBoxCreatureDeployedInventory(playerEntity.world, pos, message.creatureBoxDeployedPos);
        }
    }
}
