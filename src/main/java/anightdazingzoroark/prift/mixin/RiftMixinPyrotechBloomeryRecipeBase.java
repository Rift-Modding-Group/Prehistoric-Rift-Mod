package anightdazingzoroark.prift.mixin;

import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipeBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BloomeryRecipeBase.class)
public interface RiftMixinPyrotechBloomeryRecipeBase {
    @Invoker(value = "selectRandomFailureItemStack", remap = false)
    ItemStack invokeSelectRandomFailureItemStack();
}