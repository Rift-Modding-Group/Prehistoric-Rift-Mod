package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.projectile.IProjectile;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.riftlib.core.manager.AnimationDataProjectile;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RiftProjectile extends RiftLibProjectile implements IProjectile {
    @NotNull
    private final ProjectileBuilder projectileBuilder;
    @NotNull
    private final CreatureMoveBuilder creatureMoveBuilder;

    public RiftProjectile(World worldIn) {
        super(worldIn);
        this.projectileBuilder = new ProjectileBuilder();
        this.creatureMoveBuilder = new CreatureMoveBuilder();
    }

    public RiftProjectile(@NotNull RiftCreature shooter, @NotNull ProjectileBuilder builder, @NotNull CreatureMoveBuilder creatureMoveBuilder) {
        super(shooter.world, shooter);
        this.projectileBuilder = builder;
        this.creatureMoveBuilder = creatureMoveBuilder;
    }

    public void onUpdate() {
        super.onUpdate();

        if (this.world.isRemote) return;
        if (this.projectileBuilder.getUpdateEffect() != null) this.projectileBuilder.getUpdateEffect().accept(this);
    }

    @NotNull
    public ProjectileBuilder getBuilder() {
        return this.projectileBuilder;
    }

    //-----from RiftLibProjectile-----
    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {
        //other effects
        if (this.projectileBuilder.getOnImpactEffect() != null) {
            this.projectileBuilder.getOnImpactEffect().accept(this.getShooter(), this, entityLivingBase);
        }
    }

    @Override
    public double getDamage() {
        return CreatureMoveHelper.calculateDamage((RiftCreature) this.getShooter(), this.creatureMoveBuilder);
    }

    @Override
    public boolean canSelfDestroyUponHit() {
        return !this.projectileBuilder.getStayAfterImpact();
    }

    @Override
    public void initializeAnimationData(AnimationDataProjectile animationDataProjectile) {

    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return null;
    }

    //-----other IProjectile stuff-----
    @Override
    public Random getRNG() {
        return this.world.rand;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    @NotNull
    public ICreature getShooter() {
        return (ICreature) this.shootingEntity;
    }
}
