package anightdazingzoroark.prift.compat.mysticalmechanics.inventory;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMillstone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class MillstoneContainer extends Container {
    private TileEntityMillstone millstone;
    private final EntityPlayer player;

    public MillstoneContainer(TileEntityMillstone millstone, EntityPlayer player) {
        this.millstone = millstone;
        this.player = player;

        //millstone inputs
        for (int x = 0; x < 3; x++) {
            this.addSlotToContainer(new Slot(millstone, x, 62 + x * 18, 20));
        }

        //millstone outputs
        for (int x = 3; x < millstone.getSizeInventory(); x++) {
            this.addSlotToContainer(new Slot(millstone, x, 8 + (x - 3) * 18, 74) {
                @Override
                public boolean isItemValid(@Nonnull ItemStack stack) {
                    return false;
                }
            });
        }

        //player inventory
        for (int l = 0; l < 3; ++l) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(player.inventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 105));
            }
        }

        //player hotbar
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 163));
        }
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            if (index < 1) {
                if (!this.mergeItemStack(itemStack1, 1, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemStack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }
            else {
                slot.onSlotChanged();
            }
        }
        return itemStack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.millstone.isUsableByPlayer(playerIn);
    }
}
