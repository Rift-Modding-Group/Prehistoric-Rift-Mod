package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Everything relating to managing a creature's ability to leap happens here
 * */
public class RiftCreatureLeapHelper {
    private static final double GRAVITY = 0.08D; //wherever this came from, we will never know....
    private static final double VERTICAL_DRAG = 0.98D;
    private static final double LEAP_CLEARANCE = 0.25D;
    private static final double VERTICAL_EDGE_CLEARANCE = 0.125D;
    private static final double OBSTACLE_SCAN_STEP = 0.125D;
    private static final double OBSTACLE_LOOKAHEAD_DISTANCE = 0.25D;
    private static final double GAP_SCAN_STEP = 0.25D;
    private static final double GAP_SEARCH_ANGLE_STEP = Math.toRadians(5D);
    private static final int GAP_SEARCH_ANGLE_STEPS = 12;
    private static final double SUPPORT_PROBE_RADIUS = 0.05D;
    private static final double MINIMUM_SUPPORT_PROBE_DEPTH = 0.3D;

    @NotNull
    private final RiftCreatureMoveHelperBase moveHelper;
    @NotNull
    private final RiftCreature creature;
    private boolean leapStarted;
    private int walkingBeforeLeapTicks;
    private int leapTicks;
    private double leapObstacleHeight;
    private double leapMotionX;
    private double leapMotionZ;
    private double leapTargetX;
    private double leapTargetY;
    private double leapTargetZ;
    private double leapStartY;
    private float leapYaw;
    private boolean verticalLeap;
    private boolean clearedVerticalObstacle;

    public RiftCreatureLeapHelper(@NotNull RiftCreatureMoveHelperBase moveHelper, @NotNull RiftCreature creature) {
        this.moveHelper = moveHelper;
        this.creature = creature;
    }

    /**
     * This points the next leap at a spot and gets its launch ready.
     * */
    public boolean prepareLeapTo(double x, double y, double z) {
        if (this.isLeaping()) return false;
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (!navigation.getCanLeap() || !this.creature.onGround) {
            this.resetDelay();
            return false;
        }

        double displacementX = x - this.creature.posX;
        double displacementZ = z - this.creature.posZ;
        double horizontalDistance = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
        if (!this.isWithinLeapDistance(horizontalDistance)) {
            this.resetDelay();
            return false;
        }
        if (!this.hasSafeLandingNode(x, y, z)) {
            this.creature.setUnableToPathToTarget(true);
            this.resetDelay();
            return false;
        }

        double targetHeight = Math.max(0D, y - this.creature.posY);
        double obstacleHeight = Math.max(targetHeight, this.getObstacleClearance(x, z, navigation.getLeapHeight()));
        if (obstacleHeight > navigation.getLeapHeight() + 1E-3D) {
            this.resetDelay();
            return false;
        }

        this.leapTargetX = x;
        this.leapTargetY = y;
        this.leapTargetZ = z;
        this.leapObstacleHeight = obstacleHeight;
        this.leapYaw = (float)(Math.atan2(displacementZ, displacementX) * 180D / Math.PI) - 90F;
        this.verticalLeap = targetHeight > 1E-3D;
        this.leapStarted = false;
        this.clearedVerticalObstacle = false;
        this.leapTicks = 0;
        double upwardMotion = this.upwardVelocityForHeight(navigation.getLeapHeight() + LEAP_CLEARANCE);
        if (!this.hasClearLeapPath(upwardMotion)) {
            this.creature.setUnableToPathToTarget(true);
            this.resetDelay();
            return false;
        }
        return true;
    }

    public boolean isLeaping() {
        return this.moveHelper.creatureAction == RiftCreatureMoveHelperBase.CreatureAction.LEAP;
    }

    /**
     * Starts the little walking wait before a leap from scratch.
     * */
    public void resetDelay() {
        this.walkingBeforeLeapTicks = 0;
    }

