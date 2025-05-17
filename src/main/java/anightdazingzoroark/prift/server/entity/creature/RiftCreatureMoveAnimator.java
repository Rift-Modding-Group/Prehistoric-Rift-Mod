package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftShowParticlesOnClient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

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
    private boolean chargeUpSoundCanLoop;
    private SoundEvent chargeUpToUseSound;
    private SoundEvent useDurationSound;
    private SoundEvent recoverFromUseSound;
    //for data involving particles
    private ParticleData chargeUpToUseParticles;
    //other stuff
    private int numberOfAnims = 1; //this is for how many animations of same keyframes can be played

    public RiftCreatureMoveAnimator(RiftCreature creature) {
        this.creature = creature;
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
        return this.setChargeUpSound(value, false);
    }

    public RiftCreatureMoveAnimator setChargeUpSound(SoundEvent value, boolean canLoop) {
        this.chargeUpSound = value;
        this.chargeUpSoundCanLoop = canLoop;
        return this;
    }

    public SoundEvent getChargeUpSound() {
        return this.chargeUpSound;
    }

    public boolean chargeUpSoundCanLoop() {
        return this.chargeUpSoundCanLoop;
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

    public RiftCreatureMoveAnimator setChargeUpToUseParticles(String particleName, int particleCount, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        this.chargeUpToUseParticles = new ParticleData(particleName, particleCount, posX, posY, posZ, motionX, motionY, motionZ);
        return this;
    }

    public ParticleData getChargeUpToUseParticles() {
        return this.chargeUpToUseParticles;
    }

    //other things
    public RiftCreatureMoveAnimator setNumberOfAnims(int value) {
        this.numberOfAnims = value;
        return this;
    }

    public int getNumberOfAnims() {
        return this.numberOfAnims;
    }

    public class ParticleData {
        public final String particleName;
        public final int particleCount;
        public final double posX, posY, posZ;
        public final double motionX, motionY, motionZ;

        public ParticleData(String particleName, int particleCount, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
            this.particleName = particleName;
            this.particleCount = particleCount;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
        }

        public void createParticle() {
            for (int x = 0; x < this.particleCount; x++) {
                RiftMessages.WRAPPER.sendToAll(new RiftShowParticlesOnClient(this.particleName, this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ));
            }
        }
    }
}
