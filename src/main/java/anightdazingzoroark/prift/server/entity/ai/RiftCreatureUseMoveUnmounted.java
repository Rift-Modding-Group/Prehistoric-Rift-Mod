package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.*;

public class RiftCreatureUseMoveUnmounted extends EntityAIBase {
    private final RiftCreature creature;
    private RiftCreatureMove currentInvokedMove;
    private EntityLivingBase target;
    private int maxMoveAnimTime;
    private int moveAnimUseTime;
    private int animTime;
    private boolean finishedMoveMarker;
    private boolean finishedAnimMarker;
    private int moveChoiceCooldown;
    private boolean canAttackInRange;
    private int maxChargeTime;
    private boolean usingChargeMove;

    public RiftCreatureUseMoveUnmounted(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return (this.creature.getAttackTarget() != null || this.creature.getRevengeTarget() != null) && !this.creature.isBeingRidden();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.isBeingRidden() && !this.finishedAnimMarker;
    }

    @Override
    public void startExecuting() {
        this.finishedMoveMarker = true;
        this.finishedAnimMarker = false;
        this.creature.setCurrentCreatureMove(null);
        this.target = this.creature.getRevengeTarget() != null ? this.creature.getRevengeTarget() : this.creature.getAttackTarget();
        this.currentInvokedMove = null;
        this.maxMoveAnimTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;
        this.moveChoiceCooldown = 0;
        this.maxChargeTime = 0;
        this.usingChargeMove = false;
    }

    @Override
    public void resetTask() {
        this.creature.setAttacking(false);
        this.creature.resetSpeed();
        if (this.currentInvokedMove != null) {
            this.creature.setUsingUnchargedAnim(false);
            this.currentInvokedMove.onStopExecuting(this.creature);
            this.currentInvokedMove = null;
        }
        this.creature.setCurrentMoveUse(0);
        this.setMoveBeingUsed(false);
        this.creature.setCurrentCreatureMove(null);
        this.maxMoveAnimTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;
        this.moveChoiceCooldown = 0;
        this.maxChargeTime = 0;
        this.usingChargeMove = false;
    }

