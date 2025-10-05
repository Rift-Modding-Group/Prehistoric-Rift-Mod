package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PowerBlow extends RiftLibProjectile {
    private final float blowStrength;

    public PowerBlow(World worldIn) {
        super(worldIn);
        this.blowStrength = 2f;
    }

    public PowerBlow(World world, RiftCreature shooter, float blowStrength) {
        super(world, shooter);
        this.blowStrength = blowStrength;
    }

    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {
        if (entityLivingBase == null) return;

        Entity knockBackFrom = this.shootingEntity != null ? this.shootingEntity : this;
        double xStartPos = this.shootingEntity != null ? this.shootingEntity.posX : this.posX;
        double zStartPos = this.shootingEntity != null ? this.shootingEntity.posZ : this.posZ;

        //knock back the entity
        double d0 = xStartPos - entityLivingBase.posX;
        double d1 = zStartPos - entityLivingBase.posZ;
        double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
        entityLivingBase.knockBack(knockBackFrom, this.blowStrength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
    }

    @Override
    public double getDamage() {
        return 0;
    }

    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return null;
    }
}
