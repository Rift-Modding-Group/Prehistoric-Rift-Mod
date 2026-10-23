package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.RiftCreaturePathNavigate;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.AttackApproachPath;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UseMoveMoveResultTicker extends AbstractMoveResultTicker {
    @NotNull
    private final String selectedMoveName;
    @NotNull
    private final CreatureMoveBuilder selectedMoveBuilder;
    private final boolean canUseBlockBreak;
    private boolean wasRotatingWhileCharging;
    private boolean hasExecutedMove; //flag to set to true when a move is currently being used by a creature
    private boolean executingBlockBreak;
    private boolean blockBreakApplied;
    private int blockBreakEffectAttemptCountAtMoveStart;
    private int moveFinishCountAtMoveStart;

    //---pathing related stuff---
    private int repathCooldown;
    private boolean hasLastTargetPos;
    private double lastTargetX;
    private double lastTargetY;
    private double lastTargetZ;
    private int pathingFrustrationTicks;

    //---frustration related stuff---
    private int attackTargetHitCountAtMoveStart;
    private final boolean selectedMoveUsedDueToFrustration;

    public UseMoveMoveResultTicker(@NotNull RiftCreature creature, @NotNull MoveRuleBuilder moveRuleBuilder) {
        super(creature, moveRuleBuilder);
        this.selectedMoveName = moveRuleBuilder.getMoveName();
        this.selectedMoveBuilder = Objects.requireNonNull(this.creature.getCreatureMoves().getUsableMoveBuilder(moveRuleBuilder.getMoveName()));
        this.canUseBlockBreak = creature.hasBlockBreakZone()
                && moveRuleBuilder.getUseBlockBreak()
                && !moveRuleBuilder.getDontPathToTarget()
                && this.selectedMoveBuilder.getRequireFindTargetToUse()
                && this.selectedMoveBuilder.getMoveType() != CreatureMoveBuilder.MoveType.STATUS
                && this.selectedMoveBuilder.getMakesContact();
        this.selectedMoveUsedDueToFrustration = this.moveRuleBuilder.getUseWhenFrustrated() && this.creature.atFrustrationThreshold();
        this.creature.setUseBlockBreak(false);
    }

    @Override
    public boolean canContinueTicking() {
        EntityLivingBase target = this.creature.getAttackTarget();

        boolean targetAvailability = target != null && target.isEntityAlive();
        boolean moveBuilderTargetCondition = !this.selectedMoveBuilder.getRequireFindTargetToUse() || targetAvailability;

        //move execution depends on if current move hasnt been reset
        if (this.hasExecutedMove) return !this.creature.getCurrentMove().isEmpty();
        //when move should not path to its target, stop if target is not already in range
        else if (this.moveRuleBuilder.getDontPathToTarget()
                && this.selectedMoveBuilder.getRequireFindTargetToUse()
                && targetAvailability
                && !this.selectedMoveUsedDueToFrustration
                && !CreatureMoveHelper.targetMatchesAnyDetectionRule(this.moveRuleBuilder, this.creature, target)
        ) {
            return false;
        }
            //when frustration gets high enough, stop current pathing and pick a frustration option
        else if (!this.selectedMoveUsedDueToFrustration
                && !this.creature.getUseBlockBreak()
                && this.creature.atFrustrationThreshold()
        ) {
            return false;
        }
        //if creature hasnt executed move yet, keep it true to keep it running
        //only thing stopping it is if target is gone (if said move requires it)
        else return moveBuilderTargetCondition;
    }

    @Override
    public void onUpdate() {
        EntityLivingBase target = this.creature.getAttackTarget();
        RiftCreaturePathNavigate blockBreakNavigation = this.creature.getCreaturePathNavigate();
        boolean useBlockBreakPath = target != null && target.isEntityAlive() && this.canUseBlockBreak
                && blockBreakNavigation.shouldUseBlockBreakPath(target);
        float requiredStamina = MoveResult.USE_MOVE.staminaConsumption(this.selectedMoveBuilder);
        if (!this.hasExecutedMove && !useBlockBreakPath
                && requiredStamina > 0f && this.creature.getStamina() <= 0f) {
            this.creature.setUseBlockBreak(false);
            blockBreakNavigation.clearPath();
            return;
        }

        //---when move is already being used, stop pathing---
        if (this.hasExecutedMove && !this.creature.getCurrentMove().isEmpty()) {
            //Offense-hitbox moves do not necessarily emit moveHitEffect. Non-charge moves
            //therefore use an end fallback; charged moves use their normal release lifecycle.
            if (this.executingBlockBreak && !this.blockBreakApplied) {
                if (this.creature.getBlockBreakEffectAttemptCount() != this.blockBreakEffectAttemptCountAtMoveStart) {
                    this.blockBreakApplied = true;
                }
            }
            CreatureMoveChargeupBuilder charge = this.selectedMoveBuilder.getMoveChargeupBuilder();
            boolean rotatingWhileCharging = this.creature.canRotateWhileCharging();
            if (this.wasRotatingWhileCharging && !rotatingWhileCharging) {
                this.hasLastLookDirection = true;
                this.lastRotationYawHead = this.creature.renderYawOffset;
                this.lastPrevRotationYawHead = this.creature.prevRenderYawOffset;
                this.lastRotationPitch = this.creature.rotationPitch;
                this.lastPrevRotationPitch = this.creature.prevRotationPitch;
            }
            this.wasRotatingWhileCharging = rotatingWhileCharging;
            if (charge != null && charge.getCanLookWhileUsing()) {
                // Live head tracking replaces the heading frozen at move start.
                this.hasLastLookDirection = false;
                if (target != null && target.isEntityAlive()) {
                    this.creature.getLookHelper().setLookPositionWithEntity(target, 30f, this.creature.getVerticalFaceSpeed());
                }
            }
            else if (target != null && target.isEntityAlive() && rotatingWhileCharging) {
                this.creature.getLookHelper().setLookPositionWithEntity(target, 30f, this.creature.getVerticalFaceSpeed());
                this.hasLastLookDirection = true;
                this.lastRotationYawHead = this.creature.getAnimationData().getModelYaw();
                this.lastPrevRotationYawHead = this.creature.prevRenderYawOffset;
                this.lastRotationPitch = this.creature.rotationPitch;
                this.lastPrevRotationPitch = this.creature.prevRotationPitch;
            }
            else if (target != null && target.isEntityAlive() && this.moveRuleBuilder.getDontPathToTarget()
                    && (charge == null || !charge.getCanRotateWhileCharging())) {
                this.creature.getLookHelper().setLookPositionWithEntity(target, 30f, this.creature.getVerticalFaceSpeed());
            }
            else if (target != null && target.isEntityAlive()
                    && this.selectedMoveBuilder.getMoveChargeupBuilder() != null
                    && this.selectedMoveBuilder.getMoveChargeupBuilder().getCanRotateWhileReleasing()
                    && this.creature.getCreatureMoves().currentMoveMatches(this.selectedMoveName, ChargeupPhase.RELEASING)
            ) {
                double targetX = target.posX - this.creature.posX;
                double targetZ = target.posZ - this.creature.posZ;
                if (targetX * targetX + targetZ * targetZ >= 1E-4D) {
                    float targetYaw = (float)(Math.atan2(targetZ, targetX) * 180f / (float) Math.PI) - 90f;
                    this.setLookDirection(targetYaw);
                }
            }
            else this.preserveLastLookDirection();
            this.pathingFrustrationTicks = 0;
            this.creature.getCreaturePathNavigate().clearPath();
        }
        //---pathing to go to target is all dealt with here, if said move requires target---
        else if (this.selectedMoveBuilder.getRequireFindTargetToUse() && target != null && target.isEntityAlive()) {
            boolean dontPathToTarget = this.moveRuleBuilder.getDontPathToTarget();

            //moves without pathing can look at the target without competing with navigation
            if (dontPathToTarget) {
                this.creature.getLookHelper().setLookPositionWithEntity(target, 30f, this.creature.getVerticalFaceSpeed());
            }
            else {
                this.hasLastLookDirection = true;
                this.lastRotationYawHead = this.creature.rotationYawHead;
                this.lastPrevRotationYawHead = this.creature.prevRotationYawHead;
                this.lastRotationPitch = this.creature.rotationPitch;
                this.lastPrevRotationPitch = this.creature.prevRotationPitch;
            }

            boolean targetWithinRange = CreatureMoveHelper.targetMatchesAnyDetectionRule(this.moveRuleBuilder, this.creature, target);
            this.creature.setUseBlockBreak(useBlockBreakPath);
            if (useBlockBreakPath) {
                this.creature.setUnableToPathToTarget(false);
                Vec3d approachPosition = blockBreakNavigation.getBlockBreakApproachPosition();
                this.faceBlockBreakDirection(
                        approachPosition == null ? target.posX : approachPosition.x,
                        approachPosition == null ? target.posZ : approachPosition.z
                );
            }
            boolean breakableBlocksInFront = useBlockBreakPath && this.creature.hasBreakableBlocksInFront();

            //execute move when target is in range
            if (breakableBlocksInFront || !useBlockBreakPath && (targetWithinRange || this.selectedMoveUsedDueToFrustration)) {
                // Use the same target-facing heading as prediction, without adding saved neck yaw twice.
                if (this.selectedMoveBuilder.getUseLookBasedFacing()) {
                    float moveYaw = this.creature.getAnimationData().getModelYaw();
                    if (!breakableBlocksInFront) moveYaw = CreatureLookUtil.getLookBasedAttackYaw(this.creature, target);
                    this.setLookDirection(moveYaw);
                }
                else if (!dontPathToTarget) {
                    double targetX = target.posX - this.creature.posX;
                    double targetZ = target.posZ - this.creature.posZ;
                    //only rotate if target direction is usable
                    if (targetX * targetX + targetZ * targetZ >= 1E-4D) {
                        float targetYaw = (float)(Math.atan2(targetZ, targetX) * 180f / (float) Math.PI) - 90f;
                        this.setLookDirection(targetYaw);
                    }
                }

                //execute move
                this.creature.setCurrentMove(this.selectedMoveName);
                if (!this.creature.getCurrentMove().equals(this.selectedMoveName)) {
                    this.creature.setUseBlockBreak(false);
                    return;
                }
                //offensive use pays the move cost; clearing planned route obstructions does not
                if (!breakableBlocksInFront && !this.creature.useStamina(this.selectedMoveBuilder.getStaminaCost())) {
                    this.creature.setCurrentMove("");
                    this.creature.setUseBlockBreak(false);
                    return;
                }

                this.hasExecutedMove = true;
                this.executingBlockBreak = breakableBlocksInFront;
                if (this.executingBlockBreak) this.creature.snapshotBlockBreakPlan();
                this.blockBreakEffectAttemptCountAtMoveStart = this.creature.getBlockBreakEffectAttemptCount();
                this.moveFinishCountAtMoveStart = this.creature.getMoveFinishCount();
                this.creature.setUseBlockBreak(this.executingBlockBreak);
                this.attackTargetHitCountAtMoveStart = this.creature.getAttackTargetHitCount();
                if (this.selectedMoveUsedDueToFrustration) this.creature.resetFrustration();
                if (this.selectedMoveBuilder.getOnMoveBeginEffect() != null) {
                    this.selectedMoveBuilder.getOnMoveBeginEffect().accept(this.creature, target);
                }

                //stop pathing
                this.pathingFrustrationTicks = 0;
                this.creature.getCreaturePathNavigate().clearPath();
            }
            //---when move should not path to target, stop moving and wait for another move selection---
            else if (dontPathToTarget) {
                this.repathCooldown = 0;
                this.pathingFrustrationTicks = 0;
                this.creature.getCreaturePathNavigate().clearPath();
            }
            //pathing to ensure target can be found by creature
            else {
                //add frustration when pathing to the target takes too long
                if (!useBlockBreakPath) {
                    this.pathingFrustrationTicks++;
                    if (this.creature.atPathingFrustrationInterval(this.pathingFrustrationTicks)) {
                        this.pathingFrustrationTicks = 0;
                        this.creature.addFrustration(20);
                    }
                }

                //tick down repath cooldown
                if (this.repathCooldown > 0) this.repathCooldown--;
                PathNavigate creatureNavigation = this.creature.getCreaturePathNavigate();

                //Follow any validated local leap needed before the wall, then approach the
                //remaining obstruction directly. The move is not fired until an animated
                //frontZone overlaps a planned block which this creature can break.
                if (useBlockBreakPath) {
                    this.repathCooldown = 0;
                    if (!blockBreakNavigation.tryMoveAlongBlockBreakApproach(1D)) {
                        creatureNavigation.clearPath();
                        this.creature.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1D);
                    }
                    this.rememberTargetPos(target);
                    return;
                }

                boolean targetMoved = !this.hasLastTargetPos || target.getDistanceSq(this.lastTargetX, this.lastTargetY, this.lastTargetZ) > 1D;
                boolean shouldRepath = this.repathCooldown <= 0 && (creatureNavigation.noPath() || targetMoved);

                //---when target moved or path ended, try to repath---
                if (shouldRepath) {
                    this.rememberTargetPos(target);
                    this.repathCooldown = 4 + this.creature.world.rand.nextInt(7);
                    Path approachPath = null;
                    for (DetectionRule detection : this.moveRuleBuilder.getDetectionRules()) {
                        if (!(detection instanceof AnimatedHitboxDetectionRule animated)) continue;
                        approachPath = AttackApproachPath.findPath(this.creature, target, animated);
                        if (approachPath != null) break;
                    }
                    if (approachPath != null) {
                        // Ensure setPath retains the attack-position path even if its nodes match the old path.
                        creatureNavigation.clearPath();
                    }
                    if (approachPath == null || !creatureNavigation.setPath(approachPath, 1D)) {
                        creatureNavigation.tryMoveToEntityLiving(target, 1D);
                    }
                    else this.creature.setUnableToPathToTarget(false);
                }

                //---when navigator has no path, keep directly moving to target---
                if (creatureNavigation.noPath()) {
                    if (creatureNavigation.getPath() instanceof AttackApproachPath) {
                        // Let the look pose settle at the chosen attack position before replanning.
                        this.creature.getCreatureMoveHelper().stopMovement();
                    }
                    else this.creature.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1D);
                    this.rememberTargetPos(target);
                }
            }
        }
    }

    @Override
    public void onEndTicker() {
        if (this.executingBlockBreak && !this.blockBreakApplied && this.selectedMoveBuilder.getMoveChargeupBuilder() == null
                && this.creature.getMoveFinishCount() != this.moveFinishCountAtMoveStart
                && this.creature.getBlockBreakEffectAttemptCount() == this.blockBreakEffectAttemptCountAtMoveStart
        ) {
            this.creature.breakBlocksInFrontInPathing();
            this.blockBreakApplied = true;
        }
        if (this.hasExecutedMove && !this.executingBlockBreak && this.selectedMoveBuilder.getRequireFindTargetToUse()
                && this.selectedMoveBuilder.getMoveType() == CreatureMoveBuilder.MoveType.PHYSICAL
        ) {
            boolean moveHitTarget = this.creature.getAttackTargetHitCount() > this.attackTargetHitCountAtMoveStart;
            if (moveHitTarget) this.creature.resetFrustration();
            else this.creature.addFrustration(35);
        }
        this.creature.setUseBlockBreak(false);
        this.creature.getCreaturePathNavigate().clearPath();

        //preserve last look direction after target is gone
        EntityLivingBase target = this.creature.getAttackTarget();
        CreatureMoveChargeupBuilder charge = this.selectedMoveBuilder.getMoveChargeupBuilder();
        boolean canLookWhileUsing = charge != null && charge.getCanLookWhileUsing();
        if (!canLookWhileUsing && (target == null || !target.isEntityAlive())) {
            this.preserveLastLookDirection();
        }
    }

    @Override
    public boolean isOverridableWhileUsed() {
        return !this.hasExecutedMove;
    }

    private void rememberTargetPos(@NotNull EntityLivingBase target) {
        this.hasLastTargetPos = true;
        this.lastTargetX = target.posX;
        this.lastTargetY = target.posY;
        this.lastTargetZ = target.posZ;
    }

    private void faceBlockBreakDirection(double x, double z) {
        double targetX = x - this.creature.posX;
        double targetZ = z - this.creature.posZ;
        if (targetX * targetX + targetZ * targetZ < 1E-4D) return;

        float targetYaw = (float)(Math.atan2(targetZ, targetX) * 180F / (float)Math.PI) - 90F;
        this.setLookDirection(targetYaw);
    }
}
