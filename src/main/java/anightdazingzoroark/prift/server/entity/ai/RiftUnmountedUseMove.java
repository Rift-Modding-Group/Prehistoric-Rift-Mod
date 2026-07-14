package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.CreatureMoveSelector;
import anightdazingzoroark.prift.util.MathUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import org.jetbrains.annotations.NotNull;

/**
 * This is for managing a creature being able to use moves as well as other offensive
 * actions that are not moves.
 * */
public class RiftUnmountedUseMove extends EntityAIBase {
    private static final double TARGET_MOVED_REPATH_DISTANCE_SQ = 1D;
    private static final int DIRECT_TARGET_MOVE_STALL_TICKS = 8;
    private static final int CLOSE_TARGET_STRAFE_TICKS = 10;

    @NotNull
    private final RiftCreature creature;
    private CreatureMoveSelector.MoveRule moveRule;

    //---move use result related stuff---
    private String selectedMoveName;
    private CreatureMoveBuilder selectedMoveBuilder;
    private boolean hasExecutedMove; //flag to set to true when a move is currently being used by a creature

    //---pathing related stuff---
    private int repathCooldown;
    private boolean hasLastTargetPos;
    private double lastTargetX;
    private double lastTargetY;
    private double lastTargetZ;
    private int directTargetMoveStallTicks;
    private double lastDirectTargetDistanceSq;
    private boolean holdCloseTargetStrafe;
    private int closeTargetStrafeTicks;
    private int pathingFrustrationTicks;

    //---look direction preservation---
    private boolean hasLastLookDirection;
    private float lastRotationYawHead;
    private float lastPrevRotationYawHead;
    private float lastRotationPitch;
    private float lastPrevRotationPitch;

    //---frustration related stuff---
    private EntityLivingBase moveTarget;
    private int lastAttackedEntityTimeAtMoveStart;
    private boolean selectedMoveUsedDueToFrustration;

