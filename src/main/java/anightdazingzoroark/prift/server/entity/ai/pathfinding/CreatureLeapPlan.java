package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A frozen trajectory shared by AI path planning, rider targeting, preview, and movement.
 * */
public record CreatureLeapPlan(
        @NotNull Vec3d start, @NotNull Vec3d landing, double arcHeight,
        int durationTicks, int horizontalDelayTicks
) {
    public static final int SAMPLES_PER_TICK = 8;
    private static final double NAVIGATION_APEX_CLEARANCE = 0.25D;
    private static final double NAVIGATION_EDGE_CLEARANCE = 0.125D;

    public static boolean hasTakeoffSupport(@NotNull RiftCreature creature) {
        if (creature.onGround) return true;
        AxisAlignedBB bounds = creature.getEntityBoundingBox();
        return creature.motionY <= 0D && creature.world.collidesWithAnyBlock(new AxisAlignedBB(
                bounds.minX + 1E-4D, bounds.minY - 0.05D, bounds.minZ + 1E-4D,
                bounds.maxX - 1E-4D, bounds.minY, bounds.maxZ - 1E-4D));
    }

    @NotNull
    @SuppressWarnings("SuspiciousNameCombination")
    public Vec3d position(double progress) {
        double clampedProgress = Math.clamp(progress, 0D, 1D);
        double horizontalProgress = MathUtil.slopeResult(
                clampedProgress * this.durationTicks, true,
                this.horizontalDelayTicks, this.durationTicks, 0D, 1D
        );
        return new Vec3d(
                MathUtil.slopeResult(horizontalProgress, true, 0D, 1D, this.start.x, this.landing.x),
                MathUtil.slopeResult(clampedProgress, true, 0D, 1D, this.start.y, this.landing.y)
                        + 4D * this.arcHeight * clampedProgress * (1D - clampedProgress),
                MathUtil.slopeResult(horizontalProgress, true, 0D, 1D, this.start.z, this.landing.z)
        );
    }

    @NotNull
    public Vec3d velocity(double progress) {
        double clampedProgress = Math.clamp(progress, 0D, 1D);
        double horizontalTicks = this.durationTicks - this.horizontalDelayTicks;
        double motionX = (this.landing.x - this.start.x) / horizontalTicks;
        double motionZ = (this.landing.z - this.start.z) / horizontalTicks;
        if (clampedProgress * this.durationTicks < this.horizontalDelayTicks) {
            motionX = 0D;
            motionZ = 0D;
        }
        return new Vec3d(
                motionX,
                (this.landing.y - this.start.y + 4D * this.arcHeight * (1D - 2D * clampedProgress)) / this.durationTicks,
                motionZ);
    }

    /**
     * Builds the same arc for a forward charged leap or a charged vertical jump. Collisions stop it during travel.
     * */
    @Nullable
    public static CreatureLeapPlan charged(@NotNull RiftCreature creature, float yaw, boolean movingForward, int chargeTicks) {
        CreatureNavigationBuilder navigation = creature.getNavigationBuilder();
        if (navigation == null || !navigation.getCanLeap()) return null;
        int charge = Math.clamp(chargeTicks, 1, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS);
        double height = MathUtil.slopeResult(
                charge, true, 1, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS,
                1D, navigation.getLeapHeight()
        );
        double distance = 0D;
        if (movingForward) {
            distance = MathUtil.slopeResult(
                    charge, true, 1, RiftCreatureLeapHelper.MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS,
                    navigation.getLeapDistance() / navigation.getLeapHeight(), navigation.getLeapDistance()
            );
        }
        Vec3d start = creature.getPositionVector();
        Vec3d destination = start.add(-Math.sin(Math.toRadians(yaw)) * distance, 0D, Math.cos(Math.toRadians(yaw)) * distance);
        return arc(start, destination, height, 0D);
    }

    /**
     * Solves the vertical arc once, optionally delaying horizontal travel until a ledge has been cleared.
     * */
    @NotNull
    public static CreatureLeapPlan arc(@NotNull Vec3d start, @NotNull Vec3d destination, double height, double obstacleHeight) {
        double ascent = Math.sqrt(height);
        double descent = Math.sqrt(height - (destination.y - start.y));
        double arcHeight = (ascent + descent) * (ascent + descent) * 0.25D;
        int duration = Math.clamp((int) Math.ceil((ascent + descent) * 5D), 2, 79);
        int horizontalDelay = 0;
        if (obstacleHeight > 0D) {
            CreatureLeapPlan verticalArc = new CreatureLeapPlan(start, destination, arcHeight, duration, 0);
            for (int tick = 1; tick < duration; tick++) {
                if (verticalArc.position((double) tick / duration).y - start.y >= obstacleHeight) {
                    horizontalDelay = tick;
                    break;
                }
            }
        }
        return new CreatureLeapPlan(start, destination, arcHeight, duration, horizontalDelay);
    }

    @Nullable
    public static CreatureLeapPlan navigation(
            @NotNull RiftCreature creature, @NotNull AxisAlignedBB startingBounds,
            @NotNull Vec3d destination, double obstacleHeight
    ) {
        CreatureNavigationBuilder navigation = creature.getNavigationBuilder();
        if (navigation == null || !navigation.getCanLeap()) return null;
        double height = navigation.getLeapHeight();
        if (obstacleHeight > height + 1E-3D) return null;
        double clearance = 0D;
        if (obstacleHeight > 0D) clearance = obstacleHeight + NAVIGATION_EDGE_CLEARANCE;
        CreatureLeapPlan plan = create(creature, startingBounds, destination, height + NAVIGATION_APEX_CLEARANCE, clearance, true);
        // A navigation node describes an exact foothold; crosshair surfaces may instead lift a wide body over adjacent steps.
        if (plan != null && Math.abs(plan.landing.y - destination.y) > 1E-4D) return null;
        return plan;
    }

    @Nullable
    public static CreatureLeapPlan create(@NotNull RiftCreature creature, @Nullable RiddenAimTarget target) {
        CreatureNavigationBuilder navigation = creature.getNavigationBuilder();
        if (target == null || navigation == null || !navigation.getCanLeap()) return null;
        Vec3d hit = target.position();
        // Bounded comparisons also reject malformed numeric packet values.
        boolean validCoordinates = hit.x >= -30000000D && hit.x <= 30000000D
                && hit.y >= 0D && hit.y <= creature.world.getHeight()
                && hit.z >= -30000000D && hit.z <= 30000000D;
        if (!validCoordinates) return null;
        double maximumDistance = navigation.getLeapDistance();
        if (hit.squareDistanceTo(creature.getPositionVector()) > (maximumDistance + creature.height) * (maximumDistance + creature.height)) {
            return null;
        }

        double maximumHeight = navigation.getLeapHeight();
        double maximumDrop = Math.max(maximumHeight, creature.getMaxFallHeight());
        boolean airTarget = target.entityId() < 0 && target.blockPosition() == null && target.blockFace() == null;
        Vec3d landing;
        if (target.entityId() >= 0) {
            Entity entity = target.getEntity(creature.world);
            if (!(entity instanceof EntityLivingBase living) || !living.isEntityAlive()
                    || entity.getLowestRidingEntity() == creature.getLowestRidingEntity()) return null;
            boolean hitEntity = entity.getEntityBoundingBox().grow(0.25D).contains(hit);
            if (!hitEntity && entity.getParts() != null) {
                for (Entity part : entity.getParts()) {
                    if (part.getEntityBoundingBox().grow(0.25D).contains(hit)) {
                        hitEntity = true;
                        break;
                    }
                }
            }
            if (!hitEntity) return null;
            // Mobs select the supported ground at their feet, rather than a point inside their body.
            Vec3d feet = new Vec3d(entity.posX, entity.getEntityBoundingBox().minY + 0.05D, entity.posZ);
            RayTraceResult support = creature.world.rayTraceBlocks(feet, feet.add(0D, -maximumDrop - 0.1D, 0D), false, true, false);
            if (support == null || support.sideHit != EnumFacing.UP) return null;
            landing = support.hitVec;
        }
        else if (airTarget) {
            // The selected air position ends the guided arc; its momentum continues until touchdown.
            landing = hit;
        }
        else {
            BlockPos block = target.blockPosition();
            EnumFacing face = target.blockFace();
            if (block == null || face == null || face == EnumFacing.DOWN || !creature.world.isBlockLoaded(block)) return null;
            List<AxisAlignedBB> surfaces = new ArrayList<>();
            creature.world.getBlockState(block).addCollisionBoxToList(creature.world, block,
                    new AxisAlignedBB(block).grow(0.01D), surfaces, creature, false);
            AxisAlignedBB selectedSurface = null;
            for (AxisAlignedBB surface : surfaces) {
                if (surface.grow(0.03D).contains(hit)) {
                    selectedSurface = surface;
                    break;
                }
            }
            if (selectedSurface == null) return null;
            landing = new Vec3d(hit.x - face.getXOffset() * 0.05D, selectedSurface.maxY,
                    hit.z - face.getZOffset() * 0.05D);
        }

        return create(creature, creature.getEntityBoundingBox(), landing, maximumHeight, 0D, !airTarget);
    }

    /**
     * Validates a destination from an actual or planned takeoff footprint using the shared trajectory.
     * */
    @Nullable
    public static CreatureLeapPlan create(
            @NotNull RiftCreature creature, @NotNull AxisAlignedBB startingBounds,
            @NotNull Vec3d landing, double maximumHeight, double obstacleHeight, boolean supportedLanding
    ) {
        CreatureNavigationBuilder navigation = creature.getNavigationBuilder();
        if (navigation == null || !navigation.getCanLeap()) return null;
        Vec3d start = new Vec3d(
                (startingBounds.minX + startingBounds.maxX) * 0.5D,
                startingBounds.minY,
                (startingBounds.minZ + startingBounds.maxZ) * 0.5D
        );
        double maximumDistance = navigation.getLeapDistance();
        double maximumDrop = Math.max(maximumHeight, creature.getMaxFallHeight());
        double displacementX = landing.x - start.x;
        double displacementZ = landing.z - start.z;
        double horizontalDistanceSquared = displacementX * displacementX + displacementZ * displacementZ;
        double heightDifference = landing.y - start.y;
        if (horizontalDistanceSquared > maximumDistance * maximumDistance
                || heightDifference > maximumHeight || heightDifference < -maximumDrop
        ) {
            return null;
        }

        AxisAlignedBB landingBounds = startingBounds.offset(landing.subtract(start));
        if (!creature.world.isAreaLoaded(new BlockPos(landingBounds.minX, landingBounds.minY - 1D, landingBounds.minZ),
                new BlockPos(landingBounds.maxX, landingBounds.maxY, landingBounds.maxZ))
        ) {
            return null;
        }
        // A wide creature straddles steps: clear intersecting terrain while retaining the aimed X/Z.
        for (int attempt = 0; attempt < 16; attempt++) {
            List<AxisAlignedBB> collisions = creature.world.getCollisionBoxes(null, landingBounds.grow(-1E-4D));
            if (collisions.isEmpty()) break;
            if (!supportedLanding) return null;
            double supportedY = landing.y;
            for (AxisAlignedBB collision : collisions) supportedY = Math.max(supportedY, collision.maxY);
            if (supportedY <= landing.y + 1E-4D || supportedY - start.y > maximumHeight) return null;
            landingBounds = landingBounds.offset(0D, supportedY - landing.y, 0D);
            landing = new Vec3d(landing.x, supportedY, landing.z);
        }
        // Match ordinary movement: support beneath any part of the footprint can stop a descent.
        AxisAlignedBB supportProbe = new AxisAlignedBB(
                landingBounds.minX + 1E-4D, landing.y - 0.05D, landingBounds.minZ + 1E-4D,
                landingBounds.maxX - 1E-4D, landing.y, landingBounds.maxZ - 1E-4D
        );
        if (supportedLanding && !creature.world.collidesWithAnyBlock(supportProbe)) return null;
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(supportProbe.minX, supportProbe.minY, supportProbe.minZ),
                new BlockPos(supportProbe.maxX, landing.y, supportProbe.maxZ))) {
            if (creature.world.getBlockState(pos).getBlock() == Blocks.FIRE
                    || creature.world.getBlockState(pos).getBlock() == Blocks.CACTUS
                    || creature.world.getBlockState(pos).getBlock() == Blocks.MAGMA
                    || creature.world.getBlockState(pos).getMaterial().isLiquid()
            ) {
                return null;
            }
        }

        if (obstacleHeight >= maximumHeight) return null;
        CreatureLeapPlan plan = arc(start, landing, maximumHeight, obstacleHeight);
        return plan.isClear(creature, startingBounds) ? plan : null;
    }

    public boolean isClear(@NotNull RiftCreature creature) {
        return this.isClear(creature, creature.getEntityBoundingBox().offset(this.start.subtract(creature.getPositionVector())));
    }

    public boolean isClear(@NotNull RiftCreature creature, @NotNull AxisAlignedBB startingBounds) {
        AxisAlignedBB initialBounds = startingBounds.grow(-1E-4D);
        AxisAlignedBB previousBounds = initialBounds;
        int samples = this.durationTicks * SAMPLES_PER_TICK;
        for (int sample = 1; sample <= samples; sample++) {
            Vec3d position = this.position((double) sample / samples);
            AxisAlignedBB bounds = initialBounds.offset(position.subtract(this.start));
            AxisAlignedBB swept = previousBounds.union(bounds);
            if (!creature.world.isAreaLoaded(new BlockPos(swept.minX, swept.minY, swept.minZ),
                    new BlockPos(swept.maxX, swept.maxY, swept.maxZ))
                    || creature.world.collidesWithAnyBlock(swept)
                    || creature.world.containsAnyLiquid(swept)
            ) {
                return false;
            }
            previousBounds = bounds;
        }
        return true;
    }
}
