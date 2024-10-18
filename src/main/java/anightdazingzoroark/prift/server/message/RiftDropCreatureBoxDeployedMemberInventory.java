package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import io.netty.buffer.ByteBuf;
import net.ilexiconn.llibrary.server.network.AbstractMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftDropCreatureBoxDeployedMemberInventory extends AbstractMessage<RiftDropCreatureBoxDeployedMemberInventory> {
    private int creatureBoxPosX;
    private int creatureBoxPosY;
    private int creatureBoxPosZ;
    private int creatureBoxDeployedPos;

    public RiftDropCreatureBoxDeployedMemberInventory() {}

    public RiftDropCreatureBoxDeployedMemberInventory(BlockPos creatureBoxPos, int creatureBoxDeployedPos) {
        this.creatureBoxPosX = creatureBoxPos.getX();
        this.creatureBoxPosY = creatureBoxPos.getY();
        this.creatureBoxPosZ = creatureBoxPos.getZ();
        this.creatureBoxDeployedPos = creatureBoxDeployedPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.creatureBoxPosX = buf.readInt();
        this.creatureBoxPosY = buf.readInt();
        this.creatureBoxPosZ = buf.readInt();
        this.creatureBoxDeployedPos = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.creatureBoxPosX);
        buf.writeInt(this.creatureBoxPosY);
        buf.writeInt(this.creatureBoxPosZ);
        buf.writeInt(this.creatureBoxDeployedPos);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, RiftDropCreatureBoxDeployedMemberInventory message, EntityPlayer player, MessageContext messageContext) {

    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, RiftDropCreatureBoxDeployedMemberInventory message, EntityPlayer player, MessageContext messageContext) {
        BlockPos pos = new BlockPos(message.creatureBoxPosX, message.creatureBoxPosY, message.creatureBoxPosZ);
        RiftTileEntityCreatureBox creatureBox = (RiftTileEntityCreatureBox)player.world.getTileEntity(pos);
        IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

        NBTTagCompound partyMemNBT = creatureBox.getCreatureList().get(message.creatureBoxDeployedPos);
        NBTTagList nbtItemList = partyMemNBT.getTagList("Items", 10);
        boolean canBeSaddled = RiftCreatureType.values()[partyMemNBT.getByte("CreatureType")].invokeClass(Minecraft.getMinecraft().world).canBeSaddled();

        for (int x = 0; x < nbtItemList.tagCount(); x++) {
            //get the real creature first to make the item spawn in its location
            //otherwise it spawns at the owners location
            RiftCreature realCreature = (RiftCreature) RiftUtil.getEntityFromUUID(player.world, partyMemNBT.getUniqueId("UniqueID"));
            int itemPosX = realCreature == null ? (int)player.posX : realCreature.getPosition().getX();
            int itemPosY = realCreature == null ? (int)player.posY : realCreature.getPosition().getY();
            int itemPosZ = realCreature == null ? (int)player.posZ : realCreature.getPosition().getZ();

            NBTTagCompound nbttagcompound = nbtItemList.getCompoundTagAt(x);
            int j = nbttagcompound.getByte("Slot") & 255;
            if ((canBeSaddled && j != 0) || !canBeSaddled) {
                EntityItem entityItem = new EntityItem(player.world, itemPosX, itemPosY, itemPosZ);
                entityItem.setItem(new ItemStack(nbttagcompound));
                player.world.spawnEntity(entityItem);
            }
        }

        playerTamedCreatures.removeBoxCreatureDeployedInventory(player.world, pos, message.creatureBoxDeployedPos);

    }
}
