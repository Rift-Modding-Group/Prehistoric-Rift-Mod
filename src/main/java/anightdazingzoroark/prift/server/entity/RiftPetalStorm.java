package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.builders.RootSpikeBuilder;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityNonLiving;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeCustomComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RiftPetalStorm extends Entity implements IAnimatable<AnimationDataEntityNonLiving>, IEntityAdditionalSpawnData {
    public static final int EMISSION_TICKS = 24;
    public static final int LIFETIME = EMISSION_TICKS + 12;

    @Nullable
    private RiftCreature creator;
    private float damage;
    @NotNull
    private Element element = Element.GRASS;
    @NotNull
    private final Set<UUID> hitEntities = new HashSet<>();
    @NotNull
    private final AnimationDataEntityNonLiving animationData = new AnimationDataEntityNonLiving(this);

    public RiftPetalStorm(@NotNull World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.setNoGravity(true);
    }

    public RiftPetalStorm(@NotNull RiftCreature creator, @NotNull CreatureMoveBuilder move, @NotNull Vec3d floor) {
        this(creator.world);
        this.creator = creator;
        this.damage = (float) CreatureMoveHelper.calculateDamage(creator, null, move);
        this.element = move.getElement();
        this.setPosition(floor.x, floor.y, floor.z);
    }

    public static void spawn(@NotNull ICreature creature) {
        if (!(creature.getAnimationData().getHolder() instanceof RiftCreature creator) || creator.world.isRemote) return;
        CreatureMoveBuilder move = creator.getCreatureMoves().getMoveBuilderCurrentMove();
        if (move == null) return;

        RayTraceResult hit = creature.getCrosshairTargets();
        EntityLivingBase target = null;
        if (creature.getControllingPassenger() == null) target = creature.getAttackTarget();
        else if (hit != null && hit.entityHit instanceof EntityLivingBase living) target = living;
        Vec3d floor = RootSpikeBuilder.getStrikeFloor(creature, target);
        if (floor == null || !RootSpikeBuilder.isWithinRange(creature, floor)) return;

        creator.world.spawnEntity(new RiftPetalStorm(creator, move, floor));
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.world.isRemote) return;
        if (this.creator == null || this.ticksExisted >= LIFETIME) {
            this.setDead();
            return;
        }
        if (this.ticksExisted == 1) {
            RiftLibSoundHelper.playLoopingSound(this, RiftSounds.getSound("petal_storm.use_loop"), SoundCategory.NEUTRAL);
        }

        AxisAlignedBB damageBounds = new AxisAlignedBB(this.posX - 3D, this.posY, this.posZ - 3D,
                this.posX + 3D, this.posY + 16D, this.posZ + 3D);
        for (EntityLivingBase living : this.world.getEntitiesWithinAABB(EntityLivingBase.class, damageBounds,
                living -> living != null && living.isEntityAlive() && EntitySelectors.NOT_SPECTATING.apply(living)
                        && living != this.creator && living != this.creator.getControllingPassenger()
                        && !this.creator.isRelatedToEntity(living))) {
            if (!this.hitEntities.add(living.getUniqueID())) continue;
            living.attackEntityFrom(RiftDamage.getElementDamageSource("mob", this, this.creator, this.element), this.damage);
        }
    }

    @Override
    public void setDead() {
        if (this.isDead) return;
        RiftLibSoundHelper.stopLoopingSound(this, RiftSounds.getSound("petal_storm.use_loop"), SoundCategory.NEUTRAL);
        super.setDead();
    }

    @Override
    public void writeSpawnData(@NotNull ByteBuf buffer) {
        buffer.writeInt(this.ticksExisted);
    }

    @Override
    public void readSpawnData(@NotNull ByteBuf buffer) {
        this.ticksExisted = buffer.readInt();
    }

    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        this.setDead();
    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound compound) {
    }

    @Override
    public boolean writeToNBTOptional(@NotNull NBTTagCompound compound) {
        return false;
    }

    @Override
    public boolean writeToNBTAtomically(@NotNull NBTTagCompound compound) {
        return false;
    }

    @Override
    @NotNull
    public AnimationDataEntityNonLiving getAnimationData() {
        return this.animationData;
    }

    @Override
    public void initializeAnimationData(@NotNull AnimationDataEntityNonLiving animationData) {
        animationData.addOnUpdateAnimationValue("variable.storm_emitting",
                () -> this.isDead || this.ticksExisted >= EMISSION_TICKS ? 0D : 1D);
        animationData.addParticleEffect(
                new ParticleBuilder().setIdentifier("prift:petal_storm")
                        .setTexture(new ResourceLocation("prift", "textures/particles/petal_storm.png"))
                        .setMaterial(ParticleMaterial.BLEND)
                        .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                .setSpawnRate(960D).setMaxParticleCount(7680D))
                        .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                .setActivationExpression("query.actor_owner->variable.storm_emitting")
                                .setExpirationExpression("query.actor_owner->variable.storm_emitting == 0"))
                        .addEmitterComponent(new EmitterShapeCustomComponent.Builder()
                                .setOffset(RawMolangValue.compileExpression(
                                                "variable.storm_angle = math.random(0, 360); "
                                                        + "variable.storm_rise_speed = math.random(20, 26.666666666666668); "
                                                        + "variable.storm_next_radius = 0.4 + variable.storm_rise_speed * 0.008125; "
                                                        + "return math.cos(variable.storm_angle) * 0.4;"),
                                        new Constant(0D),
                                        RawMolangValue.compileExpression("math.sin(variable.storm_angle) * 0.4"))
                                .setDirection(RawMolangValue.compileExpression(
                                                "variable.storm_velocity_x = (variable.storm_next_radius * math.cos(variable.storm_angle + 56) - 0.4 * math.cos(variable.storm_angle)) * 20; "
                                                        + "return variable.storm_velocity_x;"),
                                        RawMolangValue.compileExpression("variable.storm_rise_speed"),
                                        RawMolangValue.compileExpression(
                                                "variable.storm_velocity_z = (variable.storm_next_radius * math.sin(variable.storm_angle + 56) - 0.4 * math.sin(variable.storm_angle)) * 20; "
                                                        + "return variable.storm_velocity_z;")))
                        .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder().setMaxLifetime(0.6D))
                        .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(
                                "variable.storm_birth_angle = variable.storm_angle; "
                                        + "variable.storm_vertical_speed = variable.storm_rise_speed; "
                                        + "return math.sqrt(variable.storm_velocity_x * variable.storm_velocity_x + variable.storm_rise_speed * variable.storm_rise_speed + variable.storm_velocity_z * variable.storm_velocity_z);"))
                        .addParticleComponent(new ParticleMotionDynamicComponent.Builder().setLinearAcceleration(
                                RawMolangValue.compileExpression(
                                        "variable.storm_orbit_angle = variable.storm_birth_angle + variable.particle_age * 1120; "
                                                + "variable.storm_orbit_radius = 0.4 + variable.storm_vertical_speed * variable.particle_age * 0.1625; "
                                                + "variable.storm_radius_step = variable.storm_vertical_speed * 0.008125; "
                                                + "return ((variable.storm_orbit_radius + variable.storm_radius_step) * math.cos(variable.storm_orbit_angle + 56) "
                                                + "- 2 * variable.storm_orbit_radius * math.cos(variable.storm_orbit_angle) "
                                                + "+ (variable.storm_orbit_radius - variable.storm_radius_step) * math.cos(variable.storm_orbit_angle - 56)) * 400;"),
                                new Constant(0D),
                                RawMolangValue.compileExpression(
                                        "((variable.storm_orbit_radius + variable.storm_radius_step) * math.sin(variable.storm_orbit_angle + 56) "
                                                + "- 2 * variable.storm_orbit_radius * math.sin(variable.storm_orbit_angle) "
                                                + "+ (variable.storm_orbit_radius - variable.storm_radius_step) * math.sin(variable.storm_orbit_angle - 56)) * 400")))
                        .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                .setSize(
                                        RawMolangValue.compileExpression("0.15 * math.pow(math.max(0, 1 - math.max(0, variable.particle_age - 0.5) / 0.1), 0.75)"),
                                        RawMolangValue.compileExpression("0.15 * math.pow(math.max(0, 1 - math.max(0, variable.particle_age - 0.5) / 0.1), 0.75)")
                                )
                                .setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                .setTextureWidth(16).setTextureHeight(8)
                                .setUv(RawMolangValue.compileExpression("variable.particle_random_1 < 0.5 ? 0 : 8"), RawMolangValue.compileExpression("0"))
                                .setUvSize(8D, 8D)),
                "base"
        );
    }
}
