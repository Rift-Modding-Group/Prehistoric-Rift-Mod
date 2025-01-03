package anightdazingzoroark.prift.server.inventory;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftChangeInventoryFromMenu;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class CreatureContainer extends Container {
    private final IInventory creatureInventory;
    private final RiftCreature creature;
    private final EntityPlayer player;
    private final int slots;
    private final int rows;

    public CreatureContainer(final RiftCreature creature, EntityPlayer player) {
        this.creatureInventory = creature.creatureInventory;
        this.creature = creature;
        this.player = player;
        this.slots = creatureInventory.getSizeInventory() - (creature.canBeSaddled() ? 1 : 0);
        this.rows = this.slots / 9;
        creatureInventory.openInventory(player);

        //creature saddle slot
        if (!this.creature.isBaby() && this.creature.canBeSaddled()) {
            this.addSlotToContainer(new Slot(creature.creatureInventory, 0, 8, 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return !stack.isEmpty() && creature.saddleItemEqual(stack);
                }

                public boolean canTakeStack(EntityPlayer playerIn) {
                    return !creature.isBeingRidden();
                }
            });
        }

        //creature inventory
        for (int j = 0; j < this.rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(creature.creatureInventory, (k + (this.creature.canBeSaddled() ? 1 : 0)) + (j * 9), 8 + k * 18, 50 + j * 18));
            }
        }

        //player inventory
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlotToContainer(new Slot(player.inventory, j1 + l * 9 + 9, 8 + j1 * 18, 172 + l * 18));
            }
        }

        //player hotbar
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 230));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.creatureInventory.isUsableByPlayer(player) && this.creature.isEntityAlive() && this.creature.getDistance(player) < 8f;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            if (index < this.creatureInventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemStack1, this.creatureInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (this.getSlot(0).isItemValid(itemStack1)) {
                if (!this.mergeItemStack(itemStack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (this.creatureInventory.getSizeInventory() <= 1 || !this.mergeItemStack(itemStack1, 1, this.creatureInventory.getSizeInventory(), false)) {
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
        RiftMessages.WRAPPER.sendToAll(new RiftChangeInventoryFromMenu(this.creature, this.player));
        RiftMessages.WRAPPER.sendToServer(new RiftChangeInventoryFromMenu(this.creature, this.player));
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        RiftMessages.WRAPPER.sendToAll(new RiftChangeInventoryFromMenu(this.creature, this.player));
        RiftMessages.WRAPPER.sendToServer(new RiftChangeInventoryFromMenu(this.creature, this.player));
        this.creatureInventory.closeInventory(player);
    }
}