    @Override
    public void updateTask() {
        //randomly select a move to use
        if (this.finishedMoveMarker) {
            if (this.moveChoiceCooldown > 0) this.moveChoiceCooldown--;
            else {
                CreatureMove selectedMove = this.selectMoveForUse();
                if (selectedMove != null) {
                    this.creature.setCurrentCreatureMove(selectedMove);
                    this.currentInvokedMove = this.creature.currentCreatureMove().invokeMove();
                    this.animTime = 0;
                    if (this.creature.currentCreatureMove().chargeType.requiresCharge()) {
                        this.maxMoveAnimTime = this.creature.currentCreatureMove().moveType.animTotalLength - (int) (this.creature.currentCreatureMove().moveType.animPercentToCharge * this.creature.currentCreatureMove().moveType.animTotalLength);
                        this.moveAnimUseTime = (int)(this.creature.currentCreatureMove().moveType.animTotalLength * (this.creature.currentCreatureMove().moveType.animPercentOnUse - this.creature.currentCreatureMove().moveType.animPercentToCharge));
                        this.maxChargeTime = RiftUtil.randomInRange((int)(this.creature.currentCreatureMove().maxUse * 0.3), this.creature.currentCreatureMove().maxUse);
                    }
                    else {
                        this.maxMoveAnimTime = this.creature.currentCreatureMove().moveType.animTotalLength;
                        this.moveAnimUseTime = (int)(this.maxMoveAnimTime * this.creature.currentCreatureMove().moveType.animPercentOnUse);
                    }
                    this.finishedMoveMarker = false;
                }
            }
        }
        else {
            if (!this.canAttackInRange) {
                this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                this.creature.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
                this.canAttackInRange = this.isWithinRange();
                if (this.canAttackInRange && this.creature.currentCreatureMove().chargeType.requiresCharge()) this.setMoveBeingUsed(true);
            }
            else {
                this.creature.getNavigator().clearPath();
                if (this.creature.currentCreatureMove().chargeType.requiresCharge()) {
                    if (!this.usingChargeMove) {
                        this.currentInvokedMove.onStartExecuting(this.creature);
                        this.usingChargeMove = true;
                    }
                    else {
                        if (this.creature.getCurrentMoveUse() < this.maxChargeTime) {
                            this.creature.setCurrentMoveUse(this.creature.getCurrentMoveUse() + 1);
                        }
                        else {
                            this.setMoveBeingUsed(false);
                            if (this.animTime == this.moveAnimUseTime) {
                                this.currentInvokedMove.onReachUsePoint(this.creature, this.target, this.creature.getCurrentMoveUse());
                            }
                            if (this.animTime >= this.maxMoveAnimTime) {
                                this.animTime = 0;
                                this.currentInvokedMove.onStopExecuting(this.creature);

                                int cooldownGradient = 1;
                                if (this.creature.currentCreatureMove().maxCooldown > 0 && this.creature.currentCreatureMove().maxUse > 0) {
                                    cooldownGradient = this.creature.currentCreatureMove().maxCooldown/this.creature.currentCreatureMove().maxUse;
                                }
                                this.setCoolDown(this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove()), this.creature.getCurrentMoveUse() * cooldownGradient);
                                this.creature.setCurrentMoveUse(0);

                                this.finishedMoveMarker = true;
                                this.finishedAnimMarker = true;
                                this.usingChargeMove = false;
                                this.canAttackInRange = false;
                                this.moveChoiceCooldown = 20;
                            }
                            if (this.animTime < this.maxMoveAnimTime) {
                                this.currentInvokedMove.whileExecuting(this.creature);
                            }
                            if (!this.finishedMoveMarker) this.animTime++;
                        }
                    }
                }
                else {
                    if (this.animTime == 0) {
                        this.creature.setUsingUnchargedAnim(true);
                        this.currentInvokedMove.onStartExecuting(this.creature);
                    }
                    if (this.animTime == this.moveAnimUseTime) {
                        this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                    }
                    if (this.animTime >= this.maxMoveAnimTime) {
                        this.animTime = 0;
                        this.creature.setUsingUnchargedAnim(false);
                        this.currentInvokedMove.onStopExecuting(this.creature);
                        this.setCoolDown(this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove()), 0);
                        this.finishedMoveMarker = true;
                        this.finishedAnimMarker = true;
                        this.canAttackInRange = false;
                        this.moveChoiceCooldown = 20;
                    }
                    if (this.animTime < this.maxMoveAnimTime) {
                        this.currentInvokedMove.whileExecuting(this.creature);
                    }
                    if (!this.finishedMoveMarker) this.animTime++;
                }
            }
        }
    }

    private void setMoveBeingUsed(boolean value) {
        int movePos = this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove());
        switch (movePos) {
            case 0:
                this.creature.setUsingMoveOne(value);
                break;
            case 1:
                this.creature.setUsingMoveTwo(value);
                break;
            case 2:
                this.creature.setUsingMoveThree(value);
                break;
        }
    }

    private void setCoolDown(int movePos, int moveCooldown) {
        switch (movePos) {
            case 0:
                this.creature.setMoveOneCooldown(moveCooldown);
                break;
            case 1:
                this.creature.setMoveTwoCooldown(moveCooldown);
                break;
            case 2:
                this.creature.setMoveThreeCooldown(moveCooldown);
                break;
        }
    }

    private boolean isCoolingDown(int movePos) {
        switch (movePos) {
            case 0:
                return this.creature.getMoveOneCooldown() > 0;
            case 1:
                return this.creature.getMoveTwoCooldown() > 0;
            case 2:
                return this.creature.getMoveThreeCooldown() > 0;
        }
        return false;
    }

    private boolean isWithinRange() {
        if (this.currentInvokedMove.creatureMove.moveType != CreatureMove.MoveType.RANGED
        && this.currentInvokedMove.creatureMove.moveType != CreatureMove.MoveType.STATUS) {
            return this.creature.getDistance(this.target) <= this.creature.attackWidth();
        }
        else return this.creature.getDistance(this.target) <= this.creature.rangedWidth();
    }

    private CreatureMove selectMoveForUse() {
        Deque<CreatureMove> movesForSelecting = new ArrayDeque<>();
        List<CreatureMove> creatureMovesRandomized = new ArrayList<>(this.creature.getLearnedMoves());
        Collections.shuffle(creatureMovesRandomized);
        for (CreatureMove creatureMove : creatureMovesRandomized) {
            if (!this.isCoolingDown(this.creature.getLearnedMoves().indexOf(creatureMove))) {
                RiftCreatureMove moveInvoked = creatureMove.invokeMove();
                if (moveInvoked.canBeExecuted(this.creature, this.target) == RiftCreatureMove.MovePriority.HIGH)
                    movesForSelecting.addFirst(creatureMove);
                if (moveInvoked.canBeExecuted(this.creature, this.target) == RiftCreatureMove.MovePriority.LOW)
                    movesForSelecting.addLast(creatureMove);
            }
        }
        if (movesForSelecting.isEmpty()) return null;
        else return movesForSelecting.getFirst();
    }
}
