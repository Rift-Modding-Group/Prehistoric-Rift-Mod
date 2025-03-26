package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.util.SoundEvent;

//this class serves to define the speed of all the parts of an animation of a creature using a move
public class RiftCreatureMoveAnimator {
    private final RiftCreature creature;
    //for nonfinalized values, are basically length
    private double startMoveDelayTime;
    private double chargeUpTime;
    private double chargeUpToUseTime;
    private double useDurationTime;
    private double recoverFromUseTime;
    //for finalized values
    private double startMoveDelayFinal; //stage one, is basically for getting into position for certain moves, this is in ticks
    private double chargeUpFinal; //stage two, for charging up the attack, this is in ticks
    private double chargeUpToUseFinal; //stage three, for transition between charging up then use, this is in ticks
    private double useDurationFinal; //stage four, for utilizing the move, this is in ticks
    private double recoverFromUseFinal; //final stage, for stopping use of move, this is in ticks
    //for sounds to make at certain points
    private SoundEvent startMoveDelaySound;
    private SoundEvent chargeUpSound;
    private SoundEvent chargeUpToUseSound;
    private SoundEvent useDurationSound;
    private SoundEvent recoverFromUseSound;

    public RiftCreatureMoveAnimator(RiftCreature creature) {
        this.creature = creature;
    }

    public CreatureMove.MoveType getMoveType() {
        if (this.creature.currentCreatureMove() == null) return null;
        return this.creature.currentCreatureMove().moveType;
    }

    public RiftCreatureMoveAnimator defineStartMoveDelayLength(double value) {
        this.startMoveDelayTime = value;
        return this;
    }

    public RiftCreatureMoveAnimator defineChargeUpLength(double value) {
        this.chargeUpTime = value;
        return this;
    }

    public RiftCreatureMoveAnimator defineChargeUpToUseLength(double value) {
        this.chargeUpToUseTime = value;
        return this;
    }

    public RiftCreatureMoveAnimator defineUseDurationLength(double value) {
        this.useDurationTime = value;
        return this;
    }

    public RiftCreatureMoveAnimator defineRecoverFromUseLength(double value) {
        this.recoverFromUseTime = value;
        return this;
    }

    public RiftCreatureMoveAnimator finalizePoints() {
        this.startMoveDelayFinal = this.startMoveDelayTime;
        this.chargeUpFinal = this.startMoveDelayTime + this.chargeUpTime;
        this.chargeUpToUseFinal = this.startMoveDelayTime + this.chargeUpTime + this.chargeUpToUseTime;
        this.useDurationFinal = this.startMoveDelayTime + this.chargeUpTime + this.chargeUpToUseTime + this.useDurationTime;
        this.recoverFromUseFinal = this.startMoveDelayTime + this.chargeUpTime + this.chargeUpToUseTime + this.useDurationTime + this.recoverFromUseTime;
        return this;
    }

    //all values returned here are finalized
    public double getStartMoveDelayPoint() {
        return this.startMoveDelayFinal;
    }

    public double getChargeUpPoint() {
        return this.chargeUpFinal;
    }

    public double getChargeUpToUsePoint() {
        return this.chargeUpToUseFinal;
    }

    public double getUseDurationPoint() {
        return this.useDurationFinal;
    }

    public double getRecoverFromUsePoint() {
        return this.recoverFromUseFinal;
    }

    //all values returned here are anim lengths
    public double getStartMoveDelayTime() {
        return this.startMoveDelayTime;
    }

    public double getChargeUpTime() {
        return this.chargeUpTime;
    }

    public double getChargeUpToUseTime() {
        return this.chargeUpToUseTime;
    }

    public double getUseDurationTime() {
        return this.useDurationTime;
    }

    public double getRecoverFromUseTime() {
        return this.recoverFromUseTime;
    }

    public RiftCreatureMoveAnimator setStartMoveDelaySound(SoundEvent value) {
        this.startMoveDelaySound = value;
        return this;
    }

    public SoundEvent getStartMoveDelaySound() {
        return this.startMoveDelaySound;
    }

    public RiftCreatureMoveAnimator setChargeUpSound(SoundEvent value) {
        this.chargeUpSound = value;
        return this;
    }

    public SoundEvent getChargeUpSound() {
        return this.chargeUpSound;
    }

    public RiftCreatureMoveAnimator setChargeUpToUseSound(SoundEvent value) {
        this.chargeUpToUseSound = value;
        return this;
    }

    public SoundEvent getChargeUpToUseSound() {
        return this.chargeUpToUseSound;
    }

    public RiftCreatureMoveAnimator setUseDurationSound(SoundEvent value) {
        this.useDurationSound = value;
        return this;
    }

    public SoundEvent getUseDurationSound() {
        return this.useDurationSound;
    }

    public RiftCreatureMoveAnimator setRecoverFromUseSound(SoundEvent value) {
        this.recoverFromUseSound = value;
        return this;
    }

    public SoundEvent getRecoverFromUseSound() {
        return this.recoverFromUseSound;
    }
}