    public RiftUnmountedUseMove(@NotNull RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    //checking if a move could be used happens here
    @Override
    public boolean shouldExecute() {
        //cannot execute if the creature has no moves
        if (!this.creature.getCreatureMoves().isInitialized()) return false;

        //cannot execute if the creature is using a move
        if (!this.creature.getCurrentMove().isEmpty()) return false;

        //-----find and use a move from current list-----
        CreatureMoveStorage creatureMoves = this.creature.getCreatureMoves();
        this.moveRule = creatureMoves.getBestMoveRuleUnmounted();
        return this.moveRule != null;
    }

    @Override
    public void startExecuting() {
        this.pathingFrustrationTicks = 0;
        this.moveTarget = null;
        this.selectedMoveUsedDueToFrustration = false;

        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.USE_MOVE) {
            this.selectedMoveName = this.moveRule.moveRuleBuilder().getMoveName();
            this.selectedMoveBuilder = this.creature.getCreatureMoves().getUsableMoveBuilder(this.selectedMoveName);
            this.selectedMoveUsedDueToFrustration = this.moveRule.moveRuleBuilder().getUseWhenFrustrated()
                    && this.creature.atFrustrationThreshold();
        }
        else if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.SPRINT) {
            if (this.creature.atFrustrationThreshold()) {
                this.creature.sprintToAttackCooldown = 0;
                this.creature.resetFrustration();
            }
            this.creature.setSprinting(true);
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase target = this.creature.getAttackTarget();
        boolean targetAvailability = target != null && target.isEntityAlive();

        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.USE_MOVE) {
            boolean moveBuilderTargetCondition = !this.selectedMoveBuilder.getRequireFindTargetToUse() || targetAvailability;

            //move execution depends on if current move hasnt been reset
            if (this.hasExecutedMove) return !this.creature.getCurrentMove().isEmpty();
            //when move should not path to its target, stop if target is not already in range
            else if (this.moveRule.moveRuleBuilder().getDontPathToTarget()
                    && this.selectedMoveBuilder.getRequireFindTargetToUse()
                    && targetAvailability
                    && !this.selectedMoveUsedDueToFrustration
                    && !this.moveRule.moveRuleBuilder().getDetectionRule().targetWithinRange(this.creature, target)
            ) return false;
            //when frustration gets high enough, stop current pathing and pick a frustration option
            else if (!this.selectedMoveUsedDueToFrustration && this.creature.atFrustrationThreshold()) return false;
            //if creature hasnt executed move yet, keep it true to keep it running
            //only thing stopping it is if target is gone (if said move requires it)
            else return moveBuilderTargetCondition;
        }
        //when sprinting towards the target, the target should be alive for this to continue executing
        else if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.SPRINT) {
            return this.creature.isSprinting() && targetAvailability;
        }
        else return false;
    }

    @Override
    public void resetTask() {
        //specific to sprinting
        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.SPRINT) {
            this.creature.sprintToAttackCooldown = MathUtil.randomInRange(this.creature.world.rand, 5, 10) * 20;
            this.creature.setSprinting(false);
        }

        //when a target-required offensive move ends without hitting its intended target
        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.USE_MOVE
                && this.hasExecutedMove
                && this.selectedMoveBuilder != null
                && this.selectedMoveBuilder.getRequireFindTargetToUse()
                && this.selectedMoveBuilder.getMoveType() == CreatureMoveHelper.MoveType.PHYSICAL
        ) {
            boolean moveHitTarget = this.moveTarget != null
                    && this.creature.getLastAttackedEntity() == this.moveTarget
                    && this.creature.getLastAttackedEntityTime() > this.lastAttackedEntityTimeAtMoveStart;
            if (moveHitTarget) this.creature.resetFrustration();
            else this.creature.addFrustration(35);
        }

        if (this.selectedMoveBuilder != null && this.selectedMoveBuilder.getOnMoveEndEffect() != null) {
            this.selectedMoveBuilder.getOnMoveEndEffect().accept(this.creature);
        }
        this.selectedMoveName = null;
        this.selectedMoveBuilder = null;
        this.hasExecutedMove = false;
        this.repathCooldown = 0;
        this.hasLastTargetPos = false;
        this.directTargetMoveStallTicks = 0;
        this.holdCloseTargetStrafe = false;
        this.closeTargetStrafeTicks = 0;
        this.pathingFrustrationTicks = 0;
        this.moveTarget = null;
        this.selectedMoveUsedDueToFrustration = false;
        this.creature.getNavigator().clearPath();

        //preserve last look direction after target is gone
        EntityLivingBase target = this.creature.getAttackTarget();
        if (target == null || !target.isEntityAlive()) this.preserveLastLookDirection();
    }

    /**
     * Mostly to update pathing to target of a move
     * */
    @Override
    public void updateTask() {
        EntityLivingBase target = this.creature.getAttackTarget();

        //---when using a normal move---
        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.USE_MOVE) {
            //---when move is already being used, stop pathing---
            if (this.hasExecutedMove && !this.creature.getCurrentMove().isEmpty()) {
                this.preserveLastLookDirection();
                this.directTargetMoveStallTicks = 0;
                this.holdCloseTargetStrafe = false;
                this.closeTargetStrafeTicks = 0;
                this.pathingFrustrationTicks = 0;
                this.creature.getNavigator().clearPath();
            }
            //---pathing to go to target is all dealt with here, if said move requires target---
            else if (this.selectedMoveBuilder.getRequireFindTargetToUse() && target != null && target.isEntityAlive()) {
                boolean dontPathToTarget = this.moveRule.moveRuleBuilder().getDontPathToTarget();

                //set look at target
                if (!dontPathToTarget) {
                    this.creature.getLookHelper().setLookPositionWithEntity(target, 30f, 0f);
                    this.hasLastLookDirection = true;
                    this.lastRotationYawHead = this.creature.rotationYawHead;
                    this.lastPrevRotationYawHead = this.creature.prevRotationYawHead;
                    this.lastRotationPitch = this.creature.rotationPitch;
                    this.lastPrevRotationPitch = this.creature.prevRotationPitch;
                }

                boolean targetWithinRange = this.moveRule.moveRuleBuilder().getDetectionRule().targetWithinRange(this.creature, target);

                //execute move when target is in range
                if (targetWithinRange || this.selectedMoveUsedDueToFrustration) {
                    //forcibly stop rotation upon using a move
                    if (dontPathToTarget) {
                        this.hasLastLookDirection = true;
                        this.lastRotationYawHead = this.creature.rotationYaw;
                        this.lastPrevRotationYawHead = this.creature.prevRotationYaw;
                        this.lastRotationPitch = this.creature.rotationPitch;
                        this.lastPrevRotationPitch = this.creature.prevRotationPitch;
                    }
                    else {
                        double targetX = target.posX - this.creature.posX;
                        double targetZ = target.posZ - this.creature.posZ;
                        //only rotate if target direction is usable
                        if (targetX * targetX + targetZ * targetZ >= 1E-4D) {
                            float targetYaw = (float)(Math.atan2(targetZ, targetX) * 180f / (float) Math.PI) - 90f;
                            this.creature.rotationYaw = targetYaw;
                            this.creature.prevRotationYaw = targetYaw;
                            this.creature.renderYawOffset = targetYaw;
                            this.creature.prevRenderYawOffset = targetYaw;
                            this.creature.rotationYawHead = targetYaw;
                            this.creature.prevRotationYawHead = targetYaw;
                            this.lastRotationYawHead = targetYaw;
                            this.lastPrevRotationYawHead = targetYaw;
                        }
                    }

                    //execute move
                    this.hasExecutedMove = true;
                    this.moveTarget = target;
                    this.lastAttackedEntityTimeAtMoveStart = this.creature.getLastAttackedEntityTime();
                    if (this.selectedMoveUsedDueToFrustration) this.creature.resetFrustration();
                    this.creature.setCurrentMove(this.selectedMoveName);

                    //stop pathing
                    this.directTargetMoveStallTicks = 0;
                    this.holdCloseTargetStrafe = false;
                    this.closeTargetStrafeTicks = 0;
                    this.pathingFrustrationTicks = 0;
                    this.creature.getNavigator().clearPath();
                }
                //---when move should not path to target, stop moving and wait for another move selection---
                else if (dontPathToTarget) {
                    this.repathCooldown = 0;
                    this.directTargetMoveStallTicks = 0;
                    this.holdCloseTargetStrafe = false;
                    this.closeTargetStrafeTicks = 0;
                    this.pathingFrustrationTicks = 0;
                    this.creature.getNavigator().clearPath();
                }
                //pathing to ensure target can be found by creature
                else {
                    //add frustration when pathing to the target takes too long
                    this.pathingFrustrationTicks++;
                    if (this.creature.atPathingFrustrationInterval(this.pathingFrustrationTicks)) {
                        this.pathingFrustrationTicks = 0;
                        this.creature.addFrustration(20);
                    }

                    //tick down repath cooldown
                    if (this.repathCooldown > 0) this.repathCooldown--;
                    PathNavigate creatureNavigation = this.creature.getNavigator();

                    //---when target is way too close, move away---
                    if (this.moveRule.moveRuleBuilder().getDetectionRule().targetTooClose(this.creature, target)) {
                        this.directTargetMoveStallTicks = 0;
                        this.holdCloseTargetStrafe = false;
                        this.closeTargetStrafeTicks = CLOSE_TARGET_STRAFE_TICKS;
                    }

                    //---when held strafe should stop due to target movement---
                    if (this.holdCloseTargetStrafe && this.hasLastTargetPos && target.getDistanceSq(this.lastTargetX, this.lastTargetY, this.lastTargetZ) > TARGET_MOVED_REPATH_DISTANCE_SQ * 4D) {
                        this.directTargetMoveStallTicks = 0;
                        this.holdCloseTargetStrafe = false;
                        this.closeTargetStrafeTicks = 0;
                    }

                    //---when creature is strafing away from close target---
                    if (this.closeTargetStrafeTicks > 0 || this.holdCloseTargetStrafe) {
                        //tick down temporary strafe
                        if (this.closeTargetStrafeTicks > 0) this.closeTargetStrafeTicks--;

                        //face close target
                        double targetX = target.posX - this.creature.posX;
                        double targetZ = target.posZ - this.creature.posZ;
                        if (targetX * targetX + targetZ * targetZ >= 1E-4D) {
                            this.creature.faceEntity(target, 90f, 0f);
                            this.creature.renderYawOffset = this.creature.rotationYaw;
                        }

                        creatureNavigation.clearPath();
                        this.directTargetMoveStallTicks = 0;
                        this.creature.getMoveHelper().strafe(-1f, 0f);
                        if (!this.holdCloseTargetStrafe) this.rememberTargetPos(target);
                    }
                    //---normal pathing---
                    else {
                        boolean targetMoved = !this.hasLastTargetPos || target.getDistanceSq(this.lastTargetX, this.lastTargetY, this.lastTargetZ) > TARGET_MOVED_REPATH_DISTANCE_SQ;
                        boolean shouldRepath = this.repathCooldown <= 0 && (creatureNavigation.noPath() || targetMoved);

                        //---when target moved or path ended, try to repath---
                        if (shouldRepath) {
                            this.rememberTargetPos(target);
                            this.repathCooldown = 4 + this.creature.world.rand.nextInt(7);
                            //when pathing succeeds, reset direct movement fallback
                            if (creatureNavigation.tryMoveToEntityLiving(target, 1D)) {
                                this.directTargetMoveStallTicks = 0;
                                this.holdCloseTargetStrafe = false;
                            }
                        }

                        //---when navigator has no path, move directly to target---
                        if (creatureNavigation.noPath()) {
                            double targetDistanceSq = this.creature.getDistanceSq(target);
                            //when direct movement is not getting closer, count a stall
                            if (this.directTargetMoveStallTicks > 0 && targetDistanceSq + 0.01D >= this.lastDirectTargetDistanceSq) {
                                this.directTargetMoveStallTicks++;
                            }
                            //otherwise reset stall counter
                            else this.directTargetMoveStallTicks = 1;
                            this.lastDirectTargetDistanceSq = targetDistanceSq;

                            //when direct movement stalls, strafe away instead
                            if (this.directTargetMoveStallTicks >= DIRECT_TARGET_MOVE_STALL_TICKS) {
                                this.directTargetMoveStallTicks = 0;
                                this.holdCloseTargetStrafe = true;
                                this.closeTargetStrafeTicks = CLOSE_TARGET_STRAFE_TICKS;
                                this.rememberTargetPos(target);
                            }
                            //otherwise keep directly moving to target
                            else {
                                this.creature.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1D);
                                this.rememberTargetPos(target);
                            }
                        }
                    }
                }
            }
        }
        //sprinting to target involves directly moving to its target position
        else if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.SPRINT) {
            if (target != null && target.isEntityAlive()) this.creature.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1D);
        }
    }

    private void rememberTargetPos(@NotNull EntityLivingBase target) {
        this.hasLastTargetPos = true;
        this.lastTargetX = target.posX;
        this.lastTargetY = target.posY;
        this.lastTargetZ = target.posZ;
    }

    private void preserveLastLookDirection() {
        if (!this.hasLastLookDirection) return;
        this.creature.rotationYaw = this.lastRotationYawHead;
        this.creature.prevRotationYaw = this.lastPrevRotationYawHead;
        this.creature.renderYawOffset = this.lastRotationYawHead;
        this.creature.prevRenderYawOffset = this.lastPrevRotationYawHead;
        this.creature.rotationYawHead = this.lastRotationYawHead;
        this.creature.prevRotationYawHead = this.lastPrevRotationYawHead;
        this.creature.rotationPitch = this.lastRotationPitch;
        this.creature.prevRotationPitch = this.lastPrevRotationPitch;
    }
}
