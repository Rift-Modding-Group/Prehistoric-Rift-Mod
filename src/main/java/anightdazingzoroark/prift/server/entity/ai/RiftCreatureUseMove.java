package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftCreatureUseMove extends EntityAIBase {
    private final RiftCreature creature;
    private RiftCreatureMove currentInvokedMove;
    private EntityLivingBase target;
    private int maxMoveAnimTime;
    private int moveAnimUseTime;
    private int animTime;
    private boolean finishedMarker;
    private int moveChoiceCooldown;
    private boolean canAttackInRange;

    public RiftCreatureUseMove(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.creature.getAttackTarget() != null || this.creature.getRevengeTarget() != null;
    }

    @Override
    public void startExecuting() {
        this.finishedMarker = true;
        this.creature.setCurrentCreatureMove(null);
        this.target = this.creature.getRevengeTarget() != null ? this.creature.getRevengeTarget() : this.creature.getAttackTarget();
        this.currentInvokedMove = null;
        this.maxMoveAnimTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;
        this.moveChoiceCooldown = 0;
    }

    @Override
    public void resetTask() {
        this.creature.setAttacking(false);
        this.creature.resetSpeed();
        if (this.currentInvokedMove != null) {
            this.currentInvokedMove.onStopExecuting(this.creature);
            this.currentInvokedMove = null;
        }
        this.creature.setCurrentCreatureMove(null);
        this.maxMoveAnimTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;
        this.moveChoiceCooldown = 0;
    }

    @Override
    public void updateTask() {
        //randomly select a move to use
        if (this.finishedMarker) {
            if (this.moveChoiceCooldown > 0) this.moveChoiceCooldown--;
            else {
                int movePos = this.creature.world.rand.nextInt(this.creature.learnableMoves().size());
                if (!this.isCoolingDown(movePos)) {
                    CreatureMove selectedMove = this.creature.learnableMoves().get(movePos);
                    this.creature.setCurrentCreatureMove(selectedMove);
                    this.currentInvokedMove = this.creature.currentCreatureMove().invokeMove();
                    this.animTime = 0;
                    this.maxMoveAnimTime = this.currentInvokedMove.animTotalLength;
                    this.moveAnimUseTime = (int)(this.maxMoveAnimTime * this.currentInvokedMove.animPercentOnUse);
                    this.finishedMarker = false;
                }
            }
        }
        else {
            if (!this.canAttackInRange) {
                this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                this.creature.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
                this.canAttackInRange = this.isWithinRange();
            }
            else {
                this.creature.getNavigator().clearPath();
                if (this.animTime == 0) {
                    this.currentInvokedMove.onStartExecuting(this.creature);
                }
                if (this.animTime == this.moveAnimUseTime) {
                    this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.animTime = 0;
                    this.currentInvokedMove.onStopExecuting(this.creature);
                    this.setCoolDown(this.creature.learnableMoves().indexOf(this.creature.currentCreatureMove()), 0);
                    this.finishedMarker = true;
                    this.canAttackInRange = false;
                    this.moveChoiceCooldown = 20;
                }
                if (this.animTime < this.maxMoveAnimTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                }
                if (!this.finishedMarker) this.animTime++;
            }
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
        else return false;
    }
}
