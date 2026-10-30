package anightdazingzoroark.prift.server.item;

import anightdazingzoroark.prift.api.projectile.IProjectile;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.projectile.ProjectileTrail;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.prift.util.RiftUtil;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RiftStinkBomb extends RiftThrowableItem {
    public RiftStinkBomb() {
        super();
        this.setMaxStackSize(16);
    }

    @Override
    @NotNull
    public ProjectileBuilder getProjectileBuilder() {
        return new ProjectileBuilder().setName("stink_bomb")
                .setProjectileModel(ProjectileBuilder.ProjectileModel.CUBE)
                .setProjectileTrail(ProjectileTrail.makeBuilderForParticleTrail()
                        .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0xC4A484, false))
                        .addParticleComponent(ProjectileTrail.DUST_APPEARANCE)
                )
                .registerBooleanValue("HasHitHitbox", false)
                .setOnImpactFromPlayerEffect((player, projectile, hitEntity, hitPos) -> {
                    this.onImpactEffect(projectile, hitPos);
                })
                .setOnImpactForDispenserEffect((projectile, hitEntity, hitPos) -> {
                    this.onImpactEffect(projectile, hitPos);
                })
                .setImpactSoundEvent(RiftSounds.getSound("stink_bomb.impact"));
    }

    private void onImpactEffect(@NotNull IProjectile projectile, @NotNull Vec3d hitPos) {
        //make particle
        RiftLibParticleHelper.createParticle(
                projectile.getEntityWorld(),
                new ParticleBuilder().setIdentifier("prift:bomb_impact")
                        .setTexture(new ResourceLocation("textures/particle/particles.png"))
                        .setMaterial(ParticleMaterial.ALPHA)
                        .addEmitterComponent(new EmitterInstantComponent.Builder().setParticleCount(48))
                        .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder())
                        .addEmitterComponent(new EmitterShapeSphereComponent.Builder().setRadius(0.15D).setDirection("outwards"))
                        .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(1D))
                        .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(4D))
                        .addParticleComponent(new ParticleMotionDynamicComponent.Builder())
                        .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                .setSize(0.2D, 0.2D).setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                .setTextureWidth(256).setTextureHeight(256)
                                .setFlipbook(true).setUv(112, 0).setUvSize(16, 16).setUvStepSize(-16, 0)
                                .setFramesPerSecond(10).setMaxFrame(8)
                        )
                        .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0xC4A484, false)),
                hitPos.x, hitPos.y, hitPos.z
        );

        //3x3 aoe
        AxisAlignedBB affectedRange = new AxisAlignedBB(
                hitPos.x - 1.5D, hitPos.y - 1.5D, hitPos.z - 1.5D,
                hitPos.x + 1.5D, hitPos.y + 1.5D, hitPos.z + 1.5D
        );
        List<Entity> affectedEntities = projectile.getEntityWorld().getEntitiesWithinAABB(Entity.class, affectedRange);
        for (Entity entity : affectedEntities) {
            if (entity == null) continue;
            this.onHitEntity(entity, projectile, false);
        }
    }

    private void onHitEntity(@NotNull Entity entity, @NotNull IProjectile projectile, boolean fromHitbox) {
        boolean canHitFromHitbox = !fromHitbox || !(boolean) projectile.getProperty("HasHitHitbox").getValue();

        if (entity instanceof MultiPartEntityPart entityPart) {
            this.onHitEntity((Entity) entityPart.parent, projectile, true);
        }
        else if (entity instanceof EntityLiving entityLiving && (!(entity instanceof EntityTameable tameable) || !tameable.isTamed())
                && !RiftUtil.entityInTargetGroup(entity, "human") && canHitFromHitbox
        ) {
            OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(entityLiving);
            if (otherEntityProperties != null) {
                otherEntityProperties.setStinkBombed(true);
                entityLiving.setAttackTarget(null);
                entityLiving.setRevengeTarget(null);
                entityLiving.getNavigator().clearPath();
            }
        }

        //extra lock
        if (fromHitbox && !(boolean) projectile.getProperty("HasHitHitbox").getValue()) projectile.setProperty("HasHitHitbox", true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag tooltipFlag) {
        tooltip.add(TextFormatting.GRAY + I18n.format("item.stink_bomb.tooltip"));
    }
}
