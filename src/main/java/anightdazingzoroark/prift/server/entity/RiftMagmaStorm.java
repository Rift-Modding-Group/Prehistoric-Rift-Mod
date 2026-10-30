package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityNonLiving;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterInitialState.EmitterInitializationComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterInitialState.EmitterLocalSpaceComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapeCustomComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** A moving, particle-only spell that retains the launching move's damage and creator. */
public class RiftMagmaStorm extends Entity implements IAnimatable<AnimationDataEntityNonLiving>, IEntityAdditionalSpawnData {
    public static final double MAXIMUM_DISTANCE = 16D;
    public static final float HEIGHT = 16f;
    public static final double RADIUS = 3D;
    public static final double SPEED = 0.8D;

    @Nullable
    private RiftCreature creator;
    @Nullable
    private CreatureMoveBuilder move;
    private double damage;
    private double travelledDistance;
    @NotNull
    private final Set<UUID> hitEntities = new HashSet<>();
    @Nullable
    private Vec3d clientTarget;
    private int interpolationTicks;
    @NotNull
    private final AnimationDataEntityNonLiving animationData = new AnimationDataEntityNonLiving(this);

    public RiftMagmaStorm(@NotNull World world) {
        super(world);
        this.setSize((float) (RADIUS * 2D), HEIGHT);
        this.stepHeight = 1f;
        this.isImmuneToFire = true;
    }

    public RiftMagmaStorm(
            @NotNull RiftCreature creator, @NotNull CreatureMoveBuilder move,
            @NotNull Vec3d origin, @NotNull Vec3d direction
    ) {
        this(creator.world);
        this.creator = creator;
        this.move = move;
        this.damage = CreatureMoveHelper.calculateDamage(creator, move);
        this.setPosition(origin.x, origin.y, origin.z);
        Vec3d velocity = new Vec3d(direction.x, 0D, direction.z).normalize().scale(SPEED);
        this.motionX = velocity.x;
        this.motionZ = velocity.z;
    }

    public static void spawnPair(@NotNull ICreature creature) {
        if (!(creature.getAnimationData().getHolder() instanceof RiftCreature creator) || creator.world.isRemote) return;
        CreatureMoveBuilder move = creator.getCreatureMoves().getMoveBuilderCurrentMove();
        if (move == null) return;

        Vec3d origin = creator.getPositionVector();
        double yaw = Math.toRadians(creator.rotationYaw);
        Vec3d direction = new Vec3d(-Math.sin(yaw), 0D, Math.cos(yaw));
        EntityLivingBase target = creature.getAttackTarget();
        if (creature.getControllingPassenger() == null && target != null && target.isEntityAlive()) {
            Vec3d offset = target.getPositionVector().subtract(origin);
            if (offset.x * offset.x + offset.z * offset.z > 1E-8D) {
                direction = new Vec3d(offset.x, 0D, offset.z).normalize();
            }
        }

        AxisAlignedBB creatorBounds = creator.getEntityBoundingBox();
        Entity[] parts = creator.getParts();
        if (parts != null) {
            for (Entity part : parts) creatorBounds = creatorBounds.union(part.getEntityBoundingBox());
        }
        double forwardClearance = creatorBounds.getCenter().subtract(origin).dotProduct(direction)
                + (creatorBounds.maxX - creatorBounds.minX) * 0.5D * Math.abs(direction.x)
                + (creatorBounds.maxZ - creatorBounds.minZ) * 0.5D * Math.abs(direction.z)
                + RADIUS * (Math.abs(direction.x) + Math.abs(direction.z)) + 1.5D;
        origin = origin.add(direction.scale(forwardClearance));
        Vec3d sideways = new Vec3d(direction.z, 0D, -direction.x).scale(RADIUS + 0.25D);
        creator.world.spawnEntity(new RiftMagmaStorm(creator, move, origin.subtract(sideways), direction));
        creator.world.spawnEntity(new RiftMagmaStorm(creator, move, origin.add(sideways), direction));
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.isDead) return;
        if (this.world.isRemote) {
            double nextX = this.posX + this.motionX;
            double nextY = this.posY + this.motionY;
            double nextZ = this.posZ + this.motionZ;
            if (this.clientTarget != null && this.interpolationTicks > 0) {
                // Carry the correction target forward instead of slowing toward a stationary packet position.
                this.clientTarget = this.clientTarget.add(this.motionX, this.motionY, this.motionZ);
                this.setPosition(
                        MathUtil.slopeResult(1D, true, 0D, this.interpolationTicks, nextX, this.clientTarget.x),
                        MathUtil.slopeResult(1D, true, 0D, this.interpolationTicks, nextY, this.clientTarget.y),
                        MathUtil.slopeResult(1D, true, 0D, this.interpolationTicks, nextZ, this.clientTarget.z)
                );
                this.interpolationTicks--;
            }
            else this.setPosition(nextX, nextY, nextZ);
            return;
        }
        if (this.creator == null || this.move == null || this.travelledDistance >= MAXIMUM_DISTANCE || this.ticksExisted > 80) {
            this.setDead();
            return;
        }

