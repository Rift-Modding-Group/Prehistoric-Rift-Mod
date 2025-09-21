package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class Mudball extends RiftLibProjectile {
    private EntityPlayer rider;

    public Mudball(World worldIn) {
        super(worldIn);
    }

    public Mudball(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public Mudball(World world, EntityLivingBase shooter) {
        super(world, shooter);
    }

    public Mudball(World world, EntityLivingBase shooter, EntityPlayer rider) {
        super(world, shooter);
        this.rider = rider;
    }

    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {
        if (entityLivingBase != null)
            entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20));
    }

    @Override
    public double getDamage() {
        double levelBasedIncrement = 0;
        if (this.shootingEntity instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) this.shootingEntity;
            levelBasedIncrement = creature.getLevel() / 10D;
        }
        return 4D + levelBasedIncrement;
    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return SoundEvents.BLOCK_SLIME_HIT;
    }

    @Override
    public boolean canRotateVertically() {
        return false;
    }

    @Override
    public void registerControllers(AnimationData animationData) {

    }
}
