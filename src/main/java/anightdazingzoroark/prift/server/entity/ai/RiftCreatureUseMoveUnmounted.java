package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.RiftSoundLooper;
import anightdazingzoroark.prift.server.entity.creature.*;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveCondition;
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
    private final List<CreatureMove> selectedMoveBlacklist = new ArrayList<>();
    private CreatureMoveCondition.Condition lastUsedCondition;

    //for force stopping when no move can be selected
    private int failCount;
    private boolean forceStopFromFail;

    //for sound looping
    private RiftSoundLooper chargeUpSoundLooper;
    private RiftSoundLooper useSoundLooper;

    public RiftCreatureUseMoveUnmounted(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return (this.creature.isTamed() || !this.creature.fleesFromDanger())
                //&& (this.creature.getAttackTarget() != null && RiftUtil.checkForNoAssociations(this.creature, this.creature.getAttackTarget()))
                && this.creature.getMoveConditionStack() != null && !this.creature.getMoveConditionStack().getConditions().isEmpty()
                && !this.creature.isBeingRidden()
                && this.waterCreatureFlopTest();
                //&& this.creature.getAttackTarget().canEntityBeSeen(this.creature);
    }

    private boolean waterCreatureFlopTest() {
        if (!(this.creature instanceof RiftWaterCreature)) return true;

        RiftWaterCreature waterCreature = (RiftWaterCreature) this.creature;
        return !waterCreature.canFlop() || waterCreature.isInWater();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.isBeingRidden() && !this.finishedAnimMarker && !this.forceStopFromFail
                && (this.creature.currentCreatureMove() != null || (this.creature.getMoveConditionStack() != null && !this.creature.getMoveConditionStack().getConditions().isEmpty()));
    }

    @Override
    public void startExecuting() {
        this.failCount = 0;
        this.forceStopFromFail = false;
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
        this.failCount = 0;
        this.forceStopFromFail = false;
        this.creature.setCanMove(true);
        if (this.currentInvokedMove != null) {
            this.creature.setUsingUnchargedAnim(false);
            this.currentInvokedMove.onStopExecuting(this.creature);
            this.currentInvokedMove = null;
        }
        this.creature.setCurrentMoveUse(0);
        this.creature.setPlayingInfiniteMoveAnim(false);
        this.setChargedMoveBeingUsed(false);
        this.creature.setCurrentCreatureMove(null);
        this.moveToTest = null;

        //remove certain conditions from stack
        //if they were top to begin with
        if (this.lastUsedCondition != null &&
                (this.lastUsedCondition.equals(CreatureMoveCondition.Condition.CHECK_HIT) || this.lastUsedCondition.equals(CreatureMoveCondition.Condition.CHECK_UNCLOAKED))) {
            this.creature.removeFromConditionStack(this.lastUsedCondition);
        }

        //clear conditions if its being mounted
        if (this.creature.isBeingRidden()) this.creature.clearConditionStack();

        this.maxMoveAnimTime = 0;
        this.moveAnimInitDelayTime = 0;
        this.moveAnimChargeUpTime = 0;
        this.moveAnimChargeToUseTime = 0;
        this.moveAnimUseTime = 0;
        this.animTime = 0;

        this.maxChargeTime = 0;

        this.creature.setUnchargedMultistepMoveStep(0);
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

                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound() != null
                            && !this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).chargeUpSoundCanLoop())
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound(),
                                1f,
                                1f);
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.creature.setPlayingChargedMoveAnim(2);
                    this.setChargedMoveBeingUsed(false);
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime >= this.moveAnimInitDelayTime && this.animTime < this.moveAnimChargeUpTime && this.creature.getCurrentMoveUse() < this.maxChargeTime) {
                    this.currentInvokedMove.whileChargingUp(this.creature);
                    this.creature.setCurrentMoveUse(this.creature.getCurrentMoveUse() + 1);
                    if (this.chargeUpSoundLooper != null
                            && this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound() != null
                            && this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).chargeUpSoundCanLoop())
                        this.chargeUpSoundLooper.playSound();
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.creature.setPlayingChargedMoveAnim(3);
                    if (this.moveCanHitTarget(this.creature.currentCreatureMove())) this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound() != null)
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound(),
                                1f,
                                1f);
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseParticles() != null)
                        this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseParticles().createParticle();
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                    if (this.useSoundLooper != null) this.useSoundLooper.playSound();
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    this.creature.setPlayingChargedMoveAnim(4);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(false);
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound() != null)
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound(),
                                1f,
                                1f);
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

                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound() != null
                            && !this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).chargeUpSoundCanLoop())
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound(),
                                1f,
                                1f);
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.creature.setPlayingChargedMoveAnim(2);
                    this.setChargedMoveBeingUsed(false);
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    this.creature.setPlayingChargedMoveAnim(3);
                    if (this.moveCanHitTarget(this.creature.currentCreatureMove())) this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound() != null)
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound(),
                                1f,
                                1f);
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseParticles() != null)
                        this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseParticles().createParticle();
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    if (!this.currentInvokedMove.forceStopFlag) this.currentInvokedMove.whileExecuting(this.creature);
                    if (this.useSoundLooper != null) this.useSoundLooper.playSound();
                    if (this.creature.getCurrentMoveUse() < this.maxChargeTime)
                        this.creature.setCurrentMoveUse(this.creature.getCurrentMoveUse() + 1);
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    this.creature.setPlayingChargedMoveAnim(4);
                    this.currentInvokedMove.onBeforeStopExecuting(this.creature);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) this.creature.setPlayingInfiniteMoveAnim(false);
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound() != null)
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound(),
                                1f,
                                1f);
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
                    this.currentInvokedMove.onStartExecuting(this.creature, this.target);
                    this.creature.setUsingUnchargedAnim(true);
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) {
                        this.creature.setUnchargedMultistepMoveStep(1);
                    }
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound() != null
                            && !this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).chargeUpSoundCanLoop())
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound(),
                                1f,
                                1f);
                }
                if (this.animTime == this.moveAnimChargeUpTime) {
                    this.currentInvokedMove.onEndChargeUp(this.creature, this.creature.getCurrentMoveUse());
                }
                if (this.animTime == this.moveAnimChargeToUseTime) {
                    if (this.moveCanHitTarget(this.creature.currentCreatureMove())) this.currentInvokedMove.onReachUsePoint(this.creature, this.target);
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound() != null)
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound(),
                                1f,
                                1f);
                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseParticles() != null)
                        this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseParticles().createParticle();
                }
                if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                    this.currentInvokedMove.whileExecuting(this.creature);
                    if (this.useSoundLooper != null) this.useSoundLooper.playSound();
                    if (this.creature.currentCreatureMove().useTimeIsInfinite) {
                        this.creature.setUnchargedMultistepMoveStep(2);
                    }
                }
                if ((this.animTime >= this.moveAnimUseTime && this.animTime <= this.maxMoveAnimTime)
                        || this.currentInvokedMove.forceStopFlag) {
                    if (this.creature.currentCreatureMove().useTimeIsInfinite)
                        this.creature.setUnchargedMultistepMoveStep(3);

                    if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound() != null)
                        this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound(),
                                1f,
                                1f);
                }
                if (this.animTime >= this.maxMoveAnimTime) {
                    this.creature.setUsingUnchargedAnim(false);
                    this.currentInvokedMove.onStopExecuting(this.creature);
                    //the cloak move only has a cooldown when removing the cloaking
                    if (this.creature.currentCreatureMove() == CreatureMove.CLOAK && !this.creature.isCloaked()) {
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
        if (this.target != null) this.creature.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
        //select the move
        if (this.selectingMove) {
            this.moveToTest = this.getMoveForConditionStack();

            if (this.moveToTest != null) {
                this.selectingMove = false;
            }
            //if no move is selected after 5 tries, just stop this ai goal entirely
            else {
                this.failCount++;
                if (this.failCount >= 5) this.forceStopFromFail = true;
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

            if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound() != null
                    && this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).chargeUpSoundCanLoop())
                this.chargeUpSoundLooper = new RiftSoundLooper(this.creature,
                        this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound(),
                        20,
                        1f,
                        1f);
            if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getUseDurationSound() != null)
                this.useSoundLooper = new RiftSoundLooper(this.creature,
                        this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getUseDurationSound(),
                        5,
                        1f,
                        1f);

            this.finishedMoveMarker = false;
            this.animTime = 0;
            this.creature.setPlayingInfiniteMoveAnim(false);
        }
    }

    private CreatureMove getMoveForConditionStack() {
        CreatureMove toReturn = null;
        for (CreatureMoveCondition.Condition condition : this.creature.getMoveConditionStack().getConditions()) {
            List<CreatureMove> movesToTest = this.creature.getUsableMovesFromConditionInStack(condition);

            //deal with rng and creature size restrictions here
            //as well as some additional stuff in the current condition
            List<CreatureMove> movesToRemove = new ArrayList<>();
            for (CreatureMove test : movesToTest) {
                //deal with rng chance
                if (test.moveCondition.getRNGChance() > 0) {
                    if (this.creature.world.rand.nextInt(test.moveCondition.getRNGChance()) != 0) movesToRemove.add(test);
                }

                //deal with restriction by size
                if (test.moveCondition.isRestrictedBySize() && this.target != null) {
                    if (!RiftUtil.isAppropriateSizeNotEqual(this.target, RiftUtil.getMobSize(this.creature))) movesToRemove.add(test);
                }

                //deal with health being below certain percentage associated with move
                if (test.moveCondition.conditions.contains(CreatureMoveCondition.Condition.HEALTH_BELOW_VALUE)) {
                    if (this.creature.getHealth() > this.creature.getMaxHealth() * test.moveCondition.getBelowHealthPercentage()) {
                        movesToRemove.add(test);
                    }
                }

                //create boolean for if creature age is within certain interval for use
                if (test.moveCondition.conditions.contains(CreatureMoveCondition.Condition.INTERVAL)) {
                    if (this.creature.lastIntervalForMoveCall != test.moveCondition.getTickInterval()) {
                        movesToRemove.add(test);
                    }
                }
            }

            //remove moves in movesToRemove from movesToTest
            movesToTest.removeAll(movesToRemove);

            //now test each move individually
            if (!movesToTest.isEmpty()) {
                if (condition.equals(CreatureMoveCondition.Condition.CHECK_TARGET)) {
                    toReturn = this.getMoveForCheckTargetCondition(movesToTest);
                }
                else toReturn = RiftUtil.getRandomFromList(movesToTest, m -> !this.selectedMoveBlacklist.contains(m) && moveIsSelectable(m));
            }

            if (toReturn != null) {
                this.lastUsedCondition = condition;
                break;
            }
        }
        return toReturn;
    }

    private CreatureMove getMoveForCheckTargetCondition(List<CreatureMove> movesToTest) {
        if (this.target == null) return null;

        CreatureMove toReturn = null;

        //when in turret mode, only select ranged moves
        if (this.creature.canEnterTurretMode() && this.creature.isTurretMode()) {
            boolean hasUsableRangedMove = movesToTest.stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                    && !this.selectedMoveBlacklist.contains(m));
            if (hasUsableRangedMove) {
                toReturn = RiftUtil.getRandomFromList(movesToTest, m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                        && !this.selectedMoveBlacklist.contains(m));
            }
            if (!this.moveIsSelectable(toReturn)) {
                this.selectedMoveBlacklist.add(toReturn);
                toReturn = null;
            }
        }
        //otherwise just choose based on a pretty strict flowchart
        else {
            if (movesToTest.stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.SUPPORT
                    && !this.selectedMoveBlacklist.contains(m))) {
                toReturn = RiftUtil.getRandomFromList(movesToTest, m -> m.moveAnimType.moveType == CreatureMove.MoveType.SUPPORT
                        && !this.selectedMoveBlacklist.contains(m));
                if (!this.moveIsSelectable(toReturn)) {
                    this.selectedMoveBlacklist.add(toReturn);
                    toReturn = null;
                }
            }
            else if (movesToTest.stream().anyMatch(m -> !this.selectedMoveBlacklist.contains(m))) {
                boolean hasUsableRangedMove = movesToTest.stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                        && !this.selectedMoveBlacklist.contains(m));
                boolean hasUsableRangedSeldomMove = movesToTest.stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED_SELDOM
                        && !this.selectedMoveBlacklist.contains(m));
                boolean hasUsableMeleeMove = movesToTest.stream().anyMatch(m -> m.moveAnimType.moveType == CreatureMove.MoveType.MELEE
                        && !this.selectedMoveBlacklist.contains(m));

                //path to target to then use ranged attack
                if (this.creature.getDistance(this.target) > this.creature.rangedWidth()) {
                    if (!this.creature.hasPath()) this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                }
                //either use ranged attack or path to target to then use melee attack
                else if (this.creature.getDistance(this.target) <= this.creature.rangedWidth()
                        && this.creature.getDistance(this.target) > (this.creature.attackWidth() + this.creature.width)) {
                    if (hasUsableRangedSeldomMove) {
                        toReturn = RiftUtil.getRandomFromList(movesToTest, m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED_SELDOM
                                && !this.selectedMoveBlacklist.contains(m));

                        if (!this.moveIsSelectable(toReturn)) {
                            this.selectedMoveBlacklist.add(toReturn);
                            toReturn = null;
                        }
                    }
                    else if (hasUsableRangedMove) {
                        toReturn = RiftUtil.getRandomFromList(movesToTest, m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                                && !this.selectedMoveBlacklist.contains(m));
                    }
                    else if (hasUsableMeleeMove) {
                        if (!this.creature.hasPath()) this.creature.getNavigator().tryMoveToEntityLiving(this.target, 1.0D);
                    }
                }
                //either use ranged attack or use melee attack
                else if (this.creature.getDistance(this.target) <= (this.creature.attackWidth() + this.creature.width)) {
                    if (hasUsableMeleeMove) {
                        toReturn = RiftUtil.getRandomFromList(movesToTest, m -> m.moveAnimType.moveType == CreatureMove.MoveType.MELEE
                                && !this.selectedMoveBlacklist.contains(m));

                    }
                    else if (hasUsableRangedSeldomMove) {
                        toReturn = RiftUtil.getRandomFromList(movesToTest, m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED_SELDOM
                                && !this.selectedMoveBlacklist.contains(m));

                        if (!this.moveIsSelectable(toReturn)) {
                            this.selectedMoveBlacklist.add(toReturn);
                            toReturn = null;
                        }
                    }
                    else if (hasUsableRangedMove) {
                        toReturn = RiftUtil.getRandomFromList(movesToTest, m -> m.moveAnimType.moveType == CreatureMove.MoveType.RANGED
                                && !this.selectedMoveBlacklist.contains(m));
                    }
                }
            }
        }

        return toReturn;
    }

    private boolean moveIsSelectable(CreatureMove move) {
        if (move == null || move.creatureMove == null) return false;

        //get pos associated with move
        int pos = this.creature.getLearnedMoves().indexOf(move);
        if (pos == -1) return false;

        //check if theres animators available for move
        if (this.creature.animatorsForMoveType().get(move.moveAnimType) == null) return false;

        //invoke the move to get its associated check for use
        RiftCreatureMove invokedCreatureMove = move.invokeMove();
        boolean energyCheck = move.chargeType.requiresCharge() ? (this.creature.getEnergy() - move.energyUse[0] >= this.creature.getWeaknessEnergy()) : (this.creature.getEnergy() - move.energyUse[0] >= 0);
        return this.creature.getMoveCooldown(pos) == 0
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
                || move.moveAnimType.moveType == CreatureMove.MoveType.SUPPORT
                || move.moveAnimType == CreatureMove.MoveAnimType.SELF_DESTRUCTION) return true;
        else return this.creature.getDistance(this.target) <= (this.creature.attackWidth() + this.creature.width);
    }
}