    /**
     * copy another leap helper
     * */
    public void read(RiftCreatureLeapHelper that) {
        this.leapStarted = that.leapStarted;
        this.walkingBeforeLeapTicks = that.walkingBeforeLeapTicks;
        this.leapTicks = that.leapTicks;
        this.leapObstacleHeight = that.leapObstacleHeight;
        this.leapMotionX = that.leapMotionX;
        this.leapMotionZ = that.leapMotionZ;
        this.leapTargetX = that.leapTargetX;
        this.leapTargetY = that.leapTargetY;
        this.leapTargetZ = that.leapTargetZ;
        this.leapStartY = that.leapStartY;
        this.leapYaw = that.leapYaw;
        this.verticalLeap = that.verticalLeap;
        this.clearedVerticalObstacle = that.clearedVerticalObstacle;
    }

    /**
     * Figures out whether this movement needs a gap leap, obstacle leap, or no leap at all.
     * */
    public boolean tryHandleLeap(
            double targetX, double targetY, double targetZ,
            double obstacleClearance, RiftCreatureMoveHelperBase.CreatureAction requestedAction
    ) {
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (!navigation.getCanLeap()) return false;

        double displacementY = targetY - this.creature.posY;
        double standardJumpClearance = RiftCreatureMoveHelper.STANDARD_JUMP_HEIGHT + 0.125D;
        boolean standardUpwardTransition = displacementY > 1E-3D && displacementY <= standardJumpClearance + 1E-3D;
        boolean standardObstacleTransition = obstacleClearance > this.creature.stepHeight + 1E-3D && obstacleClearance <= standardJumpClearance + 1E-3D;
        if (standardUpwardTransition || standardObstacleTransition) return false;

        boolean obstacleLeap = obstacleClearance > standardJumpClearance + 1E-3D && obstacleClearance <= navigation.getLeapHeight() + 1E-3D;
        boolean upwardLeap = displacementY > standardJumpClearance + 1E-3D && displacementY <= navigation.getLeapHeight() + 1E-3D;
        double downwardDistance = -displacementY;
        if (downwardDistance > 1E-3D && downwardDistance <= this.creature.getMaxFallHeight() + 1E-3D) {
            return false;
        }
        GapLeapTarget gapLeapTarget = this.findGapLeapTarget(targetX, targetZ);

        if (gapLeapTarget != null) {
            this.stopHorizontalMovement();
            if (gapLeapTarget.safe() && requestedAction != RiftCreatureMoveHelperBase.CreatureAction.CHARGE) {
                return this.moveHelper.setLeapTo(gapLeapTarget.x(), targetY, gapLeapTarget.z());
            }
            else this.resetDelay();
            return true;
        }
        if ((obstacleLeap || upwardLeap) && requestedAction != RiftCreatureMoveHelperBase.CreatureAction.CHARGE) {
            return this.moveHelper.setLeapTo(targetX, targetY, targetZ);
        }
        return false;
    }

    /**
     * This gets a leap started once the creature has walked for long enough.
     * */
    public boolean tryStartLeap() {
        if (this.leapStarted) return true;

        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (!navigation.getCanLeap() || !this.creature.onGround) {
            this.cancelLeap();
            return false;
        }

        double displacementX = this.leapTargetX - this.creature.posX;
        double displacementZ = this.leapTargetZ - this.creature.posZ;
        double horizontalDistance = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
        if (!this.isWithinLeapDistance(horizontalDistance)) {
            this.cancelLeap();
            return false;
        }

        this.faceLeapTarget();
        this.walkingBeforeLeapTicks++;
        if (this.walkingBeforeLeapTicks <= navigation.getLeapDelay()) return false;

        double upwardMotion = this.upwardVelocityForHeight(navigation.getLeapHeight() + LEAP_CLEARANCE);
        if (!this.hasClearLeapPath(upwardMotion)) {
            this.creature.setUnableToPathToTarget(true);
            this.creature.getCreaturePathNavigate().clearPath();
            this.cancelLeap();
            return false;
        }
        if (this.verticalLeap) {
            this.leapMotionX = 0D;
            this.leapMotionZ = 0D;
        }
        else {
            int crossingTicks = Math.max(1, this.ticksAboveHeight(upwardMotion, this.leapObstacleHeight));
            double horizontalMotion = horizontalDistance / crossingTicks;
            this.leapMotionX = displacementX / horizontalDistance * horizontalMotion;
            this.leapMotionZ = displacementZ / horizontalDistance * horizontalMotion;
        }
        this.leapStartY = this.creature.posY;
        this.clearedVerticalObstacle = !this.verticalLeap;
        this.creature.motionX = this.leapMotionX;
        this.creature.motionY = upwardMotion;
        this.creature.motionZ = this.leapMotionZ;
        this.creature.fallDistance = 0f;
        this.creature.isAirBorne = true;
        this.creature.getCreaturePathNavigate().clearPath();
        this.creature.setSprinting(false);
        this.moveHelper.stopWalkingControls();
        this.leapStarted = true;
        this.leapTicks = 0;
        this.resetDelay();
        ForgeHooks.onLivingJump(this.creature);
        return true;
    }

