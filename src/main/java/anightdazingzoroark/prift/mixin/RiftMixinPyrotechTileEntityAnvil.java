package anightdazingzoroark.prift.mixin;

import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.AnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.spi.TileAnvilBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TileAnvilBase.class)
public interface RiftMixinPyrotechTileEntityAnvil {
    @Invoker(value = "setRecipe", remap = false)
    void invokeAnvilSetRecipe(AnvilRecipe recipe);

    @Invoker(value = "getHitsPerDamage", remap = false)
    int invokeGetHitsPerDamage();
}
