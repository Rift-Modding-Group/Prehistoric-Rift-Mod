package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.helper.RiftUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Function;

public class CreatureInventoryHandler extends ItemStackHandler {
    private final ItemSearchResult noItemFound = new ItemSearchResult(false, ItemStack.EMPTY, -1);

    public CreatureInventoryHandler(int inventorySize) {
        super(inventorySize);
    }

    public ItemStack addItem(ItemStack itemStackToAdd) {
        ItemSearchResult similarItemResult = this.findItem(ItemSearchDirection.FIRST_TO_LAST, itemStack -> {
            return RiftUtil.itemStacksEqual(itemStack, itemStackToAdd) && itemStack.isStackable() && itemStack.getCount() < itemStackToAdd.getMaxStackSize();
        });
        ItemSearchResult emptySpaceResult = this.findItem(ItemSearchDirection.FIRST_TO_LAST, ItemStack.EMPTY);

        if (similarItemResult.successful) {
            int maxStackSize = itemStackToAdd.getMaxStackSize();
            if (similarItemResult.foundStack.getCount() + itemStackToAdd.getCount() < maxStackSize) {
                similarItemResult.foundStack.setCount(similarItemResult.foundStack.getCount() + itemStackToAdd.getCount());
                return ItemStack.EMPTY;
            }
            else {
                int remainingSize = similarItemResult.foundStack.getCount() + itemStackToAdd.getCount() - maxStackSize;
                similarItemResult.foundStack.setCount(maxStackSize);
                itemStackToAdd.setCount(remainingSize);
                return this.addItem(itemStackToAdd);
            }
        }
        else if (emptySpaceResult.successful) {
            this.setStackInSlot(emptySpaceResult.slot, itemStackToAdd);
            return ItemStack.EMPTY;
        }
        else return itemStackToAdd;
    }

    public void removeItem(ItemSearchDirection itemSearchDirection, ItemStack itemStackToRemove) {
        ItemSearchResult similarItemResult = this.findItem(itemSearchDirection, itemStackToRemove);

        if (similarItemResult.successful) {
            int subtractResult = similarItemResult.foundStack.getCount() - itemStackToRemove.getCount();
            if (subtractResult >= 0) similarItemResult.foundStack.setCount(subtractResult);
            else {
                itemStackToRemove.setCount(Math.abs(subtractResult));
                this.removeItem(itemSearchDirection, itemStackToRemove);
            }
        }
    }

    public ItemSearchResult findItem(ItemSearchDirection searchDirection, ItemStack itemToSearch) {
        if (searchDirection == ItemSearchDirection.FIRST_TO_LAST) {
            for (int i = 0; i < this.stacks.size(); i++) {
                ItemStack itemStack = this.stacks.get(i);
                if (RiftUtil.itemStacksEqual(itemToSearch, itemStack)) {
                    return new ItemSearchResult(true, itemStack, i);
                }
            }
            return this.noItemFound;
        }
        else if (searchDirection == ItemSearchDirection.LAST_TO_FIRST) {
            for (int i = this.stacks.size() - 1; i >= 0; i--) {
                ItemStack itemStack = this.stacks.get(i);
                if (RiftUtil.itemStacksEqual(itemToSearch, itemStack)) {
                    return new ItemSearchResult(true, itemStack, i);
                }
            }
            return this.noItemFound;
        }
        return this.noItemFound;
    }

    public ItemSearchResult findItem(ItemSearchDirection searchDirection, Item itemToSearch) {
        if (searchDirection == ItemSearchDirection.FIRST_TO_LAST) {
            for (int i = 0; i < this.stacks.size(); i++) {
                ItemStack itemStack = this.stacks.get(i);
                if (itemStack.getItem() == itemToSearch) {
                    return new ItemSearchResult(true, itemStack, i);
                }
            }
            return this.noItemFound;
        }
        else if (searchDirection == ItemSearchDirection.LAST_TO_FIRST) {
            for (int i = this.stacks.size() - 1; i >= 0; i--) {
                ItemStack itemStack = this.stacks.get(i);
                if (itemStack.getItem() == itemToSearch) {
                    return new ItemSearchResult(true, itemStack, i);
                }
            }
            return this.noItemFound;
        }
        return this.noItemFound;
    }

    public ItemSearchResult findItem(ItemSearchDirection searchDirection, Function<ItemStack, Boolean> itemSearchFunction) {
        if (searchDirection == ItemSearchDirection.FIRST_TO_LAST) {
            for (int i = 0; i < this.stacks.size(); i++) {
                ItemStack itemStack = this.stacks.get(i);
                if (itemSearchFunction.apply(itemStack)) return new ItemSearchResult(true, itemStack, i);
            }
            return this.noItemFound;
        }
        else if (searchDirection == ItemSearchDirection.LAST_TO_FIRST) {
            for (int i = this.stacks.size() - 1; i >= 0; i--) {
                ItemStack itemStack = this.stacks.get(i);
                if (itemSearchFunction.apply(itemStack)) return new ItemSearchResult(true, itemStack, i);
            }
            return this.noItemFound;
        }
        return this.noItemFound;
    }

    public static class ItemSearchResult {
        public final boolean successful;
        public final ItemStack foundStack;
        public final int slot;

        public ItemSearchResult(boolean successful, ItemStack foundStack, int slot) {
            this.successful = successful;
            this.foundStack = foundStack;
            this.slot = slot;
        }
    }

    public enum ItemSearchDirection {
        FIRST_TO_LAST,
        LAST_TO_FIRST;
    }
}
