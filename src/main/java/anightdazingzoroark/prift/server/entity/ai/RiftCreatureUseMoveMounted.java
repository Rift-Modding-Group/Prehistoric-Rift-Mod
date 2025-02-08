package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

public class RiftCreatureUseMoveMounted extends EntityAIBase {
    private final RiftCreature creature;
    private boolean finishFlag;
    private RiftCreatureMove currentInvokedMove;

    private int maxMoveAnimTime; //entire time spent for animation
    private int moveAnimInitDelayTime; //time until end of delay point
    private int moveAnimChargeUpTime; //time until end of charge up point
    private int moveAnimChargeToUseTime; //time until end of charge up to use point
    private int moveAnimUseTime; //time until end of use anim
    private int animTime = 0;

    private Entity target;

    public RiftCreatureUseMoveMounted(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.creature.isBeingRidden() && (this.creature.usingMoveOne() || this.creature.usingMoveTwo() || this.creature.usingMoveThree());
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.isBeingRidden() && !this.finishFlag;
    }

    @Override
    public void startExecuting() {
        if (this.creature.usingMoveOne()) {
            this.currentInvokedMove = this.creature.getLearnedMoves().get(0).invokeMove();
            this.target = this.getAttackTarget(this.currentInvokedMove.creatureMove.moveType);

            if (this.currentInvokedMove.canBeExecutedMounted(this.creature, this.target)) {
                this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(0));

                if (this.creature.currentCreatureMove().chargeType.requiresCharge()) {
                    this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                    this.moveAnimChargeUpTime = this.moveAnimInitDelayTime;
                    this.moveAnimChargeToUseTime = this.moveAnimChargeUpTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUseTime();
                    this.moveAnimUseTime = this.moveAnimChargeToUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationTime();
                    this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUseTime();
                }
                else {
                    this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                    this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpPoint();
                    this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUsePoint();
                    this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                    this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
                }
            }
            else {
                if (this.currentInvokedMove.cannotExecuteMountedMessage() != null) {
                    ((EntityPlayer) this.creature.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation(this.currentInvokedMove.cannotExecuteMountedMessage()), false);
                }
                this.currentInvokedMove = null;
                this.target = null;
            }
        }
        else if (this.creature.usingMoveTwo()) {
            this.currentInvokedMove = this.creature.getLearnedMoves().get(1).invokeMove();
            this.target = this.getAttackTarget(this.currentInvokedMove.creatureMove.moveType);

            if (this.currentInvokedMove.canBeExecutedMounted(this.creature, this.target)) {
                this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(1));

                if (this.creature.currentCreatureMove().chargeType.requiresCharge()) {
                    this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                    this.moveAnimChargeUpTime = this.moveAnimInitDelayTime;
                    this.moveAnimChargeToUseTime = this.moveAnimChargeUpTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUseTime();
                    this.moveAnimUseTime = this.moveAnimChargeToUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationTime();
                    this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUseTime();
                }
                else {
                    this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                    this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpPoint();
                    this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUsePoint();
                    this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                    this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
                }
            }
            else {
                if (this.currentInvokedMove.cannotExecuteMountedMessage() != null) {
                    ((EntityPlayer) this.creature.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation(this.currentInvokedMove.cannotExecuteMountedMessage()), false);
                }
                this.currentInvokedMove = null;
                this.target = null;
            }
        }
        else if (this.creature.usingMoveThree()) {
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(2));
            this.target = this.getAttackTarget(this.currentInvokedMove.creatureMove.moveType);

            if (this.currentInvokedMove.canBeExecutedMounted(this.creature, this.target)) {
                this.currentInvokedMove = this.creature.getLearnedMoves().get(2).invokeMove();

                if (this.creature.currentCreatureMove().chargeType.requiresCharge()) {
                    this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                    this.moveAnimChargeUpTime = this.moveAnimInitDelayTime;
                    this.moveAnimChargeToUseTime = this.moveAnimChargeUpTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUseTime();
                    this.moveAnimUseTime = this.moveAnimChargeToUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationTime();
                    this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUseTime();
                }
                else {
                    this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                    this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpPoint();
                    this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUsePoint();
                    this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                    this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
                }
            }
            else {
                if (this.currentInvokedMove.cannotExecuteMountedMessage() != null) {
                    ((EntityPlayer) this.creature.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation(this.currentInvokedMove.cannotExecuteMountedMessage()), false);
                }
                this.currentInvokedMove = null;
                this.target = null;
            }
        }
        if (this.currentInvokedMove != null && this.currentInvokedMove.canBeExecutedMounted(this.creature, this.target)) {
            this.animTime = 0;
            this.creature.setPlayingInfiniteMoveAnim(false);
            this.finishFlag = false;
        }
    }

    @Override
    public void resetTask() {
        if (this.currentInvokedMove != null) {
            this.currentInvokedMove.onStopExecuting(this.creature);
            this.currentInvokedMove = null;
        }
        this.creature.setPlayingInfiniteMoveAnim(false);
        this.creature.setCurrentCreatureMove(null);
        this.maxMoveAnimTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;
    }

    @Override
    public void updateTask() {
        if (this.currentInvokedMove == null || !this.currentInvokedMove.canBeExecutedMounted(this.creature, this.target)) {
            this.finishFlag = true;
            return;
        }

        if (!this.finishFlag) {
            if (!this.creature.currentCreatureMove().chargeType.requiresCharge()) {
                if (this.animTime == 0 && this.moveAnimInitDelayTime >= 0) {
                    this.creature.setUsingDelayAnim(true);
                }
                if (this.animTime == this.moveAnimInitDelayTime) {
                    this.creature.setUsingDelayAnim(false);
                    this.currentInvokedMove.onStartExecuting(this.creature);
                    this.creature.setUsingUnchargedAnim(true);
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    //also dont know what to put here
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.animTime = 0;
                    this.creature.setUsingUnchargedAnim(false);
                    this.currentInvokedMove.onStopExecuting(this.creature);
                    this.setCoolDown(this.creature.currentCreatureMove().maxCooldown);
                    this.finishFlag = true;
                }
                if (!this.finishFlag) {
                    this.animTime++;
                    if (this.creature.currentCreatureMove().useTimeIsInfinite && this.animTime >= this.moveAnimUseTime && !this.currentInvokedMove.forceStopFlag) {
                        this.moveAnimUseTime++;
                        this.maxMoveAnimTime++;
                    }
                }
            }
            else {
                if (this.animTime == 0 && this.moveAnimInitDelayTime >= 0) {
                    this.creature.setUsingDelayAnim(true);
                }
                if (this.animTime == this.moveAnimInitDelayTime) {
                    this.creature.setUsingDelayAnim(false);
                    this.currentInvokedMove.onStartExecuting(this.creature);
                    this.setChargedMoveBeingUsed(true);

                    //this is here because putting this in this.animTime == this.moveAnimChargeToUseTime
                    //makes the anim prematurely stop
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(true);
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.setChargedMoveBeingUsed(false);
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                    if (this.creature.currentCreatureMove().chargeUpAffectsUseTime) {
                        this.currentInvokedMove.setUseValue(this.getUse());
                        this.moveAnimUseTime += this.getUse();
                        this.maxMoveAnimTime += this.getUse();
                    }
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    if (this.currentInvokedMove.forceStopFlag && this.creature.currentCreatureMove().chargeUpAffectsUseTime) {
                        this.moveAnimUseTime -= this.currentInvokedMove.getUseValue();
                        this.maxMoveAnimTime -= this.currentInvokedMove.getUseValue();
                    }
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(false);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    int cooldownGradient = 1;
                    if (this.creature.currentCreatureMove().maxCooldown > 0 && this.creature.currentCreatureMove().maxUse > 0) {
                        cooldownGradient = this.creature.currentCreatureMove().maxCooldown/this.creature.currentCreatureMove().maxUse;
                    }
                    this.setCoolDown(this.getUse() * cooldownGradient);
                    this.finishFlag = true;
                    this.animTime = 0;
                    this.creature.setMoveOneUse(0);
                    this.creature.setMoveTwoUse(0);
                    this.creature.setMoveThreeUse(0);
                }
                if (!this.finishFlag) {
                    this.animTime++;
                    if (this.getMoveIsUsing()) {
                        this.moveAnimChargeUpTime++;
                        this.moveAnimChargeToUseTime++;
                        this.moveAnimUseTime++;
                        this.maxMoveAnimTime++;
                    }
                }
            }
        }
    }

    private int getUse() {
        if (this.creature.currentCreatureMove() == null) return 0;
        int movePos = this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove());
        switch (movePos) {
            case 0:
                return this.creature.getMoveOneUse();
            case 1:
                return this.creature.getMoveTwoUse();
            case 2:
                return this.creature.getMoveThreeUse();
        }
        return 0;
    }

    private void setChargedMoveBeingUsed(boolean value) {
        if (this.creature.currentCreatureMove() == null) return;
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

    private boolean getMoveIsUsing() {
        if (this.creature.currentCreatureMove() == null) return false;
        int movePos = this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove());
        switch (movePos) {
            case 0:
                return this.creature.usingMoveOne();
            case 1:
                return this.creature.usingMoveTwo();
            case 2:
                return this.creature.usingMoveThree();
        }
        return false;
    }

    private void setCoolDown(int moveCooldown) {
        if (this.creature.currentCreatureMove() == null) return;
        int movePos = this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove());
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

    private Entity getAttackTarget(CreatureMove.MoveType creatureMoveType) {
        if (creatureMoveType != CreatureMove.MoveType.RANGED && creatureMoveType != CreatureMove.MoveType.CHARGE) {
            return this.creature.getControlAttackTargets(this.creature.attackWidth());
        }
        else return this.creature.getControlAttackTargets(this.creature.rangedWidth());
    }
}
