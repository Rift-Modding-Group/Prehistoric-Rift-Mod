package anightdazingzoroark.prift.mixin;

import com.codetaylor.mc.athenaeum.util.RandomHelper;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.spi.TileAnvilBase;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.block.BlockBloom;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomAnvilRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BloomAnvilRecipe.class)
public class RiftMixinBloomAnvilRecipeFromAnkylosaurus {
    @Inject(method = "onRecipeCompleted", at = @At("HEAD"), remap = false, cancellable = true)
    public void onRecipeCompleted(TileAnvilBase tile, World world, ItemStackHandler stackHandler, BloomAnvilRecipe recipe, ItemStack toolItemStack, CallbackInfoReturnable<List<ItemStack>> ci) {
        if (toolItemStack == null) {
            List<ItemStack> result = new ArrayList<>();

            if (RandomHelper.random().nextDouble() < recipe.getBloomeryRecipe().getFailureChance()) {
                result.add(((RiftMixinPyrotechBloomeryRecipeBase) recipe.getBloomeryRecipe()).invokeSelectRandomFailureItemStack());
            }
            else result.add(recipe.getBloomeryRecipe().getOutput());

            ItemStack bloom = stackHandler.extractItem(0, stackHandler.getSlotLimit(0), false);
            BlockBloom.ItemBlockBloom item = (BlockBloom.ItemBlockBloom) bloom.getItem();
            int integrity = item.getIntegrity(bloom);

            integrity -= 1;

            if (integrity > 0) {
                item.setIntegrity(bloom, integrity);
                stackHandler.insertItem(0, bloom, false);
            }
            ci.setReturnValue(result);
        }
    }
}
