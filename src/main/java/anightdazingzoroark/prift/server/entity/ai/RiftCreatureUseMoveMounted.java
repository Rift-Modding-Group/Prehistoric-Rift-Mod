package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

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
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(0));
            this.currentInvokedMove = this.creature.getLearnedMoves().get(0).invokeMove();

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
                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).defineChargeUpToUsePoint();
                this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
            }
        }
        else if (this.creature.usingMoveTwo()) {
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(1));
            this.currentInvokedMove = this.creature.getLearnedMoves().get(1).invokeMove();

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
                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).defineChargeUpToUsePoint();
                this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
            }
        }
        else if (this.creature.usingMoveThree()) {
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(2));
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
                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).defineChargeUpToUsePoint();
                this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
            }
        }
        this.animTime = 0;
        this.finishFlag = false;
    }

    @Override
    public void resetTask() {
        if (this.currentInvokedMove != null) {
            this.currentInvokedMove.onStopExecuting(this.creature);
            this.currentInvokedMove = null;
        }
        this.creature.setCurrentCreatureMove(null);
        this.maxMoveAnimTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;
    }

    @Override
    public void updateTask() {
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
                if (this.animTime == this.moveAnimChargeUpTime) {}
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    EntityLivingBase entityTarget = this.creature.getControlAttackTargets() instanceof EntityLivingBase ? (EntityLivingBase) this.creature.getControlAttackTargets() : null;
                    this.currentInvokedMove.onReachUsePoint(this.creature, entityTarget);
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
                if (!this.finishFlag) this.animTime++;
            }
            else {
                if (this.animTime == 0 && this.moveAnimInitDelayTime >= 0) {
                    this.creature.setUsingDelayAnim(true);
                }
                if (this.animTime == this.moveAnimInitDelayTime) {
                    this.creature.setUsingDelayAnim(false);
                    this.currentInvokedMove.onStartExecuting(this.creature);
                    this.setChargedMoveBeingUsed(true);
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.setChargedMoveBeingUsed(false);
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    EntityLivingBase entityTarget = this.creature.getControlAttackTargets() instanceof EntityLivingBase ? (EntityLivingBase) this.creature.getControlAttackTargets() : null;
                    this.currentInvokedMove.onReachUsePoint(this.creature, entityTarget);
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    //also dont know what to put here
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
}
