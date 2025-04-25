package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
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
    //for move selection
    private boolean selectingMove = true;
    private CreatureMove moveToTest = null;
    private List<CreatureMove> selectedMoveBlacklist = new ArrayList<>();

    public RiftCreatureUseMoveUnmounted(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return  (this.creature.isTamed() || !this.creature.fleesFromDanger())
                && (this.creature.getAttackTarget() != null && RiftUtil.checkForNoAssociations(this.creature, this.creature.getAttackTarget()))
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
        this.selectingMove = true;
        this.selectedMoveBlacklist.clear();
        this.creature.setCurrentCreatureMove(null);
        this.target = this.creature.getAttackTarget();
        this.currentInvokedMove = null;

        this.maxMoveAnimTime = 0;
        this.moveAnimInitDelayTime = 0;
        this.moveAnimChargeUpTime = 0;
        this.moveAnimChargeToUseTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;

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
            else this.moveSelection();
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
                    this.currentInvokedMove.onStartExecuting(this.creature, this.target);
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
                    this.moveChoiceCooldown = 60;
                    this.animTime = 0;
                    this.finishedAnimMarker = true;
                    this.finishedMoveMarker = true;
                }

                //updating move anim tick
                if (!this.finishedMoveMarker) {
                    this.animTime++;
                    if (this.creature.currentCreatureMove().useTimeIsInfinite && this.animTime > this.moveAnimChargeToUseTime && !this.currentInvokedMove.forceStopFlag) {
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
                    this.currentInvokedMove.onStartExecuting(this.creature, this.target);
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
                    this.moveChoiceCooldown = 60;
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
                    this.currentInvokedMove.onStartExecuting(this.creature, this.target);
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
                    if (this.creature.currentCreatureMove().useTimeIsInfinite && this.animTime > this.moveAnimChargeToUseTime && !this.currentInvokedMove.forceStopFlag) {
                        this.moveAnimUseTime++;
                        this.maxMoveAnimTime++;
                    }
                }
            }
        }
    }

    private void moveSelection() {
        this.creature.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
        //select the move
        if (this.selectingMove) {
            if (this.creature.getLearnedMoves().stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.SUPPORT
                    && !selectedMoveBlacklist.contains(m))) {
                int usableSupportMoveCount = (int) this.creature.getLearnedMoves().stream().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.SUPPORT
                        && !this.selectedMoveBlacklist.contains(m)).count();
                this.moveToTest = this.creature.getLearnedMoves().stream().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.SUPPORT
                                && !this.selectedMoveBlacklist.contains(m))
                        .skip(this.creature.world.rand.nextInt(usableSupportMoveCount))
                        .findAny().get();
                if (this.moveIsSelectable(moveToTest)) this.selectingMove = false;
                else selectedMoveBlacklist.add(moveToTest);
            }
            else if (this.creature.getLearnedMoves().stream().anyMatch(m -> !selectedMoveBlacklist.contains(m))) {
                boolean hasUsableRangedMove = this.creature.getLearnedMoves().stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                        && !selectedMoveBlacklist.contains(m));
                int usableRangedMoveCount = (int) this.creature.getLearnedMoves().stream().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                        && !selectedMoveBlacklist.contains(m)).count();
                boolean hasUsableRangedSeldomMove = this.creature.getLearnedMoves().stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED_SELDOM
                        && !selectedMoveBlacklist.contains(m));
                int usableRangedSeldomMoveCount = (int) this.creature.getLearnedMoves().stream().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED_SELDOM
                        && !selectedMoveBlacklist.contains(m)).count();
                boolean hasUsableMeleeMove = this.creature.getLearnedMoves().stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.MELEE
                        && !selectedMoveBlacklist.contains(m));
                int usableMeleeMoveCount = (int) this.creature.getLearnedMoves().stream().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.MELEE
                        && !selectedMoveBlacklist.contains(m)).count();

                //path to target to then use ranged attack
                if (this.creature.getDistance(this.target) > this.creature.rangedWidth()) {
                    if (!this.creature.hasPath()) this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                }
                //either use ranged attack or path to target to then use melee attack
                else if (this.creature.getDistance(this.target) <= this.creature.rangedWidth()
                    && this.creature.getDistance(this.target) > (this.creature.attackWidth() + this.creature.width)) {
                    if (hasUsableRangedSeldomMove) {
                        this.moveToTest = this.creature.getLearnedMoves().stream()
                                .filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED_SELDOM
                                        && !selectedMoveBlacklist.contains(m))
                                .skip(this.creature.world.rand.nextInt(usableRangedSeldomMoveCount))
                                .findFirst().get();

                        if (this.moveIsSelectable(moveToTest)) this.selectingMove = false;
                        else selectedMoveBlacklist.add(moveToTest);
                    }
                    else if (hasUsableRangedMove) {
                        this.moveToTest = this.creature.getLearnedMoves().stream().parallel().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                                        && !selectedMoveBlacklist.contains(m))
                                .skip(this.creature.world.rand.nextInt(usableRangedMoveCount))
                                .findFirst().get();

                        if (this.moveIsSelectable(moveToTest)) this.selectingMove = false;
                    }
                    else if (hasUsableMeleeMove) {
                        if (!this.creature.hasPath()) this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                    }
                }
                //either use ranged attack or use melee attack
                else if (this.creature.getDistance(this.target) <= (this.creature.attackWidth() + this.creature.width)) {
                    if (hasUsableMeleeMove) {
                        this.moveToTest = this.creature.getLearnedMoves().stream().parallel().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.MELEE
                                        && !this.selectedMoveBlacklist.contains(m))
                                .skip(this.creature.world.rand.nextInt(usableMeleeMoveCount))
                                .findFirst().get();

                        if (this.moveIsSelectable(this.moveToTest)) this.selectingMove = false;
                    }
                    else if (hasUsableRangedSeldomMove) {
                        this.moveToTest = this.creature.getLearnedMoves().stream().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED_SELDOM
                                        && !this.selectedMoveBlacklist.contains(m))
                                .skip(this.creature.world.rand.nextInt(usableRangedSeldomMoveCount))
                                .findFirst().get();

                        if (this.moveIsSelectable(this.moveToTest)) this.selectingMove = false;
                        else this.selectedMoveBlacklist.add(this.moveToTest);
                    }
                    else if (hasUsableRangedMove) {
                        this.moveToTest = this.creature.getLearnedMoves().stream().parallel().filter(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                                        && !this.selectedMoveBlacklist.contains(m))
                                .skip(this.creature.world.rand.nextInt(usableRangedMoveCount))
                                .findFirst().get();

                        if (this.moveIsSelectable(this.moveToTest)) this.selectingMove = false;
                    }
                }
            }
        }
        else {
            this.creature.setCurrentCreatureMove(this.moveToTest);
            this.currentInvokedMove = this.moveToTest.invokeMove();

            if (this.moveToTest.chargeType == CreatureMove.ChargeType.GRADIENT_THEN_USE) {
                if (this.moveToTest.moveAnimType == CreatureMove.MoveAnimType.CHARGE)
                    this.maxChargeTime = (this.creature.getEnergy() - this.moveToTest.energyUse[1] >= this.creature.getWeaknessEnergy()) ? this.moveToTest.maxUse :
                            (int) RiftUtil.slopeResult(this.creature.getEnergy() - this.creature.getWeaknessEnergy(),
                                    true,
                                    this.moveToTest.energyUse[0],
                                    this.moveToTest.energyUse[1],
                                    0,
                                    this.moveToTest.maxUse);
                else {
                    int tempMaxChargeTime = RiftUtil.randomInRange(this.currentInvokedMove.unmountedChargeBounds()[0], this.currentInvokedMove.unmountedChargeBounds()[1]);
                    int tempEnergyToTest = (int) RiftUtil.slopeResult(tempMaxChargeTime, true, 0, moveToTest.maxUse, moveToTest.energyUse[0], moveToTest.energyUse[1]);
                    this.maxChargeTime = (this.creature.getEnergy() - tempEnergyToTest >= this.creature.getWeaknessEnergy()) ? tempMaxChargeTime :
                            (int)RiftUtil.slopeResult(this.creature.getEnergy() - this.creature.getWeaknessEnergy(),
                                    true,
                                    moveToTest.energyUse[0],
                                    moveToTest.energyUse[1],
                                    0,
                                    moveToTest.maxUse);
                }

                //set move anim markers
                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getStartMoveDelayPoint();
                this.moveAnimChargeUpTime = this.moveAnimInitDelayTime + this.maxChargeTime;
                this.moveAnimChargeToUseTime = this.moveAnimChargeUpTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseTime();
                this.moveAnimUseTime = this.moveAnimChargeToUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getUseDurationTime();
                this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseTime();
            }
            else if (moveToTest.chargeType == CreatureMove.ChargeType.GRADIENT_WHILE_USE) {
                int tempMaxChargeTime = RiftUtil.randomInRange(this.currentInvokedMove.unmountedChargeBounds()[0], this.currentInvokedMove.unmountedChargeBounds()[1]);
                int tempEnergyToTest = (int) RiftUtil.slopeResult(tempMaxChargeTime, true, 0, moveToTest.maxUse, moveToTest.energyUse[0], moveToTest.energyUse[1]);
                this.maxChargeTime = (this.creature.getEnergy() - tempEnergyToTest >= this.creature.getWeaknessEnergy()) ? tempMaxChargeTime :
                        (int)RiftUtil.slopeResult(this.creature.getEnergy() - this.creature.getWeaknessEnergy(),
                                true,
                                moveToTest.energyUse[0],
                                moveToTest.energyUse[1],
                                0,
                                moveToTest.maxUse);

                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getStartMoveDelayPoint();
                this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpPoint();
                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUsePoint();
                this.moveAnimUseTime = this.moveAnimChargeToUseTime + this.maxChargeTime;
                this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseTime();
            }
            else {
                this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getStartMoveDelayPoint();
                this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpPoint();
                this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUsePoint();
                this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getUseDurationPoint();
                this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUsePoint();
            }
            this.finishedMoveMarker = false;
            this.animTime = 0;
            this.creature.setPlayingInfiniteMoveAnim(false);
        }
    }

    private boolean moveIsSelectable(CreatureMove move) {
        if (move == null) return false;
        //get pos associated with move
        int pos = this.creature.getLearnedMoves().indexOf(move);
        //invoke the move to get its associated check for use
        RiftCreatureMove invokedCreatureMove = move.invokeMove();
        boolean energyCheck = move.chargeType.requiresCharge() ? (this.creature.getEnergy() - move.energyUse[0] >= this.creature.getWeaknessEnergy()) : (this.creature.getEnergy() - move.energyUse[0] >= 0);
        if (pos == -1) return false;
        else return this.creature.getMoveCooldown(pos) == 0
                && energyCheck
                && invokedCreatureMove.canBeExecutedUnmounted(this.creature, this.target);
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
        if (move.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                || move.moveAnimType.moveType == CreatureMove.MoveType.RANGED_SELDOM
                || move.moveAnimType.moveType == CreatureMove.MoveType.SUPPORT) return true;
        else return this.creature.getDistance(this.target) <= (this.creature.attackWidth() + this.creature.width);
    }
}