    /**
     * Keeps the creature aimed and moving through the rest of its leap.
     * */
    public void continueLeap() {
        this.faceLeapTarget();
        this.moveHelper.stopWalkingControls();
        this.creature.fallDistance = 0F;
        double remainingX = this.leapTargetX - this.creature.posX;
        double remainingZ = this.leapTargetZ - this.creature.posZ;
        double remainingDistanceSq = remainingX * remainingX + remainingZ * remainingZ;
        if (!this.clearedVerticalObstacle) {
            double climbedHeight = this.creature.posY - this.leapStartY;
            if (climbedHeight >= this.leapObstacleHeight + VERTICAL_EDGE_CLEARANCE) {
                this.clearedVerticalObstacle = true;
            }
            else {
                this.creature.motionX = 0D;
                this.creature.motionZ = 0D;
            }
        }

        if (this.clearedVerticalObstacle && this.verticalLeap) {
            this.setVerticalLeapMotion(remainingX, remainingZ, this.creature.posY - this.leapStartY);
        }
        double leapMotionDistanceSq = this.leapMotionX * this.leapMotionX + this.leapMotionZ * this.leapMotionZ;
        if (this.clearedVerticalObstacle && remainingDistanceSq <= leapMotionDistanceSq) {
            this.creature.motionX = remainingX;
            this.creature.motionZ = remainingZ;
        }
        else if (this.clearedVerticalObstacle) {
            this.creature.motionX = this.leapMotionX;
            this.creature.motionZ = this.leapMotionZ;
        }
        this.leapTicks++;

        if (this.leapTicks > 80 || this.leapTicks > 1 && this.creature.onGround) {
            this.creature.fallDistance = 0F;
            this.moveHelper.creatureAction = RiftCreatureMoveHelperBase.CreatureAction.WAIT;
            this.leapStarted = false;
            this.leapTicks = 0;
        }
    }

    /**
     * Adds the horizontal part once a vertical leap has cleared the ledge.
     * */
    private void setVerticalLeapMotion(double remainingX, double remainingZ, double climbedHeight) {
        double remainingDistance = Math.sqrt(remainingX * remainingX + remainingZ * remainingZ);
        if (remainingDistance < 1E-6D) return;

        int crossingTicks = this.ticksUntilDescendingBelowHeight(
                climbedHeight,
                this.creature.motionY,
                this.leapObstacleHeight + VERTICAL_EDGE_CLEARANCE
        );
        double horizontalMotion = remainingDistance / crossingTicks;
        this.leapMotionX = remainingX / remainingDistance * horizontalMotion;
        this.leapMotionZ = remainingZ / remainingDistance * horizontalMotion;
    }

    /**
     * Drops the pending leap and puts the move helper back at rest.
     * */
    private void cancelLeap() {
        this.moveHelper.creatureAction = RiftCreatureMoveHelperBase.CreatureAction.WAIT;
        this.resetDelay();
    }

