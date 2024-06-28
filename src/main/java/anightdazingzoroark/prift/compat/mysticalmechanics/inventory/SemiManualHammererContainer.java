package anightdazingzoroark.prift.compat.mysticalmechanics.inventory;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualHammerer;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.block.BlockBloom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SemiManualHammererContainer extends Container {
    private final TileEntitySemiManualHammerer semiManualHammerer;
    private final EntityPlayer player;

    public SemiManualHammererContainer(TileEntitySemiManualHammerer semiManualHammerer, EntityPlayer player) {
        this.semiManualHammerer = semiManualHammerer;
        this.player = player;

        //extractor inventory
        //item
        this.addSlotToContainer(new Slot(semiManualHammerer, 0, 45, 36));
        this.addSlotToContainer(new Slot(semiManualHammerer, 1, 116, 36) {
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

            if (index < 2) {
                if (!this.mergeItemStack(itemStack1, 2, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemStack1, 0, 2, false)) {
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
        return this.semiManualHammerer.isUsableByPlayer(playerIn);
    }
}
