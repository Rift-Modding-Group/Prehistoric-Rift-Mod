package anightdazingzoroark.prift.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Exists to make sure that when riding a creature, it will never be
 * hit, its to prevent HWYLA and similar mods from showing their info
 * */
@Mixin(EntityRenderer.class)
public class RiftMixinIgnoreRiddenEntityTarget {
    @Shadow
    private Minecraft mc;

    @Inject(method = "getMouseOver", at = @At("RETURN"))
    private void ignoreRiddenEntityTarget(float partialTicks, CallbackInfo ci) {
        if (this.mc.player == null || this.mc.objectMouseOver == null || this.mc.objectMouseOver.typeOfHit != RayTraceResult.Type.ENTITY) return;

        Entity targetedEntity = this.mc.objectMouseOver.entityHit;
        if (targetedEntity == null || targetedEntity.getLowestRidingEntity() != this.mc.player.getLowestRidingEntity()) return;

        this.mc.pointedEntity = null;
        this.mc.objectMouseOver = new RayTraceResult(
                RayTraceResult.Type.MISS,
                this.mc.objectMouseOver.hitVec,
                null, new BlockPos(this.mc.objectMouseOver.hitVec)
        );
    }
}
