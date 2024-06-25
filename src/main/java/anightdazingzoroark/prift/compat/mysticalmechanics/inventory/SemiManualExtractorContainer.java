package anightdazingzoroark.prift.compat.mysticalmechanics.inventory;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SemiManualExtractorContainer extends Container {
    private TileEntitySemiManualExtractor semiManualExtractor;
    private final EntityPlayer player;

    public SemiManualExtractorContainer(TileEntitySemiManualExtractor semiManualExtractor, EntityPlayer player) {
        this.semiManualExtractor = semiManualExtractor;
        this.player = player;

        //extractor inventory
        //item
        this.addSlotToContainer(new Slot(semiManualExtractor, 0, 45, 36));
        this.addSlotToContainer(new Slot(semiManualExtractor, 1, 138, 18) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return stack.getItem() == Items.BUCKET;
            }
        });
        this.addSlotToContainer(new Slot(semiManualExtractor, 2, 138, 54) {
            @Override
            public boolean isItemValid(@Nonnull ItemStack stack) {
                return false;
            }
        });

        //player inventory
        for (int l = 0; l < 3; ++l) {
            for (int k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(player.inventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 84));
            }
        }

        //player hotbar
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 142));
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
        return this.semiManualExtractor.isUsableByPlayer(playerIn);
    }
}
