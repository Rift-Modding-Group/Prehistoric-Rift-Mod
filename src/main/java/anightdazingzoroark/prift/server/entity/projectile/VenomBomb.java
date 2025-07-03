package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class VenomBomb extends RiftLibProjectile {
    private static final DataParameter<Boolean> COUNTING_DOWN = EntityDataManager.createKey(VenomBomb.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COUNTDOWN = EntityDataManager.createKey(VenomBomb.class, DataSerializers.VARINT);
    private EntityPlayer rider;
    private RiftCreature shooter;

    public VenomBomb(World worldIn) {
        super(worldIn);
    }

    public VenomBomb(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x, y, z);
    }

    public VenomBomb(World world, RiftCreature shooter) {
        this(world, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1, shooter.posZ);
        this.shootingEntity = shooter;
        this.shooter = shooter;
    }

    public VenomBomb(World world, RiftCreature shooter, EntityPlayer rider) {
        this(world, rider.posX, rider.posY + rider.getEyeHeight() - 0.1, rider.posZ);
        this.shootingEntity = shooter;
        this.shooter = shooter;
        this.rider = rider;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(COUNTING_DOWN, false);
        this.dataManager.register(COUNTDOWN, 100);
    }
    public void onUpdate() {
        super.onUpdate();

        //count down until explode
        if (!this.world.isRemote) {
            if (this.isCountingDown()) {
                this.setCountdown(this.getCountDown() - 1);
                if (this.getCountDown() <= 0) {
                    this.world.createExplosion(this, this.posX, this.posY, this.posZ, 2f, false);
                    this.setDead();
                }
            }
        }
    }

    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {
        this.setIsCountingDown(true);
    }

    @Override
    public double getDamage() {
        return 0;
    }

    @Override
    public boolean canRotateVertically() {
        return false;
    }

    @Override
    public boolean canSelfDestroyUponHit() {
        return false;
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

    public RiftCreature getShooter() {
        return this.shooter;
    }

    public boolean isCountingDown() {
        return this.dataManager.get(COUNTING_DOWN);
    }

    public void setIsCountingDown(boolean value) {
        this.dataManager.set(COUNTING_DOWN, value);
    }

    public int getCountDown() {
        return this.dataManager.get(COUNTDOWN);
    }

    public void setCountdown(int value) {
        this.dataManager.set(COUNTDOWN, value);
    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return SoundEvents.BLOCK_SLIME_HIT;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "default", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.venom_bomb.default", true));
                return PlayState.CONTINUE;
            }
        }));
    }
}
