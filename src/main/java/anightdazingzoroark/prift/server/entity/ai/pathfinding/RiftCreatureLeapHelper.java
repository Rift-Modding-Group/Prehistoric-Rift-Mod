package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.MoveResult;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Everything relating to managing a creature's ability to leap happens here
 * */
@SuppressWarnings("SuspiciousNameCombination")
public class RiftCreatureLeapHelper {
    public static final int MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS = 20;
    public static final int MAXIMUM_RIDDEN_LEAP_COOLDOWN_TICKS = MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS * 2;
    private static final double GRAVITY = 0.08D; //wherever this came from, we will never know....
    private static final double VERTICAL_DRAG = 0.98D;
    private static final double OBSTACLE_SCAN_STEP = 0.125D;
    private static final double OBSTACLE_LOOKAHEAD_DISTANCE = 0.25D;
    private static final double GAP_SCAN_STEP = 0.25D;
    private static final double GAP_SEARCH_ANGLE_STEP = Math.toRadians(5D);
    private static final int GAP_SEARCH_ANGLE_STEPS = 12;
    private static final double SUPPORT_PROBE_RADIUS = 0.05D;
    private static final double MAXIMUM_NON_GAP_DROP = RiftCreatureMoveHelper.STANDARD_JUMP_HEIGHT + 1E-3D;

    @NotNull
    private final RiftCreatureMoveHelperBase moveHelper;
    @NotNull
    private final RiftCreature creature;
    @NotNull
    private LeapPhase phase = LeapPhase.IDLE;
    private int phaseTicks;
    private float leapYaw;
    private boolean riddenLeap;
    @Nullable
    private CreatureLeapPlan leapPlan;
    @Nullable
    private RiddenLeapRequest.Charged queuedCharge;

    public RiftCreatureLeapHelper(@NotNull RiftCreatureMoveHelperBase moveHelper, @NotNull RiftCreature creature) {
        this.moveHelper = moveHelper;
        this.creature = creature;
    }

    /**
     * Prepares a safe AI destination using the same plan consumed by rider-controlled leaps.
     * */
    public boolean prepareLeapTo(double x, double y, double z) {
        if (this.phase != LeapPhase.IDLE || this.isLeaping()) return false;
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        this.resetDelay();
        if (navigation == null || !navigation.getCanLeap() || this.creature.getStamina() <= 0f
                || !CreatureLeapPlan.hasTakeoffSupport(this.creature) || this.creature.bodyTouchingLiquid()
                || !this.isWithinLeapDistance(Math.hypot(x - this.creature.posX, z - this.creature.posZ))
                || !this.hasSafeLandingNode(x, y, z)
        ) {
            return false;
        }
        double obstacleHeight = Math.max(y - this.creature.posY, this.getObstacleClearance(x, z, navigation.getLeapHeight()));
        this.leapPlan = CreatureLeapPlan.navigation(this.creature, this.creature.getEntityBoundingBox(), new Vec3d(x, y, z), obstacleHeight);
        this.riddenLeap = false;
        this.phaseTicks = 0;
        if (this.leapPlan == null) return false;
        this.phase = LeapPhase.AI_WINDUP;
        this.leapYaw = (float) Math.toDegrees(Math.atan2(this.creature.posX - x, z - this.creature.posZ));
        return true;
    }

    public boolean isLeaping() {
        return this.moveHelper.creatureAction == RiftCreatureMoveHelperBase.CreatureAction.LEAP;
    }

    public boolean isLeapActive() {
        return this.phase == LeapPhase.ACTIVE && this.isLeaping();
    }

    public boolean isRiddenLeap() {
        return this.riddenLeap && this.isLeaping();
    }

    public void resetDelay() {
        if (this.phase == LeapPhase.AI_WINDUP) this.phaseTicks = 0;
    }

    /**
     * Selects a charged forward/upward plan, or uses the server-approved aimed plan.
     * */
    public boolean startRiddenLeap(float yaw, boolean movingForward, int chargeTicks, @Nullable CreatureLeapPlan plan) {
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (this.phase != LeapPhase.IDLE && this.phase != LeapPhase.RIDER_WINDUP
                || this.isLeaping() || navigation == null || !navigation.getCanLeap()
        ) {
            return false;
        }
        if (plan != null && !plan.isClear(this.creature)) return false;
        CreatureLeapPlan selectedPlan = plan;
        if (selectedPlan == null) selectedPlan = CreatureLeapPlan.charged(this.creature, yaw, movingForward, chargeTicks);
        if (selectedPlan == null) return false;
        return this.startLeap(selectedPlan, true, yaw);
    }

