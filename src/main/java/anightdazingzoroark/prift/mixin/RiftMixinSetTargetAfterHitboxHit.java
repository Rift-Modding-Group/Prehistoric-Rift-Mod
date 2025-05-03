package anightdazingzoroark.prift.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public class RiftMixinSetTargetAfterHitboxHit {
    @Inject(method = "setLastAttackedEntity", at = @At(value = "HEAD"), cancellable = true)
    public void setLastAttackedEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof MultiPartEntityPart) {
            MultiPartEntityPart entityPart = (MultiPartEntityPart) entity;
            if (entityPart.parent instanceof EntityLivingBase) {
                ((EntityLivingBase) (Object)this).lastAttackedEntity = (EntityLivingBase) entityPart.parent;
            }
            ((EntityLivingBase) (Object)this).lastAttackedEntityTime = ((EntityLivingBase) (Object)this).ticksExisted;
            ci.cancel();
        }
    }
}
