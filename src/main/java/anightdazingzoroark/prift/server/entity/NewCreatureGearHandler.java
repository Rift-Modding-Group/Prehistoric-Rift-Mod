package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NewCreatureGearHandler {
    private final RiftCreature creature;
    private ItemStack saddleItem = ItemStack.EMPTY;
    private ItemStack largeWeaponItem = ItemStack.EMPTY;

    public NewCreatureGearHandler(RiftCreature creature) {
        this.creature = creature;
    }

    public void equipGear(RiftCreatureType.InventoryGearType inventoryGearType, ItemStack itemStack) {
        if (!(this.creature.creatureType.canUseGearType(inventoryGearType))) return;

        switch (inventoryGearType) {
            case SADDLE: {
                if (this.creature.saddleItemStack() == itemStack) this.saddleItem = itemStack;
                break;
            }
            case LARGE_WEAPON: {
                this.largeWeaponItem = itemStack;
                break;
            }
        }
    }

    public void clearGear(RiftCreatureType.InventoryGearType inventoryGearType) {
        if (!(this.creature.creatureType.canUseGearType(inventoryGearType))) return;

        switch (inventoryGearType) {
            case SADDLE: {
                this.saddleItem = ItemStack.EMPTY;
                break;
            }
            case LARGE_WEAPON: {
                this.largeWeaponItem = ItemStack.EMPTY;
                break;
            }
        }
    }

    public ItemStack getSaddleItem() {
        return this.saddleItem;
    }

    public ItemStack getLargeWeaponItem() {
        return this.largeWeaponItem;
    }

    public NBTTagCompound getNBTTagCompound() {
        NBTTagCompound toReturn = new NBTTagCompound();

        //encode saddle item
        if (this.saddleItem.getTagCompound() != null) toReturn.setTag("SaddleGear", this.saddleItem.getTagCompound());

        //encode large weapon item
        if (this.largeWeaponItem.getTagCompound() != null) toReturn.setTag("LargeWeaponGear", this.largeWeaponItem.getTagCompound());

        return toReturn;
    }

    public void serializeNBTTagCompound(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.hasKey("SaddleGear")) this.saddleItem = new ItemStack(nbtTagCompound.getCompoundTag("SaddleGear"));
        if (nbtTagCompound.hasKey("LargeWeaponGear")) this.largeWeaponItem = new ItemStack(nbtTagCompound.getCompoundTag("LargeWeaponGear"));
    }
}
