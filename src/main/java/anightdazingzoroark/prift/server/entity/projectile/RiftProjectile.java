package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.projectile.IProjectile;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureStatsStorage;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.riftlib.core.manager.AnimationDataProjectile;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RiftProjectile extends RiftLibProjectile implements IProjectile {
    private static final DataParameter<String> NAME = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.STRING);
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
        this.setPosition(shooter.posX, shooter.posY + shooter.height / 2D, shooter.posZ);
        this.dataManager.set(NAME, builder.getName());
    }
    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(NAME, "");
    }

    public void onUpdate() {
        super.onUpdate();

        if (this.world.isRemote) return;
        if (this.projectileBuilder.getUpdateEffect() != null) this.projectileBuilder.getUpdateEffect().accept(this);
    }

    @NotNull
    public String getName() {
        return this.dataManager.get(NAME);
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
    @NotNull
    public DamageSource getDamageSource() {
        DamageSource toReturn = super.getDamageSource();

        //apply element effects from move
        if (this.creatureMoveBuilder.getElement() != null) {
            if (this.creatureMoveBuilder.getElement() == Element.FIRE) toReturn.setFireDamage();
        }

        return toReturn;
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
    public void initializeAnimationData(AnimationDataProjectile animationDataProjectile) {}

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
