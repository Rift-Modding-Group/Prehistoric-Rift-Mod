package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityPolarBear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.monster.EntityPolarBear$AIAttackPlayer")
public class RiftMixinPolarBearAIAttackPlayer {
    //this mixin exists to ensure that polar bears aren't immune to the rage effect
    @Inject(method = "shouldExecute", at = @At(value = "HEAD"), cancellable = true)
    public void shouldExecute(CallbackInfoReturnable<Boolean> cir) {
        EntityCreature thisTaskOwner = ((EntityAITarget) ((Object) this)).taskOwner;
        if (RiftUtil.hasPotionEffect(thisTaskOwner, RiftEffects.RAGE)) cir.setReturnValue(false);
    }
}
