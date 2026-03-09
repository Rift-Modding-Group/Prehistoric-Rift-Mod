package anightdazingzoroark.prift.mixin.pyrotech;

import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipeBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value=BloomeryRecipeBase.class, remap = false)
public interface RiftMixinPyrotechBloomeryRecipeBase {
    @Invoker(value = "selectRandomFailureItemStack", remap = false)
    ItemStack invokeSelectRandomFailureItemStack();
}