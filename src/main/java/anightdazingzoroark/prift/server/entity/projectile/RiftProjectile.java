package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.projectile.IProjectile;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.entity.RiftDamage;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.riftlib.core.manager.AnimationDataProjectile;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class RiftProjectile extends RiftLibProjectile implements IProjectile {
    private static final DataParameter<String> NAME = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.STRING);
    private static final DataParameter<Byte> MODEL = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.BYTE);
    private static final DataParameter<NBTTagCompound> PARTICLE_TRAIL = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.COMPOUND_TAG);
    private static final DataParameter<Float> SCALE = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> VERTICAL_ROTATE = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DESTROY_ON_HIT = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> STRAIGHT_FLIGHT = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.BOOLEAN);

    @Nullable
    private AnimationDataProjectile data;
    @NotNull
    private final ProjectileBuilder projectileBuilder;
    @NotNull
    private final CreatureMoveBuilder creatureMoveBuilder;
    @Nullable
    private Function<EntityPlayer, Double> damageByPlayer;
    @Nullable
    private final ProjectileSourceType projectileSource;
    //--custom property values, which can be called and manipulated from a projectile builder--
    @NotNull
    private Map<String, AbstractPropertyValue<?>> propertyValueMap = Map.of();

    public RiftProjectile(World worldIn) {
        super(worldIn);
        this.projectileBuilder = new ProjectileBuilder();
        this.creatureMoveBuilder = new CreatureMoveBuilder();
        this.projectileSource = null;
    }

    //---reserved for projectiles by dispensers---
    public RiftProjectile(World worldIn, double x, double y, double z, @NotNull ProjectileBuilder builder) {
        super(worldIn, x, y, z);
        this.projectileBuilder = builder;
        this.creatureMoveBuilder = new CreatureMoveBuilder();
        this.setFromBuilder(builder);
        this.projectileSource = ProjectileSourceType.DISPENSER;
    }

    //---reserved for projectiles by players---
    public RiftProjectile(@NotNull EntityPlayer shooter, @NotNull ProjectileBuilder builder, @NotNull Function<EntityPlayer, Double> damageByPlayer) {
        super(shooter.world, shooter);
        this.projectileBuilder = builder;
        this.creatureMoveBuilder = new CreatureMoveBuilder();
        this.setPosition(shooter.posX, shooter.posY + shooter.height / 2D, shooter.posZ);
        this.setFromBuilder(builder);
        this.projectileSource = ProjectileSourceType.PLAYER;
        this.damageByPlayer = damageByPlayer;
    }

    //---reserved for projectiles by riftcreatures---
    public RiftProjectile(@NotNull RiftCreature shooter, @NotNull ProjectileBuilder builder, @NotNull CreatureMoveBuilder creatureMoveBuilder) {
        super(shooter.world, shooter);
        this.projectileBuilder = builder;
        this.creatureMoveBuilder = creatureMoveBuilder;
        this.setPosition(shooter.posX, shooter.posY + shooter.height / 2D, shooter.posZ);
        this.setFromBuilder(builder);
        this.projectileSource = ProjectileSourceType.CREATURE;
    }

    private void setFromBuilder(@NotNull ProjectileBuilder builder) {
        this.dataManager.set(NAME, builder.getName());
        this.dataManager.set(MODEL, (byte) builder.getProjectileModel().ordinal());
        this.dataManager.set(SCALE, builder.getScale());
        this.dataManager.set(VERTICAL_ROTATE, builder.getRotateAlongPitch());
        this.dataManager.set(DESTROY_ON_HIT, !builder.getStayAfterImpact());
        this.dataManager.set(STRAIGHT_FLIGHT, builder.getStraightFlight());
        this.setNoGravity(builder.getStraightFlight());
        if (builder.getProjectileTrail() != null) this.dataManager.set(PARTICLE_TRAIL, ParticleBuilder.toNBT(builder.getProjectileTrail()));
        if (builder.getPropertyValueMap() != null) this.propertyValueMap = Map.copyOf(builder.getPropertyValueMap());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(NAME, "");
        this.dataManager.register(MODEL, (byte) 2);
        this.dataManager.register(PARTICLE_TRAIL, new NBTTagCompound());
        this.dataManager.register(SCALE, 1f);
        this.dataManager.register(VERTICAL_ROTATE, false);
        this.dataManager.register(DESTROY_ON_HIT, false);
        this.dataManager.register(STRAIGHT_FLIGHT, false);
    }

    @Override
    public void onUpdate() {
        Vec3d launchMotion = this.getMotionVector();
        super.onUpdate();

        if (this.dataManager.get(STRAIGHT_FLIGHT) && !this.inGround && !this.isDead) {
            this.motionX = launchMotion.x;
            this.motionY = launchMotion.y;
            this.motionZ = launchMotion.z;
        }

        if (this.world.isRemote) return;
        if (this.projectileBuilder.getUpdateEffect() != null) this.projectileBuilder.getUpdateEffect().accept(this);
    }

    //-----mostly to ensure that projectile disappears when reloading world-----
    @Override
    public boolean writeToNBTOptional(@NotNull NBTTagCompound compound) {
        return false;
    }

    @Override
    public boolean writeToNBTAtomically(@NotNull NBTTagCompound compound) {
        return false;
    }

    //-----special client-friendly getters-----
    @NotNull
    public String getName() {
        return this.dataManager.get(NAME);
    }

    public ProjectileBuilder.ProjectileModel getProjectileModel() {
        return ProjectileBuilder.ProjectileModel.values()[this.dataManager.get(MODEL)];
    }

    public float getProjectileScale() {
        return this.dataManager.get(SCALE);
    }

    public NBTTagCompound getParticleTrailNBT() {
        return this.dataManager.get(PARTICLE_TRAIL);
    }

    //-----properties management-----
    @SuppressWarnings("unchecked")
    public <I extends AbstractPropertyValue<?>> I getProperty(@NotNull String key) {
        if (!this.propertyValueMap.containsKey(key)) {
            throw new UnsupportedOperationException("Key " + key + " does not exist in property map for projectile " + this.getName() + "!");
        }
        return (I) this.propertyValueMap.get(key);
    }

    public <I> void setProperty(@NotNull String key, I value) {
        AbstractPropertyValue<I> propertyValue = this.getProperty(key);
        if (propertyValue.getHeldClass() != value.getClass()) {
            throw new UnsupportedOperationException("Key " + key + " does not represent given value " + value + "!");
        }
        propertyValue.setValue(value);
        //todo: make this able to sync to client as well
    }

    //-----from RiftLibProjectile-----
    @Override
    public void projectileImpactEffects(@Nullable EntityLivingBase hitEntity, @NotNull Vec3d hitPos) {
        //other effects
        if (this.projectileSource == ProjectileSourceType.CREATURE && this.projectileBuilder.getOnImpactFromCreatureEffect() != null) {
            this.projectileBuilder.getOnImpactFromCreatureEffect().accept(this.getCreatureShooter(), this, hitEntity, hitPos);
        }
        else if (this.projectileSource == ProjectileSourceType.PLAYER && this.projectileBuilder.getOnImpactFromPlayerEffect() != null) {
            this.projectileBuilder.getOnImpactFromPlayerEffect().accept(this.getPlayerShooter(), this, hitEntity, hitPos);
        }
        else if (this.projectileSource == ProjectileSourceType.DISPENSER && this.projectileBuilder.getOnImpactFromDispenserEffect() != null) {
            this.projectileBuilder.getOnImpactFromDispenserEffect().accept(this, hitEntity, hitPos);
        }
    }

    @Override
    @NotNull
    public DamageSource getDamageSource() {
        return RiftDamage.getElementDamageSource(
                "arrow", this, this.shootingEntity == null ? this : this.shootingEntity,
                this.creatureMoveBuilder.getElement()
        ).setProjectile();
    }

    @Override
    public double getDamage() {
        if (this.projectileSource == ProjectileSourceType.PLAYER && this.damageByPlayer != null) {
            return this.damageByPlayer.apply(this.getPlayerShooter());
        }
        else if (this.projectileSource == ProjectileSourceType.CREATURE && this.shootingEntity instanceof RiftCreature shooter) {
            return CreatureMoveHelper.calculateDamage(shooter, null, this.creatureMoveBuilder);
        }
        return 0;
    }

    @Override
    public double getDamageMultiplierFromVelocity() {
        return 0D;
    }

    @Override
    public boolean canSelfDestroyUponHit() {
        return this.dataManager.get(DESTROY_ON_HIT);
    }

    @Override
    public boolean canRotateVertically() {
        return this.dataManager.get(VERTICAL_ROTATE);
    }

    @Override
    public void initializeAnimationData(@NotNull AnimationDataProjectile animData) {
        animData.setScale(this.getProjectileScale());

        NBTTagCompound particleTrailNBT = this.getParticleTrailNBT();
        if (!particleTrailNBT.isEmpty()) {
            animData.addParticleEffect(ParticleBuilder.fromNBT(particleTrailNBT), "center");
        }
    }

    @Override
    @NotNull
    public AnimationDataProjectile getAnimationData() {
        if (this.data == null) this.data = new AnimationDataProjectile(this);
        return this.data;
    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return this.projectileBuilder.getImpactSoundEvent();
    }

    //-----other IProjectile stuff-----
    @Override
    public Random getRNG() {
        return this.world.rand;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    @Nullable
    public ICreature getCreatureShooter() {
        return (ICreature) this.shootingEntity;
    }

    @Nullable
    public EntityPlayer getPlayerShooter() {
        return (EntityPlayer) this.shootingEntity;
    }

    @NotNull
    @Override
    public Vec3d getMotionVector() {
        return new Vec3d(this.motionX, this.motionY, this.motionZ);
    }

    private enum ProjectileSourceType {
        CREATURE,
        PLAYER,
        DISPENSER
    }
}
