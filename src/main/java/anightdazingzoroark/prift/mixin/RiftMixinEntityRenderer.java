package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.api.RiftedValkyriumAPI;

@Mixin(EntityRenderer.class)
public class RiftMixinEntityRenderer {
    @Shadow
    private Minecraft mc;

    @Nullable
    @WrapOperation(
            method = "getMouseOver",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;rayTrace(DF)Lnet/minecraft/util/math/RayTraceResult;")
    )
    private RayTraceResult ignoreRiddenPlatformBlocks(
            @NotNull Entity viewer, double reach, float partialTicks, @NotNull Operation<RayTraceResult> original
    ) {
        if (viewer.getRidingEntity() instanceof RiftCreature creature
                && creature.supportsPlatformSaddle() && creature.isPlatformEquipped()
                && Loader.isModLoaded("valkyrienskies")
        ) {
            Vec3d start = viewer.getPositionEyes(partialTicks);
            Vec3d end = start.add(viewer.getLook(partialTicks).scale(reach));
            return RiftedValkyriumAPI.rayTraceBlocksIgnoringCarrierShip(viewer.world, creature, start, end, false, false, true);
        }
        return original.call(viewer, reach, partialTicks);
    }

    /**
     * Exists to make sure that when riding a creature, it will never be
     * hit, its to prevent HWYLA and similar mods from showing their info
     * */
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
