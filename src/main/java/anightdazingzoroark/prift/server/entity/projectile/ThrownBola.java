package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import anightdazingzoroark.prift.server.entity.interfaces.IRiftProjectile;
import anightdazingzoroark.prift.server.enums.MobSize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ThrownBola extends EntityArrow implements IRiftProjectile, IProjectile {
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

    protected void onHit(RayTraceResult raytraceResultIn) {
        if (!this.world.isRemote) {
            Entity entity = raytraceResultIn.entityHit;
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                entityLivingBase.attackEntityFrom(DamageSource.causeThrownDamage(this, this.shootingEntity), 0f);
                if (RiftUtil.isAppropriateSize(entityLivingBase, MobSize.MEDIUM)) {
                    NonPotionEffectsHelper.setBolaCaptured(entityLivingBase, 300);
                }
            }
            else if (entity instanceof RiftCreaturePart) {
                RiftCreature parent = ((RiftCreaturePart)entity).getParent();
                parent.attackEntityFrom(DamageSource.causeThrownDamage(this, this.shootingEntity), 0f);
                if (RiftUtil.isAppropriateSize(parent, MobSize.MEDIUM)) {
                    NonPotionEffectsHelper.setBolaCaptured(parent, 300);
                }
            }
            this.setDead();
        }
        else super.onHit(raytraceResultIn);
    }

    @Override
    public ItemStack getItemToRender() {
        return new ItemStack(RiftProjectileRegistry.THROWN_BOLA);
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }
}
