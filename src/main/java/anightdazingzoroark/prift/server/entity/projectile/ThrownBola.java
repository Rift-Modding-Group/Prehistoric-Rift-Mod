package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.prift.server.entity.interfaces.IRiftProjectile;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ThrownBola extends RiftLibProjectile {
    public ThrownBola(World worldIn) {
        super(worldIn);
    }

    public ThrownBola(World world, EntityPlayer user) {
        this(world);
        this.setPosition(user.posX, user.posY + user.getEyeHeight() - 0.1, user.posZ);
        this.shootingEntity = user;
    }

    public void shoot(Entity shooter, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.shoot((double)f, (double)f1, (double)f2, velocity, inaccuracy);
        this.motionX += shooter.motionX;
        this.motionZ += shooter.motionZ;

        if (!shooter.onGround) this.motionY += shooter.motionY;
    }

    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {
        if (entityLivingBase != null && RiftUtil.isAppropriateSize(entityLivingBase, MobSize.MEDIUM)) {
            NonPotionEffectsHelper.setBolaCaptured(entityLivingBase, 300);
        }
    }

    @Override
    public double getDamage() {
        return 0;
    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return null;
    }

    @Override
    public boolean canRotateVertically() {
        return false;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "rotation", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bola.spinning", true));
                return PlayState.CONTINUE;
            }
        }));
    }
}
