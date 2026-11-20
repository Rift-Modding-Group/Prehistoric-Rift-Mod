package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.server.entity.creature.CreaturePlatformUtils;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public class RiftMixinPlatformDismount {
    @Inject(method = "dismountEntity", at = @At("HEAD"), cancellable = true)
    private void placeOnPlatform(@NotNull Entity mount, @NotNull CallbackInfo callbackInfo) {
        @SuppressWarnings("DataFlowIssue")
        EntityLivingBase rider = (EntityLivingBase) (Object) this;
        if (rider.world.isRemote || !Loader.isModLoaded("valkyrienskies")) return;
        if (mount instanceof RiftCreature creature && creature.supportsPlatformSaddle()
                && creature.isPlatformEquipped() && CreaturePlatformUtils.dismountOntoPlatform(creature, rider)
        ) {
            callbackInfo.cancel();
        }
    }
}
