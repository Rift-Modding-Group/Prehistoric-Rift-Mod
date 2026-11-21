package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.client.rendering.entity.RiftCreatureRenderer;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.item.RiftItems;
import anightdazingzoroark.riftlib.item.EntityItemRenderContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderItem.class)
public class RiftMixinPlatformItemFade {
    @Unique
    @Nullable
    private RiftCreatureRenderer.RiderPass prift$platformAimPass;

    @WrapMethod(method = "renderItemModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V")
    private void fadePlatformModel(
            @NotNull ItemStack stack, @NotNull IBakedModel model, @NotNull ItemCameraTransforms.TransformType transform,
            boolean leftHanded, @NotNull Operation<Void> original
    ) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityItemRenderContext context = EntityItemRenderContext.getCurrent();
        if (stack.getItem() != RiftItems.CREATURE_PLATFORM || context == null
                || !(context.getEntity() instanceof RiftCreature creature)
                || minecraft.player == null || minecraft.player.getRidingEntity() != creature
        ) {
            original.call(stack, model, transform, leftHanded);
            return;
        }
        this.prift$platformAimPass = RiftCreatureRenderer.renderAimAttachment(
                this.prift$platformAimPass, RiftCreatureRenderer.getPlatformAimVisibility(minecraft.player),
                () -> original.call(stack, model, transform, leftHanded)
        );
    }
}