        Vec3d velocity = new Vec3d(this.motionX, 0D, this.motionZ);
        double speed = Math.sqrt(velocity.lengthSquared());
        if (speed <= 1E-8D) {
            this.setDead();
            return;
        }
        Vec3d movement = velocity.scale(Math.min(speed, MAXIMUM_DISTANCE - this.travelledDistance) / speed);
        if (!this.hasNoGravity()) this.motionY -= 0.08D;
        AxisAlignedBB bounds = this.getEntityBoundingBox();
        AxisAlignedBB sweep = bounds.expand(movement.x, this.motionY, movement.z).expand(0D, this.stepHeight, 0D);
        List<AxisAlignedBB> collisions = new ArrayList<>();
        List<AxisAlignedBB> barriers = new ArrayList<>();
        for (BlockPos position : BlockPos.getAllInBoxMutable(new BlockPos(sweep.minX, sweep.minY - 1D, sweep.minZ),
                new BlockPos(Math.ceil(sweep.maxX) - 1D, Math.ceil(sweep.maxY) - 1D, Math.ceil(sweep.maxZ) - 1D))) {
            IBlockState state = this.world.getBlockState(position);
            List<AxisAlignedBB> blockCollisions = new ArrayList<>();
            state.addCollisionBoxToList(this.world, position, sweep, blockCollisions, this, false);
            collisions.addAll(blockCollisions);
            if (!isFlammable(this.world, position, state)) barriers.addAll(blockCollisions);
        }
        if (barriers.stream().anyMatch(bounds::intersects)) {
            this.setDead();
            return;
        }

        // Floors and ceilings constrain gravity, including flammable surfaces such as wooden floors.
        double vertical = this.motionY;
        for (AxisAlignedBB collision : collisions) vertical = collision.calculateYOffset(bounds, vertical);
        boolean grounded = this.motionY < 0D && vertical != this.motionY;
        AxisAlignedBB horizontalStart = bounds.offset(0D, vertical, 0D);
        double progress = 1D;
        boolean blocked = false;
        for (AxisAlignedBB barrier : barriers) {
            double contact = contactProgress(horizontalStart, barrier, movement);
            if (contact <= progress) {
                progress = contact;
                blocked = true;
            }
        }

        double stepUp = 0D;
        double stepDown = 0D;
        if (blocked && (this.onGround || grounded)) {
            double clearance = this.stepHeight;
            for (AxisAlignedBB collision : collisions) clearance = collision.calculateYOffset(horizontalStart, clearance);
            AxisAlignedBB stepped = horizontalStart.offset(0D, clearance, 0D);
            double stepProgress = 1D;
            boolean stepBlocked = false;
            for (AxisAlignedBB barrier : barriers) {
                double contact = contactProgress(stepped, barrier, movement);
                if (contact <= stepProgress) {
                    stepProgress = contact;
                    stepBlocked = true;
                }
            }
            if (stepProgress > progress) {
                stepUp = clearance;
                horizontalStart = stepped;
                progress = stepProgress;
                blocked = stepBlocked;
                AxisAlignedBB stepEnd = stepped.offset(movement.x * progress, 0D, movement.z * progress);
                stepDown = -clearance;
                for (AxisAlignedBB collision : collisions) stepDown = collision.calculateYOffset(stepEnd, stepDown);
                grounded = stepDown > -clearance;
            }
        }