    /**
     * All sources enter the same launch lifecycle. Stamina/approval belong to the requesting controller.
     * */
    private boolean startLeap(@NotNull CreatureLeapPlan plan, boolean ridden, float fallbackYaw) {
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (navigation == null || !navigation.getCanLeap() || !CreatureLeapPlan.hasTakeoffSupport(this.creature)
                || this.creature.bodyTouchingLiquid()
        ) {
            return false;
        }
        this.leapPlan = plan;
        this.riddenLeap = ridden;
        this.leapYaw = fallbackYaw;
        if (Math.hypot(plan.landing().x - plan.start().x, plan.landing().z - plan.start().z) > 1E-6D) {
            this.leapYaw = (float) Math.toDegrees(Math.atan2(plan.start().x - plan.landing().x, plan.landing().z - plan.start().z));
        }
        this.creature.setPosition(plan.start().x, plan.start().y, plan.start().z);
        this.phase = LeapPhase.ACTIVE;
        this.phaseTicks = 0;
        this.queuedCharge = null;
        this.moveHelper.creatureAction = RiftCreatureMoveHelperBase.CreatureAction.LEAP;
        Vec3d velocity = plan.velocity(0D);
        this.creature.motionX = velocity.x;
        this.creature.motionY = velocity.y;
        this.creature.motionZ = velocity.z;
        this.creature.fallDistance = 0f;
        this.creature.isAirBorne = true;
        this.creature.velocityChanged = true;
        this.creature.getCreaturePathNavigate().clearPath();
        this.creature.setSprinting(false);
        this.moveHelper.stopWalkingControls();
        ForgeHooks.onLivingJump(this.creature);
        return true;
    }

    public void read(@NotNull RiftCreatureLeapHelper that) {
        this.phase = that.phase;
        this.phaseTicks = that.phaseTicks;
        this.leapYaw = that.leapYaw;
        this.riddenLeap = that.riddenLeap;
        this.leapPlan = that.leapPlan;
        this.queuedCharge = that.queuedCharge;
    }