    /**
     * Stops walking momentum so it cannot fight the leap calculations.
     * */
    private void stopHorizontalMovement() {
        this.moveHelper.stopWalkingControls();
        this.creature.motionX = 0D;
        this.creature.motionZ = 0D;
    }

    /**
     * Keeps the body and head pointed at the chosen landing spot.
     * */
    private void faceLeapTarget() {
        this.creature.rotationYaw = this.leapYaw;
        this.creature.rotationYawHead = this.leapYaw;
        this.creature.renderYawOffset = this.leapYaw;

        double yawRadians = Math.toRadians(this.leapYaw + 90F);
        this.creature.getLookHelper().setLookPosition(
                this.creature.posX + Math.cos(yawRadians),
                this.creature.posY + this.creature.getEyeHeight(),
                this.creature.posZ + Math.sin(yawRadians),
                360F,
                0F
        );
    }

    /**
     * Makes sure a landing is neither right underfoot nor beyond leap range.
     * */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isWithinLeapDistance(double horizontalDistance) {
        return horizontalDistance > 1E-6D && horizontalDistance <= this.creature.getNavigationBuilder().getLeapDistance();
    }

    /**
     * Finds a safe landing across a gap in the direction of a target. This lets
     * attack leaps move toward distant targets without making the target's exact
     * position the required landing point.
     * */
    @Nullable
    public Vec3d findGapLeapLandingToward(double targetX, double targetY, double targetZ) {
        GapLeapTarget gapLeapTarget = this.findGapLeapTarget(targetX, targetZ);
        if (gapLeapTarget == null || !gapLeapTarget.safe()) return null;
        return new Vec3d(gapLeapTarget.x(), targetY, gapLeapTarget.z());
    }

    /**
     * This looks ahead for a nearby gap and a safe place to land on the other side.
     * */
    @Nullable
    private GapLeapTarget findGapLeapTarget(double targetX, double targetZ) {
        double displacementX = targetX - this.creature.posX;
        double displacementZ = targetZ - this.creature.posZ;
        double horizontalDistance = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
        if (!this.creature.onGround || horizontalDistance < 1E-6D) return null;

        double directionX = displacementX / horizontalDistance;
        double directionZ = displacementZ / horizontalDistance;
        double leapDistance = this.creature.getNavigationBuilder().getLeapDistance();
        GapLeapTarget directTarget = this.scanGapRay(
                directionX,
                directionZ,
                Math.min(horizontalDistance, leapDistance)
        );
        if (directTarget == null || directTarget.safe) return directTarget;

        double alternativeSearchDistance = Math.max(2D, this.creature.width * 0.5D + GAP_SCAN_STEP);
        if (directTarget.gapStartDistance <= alternativeSearchDistance + 1E-6D) {
            //targets can point this toward a wall, so try a few nearby directions too
            for (int angleStep = 1; angleStep <= GAP_SEARCH_ANGLE_STEPS; angleStep++) {
                double angle = GAP_SEARCH_ANGLE_STEP * angleStep;
                double cosine = Math.cos(angle);
                double sine = Math.sin(angle);

                GapLeapTarget leftTarget = this.scanGapRay(
                        directionX * cosine - directionZ * sine,
                        directionX * sine + directionZ * cosine,
                        leapDistance
                );
                if (leftTarget != null && leftTarget.safe) return leftTarget;

                GapLeapTarget rightTarget = this.scanGapRay(
                        directionX * cosine + directionZ * sine,
                        -directionX * sine + directionZ * cosine,
                        leapDistance
                );
                if (rightTarget != null && rightTarget.safe) return rightTarget;
            }
        }

        double stoppingDistance = Math.max(GAP_SCAN_STEP, this.creature.width * 0.25D);
        return directTarget.gapStartDistance <= stoppingDistance + 1E-6D
                ? directTarget
                : null;
    }

