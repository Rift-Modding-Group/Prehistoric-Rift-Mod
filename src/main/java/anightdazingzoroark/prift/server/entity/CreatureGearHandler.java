package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class CreatureGearHandler extends ItemStackHandler {
    private final RiftCreature creature;

    public CreatureGearHandler(RiftCreature creature) {
        super(creature.creatureType.gearSlotCount());
        this.creature = creature;
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
        for (int i = 0; i < this.creature.creatureType.gearSlotCount(); i++) {
            if (slot != i) continue;
            RiftCreatureType.InventoryGearType gearType = this.creature.creatureType.usableGear[i];
            switch (gearType) {
                case SADDLE: {
                    if (RiftUtil.itemStacksEqual(this.creature.saddleItemStack(), stack)) {
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
        for (RiftCreatureType.InventoryGearType inventoryGearType : this.creature.creatureType.usableGear) {
            System.out.println("inventoryGearType: "+inventoryGearType);
            switch (inventoryGearType) {
                case SADDLE: {
                    if (RiftUtil.itemStacksEqual(this.creature.saddleItemStack(), itemStack)) return true;
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
                && slot >= 0 && this.getStackInSlot(slot) != ItemStack.EMPTY;
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
        for (RiftCreatureType.InventoryGearType gearType : this.creature.creatureType.usableGear) {
            if (gearTypeToSearch == gearType) return true;
        }
        return false;
    }

    private int getSlotForInventoryGearType(RiftCreatureType.InventoryGearType gearTypeToSearch) {
        for (int i = 0; i < this.creature.creatureType.usableGear.length; i++) {
            RiftCreatureType.InventoryGearType gearType = this.creature.creatureType.usableGear[i];
            if (gearTypeToSearch == gearType) return i;
        }
        return -1;
    }
}
