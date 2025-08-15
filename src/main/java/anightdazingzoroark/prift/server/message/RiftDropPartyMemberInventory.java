package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftDropPartyMemberInventory implements IMessage {
    private int partyPos;

    public RiftDropPartyMemberInventory() {}

    public RiftDropPartyMemberInventory(int partyPos) {
        this.partyPos = partyPos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.partyPos = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.partyPos);
    }

    public static class Handler implements IMessageHandler<RiftDropPartyMemberInventory, IMessage> {
        @Override
        public IMessage onMessage(RiftDropPartyMemberInventory message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftDropPartyMemberInventory message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;

            IPlayerTamedCreatures playerTamedCreatures = playerEntity.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

            CreatureNBT partyMemNBT = playerTamedCreatures.getPartyNBT().get(message.partyPos);
            NBTTagList nbtItemList = partyMemNBT.getItemListNBT();

            for (int x = 0; x < nbtItemList.tagCount(); x++) {
                //get the real creature first to make the item spawn in its location
                //otherwise it spawns at the owners location
                RiftCreature realCreature = (RiftCreature) RiftUtil.getEntityFromUUID(playerEntity.world, partyMemNBT.getUniqueID());
                int itemPosX = realCreature == null ? (int)playerEntity.posX : realCreature.getPosition().getX();
                int itemPosY = realCreature == null ? (int)playerEntity.posY : realCreature.getPosition().getY();
                int itemPosZ = realCreature == null ? (int)playerEntity.posZ : realCreature.getPosition().getZ();

                NBTTagCompound nbttagcompound = nbtItemList.getCompoundTagAt(x);
                int j = nbttagcompound.getByte("Slot") & 255;
                RiftCreatureType creatureType = partyMemNBT.getCreatureType();

                boolean unremovableSlot = (creatureType.canBeSaddled && j == creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE))
                        || (creatureType.canHoldLargeWeapon && j == creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON));
                if (!unremovableSlot) {
                    EntityItem entityItem = new EntityItem(playerEntity.world, itemPosX, itemPosY, itemPosZ);
                    entityItem.setItem(new ItemStack(nbttagcompound));
                    playerEntity.world.spawnEntity(entityItem);
                }
            }

            playerTamedCreatures.removePartyCreatureInventory(message.partyPos);
        }
    }
}
