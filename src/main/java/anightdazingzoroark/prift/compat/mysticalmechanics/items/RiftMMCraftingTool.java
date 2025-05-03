package anightdazingzoroark.prift.compat.mysticalmechanics.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RiftMMCraftingTool extends Item {
    public RiftMMCraftingTool() {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(64);
    }

    @Override
    public boolean isRepairable() {
        return true;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        ItemStack containerItem = itemStack.copy();
        containerItem.setItemDamage(containerItem.getItemDamage() + 1);

        if (containerItem.getItemDamage() >= containerItem.getMaxDamage()) {
            return ItemStack.EMPTY;
        }

        return containerItem;
    }
}
