package anightdazingzoroark.prift.server.entity.inventory;

import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class CreatureGearHandler extends RiftInventoryHandler {
    private final RiftCreatureType creatureType;

    public CreatureGearHandler(RiftCreatureType creatureType) {
        super(creatureType.gearSlotCount());
        this.creatureType = creatureType;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        this.validateSlotIndex(slot);

        //end early if stack is empty
        if (stack == ItemStack.EMPTY) {
            super.setStackInSlot(slot, stack);
            return;
        }

        //apply item to the stack
        for (int i = 0; i < this.creatureType.gearSlotCount(); i++) {
            if (slot != i) continue;
            RiftCreatureType.InventoryGearType gearType = this.creatureType.usableGear[i];
            switch (gearType) {
                case SADDLE: {
                    if (RiftUtil.itemStacksEqual(this.saddleItemStack(), stack)) {
                        super.setStackInSlot(i, stack);
                    }
                    return;
                }
                case LARGE_WEAPON: {
                    super.setStackInSlot(i, stack);
                    return;
                }
            }
        }

        //nothing else could happen
        super.setStackInSlot(slot, stack);
    }

    public boolean itemStackUsableAsGear(ItemStack itemStack) {
        //empty itemstacks are valid
        if (itemStack.isEmpty()) return true;

        //go on to check valid itemstack types
        for (RiftCreatureType.InventoryGearType inventoryGearType : this.creatureType.usableGear) {
            switch (inventoryGearType) {
                case SADDLE: {
                    if (RiftUtil.itemStacksEqual(this.saddleItemStack(), itemStack)) return true;
                }
                case LARGE_WEAPON: {
                    if (RiftLargeWeaponType.itemStackIsLargeWeapon(itemStack)) return true;
                }
            }
        }
        return false;
    }

    public boolean hasSaddle() {
        int slot = this.getSlotForInventoryGearType(RiftCreatureType.InventoryGearType.SADDLE);
        return this.hasInventoryGearType(RiftCreatureType.InventoryGearType.SADDLE)
                && slot >= 0 && !this.getStackInSlot(slot).isEmpty();
    }

    public boolean hasLargeWeapon() {
        int slot = this.getSlotForInventoryGearType(RiftCreatureType.InventoryGearType.LARGE_WEAPON);
        if (slot < 0) return false;

        return this.hasInventoryGearType(RiftCreatureType.InventoryGearType.LARGE_WEAPON)
                && RiftLargeWeaponType.itemStackIsLargeWeapon(this.getStackInSlot(slot));
    }

    public RiftLargeWeaponType getLargeWeapon() {
        if (!this.hasLargeWeapon()) return RiftLargeWeaponType.NONE;

        int slot = this.getSlotForInventoryGearType(RiftCreatureType.InventoryGearType.LARGE_WEAPON);
        return RiftLargeWeaponType.getFromItem(this.getStackInSlot(slot).getItem());
    }

    private boolean hasInventoryGearType(RiftCreatureType.InventoryGearType gearTypeToSearch) {
        for (RiftCreatureType.InventoryGearType gearType : this.creatureType.usableGear) {
            if (gearTypeToSearch == gearType) return true;
        }
        return false;
    }

    private int getSlotForInventoryGearType(RiftCreatureType.InventoryGearType gearTypeToSearch) {
        for (int i = 0; i < this.creatureType.usableGear.length; i++) {
            RiftCreatureType.InventoryGearType gearType = this.creatureType.usableGear[i];
            if (gearTypeToSearch == gearType) return i;
        }
        return -1;
    }

    private ItemStack saddleItemStack() {
        String saddleItemId = RiftConfigHandler.getConfig(this.creatureType).general.saddleItem;
        return RiftUtil.getItemStackFromString(saddleItemId);
    }
}
