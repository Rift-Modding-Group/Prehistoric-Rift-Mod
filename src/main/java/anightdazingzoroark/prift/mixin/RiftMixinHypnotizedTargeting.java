package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(EntityLiving.class)
public class RiftMixinHypnotizedTargeting {
    @Inject(method = "setAttackTarget", at = @At(value = "HEAD"), cancellable = true)
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbase, CallbackInfo ci) {
        //perform check involving whether or not the entity has been hypnotized
        //cancel if the hypnotizer has no target
        EntityLiving user = (EntityLiving) ((Object) this);
        if (user instanceof EntityCreature) {
            EntityCreature userCreature = (EntityCreature) user;
            if (NonPotionEffectsHelper.isHypnotized(userCreature) && NonPotionEffectsHelper.targetingBlockableForHypnotized(userCreature)) {
                userCreature.attackTarget = null;
                net.minecraftforge.common.ForgeHooks.onLivingSetAttackTarget(userCreature, null);
                ci.cancel();
            }
        }
    }
}
