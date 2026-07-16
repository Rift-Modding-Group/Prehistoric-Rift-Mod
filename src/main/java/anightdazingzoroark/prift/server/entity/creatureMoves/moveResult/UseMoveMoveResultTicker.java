package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.MoveRuleBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathNavigate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UseMoveMoveResultTicker extends AbstractMoveResultTicker {
    private static final double TARGET_MOVED_REPATH_DISTANCE_SQ = 1D;
    private static final int DIRECT_TARGET_MOVE_STALL_TICKS = 8;
    private static final int CLOSE_TARGET_STRAFE_TICKS = 10;

    @NotNull
    private final String selectedMoveName;
    @NotNull
    private final CreatureMoveBuilder selectedMoveBuilder;
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

    //---frustration related stuff---
    private int attackTargetHitCountAtMoveStart;
    private final boolean selectedMoveUsedDueToFrustration;

    public UseMoveMoveResultTicker(@NotNull RiftCreature creature, @NotNull MoveRuleBuilder moveRuleBuilder) {
        super(creature, moveRuleBuilder);
        this.selectedMoveName = moveRuleBuilder.getMoveName();
        this.selectedMoveBuilder = Objects.requireNonNull(this.creature.getCreatureMoves().getUsableMoveBuilder(moveRuleBuilder.getMoveName()));
        this.selectedMoveUsedDueToFrustration = this.moveRuleBuilder.getUseWhenFrustrated() && this.creature.atFrustrationThreshold();
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
                && !this.moveRuleBuilder.getDetectionRule().targetWithinRange(this.creature, target)
        ) return false;
        //when frustration gets high enough, stop current pathing and pick a frustration option
        else if (!this.selectedMoveUsedDueToFrustration && this.creature.atFrustrationThreshold()) return false;
        //if creature hasnt executed move yet, keep it true to keep it running
        //only thing stopping it is if target is gone (if said move requires it)
        else return moveBuilderTargetCondition;
    }

    @Override
    public void onUpdate() {
        EntityLivingBase target = this.creature.getAttackTarget();

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
            boolean dontPathToTarget = this.moveRuleBuilder.getDontPathToTarget();

            //set look at target
            if (!dontPathToTarget) {
                this.creature.getLookHelper().setLookPositionWithEntity(target, 30f, 0f);
                this.hasLastLookDirection = true;
                this.lastRotationYawHead = this.creature.rotationYawHead;
                this.lastPrevRotationYawHead = this.creature.prevRotationYawHead;
                this.lastRotationPitch = this.creature.rotationPitch;
                this.lastPrevRotationPitch = this.creature.prevRotationPitch;
            }

            boolean targetWithinRange = this.moveRuleBuilder.getDetectionRule().targetWithinRange(this.creature, target);

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
                this.attackTargetHitCountAtMoveStart = this.creature.getAttackTargetHitCount();
                if (this.selectedMoveUsedDueToFrustration) this.creature.resetFrustration();
                this.creature.setCurrentMove(this.selectedMoveName);
                if (this.selectedMoveBuilder.getOnMoveBeginEffect() != null) {
                    this.selectedMoveBuilder.getOnMoveBeginEffect().accept(this.creature, target);
                }

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

    @Override
    public void onEndTicker() {
        if (this.hasExecutedMove && this.selectedMoveBuilder.getRequireFindTargetToUse()
                && this.selectedMoveBuilder.getMoveType() == CreatureMoveHelper.MoveType.PHYSICAL
        ) {
            boolean moveHitTarget = this.creature.getAttackTargetHitCount() > this.attackTargetHitCountAtMoveStart;
            if (moveHitTarget) this.creature.resetFrustration();
            else this.creature.addFrustration(35);
        }

        if (this.selectedMoveBuilder.getOnMoveEndEffect() != null) {
            this.selectedMoveBuilder.getOnMoveEndEffect().accept(this.creature);
        }
        this.creature.getNavigator().clearPath();

        //preserve last look direction after target is gone
        EntityLivingBase target = this.creature.getAttackTarget();
        if (target == null || !target.isEntityAlive()) this.preserveLastLookDirection();
    }

    private void rememberTargetPos(@NotNull EntityLivingBase target) {
        this.hasLastTargetPos = true;
        this.lastTargetX = target.posX;
        this.lastTargetY = target.posY;
        this.lastTargetZ = target.posZ;
    }
}