    /**
     * This checks one direction for a gap and a safe landing spot past it.
     * */
    @Nullable
    private GapLeapTarget scanGapRay(double directionX, double directionZ, double maximumDistance) {
        double feetY = this.creature.getEntityBoundingBox().minY;
        double landingInset = Math.clamp(this.creature.width * 0.25D, GAP_SCAN_STEP * 2D, 1D);
        boolean foundGap = false;
        double gapStartDistance = 0D;
        boolean foundLandingSupport = false;
        double landingSupportStartDistance = 0D;

        for (double distance = GAP_SCAN_STEP; distance <= maximumDistance; distance += GAP_SCAN_STEP) {
            double candidateX = this.creature.posX + directionX * distance;
            double candidateZ = this.creature.posZ + directionZ * distance;
            boolean centerSupported = this.hasSupportAt(candidateX, candidateZ, feetY);
            if (!centerSupported && this.hasRaisedTerrainAt(candidateX, candidateZ, feetY)) return null;

            if (!foundGap) {
                if (!centerSupported) {
                    foundGap = true;
                    gapStartDistance = distance;
                }
                continue;
            }

            if (!centerSupported) {
                foundLandingSupport = false;
                continue;
            }
            if (!foundLandingSupport) {
                foundLandingSupport = true;
                landingSupportStartDistance = distance;
            }
            if (distance - landingSupportStartDistance >= landingInset - 1E-6D
                    && this.hasLandingClearance(candidateX, candidateZ)) {
                return GapLeapTarget.safe(candidateX, candidateZ);
            }
        }
        return foundGap ? GapLeapTarget.blocked(gapStartDistance) : null;
    }

    /**
     * Checks whether there is safe ground close enough below this little sample point.
     * */
    private boolean hasSupportAt(double x, double z, double feetY) {
        double supportProbeDepth = Math.max(
                MINIMUM_SUPPORT_PROBE_DEPTH,
                this.creature.getMaxFallHeight() + 1E-3D
        );
        AxisAlignedBB supportProbe = new AxisAlignedBB(
                x - SUPPORT_PROBE_RADIUS,
                feetY - supportProbeDepth,
                z - SUPPORT_PROBE_RADIUS,
                x + SUPPORT_PROBE_RADIUS,
                feetY - 1E-3D,
                z + SUPPORT_PROBE_RADIUS
        );
        return this.creature.world.collidesWithAnyBlock(supportProbe);
    }

    /**
     * Spots raised terrain so a normal step does not get mistaken for a gap.
     * */
    private boolean hasRaisedTerrainAt(double x, double z, double feetY) {
        AxisAlignedBB terrainProbe = new AxisAlignedBB(
                x - SUPPORT_PROBE_RADIUS,
                feetY + 1E-3D,
                z - SUPPORT_PROBE_RADIUS,
                x + SUPPORT_PROBE_RADIUS,
                feetY + RiftCreatureMoveHelper.STANDARD_JUMP_HEIGHT + 0.125D,
                z + SUPPORT_PROBE_RADIUS
        );
        return this.creature.world.collidesWithAnyBlock(terrainProbe);
    }

    /**
     * Checks that the whole creature can actually fit at a possible landing spot.
     * */
    private boolean hasLandingClearance(double centerX, double centerZ) {
        AxisAlignedBB landingBounds = this.creature.getEntityBoundingBox().offset(
                centerX - this.creature.posX,
                1E-3D,
                centerZ - this.creature.posZ
        );
        return !this.creature.world.collidesWithAnyBlock(landingBounds);
    }

    /**
     * make sure creature doesn't jump into a dangerous space
     * */
    private boolean hasSafeLandingNode(double x, double y, double z) {
        WalkNodeProcessor nodeProcessor = new WalkNodeProcessor();
        PathNodeType landingType = nodeProcessor.getPathNodeType(
                this.creature.world,
                MathHelper.floor(x),
                MathHelper.floor(y),
                MathHelper.floor(z),
                this.creature,
                MathHelper.ceil(this.creature.width / 2),
                MathHelper.ceil(this.creature.height),
                MathHelper.ceil(this.creature.width / 2),
                false,
                false
        );
        return landingType == PathNodeType.WALKABLE && this.creature.getPathPriority(landingType) >= 0f;
    }

