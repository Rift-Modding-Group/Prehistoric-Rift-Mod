package anightdazingzoroark.prift.api.projectile;

import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Lots of default projectile trails methods and variables that are free to use
 * */
public class ProjectileTrail {
    public static final AppearanceBillboardComponent.Builder DUST_APPEARANCE = makeAppearanceForParticleTrail()
            .setSize(0.2D, 0.2D).setUv(112, 0).setUvSize(16, 16)
            .setFlipbook(true).setUvStepSize(-16, 0).setMaxFrame(8)
            .setFramesPerSecond(5).setStretchToLifetime(true).copy();

    @NotNull
    public static ParticleBuilder makeBuilderForParticleTrail() {
        return new ParticleBuilder()
                .setTexture(new ResourceLocation("textures/particle/particles.png"))
                .setMaterial(ParticleMaterial.ALPHA)
                .addEmitterComponent(new EmitterSteadyComponent.Builder().setSpawnRate(16).setMaxParticleCount(100))
                .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder().setActivationExpression(1D))
                .addEmitterComponent(new EmitterShapePointComponent.Builder().setDirection(0, -1, 0))
                .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(1D))
                .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(2D))
                .addParticleComponent(new ParticleMotionDynamicComponent.Builder());
    }

    @NotNull
    public static AppearanceBillboardComponent.Builder makeAppearanceForParticleTrail() {
        return new AppearanceBillboardComponent.Builder()
                .setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                .setTextureWidth(256).setTextureHeight(256).copy();
    }
}