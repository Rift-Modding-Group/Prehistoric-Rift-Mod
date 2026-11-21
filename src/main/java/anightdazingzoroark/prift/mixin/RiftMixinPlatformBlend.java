package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.client.rendering.entity.RiftCreatureRenderer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GlStateManager.class)
public class RiftMixinPlatformBlend {
    @WrapMethod(method = "blendFunc(II)V")
    private static void preservePlatformAlpha(int source, int destination, @NotNull Operation<Void> original) {
        if (RiftCreatureRenderer.isRenderingAimAttachment() && source == GL11.GL_SRC_ALPHA && destination == GL11.GL_ONE_MINUS_SRC_ALPHA) {
            GlStateManager.tryBlendFuncSeparate(source, destination, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        else original.call(source, destination);
    }

    @WrapMethod(method = "tryBlendFuncSeparate(IIII)V")
    private static void preserveSeparatePlatformAlpha(
            int sourceRgb, int destinationRgb, int sourceAlpha, int destinationAlpha, @NotNull Operation<Void> original
    ) {
        if (RiftCreatureRenderer.isRenderingAimAttachment() && sourceRgb == GL11.GL_SRC_ALPHA && destinationRgb == GL11.GL_ONE_MINUS_SRC_ALPHA) {
            original.call(sourceRgb, destinationRgb, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        else original.call(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
    }
}