    /**
     * This checks how high the creature needs to go to clear whatever is in front of it.
     * */
    public double getObstacleClearance(double targetX, double targetZ, double maximumHeight) {
        double displacementX = targetX - this.creature.posX;
        double displacementZ = targetZ - this.creature.posZ;
        double horizontalDistance = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
        if (horizontalDistance < 1E-6D) return 0D;

        double probeDistance = Math.min(horizontalDistance, OBSTACLE_LOOKAHEAD_DISTANCE);
        double offsetX = displacementX / horizontalDistance * probeDistance;
        double offsetZ = displacementZ / horizontalDistance * probeDistance;
        AxisAlignedBB creatureBounds = this.creature.getEntityBoundingBox();
        AxisAlignedBB xProbe = this.createXObstacleProbe(creatureBounds, offsetX, offsetZ);
        AxisAlignedBB zProbe = this.createZObstacleProbe(creatureBounds, offsetX, offsetZ);
        if (!this.collidesWithObstacleProbe(xProbe, zProbe, 0D)) return 0D;

        for (double clearance = OBSTACLE_SCAN_STEP;
                clearance <= maximumHeight + 1E-6D;
                clearance += OBSTACLE_SCAN_STEP) {
            if (!this.collidesWithObstacleProbe(xProbe, zProbe, clearance + 1E-3D)) {
                return clearance;
            }
        }
        return maximumHeight + OBSTACLE_SCAN_STEP;
    }

    /**
     * Builds the thin X-facing slice that the creature is about to move into.
     * */
    @Nullable
    private AxisAlignedBB createXObstacleProbe(AxisAlignedBB bounds, double offsetX, double offsetZ) {
        if (Math.abs(offsetX) < 1E-6D) return null;

        double minimumX = offsetX > 0D ? bounds.maxX : bounds.minX + offsetX;
        double maximumX = offsetX > 0D ? bounds.maxX + offsetX : bounds.minX;
        return new AxisAlignedBB(
                minimumX,
                bounds.minY,
                Math.min(bounds.minZ, bounds.minZ + offsetZ),
                maximumX,
                bounds.maxY,
                Math.max(bounds.maxZ, bounds.maxZ + offsetZ)
        );
    }

    /**
     * Builds the thin Z-facing slice that the creature is about to move into.
     * */
    @Nullable
    private AxisAlignedBB createZObstacleProbe(AxisAlignedBB bounds, double offsetX, double offsetZ) {
        if (Math.abs(offsetZ) < 1E-6D) return null;

        double minimumZ = offsetZ > 0D ? bounds.maxZ : bounds.minZ + offsetZ;
        double maximumZ = offsetZ > 0D ? bounds.maxZ + offsetZ : bounds.minZ;
        return new AxisAlignedBB(
                Math.min(bounds.minX, bounds.minX + offsetX),
                bounds.minY,
                minimumZ,
                Math.max(bounds.maxX, bounds.maxX + offsetX),
                bounds.maxY,
                maximumZ
        );
    }

    /**
     * Checks either leading slice for blocks after lifting it by the requested amount.
     * */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean collidesWithObstacleProbe(@Nullable AxisAlignedBB xProbe, @Nullable AxisAlignedBB zProbe, double verticalOffset) {
        return xProbe != null && this.creature.world.collidesWithAnyBlock(xProbe.offset(0D, verticalOffset, 0D))
                || zProbe != null && this.creature.world.collidesWithAnyBlock(zProbe.offset(0D, verticalOffset, 0D));
    }