    /**
     * Handles rider input and server approval without maintaining a second leap state on the creature.
     * */
    @Nullable
    public CreatureLeapPlan jumpFromRider(@NotNull EntityPlayer rider, @NotNull RiddenLeapRequest request) {
        if (this.creature.getControllingPassenger() != rider) return null;
        if (request instanceof RiddenLeapRequest.Approval(CreatureLeapPlan acceptedPlan)) {
            if (!this.creature.world.isRemote || this.phase != LeapPhase.AWAITING_APPROVAL) return null;
            this.cancelLeap();
            // Approval may arrive after the server consumed the rider's last stamina.
            if (acceptedPlan == null || !this.creature.riderCanControl() || !CreatureLeapPlan.hasTakeoffSupport(this.creature)
                    || this.creature.bodyTouchingLiquid()
            ) {
                return null;
            }
            this.leapPlan = acceptedPlan;
            this.riddenLeap = true;
            this.phase = LeapPhase.RIDER_WINDUP;
            return acceptedPlan;
        }

        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (navigation == null || !navigation.getCanLeap() || !this.creature.riderCanControl()) return null;
        boolean aimed = request instanceof RiddenLeapRequest.Aimed;
        if ((this.creature.world.isRemote || aimed) && !this.canChargeRiddenLeap(rider)) return null;
        if (!this.creature.world.isRemote && (this.creature.bodyTouchingLiquid() || this.isLeaping()
                || this.phase != LeapPhase.IDLE || this.creature.getLeapCooldown() > 0)) return null;

        RiddenLeapRequest.Charged charge = null;
        CreatureLeapPlan plan;
        switch (request) {
            case RiddenLeapRequest.Charged(int chargeTicks, boolean movingForward) -> {
                charge = new RiddenLeapRequest.Charged(
                        Math.clamp(chargeTicks, 1, MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS),
                        movingForward || rider.moveForward > 0f
                );
                plan = CreatureLeapPlan.charged(this.creature, this.creature.renderYawOffset, charge.movingForward(), charge.chargeTicks());
            }
            case RiddenLeapRequest.Aimed aim -> plan = CreatureLeapPlan.create(this.creature, aim.target());
            default -> throw new IllegalStateException("Unexpected value: " + request);
        }
        if (plan == null) return null;

        if (this.creature.world.isRemote) {
            this.riddenLeap = true;
            this.phaseTicks = 0;
            if (aimed) {
                this.phase = LeapPhase.AWAITING_APPROVAL;
                this.leapPlan = plan;
                this.queuedCharge = null;
                this.creature.motionX = 0D;
                this.creature.motionY = 0D;
                this.creature.motionZ = 0D;
            }
            else {
                this.phase = LeapPhase.RIDER_WINDUP;
                this.queuedCharge = charge;
                this.leapPlan = null;
                this.creature.rotationYaw = this.creature.renderYawOffset;
            }
        }
        else {
            if (!this.creature.useStamina(MoveResult.LEAP.staminaConsumption())) return null;
            if (aimed) this.creature.stopRiddenAim();
            this.riddenLeap = true;
            this.phase = CreatureLeapPlan.hasTakeoffSupport(this.creature) ? LeapPhase.REMOTE_TAKEOFF : LeapPhase.REMOTE_AIRBORNE;
            this.phaseTicks = 0;
            this.leapPlan = plan;
            this.queuedCharge = null;
            this.moveHelper.creatureAction = RiftCreatureMoveHelperBase.CreatureAction.LEAP;
            this.creature.rotationYaw = this.creature.renderYawOffset;
            this.creature.getCreaturePathNavigate().clearPath();
            this.creature.setSprinting(false);
            this.creature.setLeaping(true);
        }
        return plan;
    }

    public boolean canChargeRiddenLeap(@NotNull EntityPlayer rider) {
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        return navigation != null && navigation.getCanLeap() && this.creature.getControllingPassenger() == rider
                && this.phase == LeapPhase.IDLE && !this.isLeaping()
                && !this.creature.getIsSleeping() && !this.creature.isStaggered()
                && CreatureLeapPlan.hasTakeoffSupport(this.creature) && !this.creature.bodyTouchingLiquid()
                && this.creature.getCurrentMove().isEmpty() && this.creature.getLeapCooldown() <= 0 && this.creature.getStamina() > 0f;
    }

    public boolean isRiddenLeapPendingOrActive() {
        return this.riddenLeap && this.phase != LeapPhase.IDLE;
    }

