package com.anightdazingzoroark.rift.mixin;

import com.anightdazingzoroark.rift.InitializeClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.util.Session;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SplashTextResourceSupplier.class)
public class Splashes {
    @Shadow @Final @Mutable private static Identifier RESOURCE_ID;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void changeSplashText(Session session, CallbackInfo ci) {
        RESOURCE_ID = new Identifier(InitializeClient.MOD_ID, "texts/splashes.txt");
    }
}