    /**
     * Check if leap path is clear. Good for making sure it don't bump into a ceiling
     * */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasClearLeapPath(double upwardMotion) {
        AxisAlignedBB startingBounds = this.creature.getEntityBoundingBox();
        double targetOffsetX = this.leapTargetX - this.creature.posX;
        double targetOffsetZ = this.leapTargetZ - this.creature.posZ;
        double horizontalDistance = Math.sqrt(targetOffsetX * targetOffsetX + targetOffsetZ * targetOffsetZ);
        if (horizontalDistance < 1E-6D) return false;

        AxisAlignedBB landingBounds = startingBounds.offset(
                targetOffsetX,
                this.leapTargetY - startingBounds.minY + 1E-3D,
                targetOffsetZ
        );
        if (this.creature.world.collidesWithAnyBlock(landingBounds)) return false;

        double fixedMotionX = 0D;
        double fixedMotionZ = 0D;
        if (!this.verticalLeap) {
            int crossingTicks = Math.max(1, this.ticksAboveHeight(upwardMotion, this.leapObstacleHeight));
            double horizontalMotion = horizontalDistance / crossingTicks;
            fixedMotionX = targetOffsetX / horizontalDistance * horizontalMotion;
            fixedMotionZ = targetOffsetZ / horizontalDistance * horizontalMotion;
        }

        double simulatedX = 0D;
        double simulatedZ = 0D;
        double simulatedHeight = 0D;
        double simulatedVerticalMotion = upwardMotion;
        boolean simulatedObstacleCleared = !this.verticalLeap;
        double clearanceFloor = Math.min(startingBounds.minY, this.leapTargetY)
                + this.leapObstacleHeight + VERTICAL_EDGE_CLEARANCE;

        for (int tick = 0; tick < 80; tick++) {
            if (!simulatedObstacleCleared
                    && simulatedHeight >= this.leapObstacleHeight + VERTICAL_EDGE_CLEARANCE) {
                simulatedObstacleCleared = true;
            }

            double remainingX = targetOffsetX - simulatedX;
            double remainingZ = targetOffsetZ - simulatedZ;
            double horizontalMotionX = 0D;
            double horizontalMotionZ = 0D;
            if (simulatedObstacleCleared) {
                if (this.verticalLeap) {
                    double remainingDistance = Math.sqrt(remainingX * remainingX + remainingZ * remainingZ);
                    if (remainingDistance >= 1E-6D) {
                        int crossingTicks = this.ticksUntilDescendingBelowHeight(
                                simulatedHeight,
                                simulatedVerticalMotion,
                                this.leapObstacleHeight + VERTICAL_EDGE_CLEARANCE
                        );
                        double horizontalMotion = remainingDistance / crossingTicks;
                        horizontalMotionX = remainingX / remainingDistance * horizontalMotion;
                        horizontalMotionZ = remainingZ / remainingDistance * horizontalMotion;
                    }
                }
                else {
                    horizontalMotionX = fixedMotionX;
                    horizontalMotionZ = fixedMotionZ;
                }

                double remainingDistanceSq = remainingX * remainingX + remainingZ * remainingZ;
                double motionDistanceSq = horizontalMotionX * horizontalMotionX + horizontalMotionZ * horizontalMotionZ;
                if (remainingDistanceSq <= motionDistanceSq) {
                    horizontalMotionX = remainingX;
                    horizontalMotionZ = remainingZ;
                }
            }

            double nextX = simulatedX + horizontalMotionX;
            double nextZ = simulatedZ + horizontalMotionZ;
            double nextHeight = simulatedHeight + simulatedVerticalMotion;
            int sampleCount = Math.max(1, (int)Math.ceil(Math.max(
                    Math.max(Math.abs(horizontalMotionX), Math.abs(horizontalMotionZ)),
                    Math.abs(simulatedVerticalMotion)
            ) / OBSTACLE_SCAN_STEP));

            for (int sample = 1; sample <= sampleCount; sample++) {
                double progress = (double)sample / sampleCount;
                double sampleX = simulatedX + horizontalMotionX * progress;
                double sampleZ = simulatedZ + horizontalMotionZ * progress;
                double sampleHeight = simulatedHeight + simulatedVerticalMotion * progress;
                double sampleFeetY = startingBounds.minY + sampleHeight;
                double sampleRemainingX = targetOffsetX - sampleX;
                double sampleRemainingZ = targetOffsetZ - sampleZ;
                boolean reachedLandingHeight = simulatedVerticalMotion <= 0D
                        && sampleFeetY <= this.leapTargetY + 1E-3D;
                if (reachedLandingHeight) {
                    double remainingDistanceSq = sampleRemainingX * sampleRemainingX
                            + sampleRemainingZ * sampleRemainingZ;
                    return remainingDistanceSq <= OBSTACLE_LOOKAHEAD_DISTANCE * OBSTACLE_LOOKAHEAD_DISTANCE;
                }

                AxisAlignedBB sampleBounds = startingBounds.offset(sampleX, sampleHeight, sampleZ);
                double overheadMinimumY = Math.max(sampleBounds.minY, clearanceFloor);
                if (sampleBounds.maxY > overheadMinimumY + 1E-6D) {
                    AxisAlignedBB overheadBounds = new AxisAlignedBB(
                            sampleBounds.minX,
                            overheadMinimumY,
                            sampleBounds.minZ,
                            sampleBounds.maxX,
                            sampleBounds.maxY,
                            sampleBounds.maxZ
                    );
                    if (this.creature.world.collidesWithAnyBlock(overheadBounds)) return false;
                }
            }

            simulatedX = nextX;
            simulatedZ = nextZ;
            simulatedHeight = nextHeight;
            simulatedVerticalMotion = (simulatedVerticalMotion - GRAVITY) * VERTICAL_DRAG;
        }
        return false;
    }

