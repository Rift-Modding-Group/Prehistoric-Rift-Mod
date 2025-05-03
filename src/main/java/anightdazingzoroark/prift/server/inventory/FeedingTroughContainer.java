package anightdazingzoroark.prift.server.inventory;

import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FeedingTroughContainer extends Container {
    private RiftTileEntityFeedingTrough trough;
    private final EntityPlayer player;
    private final int slots = 9;

    public FeedingTroughContainer(RiftTileEntityFeedingTrough trough, EntityPlayer player) {
        this.trough = trough;
        this.player = player;

        //trough inventory
        IItemHandler itemHandler = this.trough.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        for (int k = 0; k < this.slots; ++k) {
            this.addSlotToContainer(new SlotItemHandler(itemHandler, k , 8 + k * 18, 20));
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

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();

            if (index < RiftTileEntityFeedingTrough.INV_SIZE) {
                if (!this.mergeItemStack(itemStack1, RiftTileEntityFeedingTrough.INV_SIZE, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemStack1, 0, RiftTileEntityFeedingTrough.INV_SIZE, false)) {
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
        return this.trough.canInteractWith(this.player);
    }
}
