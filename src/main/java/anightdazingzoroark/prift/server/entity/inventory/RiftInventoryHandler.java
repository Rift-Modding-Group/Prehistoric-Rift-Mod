package anightdazingzoroark.prift.server.entity.inventory;

import anightdazingzoroark.prift.helper.RiftUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.IntStream;

public class RiftInventoryHandler extends ItemStackHandler {
    //fallback value for item searching if nothing can be found
    private final ItemSearchResult noItemFound = new ItemSearchResult(false, ItemStack.EMPTY, -1);

    public RiftInventoryHandler() {
        super();
    }

    public RiftInventoryHandler(int size) {
        super(size);
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this.stacks) {
            if (!itemStack.isEmpty()) return false;
        }
        return true;
    }

    public ItemStack addItem(ItemStack itemStackToAdd) {
        ItemSearchResult similarItemResult = this.findItem(
                ItemSearchDirection.FIRST_TO_LAST,
                itemStack -> {
                    return RiftUtil.itemStacksEqual(itemStack, itemStackToAdd)
                        && itemStack.isStackable()
                        && itemStack.getCount() < itemStackToAdd.getMaxStackSize();
                }
        );
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

    public void removeItem(@NotNull ItemSearchDirection itemSearchDirection, ItemStack itemStackToRemove) {
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

    public ItemSearchResult findItem(@NotNull ItemSearchDirection searchDirection, ItemStack itemToSearch) {
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

    public ItemSearchResult findItem(@NotNull ItemSearchDirection searchDirection, Item itemToSearch) {
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

    public ItemSearchResult findItem(@NotNull ItemSearchDirection searchDirection, Function<ItemStack, Boolean> itemSearchFunction) {
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

    public void dropAllItems(World world, BlockPos pos) {
        if (world.isRemote) return;

        //drop all items in this creatures inventory
        for (int i = 0; i < this.stacks.size(); i++) {
            ItemStack itemStack = this.stacks.get(i);
            if (itemStack.isEmpty()) continue;

            //create itemStack as an entity and spawn it
            EntityItem droppedItem = new EntityItem(world);
            droppedItem.setItem(itemStack);
            droppedItem.setPosition(pos.getX(), pos.getY() + 0.5D, pos.getZ());
            world.spawnEntity(droppedItem);

            //clear up after dropping
            this.stacks.set(i, ItemStack.EMPTY);
        }
    }

    //-----methods for use in ISidedInventory starts here-----
    public int[] getSlotIndexes() {
        return this.getSlotIndexes(0);
    }

    public int[] getSlotIndexes(int displacement) {
        return IntStream.range(displacement, this.getSlots() + displacement).toArray();
    }
    //-----methods for use in ISidedInventory ends here-----

    @Override
    public String toString() {
        return this.stacks.toString();
    }

    public record ItemSearchResult(boolean successful, ItemStack foundStack, int slot) {}

    public enum ItemSearchDirection {
        FIRST_TO_LAST,
        LAST_TO_FIRST;
    }
}