    /**
     * Works out the upward speed needed to reach a particular height.
     * */
    private double upwardVelocityForHeight(double height) {
        double low = 0D;
        double high = Math.max(0.42D, Math.sqrt(2D * GRAVITY * height) + GRAVITY);
        for (int expansion = 0; expansion < 64 && this.simulatedJumpHeight(high) < height; expansion++) {
            high *= 2D;
        }
        for (int iteration = 0; iteration < 48; iteration++) {
            double middle = (low + high) * 0.5D;
            if (this.simulatedJumpHeight(middle) < height) low = middle;
            else high = middle;
        }
        return high;
    }

    /**
     * Runs a tiny jump simulation so the velocity search has something to measure.
     * */
    private double simulatedJumpHeight(double upwardMotion) {
        double height = 0D;
        double motion = upwardMotion;
        for (int tick = 0; tick < 10000 && motion > 0D; tick++) {
            height += motion;
            motion = (motion - GRAVITY) * VERTICAL_DRAG;
        }
        return height;
    }

    /**
     * Counts how many ticks the jump arc stays above the obstacle.
     * */
    private int ticksAboveHeight(double upwardMotion, double targetHeight) {
        double height = 0D;
        double motion = upwardMotion;
        int ticks = 0;
        int ticksAboveHeight = 0;
        do {
            height += motion;
            if (height > targetHeight) ticksAboveHeight++;
            motion = (motion - GRAVITY) * VERTICAL_DRAG;
            ticks++;
        }
        while (height > 0D && ticks < 80);
        return ticksAboveHeight;
    }

    /**
     * Estimates when a falling leap will dip back below the requested height.
     * */
    private int ticksUntilDescendingBelowHeight(double height, double motion, double targetHeight) {
        for (int ticks = 1; ticks <= 80; ticks++) {
            height += motion;
            motion = (motion - GRAVITY) * VERTICAL_DRAG;
            if (motion <= 0D && height <= targetHeight) return Math.max(1, ticks - 1);
        }
        return 80;
    }

    /**
     * This is just the gap check result, along with its landing spot when one is safe.
     * */
    private record GapLeapTarget(boolean safe, double x, double z, double gapStartDistance) {
        /**
         * Makes a successful result with the landing coordinates filled in.
         * */
        private static GapLeapTarget safe(double x, double z) {
            return new GapLeapTarget(true, x, z, 0D);
        }

        /**
         * Makes a blocked result that remembers where the gap started.
         * */
        private static GapLeapTarget blocked(double gapStartDistance) {
            return new GapLeapTarget(false, 0D, 0D, gapStartDistance);
        }
    }
}
