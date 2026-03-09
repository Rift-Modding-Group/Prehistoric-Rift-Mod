package anightdazingzoroark.prift.mixin.pyrotech;

import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.TileChoppingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value=TileChoppingBlock.class, remap = false)
public interface RiftMixinPyrotechTileEntityChoppingBlock {
    @Invoker(value = "setRecipeProgress", remap = false)
    void invokeSetRecipeProgress(float recipeProgress);

    @Invoker(value = "setDurabilityUntilNextDamage", remap = false)
    void invokeSetDurabilityUntilNextDamage(int durabilityUntilNextDamage);

    @Invoker(value = "getDurabilityUntilNextDamage", remap = false)
    int invokeGetDurabilityUntilNextDamage();
}