        if (blocked && progress <= 1E-8D) {
            this.setDead();
            return;
        }
        Vec3d actualMovement = movement.scale(progress);
        AxisAlignedBB destination = horizontalStart.offset(actualMovement.x, stepDown, actualMovement.z);
        List<AxisAlignedBB> verticalSweeps = List.of(bounds.expand(0D, vertical + stepUp, 0D),
                destination.expand(0D, -stepDown, 0D));
        AxisAlignedBB travelledArea = horizontalStart.expand(actualMovement.x, 0D, actualMovement.z)
                .union(verticalSweeps.getFirst()).union(verticalSweeps.getLast());
        for (Entity target : this.world.getEntitiesWithinAABB(Entity.class, travelledArea)) {
            Entity victim = target instanceof MultiPartEntityPart part && part.parent instanceof Entity parent ? parent : target;
            if (!(victim instanceof EntityLivingBase) || !victim.isEntityAlive()
                    || victim.getLowestRidingEntity() == this.creator.getLowestRidingEntity()
                    || this.creator.isRelatedToEntity(victim) || this.hitEntities.contains(victim.getUniqueID())
                    || (contactProgress(horizontalStart, target.getEntityBoundingBox(), actualMovement) > 1D
                    && verticalSweeps.stream().noneMatch(box -> box.intersects(target.getEntityBoundingBox())))) continue;
            this.hitEntities.add(victim.getUniqueID());
            if (target.attackEntityFrom(RiftDamage.getElementDamageSource("mob", this, this.creator, this.move.getElement()), (float) this.damage)) {
                victim.setFire(5);
            }
        }

