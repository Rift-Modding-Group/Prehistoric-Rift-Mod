package anightdazingzoroark.prift.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = EntityLivingBase.class)
public class RiftMixinSetTargetAfterHitboxHit {
    @Overwrite
    public void setLastAttackedEntity(Entity entity) {
        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase)(Object)this).lastAttackedEntity = (EntityLivingBase)entity;
        }
        else if (entity instanceof MultiPartEntityPart) {
            MultiPartEntityPart entityPart = (MultiPartEntityPart) entity;

            if (entityPart.parent instanceof EntityLivingBase) {
                ((EntityLivingBase)(Object)this).lastAttackedEntity = (EntityLivingBase) entityPart.parent;
            }
        }
        else ((EntityLivingBase)(Object)this).lastAttackedEntity = null;

        ((EntityLivingBase)(Object)this).lastAttackedEntityTime = ((EntityLivingBase)(Object)this).ticksExisted;
    }
}
