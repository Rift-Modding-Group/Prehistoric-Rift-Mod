package anightdazingzoroark.prift.server.inventory;

import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.message.RiftChangeWeaponInvFromMenu;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class WeaponContainer extends Container {
    private final IInventory weaponInventory;
    private final RiftLargeWeapon weapon;
    private final EntityPlayer player;
    private final int slots;

    public WeaponContainer(final RiftLargeWeapon weapon, EntityPlayer player) {
        this.weaponInventory = weapon.weaponInventory;
        this.weapon = weapon;
        this.player = player;
        this.slots = weaponInventory.getSizeInventory();
        weaponInventory.openInventory(player);

        //weapon inventory
        for (int i = 0; i < slots; i++) {
            this.addSlotToContainer(new Slot(weapon.weaponInventory, i, 44 + i * 18, 20) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return !stack.isEmpty() && stack.getItem() == weapon.ammoItem;
                }
            });
        }

        //player inventory
        for (int l = 0; l < 3; ++l) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(player.inventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 51));
            }
        }

        //player hotbar
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 109));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.weaponInventory.isUsableByPlayer(player) && this.weapon.isEntityAlive() && this.weapon.getDistance(player) < 8f;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            if (index < this.weaponInventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemStack1, this.weaponInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (this.getSlot(0).isItemValid(itemStack1)) {
                if (!this.mergeItemStack(itemStack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (this.weaponInventory.getSizeInventory() <= 1 || !this.mergeItemStack(itemStack1, 1, this.weaponInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }
            else if (itemStack1.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }
        return itemStack;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        RiftMessages.WRAPPER.sendToServer(new RiftChangeWeaponInvFromMenu(this.weapon, this.player));
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        RiftMessages.WRAPPER.sendToServer(new RiftChangeWeaponInvFromMenu(this.weapon, this.player));
        this.weaponInventory.closeInventory(player);
    }
}