    /** The tick hook tracks replicated rider movement; the travel hook exclusively advances local leap movement. */
    public boolean updateLeap(@NotNull UpdatePhase updatePhase) {
        if (updatePhase == UpdatePhase.TICK) {
            if (this.creature.world.isRemote) return false;
            if (this.creature.getLeapCooldown() > 0) this.creature.setLeapCooldown(this.creature.getLeapCooldown() - 1);
            if (this.phase == LeapPhase.REMOTE_TAKEOFF || this.phase == LeapPhase.REMOTE_AIRBORNE) {
                this.phaseTicks++;
                boolean supported = CreatureLeapPlan.hasTakeoffSupport(this.creature);
                if (!supported) this.phase = LeapPhase.REMOTE_AIRBORNE;
                boolean landed = this.phase == LeapPhase.REMOTE_AIRBORNE && supported;
                boolean blocked = this.phase == LeapPhase.REMOTE_AIRBORNE && this.creature.collidedHorizontally;
                if (landed || blocked) this.creature.setLeapCooldown(MAXIMUM_RIDDEN_LEAP_COOLDOWN_TICKS);
                if (this.creature.bodyTouchingLiquid() || this.phaseTicks > 80 || landed || blocked) this.cancelLeap();
            }
            this.creature.setLeaping(this.isLeaping());
            return false;
        }

        if ((this.phase == LeapPhase.AWAITING_APPROVAL || this.phase == LeapPhase.RIDER_WINDUP)
                && (!this.creature.isBeingRidden() || !this.creature.riderCanControl())) {
            this.cancelLeap();
            return false;
        }
        if (this.phase == LeapPhase.AWAITING_APPROVAL) {
            if (++this.phaseTicks >= 80) this.cancelLeap();
            this.creature.motionX = 0D;
            this.creature.motionY = 0D;
            this.creature.motionZ = 0D;
            return true;
        }
        if (this.phase == LeapPhase.REMOTE_TAKEOFF || this.phase == LeapPhase.REMOTE_AIRBORNE) return true;
        if (this.phase == LeapPhase.RIDER_WINDUP && CreatureLeapPlan.hasTakeoffSupport(this.creature)) {
            CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
            if (navigation == null || !navigation.getCanLeap()) this.cancelLeap();
            else if (this.phaseTicks >= navigation.getLeapDelay()) {
                this.creature.rotationYaw = this.creature.renderYawOffset;
                int chargeTicks = MAXIMUM_RIDDEN_LEAP_CHARGE_TICKS;
                boolean movingForward = true;
                if (this.queuedCharge != null) {
                    chargeTicks = this.queuedCharge.chargeTicks();
                    movingForward = this.queuedCharge.movingForward();
                }
                if (!this.startRiddenLeap(this.creature.rotationYaw, movingForward, chargeTicks, this.leapPlan)) this.cancelLeap();
            }
            else {
                this.phaseTicks++;
                if (this.leapPlan != null) {
                    this.creature.motionX = 0D;
                    this.creature.motionY = 0D;
                    this.creature.motionZ = 0D;
                    return true;
                }
            }
        }
        if (!this.isLeapActive()) return false;
        this.advanceLeap();
        return true;
    }

