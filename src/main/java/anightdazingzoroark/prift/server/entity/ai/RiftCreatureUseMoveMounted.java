package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftCreatureUseMoveMounted extends EntityAIBase {
    private final RiftCreature creature;
    private boolean finishFlag;
    private RiftCreatureMove currentInvokedMove;
    private int maxMoveAnimTime;
    private int moveAnimUseTime;
    private int animTime;

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
                this.maxMoveAnimTime = this.creature.currentCreatureMove().moveType.animTotalLength - (int) (this.creature.currentCreatureMove().moveType.animPercentToCharge * this.creature.currentCreatureMove().moveType.animTotalLength);
                this.moveAnimUseTime = (int)(this.creature.currentCreatureMove().moveType.animTotalLength * (this.creature.currentCreatureMove().moveType.animPercentOnUse - this.creature.currentCreatureMove().moveType.animPercentToCharge));
                this.currentInvokedMove.onStartExecuting(this.creature);
            }
            else {
                this.maxMoveAnimTime = this.creature.currentCreatureMove().moveType.animTotalLength;
                this.moveAnimUseTime = (int)(this.maxMoveAnimTime * this.creature.currentCreatureMove().moveType.animPercentOnUse);
            }
        }
        else if (this.creature.usingMoveTwo()) {
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(1));
            this.currentInvokedMove = this.creature.getLearnedMoves().get(1).invokeMove();

            if (this.creature.currentCreatureMove().chargeType.requiresCharge()) {
                this.maxMoveAnimTime = this.creature.currentCreatureMove().moveType.animTotalLength - (int) (this.creature.currentCreatureMove().moveType.animPercentToCharge * this.creature.currentCreatureMove().moveType.animTotalLength);
                this.moveAnimUseTime = (int)(this.creature.currentCreatureMove().moveType.animTotalLength * (this.creature.currentCreatureMove().moveType.animPercentOnUse - this.creature.currentCreatureMove().moveType.animPercentToCharge));
                this.currentInvokedMove.onStartExecuting(this.creature);
            }
            else {
                this.maxMoveAnimTime = this.creature.currentCreatureMove().moveType.animTotalLength;
                this.moveAnimUseTime = (int)(this.maxMoveAnimTime * this.creature.currentCreatureMove().moveType.animPercentOnUse);
            }
        }
        else if (this.creature.usingMoveThree()) {
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(2));
            this.currentInvokedMove = this.creature.getLearnedMoves().get(2).invokeMove();

            if (this.creature.currentCreatureMove().chargeType.requiresCharge()) {
                this.maxMoveAnimTime = this.creature.currentCreatureMove().moveType.animTotalLength - (int) (this.creature.currentCreatureMove().moveType.animPercentToCharge * this.creature.currentCreatureMove().moveType.animTotalLength);
                this.moveAnimUseTime = (int)(this.creature.currentCreatureMove().moveType.animTotalLength * (this.creature.currentCreatureMove().moveType.animPercentOnUse - this.creature.currentCreatureMove().moveType.animPercentToCharge));
                this.currentInvokedMove.onStartExecuting(this.creature);
            }
            else {
                this.maxMoveAnimTime = this.creature.currentCreatureMove().moveType.animTotalLength;
                this.moveAnimUseTime = (int)(this.maxMoveAnimTime * this.creature.currentCreatureMove().moveType.animPercentOnUse);
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
                if (this.animTime == 0) {
                    this.creature.setUsingUnchargedAnim(true);
                    this.currentInvokedMove.onStartExecuting(this.creature);
                }
                if (this.animTime == this.moveAnimUseTime) {
                    EntityLivingBase entityTarget = this.creature.getControlAttackTargets() instanceof EntityLivingBase ? (EntityLivingBase) this.creature.getControlAttackTargets() : null;
                    this.currentInvokedMove.onReachUsePoint(this.creature, entityTarget);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.animTime = 0;
                    this.creature.setUsingUnchargedAnim(false);
                    this.currentInvokedMove.onStopExecuting(this.creature);
                    this.setCoolDown(this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove()), this.creature.currentCreatureMove().maxCooldown);
                    this.finishFlag = true;
                }
                if (this.animTime < this.maxMoveAnimTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                }

                if (!this.finishFlag) this.animTime++;
            }
            else {
                int movePos = this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove());
                if (!this.getMoveIsUsing(movePos)) {
                    if (this.animTime == this.moveAnimUseTime) {
                        EntityLivingBase entityTarget = this.creature.getControlAttackTargets() instanceof EntityLivingBase ? (EntityLivingBase) this.creature.getControlAttackTargets() : null;
                        this.currentInvokedMove.onReachUsePoint(this.creature, entityTarget, this.getUse(movePos));
                    }
                    if (this.animTime >= this.maxMoveAnimTime) {
                        int cooldownGradient = 1;
                        if (this.creature.currentCreatureMove().maxCooldown > 0 && this.creature.currentCreatureMove().maxUse > 0) {
                            cooldownGradient = this.creature.currentCreatureMove().maxCooldown/this.creature.currentCreatureMove().maxUse;
                        }
                        this.setCoolDown(movePos, this.getUse(movePos) * cooldownGradient);
                        this.finishFlag = true;
                        this.animTime = 0;
                        this.creature.setMoveOneUse(0);
                        this.creature.setMoveTwoUse(0);
                        this.creature.setMoveThreeUse(0);
                    }
                    if (!this.finishFlag) this.animTime++;
                }
            }
        }
    }

    private int getUse(int movePos) {
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

    private boolean getMoveIsUsing(int movePos) {
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
}
