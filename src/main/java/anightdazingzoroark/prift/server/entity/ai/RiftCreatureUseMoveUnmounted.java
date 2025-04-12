package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.entity.creature.Sarcosuchus;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.*;

public class RiftCreatureUseMoveUnmounted extends EntityAIBase {
    private final RiftCreature creature;
    private RiftCreatureMove currentInvokedMove;
    private EntityLivingBase target;
    private int maxMoveAnimTime; //entire time spent for animation
    private int moveAnimInitDelayTime; //time until end of delay point
    private int moveAnimChargeUpTime; //time until end of charge up point
    private int moveAnimChargeToUseTime; //time until end of charge up to use point
    private int moveAnimUseTime; //time until end of use anim
    private int animTime = 0;
    private boolean finishedMoveMarker;
    private boolean finishedAnimMarker;
    private int moveChoiceCooldown;
    private int maxChargeTime;

    public RiftCreatureUseMoveUnmounted(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return ((this.creature.getAttackTarget() != null && RiftUtil.checkForNoAssociations(this.creature, this.creature.getAttackTarget()))
                || (this.creature.getRevengeTarget() != null && RiftUtil.checkForNoAssociations(this.creature, this.creature.getRevengeTarget())))
                && !this.creature.isBeingRidden()
                && (!(this.creature instanceof RiftWaterCreature) || !((RiftWaterCreature) this.creature).canFlop());
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
        this.moveAnimInitDelayTime = 0;
        this.moveAnimChargeUpTime = 0;
        this.moveAnimChargeToUseTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;

        this.moveChoiceCooldown = 0;
        this.maxChargeTime = 0;
    }

    @Override
    public void resetTask() {
        this.creature.resetSpeed();
        if (this.currentInvokedMove != null) {
            this.creature.setUsingUnchargedAnim(false);
            this.currentInvokedMove.onStopExecuting(this.creature);
            this.currentInvokedMove = null;
        }
        this.creature.setCurrentMoveUse(0);
        this.creature.setPlayingInfiniteMoveAnim(false);
        this.setChargedMoveBeingUsed(false);
        this.creature.setCurrentCreatureMove(null);

        this.maxMoveAnimTime = 0;
        this.moveAnimInitDelayTime = 0;
        this.moveAnimChargeUpTime = 0;
        this.moveAnimChargeToUseTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;

        this.moveChoiceCooldown = 0;
        this.maxChargeTime = 0;

        this.creature.setMultistepMoveStep(0);
        this.creature.setPlayingChargedMoveAnim(-1);
    }