        this.setPosition(this.posX + actualMovement.x, destination.minY, this.posZ + actualMovement.z);
        this.onGround = grounded;
        this.collidedHorizontally = blocked;
        this.collidedVertically = vertical != this.motionY || stepUp + stepDown > 0D;
        this.collided = this.collidedHorizontally || this.collidedVertically;
        if (this.collidedVertically) this.motionY = 0D;
        else this.motionY *= 0.98D;
        this.travelledDistance += Math.sqrt(actualMovement.lengthSquared());
        if (ForgeEventFactory.getMobGriefingEvent(this.world, this.creator)) {
            // Include supporting blocks directly below the storm, without reaching blocks behind a wall.
            AxisAlignedBB breakingStart = horizontalStart.grow(0D, 0.01D, 0D);
            for (BlockPos position : BlockPos.getAllInBoxMutable(
                    new BlockPos(travelledArea.minX, travelledArea.minY - 0.01D, travelledArea.minZ),
                    new BlockPos(Math.ceil(travelledArea.maxX) - 1D, Math.ceil(travelledArea.maxY) - 1D, Math.ceil(travelledArea.maxZ) - 1D))) {
                if (!isFlammable(this.world, position, this.world.getBlockState(position))) continue;
                AxisAlignedBB blockBounds = new AxisAlignedBB(position);
                if (contactProgress(breakingStart, blockBounds, actualMovement) <= 1D
                        || verticalSweeps.stream().anyMatch(box -> box.grow(0D, 0.01D, 0D).intersects(blockBounds))) {
                    this.world.destroyBlock(position, true);
                }
            }
            for (BlockPos position : BlockPos.getAllInBoxMutable(
                    new BlockPos(travelledArea.minX, travelledArea.minY - 2D, travelledArea.minZ),
                    new BlockPos(Math.ceil(travelledArea.maxX) - 1D, travelledArea.minY + 1D, Math.ceil(travelledArea.maxZ) - 1D))) {
                if (this.world.isAirBlock(position) && Blocks.FIRE.canPlaceBlockAt(this.world, position)) {
                    this.world.setBlockState(position, Blocks.FIRE.getDefaultState(), 3);
                }
            }
        }
        if (blocked || this.travelledDistance >= MAXIMUM_DISTANCE - 1E-8D) this.setDead();
    }

    /** Returns a finite sentinel beyond the movement when the swept volumes do not meet. */
    public static double contactProgress(@NotNull AxisAlignedBB moving, @NotNull AxisAlignedBB collider, @NotNull Vec3d movement) {
        if (!moving.expand(movement.x, movement.y, movement.z).intersects(collider)) return 2D;
        if (moving.intersects(collider)) return 0D;
        if (movement.lengthSquared() <= 1E-12D) return 2D;
        Vec3d center = moving.getCenter();
        AxisAlignedBB expanded = collider.grow((moving.maxX - moving.minX) * 0.5D,
                (moving.maxY - moving.minY) * 0.5D, (moving.maxZ - moving.minZ) * 0.5D);
        RayTraceResult contact = expanded.calculateIntercept(center, center.add(movement));
        if (contact == null) return 2D;
        return Math.clamp(center.distanceTo(contact.hitVec) / Math.sqrt(movement.lengthSquared()), 0D, 1D);
    }

    public static boolean isFlammable(@NotNull World world, @NotNull BlockPos position, @NotNull IBlockState state) {
        for (EnumFacing face : EnumFacing.values()) {
            if (state.getBlock().isFlammable(world, position, face)) return true;
        }
        return false;
    }


    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int ticks, boolean teleport) {
        if (teleport && this.getDistanceSq(x, y, z) > 16D) {
            this.setPosition(x, y, z);
            this.clientTarget = null;
            this.interpolationTicks = 0;
        }
        else {
            this.clientTarget = new Vec3d(x, y, z);
            this.interpolationTicks = Math.max(1, ticks);
        }
    }

    @Override
    public void writeSpawnData(@NotNull ByteBuf buffer) {
        buffer.writeDouble(this.motionX);
        buffer.writeDouble(this.motionY);
        buffer.writeDouble(this.motionZ);
    }

    @Override
    public void readSpawnData(@NotNull ByteBuf buffer) {
        this.setVelocity(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public void setDead() {
        if (this.isDead) return;
        this.animationData.setVariable("variable.storm_dissipating", 1D);
        super.setDead();
    }

    //---nbt overrides to ensure that upon restarting, the magma storms disappears---
    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        this.setDead();
    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound compound) {}

    @Override
    public boolean writeToNBTOptional(@NotNull NBTTagCompound compound) {
        return false;
    }

    @Override
    public boolean writeToNBTAtomically(@NotNull NBTTagCompound compound) {
        return false;
    }

    //---animation stuff---
    @Override
    @NotNull
    public AnimationDataEntityNonLiving getAnimationData() {
        return this.animationData;
    }

    @Override
    public void initializeAnimationData(@NotNull AnimationDataEntityNonLiving animationData) {
        animationData.setVariable("variable.storm_radius", RADIUS);
        animationData.setVariable("variable.storm_height", HEIGHT);
        animationData.setVariable("variable.storm_dissipating", this.isDead ? 1D : 0D);

        animationData.addParticleEffect(
                new ParticleBuilder().setIdentifier("prift:magma_storm_smoke")
                        .setTexture(new ResourceLocation("textures/particle/particles.png"))
                        .setMaterial(ParticleMaterial.BLEND)
                        .addEmitterComponent(new EmitterLocalSpaceComponent.Builder().setPosition(true))
                        .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                .setSpawnRate("640 * math.max(0, 1 - variable.storm_dissipation_age / 0.8)").setMaxParticleCount(512D))
                        .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                .setActivationExpression(1D).setExpirationExpression(0D))
                        .addEmitterComponent(new EmitterInitializationComponent.Builder().setPerUpdateExpression(
                                "variable.storm_phase = variable.emitter_age * 1120; "
                                        + "variable.storm_dissipating = query.actor_owner->variable.storm_dissipating || variable.emitter_age >= "
                                        + (MAXIMUM_DISTANCE * 0.75D / (SPEED * 20D)) + "; "
                                        + "variable.storm_dissipation_age = variable.storm_dissipating ? variable.storm_dissipation_age + 0.05 : 0;"))
                        .addEmitterComponent(new EmitterShapeCustomComponent.Builder()
                                .setOffset(RawMolangValue.compileExpression(
                                                "variable.storm_height = math.random(0, " + HEIGHT + "); "
                                                        + "variable.storm_angle = variable.storm_phase + variable.storm_height * 300 + math.random(-20, 20); "
                                                        + "variable.storm_radius = 0.4 + variable.storm_height * " + ((RADIUS - 0.4D) / HEIGHT) + "; "
                                                        + "return math.cos(variable.storm_angle) * variable.storm_radius;"
                                        ),
                                        RawMolangValue.compileExpression("variable.storm_height"),
                                        RawMolangValue.compileExpression("math.sin(variable.storm_angle) * variable.storm_radius"))
                                .setDirection(RawMolangValue.compileExpression("-math.sin(variable.storm_angle)"),
                                        RawMolangValue.compileExpression("0.3"),
                                        RawMolangValue.compileExpression("math.cos(variable.storm_angle)")))
                        .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder()
                                .setMaxLifetime("variable.storm_active_age = variable.storm_dissipating ? variable.storm_active_age : variable.particle_age + 0.05; "
                                        + "return 0.6 + variable.storm_dissipating * (1 + variable.particle_random_3 * 0.6)")
                                .setExpirationExpression("variable.storm_dissipation_age >= 1 + variable.particle_random_3 * 0.6"))
                        .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(4D))
                        .addParticleComponent(new ParticleMotionDynamicComponent.Builder().setLinearAcceleration(
                                RawMolangValue.compileExpression("math.cos(variable.particle_random_2 * 360) * math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * 12"),
                                RawMolangValue.compileExpression("math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * 4"),
                                RawMolangValue.compileExpression("math.sin(variable.particle_random_2 * 360) * math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * 12")))
                        .addParticleComponent(new AppearanceTintingComponent.Builder()
                                .setRed(0.25D).setGreen(0.25D).setBlue(0.25D))
                        .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                .setSize(RawMolangValue.compileExpression("0.7 * math.pow(math.max(0, 1 - math.max(0, variable.storm_active_age - 0.2) / 0.4), 0.75) * math.pow(math.max(0, 1 - variable.storm_dissipation_age / (1 + variable.particle_random_3 * 0.6)), 0.75)"),
                                        RawMolangValue.compileExpression("0.7 * math.pow(math.max(0, 1 - math.max(0, variable.storm_active_age - 0.2) / 0.4), 0.75) * math.pow(math.max(0, 1 - variable.storm_dissipation_age / (1 + variable.particle_random_3 * 0.6)), 0.75)"))
                                .setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                .setTextureWidth(128).setTextureHeight(128)
                                .setUv(56D, 0D).setUvSize(8D, 8D)),
                "base"
        );
        animationData.addParticleEffect(
                new ParticleBuilder().setIdentifier("prift:magma_storm_magma")
                        .setTexture(new ResourceLocation("textures/particle/particles.png"))
                        .setMaterial(ParticleMaterial.BLEND)
                        .addEmitterComponent(new EmitterLocalSpaceComponent.Builder().setPosition(true))
                        .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                .setSpawnRate("960 * math.max(0, 1 - variable.storm_dissipation_age / 0.8)").setMaxParticleCount(768D))
                        .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                .setActivationExpression(1D).setExpirationExpression(0D))
                        .addEmitterComponent(new EmitterInitializationComponent.Builder().setPerUpdateExpression(
                                "variable.storm_phase = variable.emitter_age * 1120; "
                                        + "variable.storm_dissipating = query.actor_owner->variable.storm_dissipating || variable.emitter_age >= "
                                        + (MAXIMUM_DISTANCE * 0.75D / (SPEED * 20D)) + "; "
                                        + "variable.storm_dissipation_age = variable.storm_dissipating ? variable.storm_dissipation_age + 0.05 : 0;"))
                        .addEmitterComponent(new EmitterShapeCustomComponent.Builder()
                                .setOffset(RawMolangValue.compileExpression(
                                                "variable.storm_height = math.random(0, " + HEIGHT + "); "
                                                        + "variable.storm_angle = variable.storm_phase + variable.storm_height * 300 + math.random(-20, 20); "
                                                        + "variable.storm_radius = 0.4 + variable.storm_height * " + ((RADIUS - 0.4D) / HEIGHT) + "; "
                                                        + "return math.cos(variable.storm_angle) * variable.storm_radius;"),
                                        RawMolangValue.compileExpression("variable.storm_height"),
                                        RawMolangValue.compileExpression("math.sin(variable.storm_angle) * variable.storm_radius"))
                                .setDirection(RawMolangValue.compileExpression("-math.sin(variable.storm_angle)"),
                                        RawMolangValue.compileExpression("0.3"),
                                        RawMolangValue.compileExpression("math.cos(variable.storm_angle)")))
                        .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder()
                                .setMaxLifetime("variable.storm_active_age = variable.storm_dissipating ? variable.storm_active_age : variable.particle_age + 0.05; "
                                        + "return 0.6 + variable.storm_dissipating * (0.65 + variable.particle_random_3 * 0.4)")
                                .setExpirationExpression("variable.storm_dissipation_age >= 0.65 + variable.particle_random_3 * 0.4"))
                        .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(4D))
                        .addParticleComponent(new ParticleMotionDynamicComponent.Builder().setLinearAcceleration(
                                RawMolangValue.compileExpression("math.cos(variable.particle_random_2 * 360) * math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * 12"),
                                RawMolangValue.compileExpression("math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * -4"),
                                RawMolangValue.compileExpression("math.sin(variable.particle_random_2 * 360) * math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * 12")))
                        .addParticleComponent(new AppearanceTintingComponent.Builder()
                                .setRed("variable.particle_random_1 < 0.35 ? 0.22 : 1")
                                .setGreen("variable.particle_random_1 < 0.35 ? 0.2 : (variable.particle_random_1 < 0.8 ? 0.65 : 1)")
                                .setBlue("variable.particle_random_1 < 0.35 ? 0.18 : (variable.particle_random_1 < 0.8 ? 0.18 : 0.65)"))
                        .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                .setSize(RawMolangValue.compileExpression("0.5 * math.pow(math.max(0, 1 - math.max(0, variable.storm_active_age - 0.2) / 0.4), 0.75) * math.pow(math.max(0, 1 - variable.storm_dissipation_age / (0.65 + variable.particle_random_3 * 0.4)), 0.75)"),
                                        RawMolangValue.compileExpression("0.5 * math.pow(math.max(0, 1 - math.max(0, variable.storm_active_age - 0.2) / 0.4), 0.75) * math.pow(math.max(0, 1 - variable.storm_dissipation_age / (0.65 + variable.particle_random_3 * 0.4)), 0.75)"))
                                .setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                .setTextureWidth(128).setTextureHeight(128)
                                .setUv(8D, 24D).setUvSize(8D, 8D)),
                "base");
        animationData.addParticleEffect(
                new ParticleBuilder().setIdentifier("prift:magma_storm_fire")
                        .setTexture(new ResourceLocation("textures/particle/particles.png"))
                        .setMaterial(ParticleMaterial.BLEND)
                        .addEmitterComponent(new EmitterLocalSpaceComponent.Builder().setPosition(true))
                        .addEmitterComponent(new EmitterSteadyComponent.Builder()
                                .setSpawnRate("640 * math.max(0, 1 - variable.storm_dissipation_age / 0.8)").setMaxParticleCount(512D))
                        .addEmitterComponent(new EmitterLifetimeExpressionComponent.Builder()
                                .setActivationExpression(1D).setExpirationExpression(0D))
                        .addEmitterComponent(new EmitterInitializationComponent.Builder().setPerUpdateExpression(
                                "variable.storm_phase = variable.emitter_age * 1120; "
                                        + "variable.storm_dissipating = query.actor_owner->variable.storm_dissipating || variable.emitter_age >= "
                                        + (MAXIMUM_DISTANCE * 0.75D / (SPEED * 20D)) + "; "
                                        + "variable.storm_dissipation_age = variable.storm_dissipating ? variable.storm_dissipation_age + 0.05 : 0;"))
                        .addEmitterComponent(new EmitterShapeCustomComponent.Builder()
                                .setOffset(RawMolangValue.compileExpression(
                                                "variable.storm_height = math.random(0, " + HEIGHT + "); "
                                                        + "variable.storm_angle = variable.storm_phase + variable.storm_height * 300 + math.random(-20, 20); "
                                                        + "variable.storm_radius = 0.4 + variable.storm_height * " + ((RADIUS - 0.4D) / HEIGHT) + "; "
                                                        + "return math.cos(variable.storm_angle) * variable.storm_radius;"),
                                        RawMolangValue.compileExpression("variable.storm_height"),
                                        RawMolangValue.compileExpression("math.sin(variable.storm_angle) * variable.storm_radius"))
                                .setDirection(RawMolangValue.compileExpression("-math.sin(variable.storm_angle)"),
                                        RawMolangValue.compileExpression("0.3"),
                                        RawMolangValue.compileExpression("math.cos(variable.storm_angle)")))
                        .addParticleComponent(new ParticleLifetimeExpressionComponent.Builder()
                                .setMaxLifetime("variable.storm_active_age = variable.storm_dissipating ? variable.storm_active_age : variable.particle_age + 0.05; "
                                        + "return 0.6 + variable.storm_dissipating * (0.4 + variable.particle_random_3 * 0.3)")
                                .setExpirationExpression("variable.storm_dissipation_age >= 0.4 + variable.particle_random_3 * 0.3"))
                        .addParticleComponent(new ParticleInitialSpeedComponent.Builder().setInitialSpeed(4D))
                        .addParticleComponent(new ParticleMotionDynamicComponent.Builder().setLinearAcceleration(
                                RawMolangValue.compileExpression("math.cos(variable.particle_random_2 * 360) * math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * 12"),
                                RawMolangValue.compileExpression("math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * 2"),
                                RawMolangValue.compileExpression("math.sin(variable.particle_random_2 * 360) * math.max(math.max(0, (variable.storm_active_age - 0.3) / 0.3), math.min(1, variable.storm_dissipation_age * 2)) * 12")))
                        .addParticleComponent(new AppearanceTintingComponent.Builder()
                                .setRed(1D).setGreen("variable.particle_random_1 < 0.5 ? 0.8 : 1").setBlue(0.6D))
                        .addParticleComponent(new AppearanceBillboardComponent.Builder()
                                .setSize(RawMolangValue.compileExpression("0.35 * math.pow(math.max(0, 1 - math.max(0, variable.storm_active_age - 0.2) / 0.4), 0.75) * math.pow(math.max(0, 1 - variable.storm_dissipation_age / (0.4 + variable.particle_random_3 * 0.3)), 0.75)"),
                                        RawMolangValue.compileExpression("0.35 * math.pow(math.max(0, 1 - math.max(0, variable.storm_active_age - 0.2) / 0.4), 0.75) * math.pow(math.max(0, 1 - variable.storm_dissipation_age / (0.4 + variable.particle_random_3 * 0.3)), 0.75)"))
                                .setCameraMode(ParticleCameraMode.ROTATE_XYZ)
                                .setTextureWidth(128).setTextureHeight(128)
                                .setUv(0D, 24D).setUvSize(8D, 8D)),
                "base");
    }
}
