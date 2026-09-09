package anightdazingzoroark.prift.server.entity.projectile;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.projectile.IProjectile;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataProjectile;
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

import java.util.Random;
import java.util.function.Function;

public class RiftProjectile extends RiftLibProjectile implements IProjectile {
    private static final DataParameter<String> NAME = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> USE_CUBE_MODEL = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_PARTICLE_TAIL = EntityDataManager.createKey(RiftProjectile.class, DataSerializers.BOOLEAN);
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
        this.setSpecialGetters(builder);
        this.projectileSource = ProjectileSourceType.DISPENSER;
    }

    //---reserved for projectiles by players---
    public RiftProjectile(@NotNull EntityPlayer shooter, @NotNull ProjectileBuilder builder, @NotNull Function<EntityPlayer, Double> damageByPlayer) {
        super(shooter.world, shooter);
        this.projectileBuilder = builder;
        this.creatureMoveBuilder = new CreatureMoveBuilder();
        this.setPosition(shooter.posX, shooter.posY + shooter.height / 2D, shooter.posZ);
        this.setSpecialGetters(builder);
        this.projectileSource = ProjectileSourceType.PLAYER;
        this.damageByPlayer = damageByPlayer;
    }

    //---reserved for projectiles by riftcreatures---
    public RiftProjectile(@NotNull RiftCreature shooter, @NotNull ProjectileBuilder builder, @NotNull CreatureMoveBuilder creatureMoveBuilder) {
        super(shooter.world, shooter);
        this.projectileBuilder = builder;
        this.creatureMoveBuilder = creatureMoveBuilder;
        this.setPosition(shooter.posX, shooter.posY + shooter.height / 2D, shooter.posZ);
        this.setSpecialGetters(builder);
        this.projectileSource = ProjectileSourceType.CREATURE;
    }

    private void setSpecialGetters(@NotNull ProjectileBuilder builder) {
        this.dataManager.set(NAME, builder.getName());
        this.dataManager.set(USE_CUBE_MODEL, builder.getUseCubeModel());
        this.dataManager.set(HAS_PARTICLE_TAIL, builder.getHasParticleTrail());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(NAME, "");
        this.dataManager.register(USE_CUBE_MODEL, false);
        this.dataManager.register(HAS_PARTICLE_TAIL, false);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

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

    public boolean getUseCubeModel() {
        return this.dataManager.get(USE_CUBE_MODEL);
    }

    public boolean getHasParticleTrail() {
        return this.dataManager.get(HAS_PARTICLE_TAIL);
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
        DamageSource toReturn = super.getDamageSource();

        //apply element effects from move
        if (this.creatureMoveBuilder.getElement() != null) {
            if (this.creatureMoveBuilder.getElement() == Element.FIRE) toReturn.setFireDamage();
        }

        return toReturn;
    }

    @Override
    public double getDamage() {
        if (this.projectileSource == ProjectileSourceType.PLAYER && this.damageByPlayer != null) {
            return this.damageByPlayer.apply(this.getPlayerShooter());
        }
        else if (this.projectileSource == ProjectileSourceType.CREATURE && this.shootingEntity instanceof RiftCreature shooter) {
            return CreatureMoveHelper.calculateDamage(shooter, this.creatureMoveBuilder);
        }
        return 0;
    }

    @Override
    public double getDamageMultiplierFromVelocity() {
        return 0D;
    }

    @Override
    public boolean canSelfDestroyUponHit() {
        return !this.projectileBuilder.getStayAfterImpact();
    }

    @Override
    public boolean canRotateVertically() {
        return this.projectileBuilder.getRotateAlongPitch();
    }

    @Override
    public void initializeAnimationData(@NotNull AnimationDataProjectile animData) {
        if (!this.getHasParticleTrail()) return;
        animData.addAnimationController(new AnimationController<RiftProjectile, AnimationDataProjectile>(this, "particleTrail", "default",
                new AnimationControllerState<AnimationDataProjectile>("default")
                        .addParticleEffect(RiftInitialize.MODID + ":" + this.getName(), "center")
        ));
    }

    @Override
    @NotNull
    public AnimationDataProjectile getAnimationData() {
        if (this.data == null) this.data = new AnimationDataProjectile(this);
        return this.data;
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

    @Nullable
    public ICreature getCreatureShooter() {
        return (ICreature) this.shootingEntity;
    }

    @Nullable
    public EntityPlayer getPlayerShooter() {
        return (EntityPlayer) this.shootingEntity;
    }

    private enum ProjectileSourceType {
        CREATURE,
        PLAYER,
        DISPENSER
    }
}
