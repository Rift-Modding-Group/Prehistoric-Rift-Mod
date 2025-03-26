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
    private boolean canBeExecutedMountedResult = false;

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
            this.canBeExecutedMountedResult = this.setCanBeExecutedMountedResult();

            if (this.canBeExecutedMountedResult) {
                this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(0));

                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpPoint();
                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUsePoint();
                this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
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
            this.canBeExecutedMountedResult = this.setCanBeExecutedMountedResult();

            if (this.canBeExecutedMountedResult) {
                this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(1));

                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpPoint();
                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUsePoint();
                this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
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
            this.currentInvokedMove = this.creature.getLearnedMoves().get(2).invokeMove();
            this.target = this.getAttackTarget(this.currentInvokedMove.creatureMove.moveType);
            this.canBeExecutedMountedResult = this.setCanBeExecutedMountedResult();

            if (this.canBeExecutedMountedResult) {
                this.creature.setCurrentCreatureMove(this.creature.getLearnedMoves().get(2));

                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpPoint();
                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUsePoint();
                this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
            }
            else {
                if (this.currentInvokedMove.cannotExecuteMountedMessage() != null) {
                    ((EntityPlayer) this.creature.getControllingPassenger()).sendStatusMessage(new TextComponentTranslation(this.currentInvokedMove.cannotExecuteMountedMessage()), false);
                }
                this.currentInvokedMove = null;
                this.target = null;
            }
        }
        if (this.currentInvokedMove != null && this.canBeExecutedMountedResult) {
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
        this.creature.setMultistepMoveStep(0);
        this.canBeExecutedMountedResult = false;
        this.creature.setPlayingInfiniteMoveAnim(false);
        this.creature.setCurrentCreatureMove(null);
        this.maxMoveAnimTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;
        this.creature.setPlayingChargedMoveAnim(-1);
    }

    @Override
    public void updateTask() {
        if (this.currentInvokedMove == null || !this.canBeExecutedMountedResult) {
            this.finishFlag = true;
            return;
        }

        if (!this.finishFlag) {
            //gradient then use, aka holding click charges up, then releasing it activates the move
            if (this.creature.currentCreatureMove().chargeType == CreatureMove.ChargeType.GRADIENT_THEN_USE) {
                if (this.animTime == 0 && this.moveAnimInitDelayTime >= 0) {
                    this.creature.setPlayingChargedMoveAnim(0);
                }
                if (this.animTime == this.moveAnimInitDelayTime) {
                    this.creature.setPlayingChargedMoveAnim(1);
                    this.currentInvokedMove.onStartExecuting(this.creature);
                    this.setChargedMoveBeingUsed(true);

                    //this is here because putting this in this.animTime == this.moveAnimChargeToUseTime
                    //makes the anim prematurely stop
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(true);
                }
                if (this.animTime == this.moveAnimChargeUpTime || (this.creature.currentCreatureMove().stopUponFullCharge && this.getUse() >= this.creature.currentCreatureMove().maxUse)) {
                    this.creature.setPlayingChargedMoveAnim(2);
                    this.setChargedMoveBeingUsed(false);
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime >= this.moveAnimInitDelayTime && this.animTime <= this.moveAnimChargeUpTime) {
                    this.currentInvokedMove.whileChargingUp(this.creature);
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.creature.setPlayingChargedMoveAnim(3);
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
                    this.creature.setPlayingChargedMoveAnim(4);
                    if (this.currentInvokedMove.forceStopFlag && this.creature.currentCreatureMove().chargeUpAffectsUseTime) {
                        this.moveAnimUseTime -= this.currentInvokedMove.getUseValue();
                        this.maxMoveAnimTime -= this.currentInvokedMove.getUseValue();
                    }
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(false);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.creature.setPlayingChargedMoveAnim(-1);
                    int cooldownGradient = 1;
                    if (this.creature.currentCreatureMove().maxCooldown > 0 && this.creature.currentCreatureMove().maxUse > 0) {
                        cooldownGradient = this.creature.currentCreatureMove().maxCooldown/this.creature.currentCreatureMove().maxUse;
                    }
                    this.creature.setMoveCooldown(this.getUse() * cooldownGradient);
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
            //gradient while use, aka holding click uses the move, reaching max use automatically stops the move
            else if (this.creature.currentCreatureMove().chargeType == CreatureMove.ChargeType.GRADIENT_WHILE_USE) {
                if (this.animTime == 0 && this.moveAnimInitDelayTime >= 0) {
                    this.creature.setPlayingChargedMoveAnim(0);
                }
                if (this.animTime == this.moveAnimInitDelayTime) {
                    this.creature.setPlayingChargedMoveAnim(1);
                    this.currentInvokedMove.onStartExecuting(this.creature);
                    this.setChargedMoveBeingUsed(true);

                    //gradient while use moves should always have infinite move use anim time
                    //(by infinite its until they release the mouse button associated with the move)
                    this.creature.setPlayingInfiniteMoveAnim(true);
                }
                if (this.animTime == this.moveAnimChargeUpTime || (this.creature.currentCreatureMove().stopUponFullCharge && this.getUse() >= this.creature.currentCreatureMove().maxUse)) {
                    this.creature.setPlayingChargedMoveAnim(2);
                    this.setChargedMoveBeingUsed(false);
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime >= this.moveAnimInitDelayTime && this.animTime <= this.moveAnimChargeUpTime) {
                    this.currentInvokedMove.whileChargingUp(this.creature);
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.creature.setPlayingChargedMoveAnim(3);
                    this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                    this.currentInvokedMove.setUseValue(this.getUse());
                    this.moveAnimUseTime += this.getUse();
                    this.maxMoveAnimTime += this.getUse();
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    this.creature.setPlayingChargedMoveAnim(4);
                    if (this.currentInvokedMove.forceStopFlag) {
                        this.moveAnimUseTime -= this.currentInvokedMove.getUseValue();
                        this.maxMoveAnimTime -= this.currentInvokedMove.getUseValue();
                    }
                    this.creature.setPlayingInfiniteMoveAnim(false);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.creature.setPlayingChargedMoveAnim(-1);
                    int cooldownGradient = 1;
                    if (this.creature.currentCreatureMove().maxCooldown > 0 && this.creature.currentCreatureMove().maxUse > 0) {
                        cooldownGradient = this.creature.currentCreatureMove().maxCooldown/this.creature.currentCreatureMove().maxUse;
                    }
                    this.creature.setMoveCooldown(this.getUse() * cooldownGradient);
                    this.finishFlag = true;
                    this.animTime = 0;
                    this.creature.setMoveOneUse(0);
                    this.creature.setMoveTwoUse(0);
                    this.creature.setMoveThreeUse(0);
                }
                if (!this.finishFlag) {
                    this.animTime++;
                    if (this.getMoveIsUsing()) {
                        this.moveAnimUseTime++;
                        this.maxMoveAnimTime++;
                    }
                }
            }
            //no gradients or whatever, so just clicking activates the move
            else {
                if (this.animTime == 0) {
                    this.currentInvokedMove.onStartExecuting(this.creature);
                    this.creature.setUsingUnchargedAnim(true);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) {
                        this.creature.setMultistepMoveStep(0);
                    }
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime >= this.moveAnimInitDelayTime && this.animTime <= this.moveAnimChargeUpTime) {
                    this.currentInvokedMove.whileChargingUp(this.creature);
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) {
                        this.creature.setMultistepMoveStep(1);
                    }
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) {
                        this.creature.setMultistepMoveStep(2);
                    }
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.animTime = 0;
                    this.creature.setUsingUnchargedAnim(false);
                    this.currentInvokedMove.onStopExecuting(this.creature);
                    //the cloak move only has a cooldown when removing the cloaking
                    if (this.creature.canUtilizeCloaking() && this.creature.currentCreatureMove() == CreatureMove.CLOAK && !this.creature.isCloaked()) {
                        this.creature.setMoveCooldown(this.creature.currentCreatureMove().maxCooldown);
                    }
                    //all other moves should have their cooldown applied as usual
                    else if (this.creature.currentCreatureMove() != CreatureMove.CLOAK) this.creature.setMoveCooldown(this.creature.currentCreatureMove().maxCooldown);
                    //if move is build up type, upon reaching this phase the move use resets
                    if (this.creature.currentCreatureMove().chargeType == CreatureMove.ChargeType.BUILDUP) {
                        this.creature.setCurrentMoveUse(0);
                    }
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setMultistepMoveStep(0);
                    this.finishFlag = true;
                }
                if (!this.finishFlag) {
                    this.animTime++;
                    if (this.creature.currentCreatureMove().useTimeIsInfinite && this.animTime >= this.moveAnimChargeToUseTime && !this.currentInvokedMove.forceStopFlag) {
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

    private Entity getAttackTarget(CreatureMove.MoveType creatureMoveType) {
        if (creatureMoveType != CreatureMove.MoveType.RANGED && creatureMoveType != CreatureMove.MoveType.CHARGE) {
            return this.creature.getClosestTargetInFront();
        }
        else return this.creature.getClosestTargetInFront(true);
    }

    private boolean setCanBeExecutedMountedResult() {
        //if current invoked move is a buildup type move, make sure that in addition to the moves conditions,
        //the moves use bar is full
        if (this.currentInvokedMove.creatureMove.chargeType == CreatureMove.ChargeType.BUILDUP) {
            int movePos = this.creature.getLearnedMoves().indexOf(this.currentInvokedMove.creatureMove);
            return this.creature.getMoveUse(movePos) == this.currentInvokedMove.creatureMove.maxUse && this.currentInvokedMove.canBeExecutedMounted(this.creature, this.target);
        }
        //otherwise, just use the moves conditions
        return this.currentInvokedMove.canBeExecutedMounted(this.creature, this.target);
    }
}
