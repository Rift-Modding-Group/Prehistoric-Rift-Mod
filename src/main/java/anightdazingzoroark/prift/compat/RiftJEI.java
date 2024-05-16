package anightdazingzoroark.prift.compat;

import anightdazingzoroark.prift.server.entity.projectile.RiftProjectiles;
import anightdazingzoroark.prift.server.items.RiftItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class RiftJEI implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        //hide the projectile animators from jei
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.CANNONBALL));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.MORTAR_SHELL));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.CATAPULT_BOULDER));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_STEGOSAURUS_PLATE_ONE));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_STEGOSAURUS_PLATE_TWO));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_STEGOSAURUS_PLATE_THREE));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_STEGOSAURUS_PLATE_FOUR));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftProjectiles.THROWN_BOLA));

        //hide le hidden items
        blacklist.addIngredientToBlacklist(new ItemStack(RiftItems.DETECT_ALERT));
        blacklist.addIngredientToBlacklist(new ItemStack(RiftItems.CHEST_DETECT_ALERT));
    }
}