    /**
     * Figures out whether this movement needs a gap leap, obstacle leap, or no leap at all.
     * */
    public boolean tryHandleLeap(
            double targetX, double targetY, double targetZ,
            double obstacleClearance, RiftCreatureMoveHelperBase.CreatureAction requestedAction
    ) {
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (navigation == null || !navigation.getCanLeap() || this.creature.bodyTouchingLiquid()) return false;

        double displacementY = targetY - this.creature.posY;
        double standardJumpClearance = RiftCreatureMoveHelper.STANDARD_JUMP_CLEARANCE;
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
     * AI requests wait for their configured windup, then enter the shared launch path.
     */
    public void tryStartLeap() {
        if (this.phase != LeapPhase.AI_WINDUP) return;
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        if (this.leapPlan == null || navigation == null || !navigation.getCanLeap()
                || !CreatureLeapPlan.hasTakeoffSupport(this.creature) || this.creature.bodyTouchingLiquid()
        ) {
            this.cancelLeap();
            return;
        }
        this.faceLeapTarget();
        this.moveHelper.stopWalkingControls();
        if (++this.phaseTicks <= navigation.getLeapDelay()) return;
        Vec3d landing = this.leapPlan.landing();
        double obstacleHeight = Math.max(landing.y - this.creature.posY,
                this.getObstacleClearance(landing.x, landing.z, navigation.getLeapHeight()));
        CreatureLeapPlan plan = CreatureLeapPlan.navigation(this.creature, this.creature.getEntityBoundingBox(), landing, obstacleHeight);
        if (plan == null) {
            this.creature.getCreaturePathNavigate().clearPath();
            this.cancelLeap();
            return;
        }
        if (!this.creature.useStamina(MoveResult.LEAP.staminaConsumption())) {
            this.cancelLeap();
            return;
        }
        this.startLeap(plan, false, this.leapYaw);
    }

    /**
     * Advances AI, charged, and aimed leaps once from travel, using the same collision-aware motion.
     * */
    public void advanceLeap() {
        if (!this.isLeapActive() || this.leapPlan == null) return;
        if (this.creature.bodyTouchingLiquid()) {
            this.cancelLeap();
            this.creature.motionX = 0D;
            this.creature.motionY = 0D;
            this.creature.motionZ = 0D;
            return;
        }
        this.faceLeapTarget();
        this.moveHelper.stopWalkingControls();
        Vec3d previousPosition = this.creature.getPositionVector();
        boolean followingThrough = this.phaseTicks >= this.leapPlan.durationTicks();
        int nextTick = this.phaseTicks + 1;
        double progress = (double) nextTick / this.leapPlan.durationTicks();
        Vec3d nextPosition;
        Vec3d nextVelocity;
        if (followingThrough) {
            nextPosition = previousPosition.add(this.creature.motionX, this.creature.motionY, this.creature.motionZ);
            nextVelocity = new Vec3d(this.creature.motionX * 0.91D,
                    (this.creature.motionY - GRAVITY) * VERTICAL_DRAG, this.creature.motionZ * 0.91D);
        }
        else {
            nextPosition = this.leapPlan.position(progress);
            nextVelocity = this.leapPlan.velocity(progress);
        }
        for (int sample = 1; sample <= CreatureLeapPlan.SAMPLES_PER_TICK; sample++) {
            double sampleX = MathUtil.slopeResult(sample, true, 0, CreatureLeapPlan.SAMPLES_PER_TICK, previousPosition.x, nextPosition.x);
            double sampleY = MathUtil.slopeResult(sample, true, 0, CreatureLeapPlan.SAMPLES_PER_TICK, previousPosition.y, nextPosition.y);
            double sampleZ = MathUtil.slopeResult(sample, true, 0, CreatureLeapPlan.SAMPLES_PER_TICK, previousPosition.z, nextPosition.z);
            // Delayed lateral motion can begin within this tick; sample the same piecewise plan used by validation.
            if (!followingThrough) {
                Vec3d plannedSample = this.leapPlan.position((this.phaseTicks + (double) sample / CreatureLeapPlan.SAMPLES_PER_TICK)
                        / this.leapPlan.durationTicks());
                sampleX = plannedSample.x;
                sampleY = plannedSample.y;
                sampleZ = plannedSample.z;
            }
            this.creature.move(MoverType.SELF, sampleX - this.creature.posX, sampleY - this.creature.posY, sampleZ - this.creature.posZ);
            if (Math.abs(this.creature.posX - sampleX) > 1E-4D || Math.abs(this.creature.posY - sampleY) > 1E-4D
                    || Math.abs(this.creature.posZ - sampleZ) > 1E-4D) break;
        }
        this.creature.motionX = nextVelocity.x;
        this.creature.motionY = nextVelocity.y;
        this.creature.motionZ = nextVelocity.z;
        this.creature.fallDistance = 0f;
        this.creature.isAirBorne = true;
        this.creature.velocityChanged = true;
        this.phaseTicks = nextTick;
        boolean blocked = Math.abs(this.creature.posX - nextPosition.x) > 1E-4D
                || Math.abs(this.creature.posY - nextPosition.y) > 1E-4D
                || Math.abs(this.creature.posZ - nextPosition.z) > 1E-4D;
        boolean landed = this.creature.onGround && nextTick > 1;
        if (!blocked && !landed && nextTick == this.leapPlan.durationTicks() && CreatureLeapPlan.hasTakeoffSupport(this.creature)) {
            this.creature.move(MoverType.SELF, 0D, -1E-3D, 0D);
            landed = this.creature.onGround;
        }
        if (blocked || landed) {
            if (this.riddenLeap) this.creature.setLeapCooldown(MAXIMUM_RIDDEN_LEAP_COOLDOWN_TICKS);
            else this.creature.resetLeapToAttackCooldown();
            this.cancelLeap();
            this.creature.motionX = 0D;
            this.creature.motionY = 0D;
            this.creature.motionZ = 0D;
        }
    }

    public void cancelLeap() {
        this.moveHelper.creatureAction = RiftCreatureMoveHelperBase.CreatureAction.WAIT;
        this.phase = LeapPhase.IDLE;
        this.phaseTicks = 0;
        this.riddenLeap = false;
        this.leapPlan = null;
        this.queuedCharge = null;
    }

    private void stopHorizontalMovement() {
        this.moveHelper.stopWalkingControls();
        this.creature.motionX = 0D;
        this.creature.motionZ = 0D;
    }

    /**
     * Keeps the body pointed at the chosen landing spot without overriding the head's look direction.
     * */
    private void faceLeapTarget() {
        this.creature.rotationYaw = this.leapYaw;
        this.creature.renderYawOffset = this.leapYaw;
    }

    /**
     * Makes sure a landing is neither right underfoot nor beyond leap range.
     * */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isWithinLeapDistance(double horizontalDistance) {
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        return navigation != null && horizontalDistance > 1E-6D && horizontalDistance <= navigation.getLeapDistance();
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
        CreatureNavigationBuilder navigation = this.creature.getNavigationBuilder();
        double displacementX = targetX - this.creature.posX;
        double displacementZ = targetZ - this.creature.posZ;
        double horizontalDistance = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
        if (navigation == null || !this.creature.onGround || horizontalDistance < 1E-6D) return null;

        double directionX = displacementX / horizontalDistance;
        double directionZ = displacementZ / horizontalDistance;
        double leapDistance = navigation.getLeapDistance();
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
        return directTarget.gapStartDistance <= stoppingDistance + 1E-6D ? directTarget : null;
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
     * Checks whether the sample is still on ordinary walking terrain. A deeper floor may make a
     * fall survivable, but it must still count as a gap so a leap across it remains possible.
     * */
    private boolean hasSupportAt(double x, double z, double feetY) {
        AxisAlignedBB supportProbe = new AxisAlignedBB(
                x - SUPPORT_PROBE_RADIUS,
                feetY - MAXIMUM_NON_GAP_DROP,
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
                feetY + RiftCreatureMoveHelper.STANDARD_JUMP_CLEARANCE,
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
        return this.getObstacleClearance(this.creature.getEntityBoundingBox(), targetX, targetZ, maximumHeight
        );
    }

    public double getPlannedObstacleClearance(
            @NotNull AxisAlignedBB startingBounds,
            double targetX, double targetZ, double maximumHeight
    ) {
        return this.getObstacleClearance(startingBounds, targetX, targetZ, maximumHeight);
    }

    private double getObstacleClearance(
            @NotNull AxisAlignedBB startingBounds,
            double targetX, double targetZ, double maximumHeight
    ) {
        double startingCenterX = (startingBounds.minX + startingBounds.maxX) * 0.5D;
        double startingCenterZ = (startingBounds.minZ + startingBounds.maxZ) * 0.5D;
        double displacementX = targetX - startingCenterX;
        double displacementZ = targetZ - startingCenterZ;
        double horizontalDistance = Math.sqrt(displacementX * displacementX + displacementZ * displacementZ);
        if (horizontalDistance < 1E-6D) return 0D;

        double probeDistance = Math.min(horizontalDistance, OBSTACLE_LOOKAHEAD_DISTANCE);
        double offsetX = displacementX / horizontalDistance * probeDistance;
        double offsetZ = displacementZ / horizontalDistance * probeDistance;
        AxisAlignedBB xProbe = this.createXObstacleProbe(startingBounds, offsetX, offsetZ);
        AxisAlignedBB zProbe = this.createZObstacleProbe(startingBounds, offsetX, offsetZ);
        if (!this.collidesWithObstacleProbe(xProbe, zProbe, 0D)) return 0D;

        for (double clearance = OBSTACLE_SCAN_STEP; clearance <= maximumHeight + 1E-6D; clearance += OBSTACLE_SCAN_STEP) {
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

    /**
     * Selects which creature update hook is calling the helper, keeping movement from advancing twice per tick.
     * */
    public enum UpdatePhase {
        //called from onLivingUpdate for server cooldowns, rider takeoff/landing tracking, and pose synchronization.
        TICK,
        //called from travel to process rider approval/windup and advance locally simulated AI or rider leaps.
        TRAVEL
    }

    /**
     * Tracks the leap's lifecycle; each phase determines how the shared phaseTicks timer is used.
     * */
    private enum LeapPhase {
        //no leap is queued or running
        IDLE,
        //the AI has a validated destination and is waiting through its launch delay
        AI_WINDUP,
        //the rider's charged jump or approved aimed leap is queued for launch.
        RIDER_WINDUP,
        //the rider's client holds its takeoff position while awaiting server acceptance of an aimed leap.
        AWAITING_APPROVAL,
        //this side simulates the trajectory and its airborne continuation until landing or collision.
        ACTIVE,
        //the server accepted a rider leap and is waiting for client-controlled movement to leave the ground.
        REMOTE_TAKEOFF,
        //the server observes the rider's airborne movement until landing, collision, or timeout.
        REMOTE_AIRBORNE
    }
}
