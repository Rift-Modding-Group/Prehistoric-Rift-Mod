package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
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
        System.out.println("start");
        if (this.creature.usingMoveOne()) {
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(0));
            this.currentInvokedMove = this.creature.getLearnedMoves().get(0).invokeMove();
            this.maxMoveAnimTime = this.currentInvokedMove.animTotalLength;
            this.moveAnimUseTime = (int)(this.maxMoveAnimTime * this.currentInvokedMove.animPercentOnUse);
        }
        else if (this.creature.usingMoveTwo()) {
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(1));
            this.currentInvokedMove = this.creature.getLearnedMoves().get(1).invokeMove();
            this.maxMoveAnimTime = this.currentInvokedMove.animTotalLength;
            this.moveAnimUseTime = (int)(this.maxMoveAnimTime * this.currentInvokedMove.animPercentOnUse);
        }
        else if (this.creature.usingMoveThree()) {
            this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(2));
            this.currentInvokedMove = this.creature.getLearnedMoves().get(2).invokeMove();
            this.maxMoveAnimTime = this.currentInvokedMove.animTotalLength;
            this.moveAnimUseTime = (int)(this.maxMoveAnimTime * this.currentInvokedMove.animPercentOnUse);
        }
        this.animTime = 0;
        this.finishFlag = false;
    }

    @Override
    public void resetTask() {
        System.out.println("stop");
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
            if (this.animTime == 0) {
                this.currentInvokedMove.onStartExecuting(this.creature);
            }
            if (this.animTime == this.moveAnimUseTime) {
                EntityLivingBase entityTarget = this.creature.getControlAttackTargets() instanceof EntityLivingBase ? (EntityLivingBase) this.creature.getControlAttackTargets() : null;
                if (entityTarget != null) this.currentInvokedMove.onReachUsePoint(this.creature, entityTarget);
            }
            if (this.animTime >= this.maxMoveAnimTime) {
                this.animTime = 0;
                this.currentInvokedMove.onStopExecuting(this.creature);
                this.setCoolDown(this.creature.getLearnedMoves().indexOf(this.creature.currentCreatureMove()), 0);
                this.finishFlag = true;
            }
            if (this.animTime < this.maxMoveAnimTime) {
                this.currentInvokedMove.whileExecuting(this.creature);
            }

            if (!this.finishFlag) this.animTime++;
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
}
