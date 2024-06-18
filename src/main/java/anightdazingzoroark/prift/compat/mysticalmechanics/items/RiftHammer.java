package anightdazingzoroark.prift.compat.mysticalmechanics.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RiftHammer extends Item {
    public RiftHammer() {
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
        return true; // This tells Minecraft that this item has a container item
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        // Create a copy of the item stack and reduce its durability
        ItemStack containerItem = itemStack.copy();
        containerItem.setItemDamage(containerItem.getItemDamage() + 1);

        // If the item is completely damaged, return an empty item stack
        if (containerItem.getItemDamage() >= containerItem.getMaxDamage()) {
            return ItemStack.EMPTY;
        }

        return containerItem;
    }
}
