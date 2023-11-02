package anightdazingzoroark.rift.server.recipes;

import anightdazingzoroark.rift.server.items.RiftItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RiftRecipes {
    public static void registerSmelting() {
        GameRegistry.addSmelting(RiftItems.RAW_EXOTIC_MEAT, new ItemStack(RiftItems.COOKED_EXOTIC_MEAT), 1.0F);
        GameRegistry.addSmelting(RiftItems.RAW_FIBROUS_MEAT, new ItemStack(RiftItems.COOKED_FIBROUS_MEAT), 1.0F);
    }
}
