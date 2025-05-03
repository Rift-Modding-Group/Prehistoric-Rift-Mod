package anightdazingzoroark.prift.server.inventory;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftChangeInventoryFromMenu;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreatureContainer extends Container {
    private final IInventory creatureInventory;
    private EntityPlayer player;
    private final RiftCreature creature;

    public CreatureContainer(final RiftCreature creature, EntityPlayer player) {
        this.creatureInventory = creature.creatureInventory;
        this.player = player;
        this.creature = creature;
        int slots = creatureInventory.getSizeInventory() - creature.creatureType.gearSlotCount();
        int rows = slots / 9;
        this.creatureInventory.openInventory(player);

        //saddle slot
        if (!this.creature.isBaby() && this.creature.creatureType.canBeSaddled) {
            this.addSlotToContainer(new Slot(creature.creatureInventory, creature.creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.SADDLE), 8, 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return !stack.isEmpty() && creature.saddleItemStack().getItem() == stack.getItem() && creature.saddleItemStack().getMetadata() == stack.getMetadata();
                }

                public boolean canTakeStack(EntityPlayer playerIn) {
                    return !creature.isBeingRidden();
                }
            });
        }

        //large weapon slot
        if (!this.creature.isBaby() && this.creature.creatureType.canHoldLargeWeapon) {
            this.addSlotToContainer(new Slot(creature.creatureInventory, creature.creatureType.slotIndexForGear(RiftCreatureType.InventoryGearType.LARGE_WEAPON), 26, 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return !stack.isEmpty() && creature.itemStackIsLargeWeapon(stack) && creature.getLargeWeaponCooldown() <= 0 && creature.getLargeWeaponUse() <= 0;
                }
            });
        }

        //creature inventory
        for (int j = 0; j < rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(creature.creatureInventory, (k + this.creature.creatureType.gearSlotCount()) + (j * 9), 8 + k * 18, 50 + j * 18));
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
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack transferred = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack current = slot.getStack();
            transferred = current.copy();

            if (index < this.creatureInventory.getSizeInventory()) {
                if (!this.mergeItemStack(current, this.creatureInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (this.getSlot(0).isItemValid(current)) {
                if (!this.mergeItemStack(current, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (this.creatureInventory.getSizeInventory() <= 1 || !this.mergeItemStack(current, 1, this.creatureInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }
            else if (current.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }
        return transferred;
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