    @Override
    public void updateTask() {
        //randomly select a move to use, after which it will use that move
        if (this.finishedMoveMarker) {
            //cooldown of 1 second after using a move
            if (this.moveChoiceCooldown > 0) this.moveChoiceCooldown--;
            else {
                this.creature.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);

                //manage pathing towards target
                //non melee attacks are to be prioritized
                if (this.creature.getLearnedMoves().stream().anyMatch(m -> (m.moveType == CreatureMove.MoveType.RANGED
                        || (m.moveType == CreatureMove.MoveType.CHARGE && m.chargeType == CreatureMove.ChargeType.GRADIENT_THEN_USE)
                        || m.moveType == CreatureMove.MoveType.LEAP) && this.creature.getMoveCooldown(m) == 0)) {
                    if (this.creature.getDistance(this.target) > this.creature.rangedWidth()) {
                        this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                    }
                    else if (this.creature.getDistance(this.target) <= this.creature.rangedWidth()
                            && this.creature.getDistance(this.target) > this.creature.attackWidth() + 1
                            && this.creature.hasPath()) {
                        this.creature.getNavigator().clearPath();
                    }
                    else if (this.creature.getDistance(this.target) <= this.creature.attackWidth() + 1
                            && this.creature.getDistance(this.target) > this.creature.attackWidth()) {
                        this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                    }
                    else if (this.creature.getDistance(this.target) <= this.creature.attackWidth()
                            && this.creature.hasPath()) {
                        this.creature.getNavigator().clearPath();
                    }
                }
                //mostly for melee attacks
                else {
                    if (this.creature.getDistance(this.target) > this.creature.attackWidth()) {
                        this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                    }
                    else if (this.creature.getDistance(this.target) <= this.creature.attackWidth() && this.creature.hasPath()) {
                        this.creature.getNavigator().clearPath();
                    }
                }

                //selecting and starting to use move
                if (this.creature.getNavigator().noPath()) {
                    //select a valid move
                    CreatureMove selectedMove = this.selectMoveForUse();

                    if (selectedMove != null) {
                        //invoke move
                        this.currentInvokedMove = selectedMove.invokeMove();

                        //generate charge time
                        if (selectedMove.chargeType == CreatureMove.ChargeType.GRADIENT_THEN_USE) {
                            //if move involves charging into target, or is gradient while use,
                            //the max charge time is the whole value
                            //otherwise its random based on whats given in the invoked creature move's class
                            if (selectedMove.moveType == CreatureMove.MoveType.CHARGE)
                                this.maxChargeTime = this.creature.currentCreatureMove().maxUse;
                            else this.maxChargeTime = RiftUtil.randomInRange(this.currentInvokedMove.unmountedChargeBounds()[0], this.currentInvokedMove.unmountedChargeBounds()[1]);
                        }
                        else if (selectedMove.chargeType == CreatureMove.ChargeType.GRADIENT_WHILE_USE) {
                            this.maxChargeTime = RiftUtil.randomInRange(this.currentInvokedMove.unmountedChargeBounds()[0], this.currentInvokedMove.unmountedChargeBounds()[1]);
                        }

                        //when move is finally selected, set up everything for animatin the move
                        if (this.moveCanHitTarget(selectedMove) && this.energySufficientForMove(selectedMove)) {
                            this.creature.setCurrentCreatureMove(selectedMove);

                            if (selectedMove.chargeType == CreatureMove.ChargeType.GRADIENT_THEN_USE) {
                                //set move anim markers
                                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                                this.moveAnimChargeUpTime = this.moveAnimInitDelayTime + this.maxChargeTime;
                                this.moveAnimChargeToUseTime = this.moveAnimChargeUpTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUseTime();
                                this.moveAnimUseTime = this.moveAnimChargeToUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationTime();
                                this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUseTime();
                            }
                            else if (selectedMove.chargeType == CreatureMove.ChargeType.GRADIENT_WHILE_USE) {
                                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                                this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpPoint();
                                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUsePoint();
                                this.moveAnimUseTime = this.moveAnimChargeToUseTime + this.maxChargeTime;
                                this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUseTime();
                            }
                            else {
                                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getStartMoveDelayPoint();
                                this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpPoint();
                                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getChargeUpToUsePoint();
                                this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getUseDurationPoint();
                                this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveType).getRecoverFromUsePoint();
                            }
                            this.finishedMoveMarker = false;
                            this.animTime = 0;
                            this.creature.setPlayingInfiniteMoveAnim(false);
                        }
                    }
                }
            }
        }
        //use the selected move
        else {
            this.creature.getNavigator().clearPath();
            this.currentInvokedMove.lookAtTarget(this.creature, this.target);

            //gradient then use, aka holding click charges up, then releasing it activates the move
            if (this.creature.currentCreatureMove().chargeType == CreatureMove.ChargeType.GRADIENT_THEN_USE) {
                if (this.animTime == 0 && this.moveAnimInitDelayTime >= 0) {
                    this.creature.setPlayingChargedMoveAnim(0);
                }
                if (this.animTime == this.moveAnimInitDelayTime) {
                    this.creature.setPlayingChargedMoveAnim(1);
                    this.currentInvokedMove.onStartExecuting(this.creature);
                    this.setChargedMoveBeingUsed(true);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(true);
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.creature.setPlayingChargedMoveAnim(2);
                    this.setChargedMoveBeingUsed(false);
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime >= this.moveAnimInitDelayTime && this.animTime < this.moveAnimChargeUpTime && this.creature.getCurrentMoveUse() < this.maxChargeTime) {
                    this.currentInvokedMove.whileChargingUp(this.creature);
                    this.creature.setCurrentMoveUse(this.creature.getCurrentMoveUse() + 1);
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.creature.setPlayingChargedMoveAnim(3);
                    if (this.moveCanHitTarget(this.creature.currentCreatureMove())) this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    this.creature.setPlayingChargedMoveAnim(4);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(false);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.creature.setPlayingChargedMoveAnim(-1);
                    this.currentInvokedMove.onStopExecuting(this.creature);
                    this.setChargedMoveBeingUsed(false);
                    double cooldownGradient = 1;
                    if (this.creature.currentCreatureMove().maxCooldown > 0 && this.creature.currentCreatureMove().maxUse > 0) {
                        cooldownGradient = this.creature.currentCreatureMove().maxCooldown/(double)this.creature.currentCreatureMove().maxUse;
                    }
                    this.creature.setMoveCooldown((int) (this.creature.getCurrentMoveUse() * cooldownGradient));
                    this.creature.setEnergy(Math.max(this.creature.getEnergy() - Math.round(RiftUtil.slopeResult(this.creature.getCurrentMoveUse(),
                            true,
                            0,
                            this.currentInvokedMove.creatureMove.maxUse,
                            this.currentInvokedMove.creatureMove.energyUse[0],
                            this.currentInvokedMove.creatureMove.energyUse[1])), this.creature.getWeaknessEnergy()));
                    this.creature.setCurrentMoveUse(0);
                    this.moveChoiceCooldown = 20;
                    this.animTime = 0;
                    this.finishedAnimMarker = true;
                    this.finishedMoveMarker = true;
                }

                //updating move anim tick
                if (!this.finishedMoveMarker) this.animTime++;
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
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(true);
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.creature.setPlayingChargedMoveAnim(2);
                    this.setChargedMoveBeingUsed(false);
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.creature.setPlayingChargedMoveAnim(3);
                    if (this.moveCanHitTarget(this.creature.currentCreatureMove())) this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                    if (this.creature.getCurrentMoveUse() < this.maxChargeTime)
                        this.creature.setCurrentMoveUse(this.creature.getCurrentMoveUse() + 1);
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    this.creature.setPlayingChargedMoveAnim(4);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(false);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.creature.setPlayingChargedMoveAnim(-1);
                    this.currentInvokedMove.onStopExecuting(this.creature);
                    this.setChargedMoveBeingUsed(false);
                    double cooldownGradient = 1;
                    if (this.creature.currentCreatureMove().maxCooldown > 0 && this.creature.currentCreatureMove().maxUse > 0) {
                        cooldownGradient = this.creature.currentCreatureMove().maxCooldown/(double)this.creature.currentCreatureMove().maxUse;
                    }
                    this.creature.setMoveCooldown((int) (this.creature.getCurrentMoveUse() * cooldownGradient));
                    this.creature.setEnergy(Math.max(this.creature.getEnergy() - Math.round(RiftUtil.slopeResult(this.creature.getCurrentMoveUse(),
                            true,
                            0,
                            this.currentInvokedMove.creatureMove.maxUse,
                            this.currentInvokedMove.creatureMove.energyUse[0],
                            this.currentInvokedMove.creatureMove.energyUse[1])), this.creature.getWeaknessEnergy()));
                    this.creature.setCurrentMoveUse(0);
                    this.moveChoiceCooldown = 20;
                    this.animTime = 0;
                    this.finishedAnimMarker = true;
                    this.finishedMoveMarker = true;
                }

                //updating move anim tick
                if (!this.finishedMoveMarker) this.animTime++;
            }
            //anything else thats just one click then use goes here
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
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    if (this.moveCanHitTarget(this.creature.currentCreatureMove())) this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) {
                        this.creature.setMultistepMoveStep(1);
                    }
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    //also dont know what to put here
                    if (this.creature.currentCreatureMove().useTimeIsInfinite)
                        this.creature.setMultistepMoveStep(2);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    if (this.currentInvokedMove != null
                            && this.currentInvokedMove.creatureMove == CreatureMove.LEAP_ATTACK) System.out.println("end move");
                    this.creature.setUsingUnchargedAnim(false);
                    this.currentInvokedMove.onStopExecuting(this.creature);
                    //the cloak move only has a cooldown when removing the cloaking
                    if (this.creature.canUtilizeCloaking() && this.creature.currentCreatureMove() == CreatureMove.CLOAK && !this.creature.isCloaked()) {
                        this.creature.setMoveCooldown(this.creature.currentCreatureMove().maxCooldown);
                    }
                    //all other moves should have their cooldown applied as usual
                    else if (this.creature.currentCreatureMove() != CreatureMove.CLOAK) this.creature.setMoveCooldown(this.creature.currentCreatureMove().maxCooldown);
                    this.creature.setEnergy(this.creature.getEnergy() - this.creature.currentCreatureMove().energyUse[0]);
                    this.moveChoiceCooldown = 20;
                    this.animTime = 0;
                    this.finishedAnimMarker = true;
                    this.finishedMoveMarker = true;
                }

                //updating move anim tick
                if (!this.finishedMoveMarker) {
                    this.animTime++;
                    if (this.creature.currentCreatureMove().useTimeIsInfinite && this.animTime >= this.moveAnimChargeToUseTime && !this.currentInvokedMove.forceStopFlag) {
                        this.moveAnimUseTime++;
                        this.maxMoveAnimTime++;
                    }
                }
            }
        }
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

    private boolean moveCanHitTarget(CreatureMove move) {
        if (move.moveType == CreatureMove.MoveType.RANGED
                || move.moveType == CreatureMove.MoveType.STATUS
                || move.moveType == CreatureMove.MoveType.CHARGE
                || move.moveType == CreatureMove.MoveType.LEAP) return true;
        else return this.creature.getDistance(this.target) <= this.creature.attackWidth();
    }

    private CreatureMove selectMoveForUse() {
        Deque<CreatureMove> movesForSelecting = new ArrayDeque<>();
        List<CreatureMove> creatureMovesRandomized = new ArrayList<>(this.creature.getLearnedMoves());
        Collections.shuffle(creatureMovesRandomized);
        for (CreatureMove creatureMove : creatureMovesRandomized) {
            if (this.creature.getMoveCooldown(this.creature.getLearnedMoves().indexOf(creatureMove)) == 0) {
                RiftCreatureMove moveInvoked = creatureMove.invokeMove();
                if (moveInvoked.canBeExecutedUnmounted(this.creature, this.target) == RiftCreatureMove.MovePriority.HIGH)
                    movesForSelecting.addFirst(creatureMove);
                if (moveInvoked.canBeExecutedUnmounted(this.creature, this.target) == RiftCreatureMove.MovePriority.LOW)
                    movesForSelecting.addLast(creatureMove);
            }
        }
        if (movesForSelecting.isEmpty()) return null;
        else return movesForSelecting.getFirst();
    }

    private boolean energySufficientForMove(CreatureMove move) {
        if (move.chargeType.requiresCharge()) {
            return this.creature.getEnergy() - RiftUtil.slopeResult(
                    this.maxChargeTime,
                    true,
                    0,
                    move.maxUse,
                    move.energyUse[0],
                    move.energyUse[1]
            ) >= this.creature.getWeaknessEnergy();
        }
        return this.creature.getEnergy() - move.energyUse[0] >= 0;
    }
}
