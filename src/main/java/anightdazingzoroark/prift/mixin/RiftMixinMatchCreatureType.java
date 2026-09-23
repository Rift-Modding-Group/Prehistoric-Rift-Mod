package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityList.class)
public class RiftMixinMatchCreatureType {
    @Inject(method = "isMatchingName", at = @At("HEAD"), cancellable = true)
    private static void matchCreatureType(Entity entity, ResourceLocation entityName, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!(entity instanceof RiftCreature creature)) return;

        ResourceLocation creatureName = new ResourceLocation(RiftInitialize.MODID, creature.getCreatureType().getName());
        if (creatureName.equals(entityName)) callbackInfo.setReturnValue(true);
    }
}
