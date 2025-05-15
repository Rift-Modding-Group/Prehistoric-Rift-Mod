package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.RiftSoundLooper;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.prift.server.entity.creatureMoves.RiftCreatureMove;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.entity.workstationData.RiftWorkstation;
import anightdazingzoroark.prift.server.entity.workstationData.RiftWorkstationData;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class RiftCreatureOperateWorkstation extends EntityAIBase {
    private final RiftCreature creature;
    private final IWorkstationUser workstationUser;
    private BlockPos workstationPos;

    private RiftWorkstation workstation;
    private RiftWorkstationData invokedWorkstation;

    private boolean destroyedFlag;
    private CreatureMove moveForOperation;
    private RiftCreatureMove invokedMoveForOperation;

    private boolean canWorkWithWorkstation;

    private int maxMoveAnimTime; //entire time spent for animation
    private int moveAnimInitDelayTime; //time until end of delay point
    private int moveAnimChargeUpTime; //time until end of charge up point
    private int moveAnimChargeToUseTime; //time until end of charge up to use point
    private int moveAnimUseTime; //time until end of use anim
    private int animTime = -100; //-100 instead of 0 because there will be a cooldown of 5 seconds after every time a move is used

    //for sound looping
    private RiftSoundLooper chargeUpSoundLooper;
    private RiftSoundLooper useSoundLooper;

    public RiftCreatureOperateWorkstation(RiftCreature creature) {
        this.creature = creature;
        this.workstationUser = (IWorkstationUser)creature;
    }

    @Override
    public boolean shouldExecute() {
        if (!(this.creature instanceof IWorkstationUser)) return false;
        this.workstationPos = this.workstationUser.getWorkstationPos();

        TileEntity workstationTE = this.creature.world.getTileEntity(this.workstationUser.getWorkstationPos());

        //get the workstationTE to use
        this.workstation = RiftWorkstation.getWorkstation(this.creature, this.workstationPos);

        if (this.workstation != null) {
            this.moveForOperation = RiftWorkstation.getMoveForWorkstationUse(this.workstation, this.creature);
            this.invokedWorkstation = this.workstation.invokedWorkstationData();
            return workstationTE != null && this.moveForOperation != null && this.invokedWorkstation != null;
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.busyAtWorkWithNoTargets() && !this.destroyedFlag;
    }

    @Override
    public void startExecuting() {
        this.animTime = -60;

        if (this.moveForOperation.chargeType == CreatureMove.ChargeType.GRADIENT_THEN_USE) {
            this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getStartMoveDelayPoint();
            this.moveAnimChargeUpTime = this.moveAnimInitDelayTime + (int) (this.moveForOperation.maxUse * 0.2);
            this.moveAnimChargeToUseTime = this.moveAnimChargeUpTime + (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getChargeUpToUseTime();
            this.moveAnimUseTime = this.moveAnimChargeToUseTime + (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getUseDurationTime();
            this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getRecoverFromUseTime();
        }
        else if (this.moveForOperation.chargeType == CreatureMove.ChargeType.GRADIENT_WHILE_USE) {
            this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getStartMoveDelayPoint();
            this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getChargeUpPoint();
            this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getChargeUpToUsePoint();
            this.moveAnimUseTime = this.moveAnimChargeToUseTime + (int)(this.moveForOperation.maxUse * 0.5);
            this.maxMoveAnimTime = this.moveAnimUseTime + (int)this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseTime();
        }
        else {
            this.moveAnimInitDelayTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getStartMoveDelayPoint();
            this.moveAnimChargeUpTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getChargeUpPoint();
            this.moveAnimChargeToUseTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getChargeUpToUsePoint();
            this.moveAnimUseTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getUseDurationPoint();
            this.maxMoveAnimTime = (int)this.creature.animatorsForMoveType().get(this.moveForOperation.moveAnimType).getRecoverFromUsePoint();
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

        this.invokedMoveForOperation = this.moveForOperation.invokeMove();
    }

    @Override
    public void resetTask() {
        this.animTime = -60;
        this.moveAnimInitDelayTime = 0;
        this.moveAnimChargeUpTime = 0;
        this.moveAnimChargeToUseTime = 0;
        this.moveAnimUseTime = 0;
        this.maxMoveAnimTime = 0;
        this.creature.setUsingUnchargedAnim(false);
        this.setChargedMoveBeingUsed(false);
        this.creature.setPlayingChargedMoveAnim(-1);
        this.canWorkWithWorkstation = false;

        if (this.destroyedFlag) this.workstationUser.clearWorkstation(true);
        this.destroyedFlag = false;
    }

    @Override
    public void updateTask() {
        if (this.workstationUser.workstationUseFromPos() != null) {
            this.creature.getLookHelper().setLookPosition(this.workstationUser.getWorkstationPos().getX(), this.workstationUser.getWorkstationPos().getY(), this.workstationUser.getWorkstationPos().getZ(), 30, 30);
            if (RiftUtil.entityAtLocation(this.creature, this.workstationUser.workstationUseFromPos(), 3)) {
                if (this.canWorkWithWorkstation) {
                    if (this.animTime == 0) {
                        this.invokedWorkstation.onStartWorkstationUse(this.creature, this.workstationPos);
                        this.creature.setCurrentCreatureMove(this.moveForOperation);
                        if (this.moveForOperation.chargeType.requiresCharge()) this.creature.setPlayingChargedMoveAnim(0);
                        else this.creature.setUsingUnchargedAnim(true);
                    }
                    if (this.animTime == this.moveAnimInitDelayTime) {
                        if (this.moveForOperation.chargeType.requiresCharge()) {
                            this.creature.setPlayingChargedMoveAnim(1);
                            this.setChargedMoveBeingUsed(true);
                        }

                        if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound() != null
                                && !this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).chargeUpSoundCanLoop())
                            this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound(),
                                    1f,
                                    1f);
                    }
                    if (this.animTime >= this.moveAnimInitDelayTime && this.animTime <= this.moveAnimChargeUpTime) {
                        if (this.chargeUpSoundLooper != null
                                && this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpSound() != null
                                && this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).chargeUpSoundCanLoop())
                            this.chargeUpSoundLooper.playSound();
                    }
                    if (this.animTime == this.moveAnimChargeUpTime) {
                        if (this.moveForOperation.chargeType.requiresCharge()) {
                            this.creature.setPlayingChargedMoveAnim(2);
                            this.setChargedMoveBeingUsed(false);
                        }
                    }
                    if (this.animTime == this.moveAnimChargeToUseTime) {
                        if (this.moveForOperation.chargeType.requiresCharge()) this.creature.setPlayingChargedMoveAnim(3);
                        if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound() != null)
                            this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getChargeUpToUseSound(),
                                    1f,
                                    1f);
                    }
                    if (this.animTime >= this.moveAnimChargeToUseTime && this.animTime <= this.moveAnimUseTime) {
                        if (this.useSoundLooper != null) this.useSoundLooper.playSound();
                    }
                    if (this.animTime == this.moveAnimUseTime) {
                        this.invokedWorkstation.onHitWorkstation(this.creature, this.workstationPos);
                        if (this.moveForOperation.chargeType.requiresCharge()) this.creature.setPlayingChargedMoveAnim(4);
                        if (this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound() != null)
                            this.creature.playSound(this.creature.animatorsForMoveType().get(this.creature.currentCreatureMove().moveAnimType).getRecoverFromUseSound(),
                                    1f,
                                    1f);
                    }
                    if (this.animTime >= this.maxMoveAnimTime) {
                        this.invokedWorkstation.onEndWorkstationUse(this.creature, this.workstationPos);
                        this.creature.setPlayingChargedMoveAnim(1);
                        this.creature.setUsingUnchargedAnim(false);
                        this.creature.setCurrentCreatureMove(null);
                        this.creature.setXP(this.creature.getXP() + 5);
                        this.animTime = -60;
                        this.canWorkWithWorkstation = false;
                    }
                    if (this.canWorkWithWorkstation) this.animTime++;
                }
                else this.canWorkWithWorkstation = this.workstationUser.isWorkstation(this.creature.world, this.workstationPos)
                        && this.creature.getEnergy() > 0
                        && this.invokedWorkstation.canUseWorkstation(this.creature, this.workstationPos);
            }
            else {
                //move to front of workstation
                this.creature.getMoveHelper().setMoveTo(this.workstationPos.getX(), this.workstationPos.getY(), this.workstationPos.getZ(), 1);
            }
        }
        if (!this.workstationUser.isWorkstation(this.creature.world, this.workstationPos)) this.destroyedFlag = true;
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
}
