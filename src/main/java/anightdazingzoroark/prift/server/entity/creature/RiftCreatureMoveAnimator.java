package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftShowParticlesOnClient;
import net.minecraft.util.SoundEvent;

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

    public RiftCreatureMoveAnimator setChargeUpToUseParticles(String particleName, int particleCount, double posX, double posY, double posZ) {
        this.chargeUpToUseParticles = new ParticleData(particleName, particleCount, posX, posY, posZ);
        return this;
    }

    public RiftCreatureMoveAnimator setChargeUpToUseParticles(String particleName, int particleCount, double posX, double posY, double posZ, double motionX, double motionY, double motionZ) {
        this.chargeUpToUseParticles = new ParticleData(particleName, particleCount, posX, posY, posZ, motionX, motionY, motionZ);
        return this;
    }

    public RiftCreatureMoveAnimator setChargeUpToUseParticleXBounds(int xBoundLow, int xBoundHigh) {
        if (this.chargeUpToUseParticles != null) this.chargeUpToUseParticles.setXBounds(xBoundLow, xBoundHigh);
        return this;
    }

    public RiftCreatureMoveAnimator setChargeUpToUseParticleYBounds(int yBoundLow, int yBoundHigh) {
        if (this.chargeUpToUseParticles != null) this.chargeUpToUseParticles.setYBounds(yBoundLow, yBoundHigh);
        return this;
    }

    public RiftCreatureMoveAnimator setChargeUpToUseParticleZBounds(int zBoundLow, int zBoundHigh) {
        if (this.chargeUpToUseParticles != null) this.chargeUpToUseParticles.setZBounds(zBoundLow, zBoundHigh);
        return this;
    }

    public RiftCreatureMoveAnimator setChargeUpToUseParticleColor(int color) {
        if (this.chargeUpToUseParticles != null) this.chargeUpToUseParticles.setParticleColor(color);
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
        private final String particleName;
        private final int particleCount;
        private final double posX, posY, posZ;
        private double motionX, motionY, motionZ;
        private boolean motionExceptYRandom;
        private int xBoundLow, xBoundHigh;
        private int yBoundLow, yBoundHigh;
        private int zBoundLow, zBoundHigh;
        private int particleColor = -1;

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

        public ParticleData(String particleName, int particleCount, double posX, double posY, double posZ) {
            this.particleName = particleName;
            this.particleCount = particleCount;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.motionExceptYRandom = true;
        }

        public void setXBounds(int xBoundLow, int xBoundHigh) {
            this.xBoundLow = xBoundLow;
            this.xBoundHigh = xBoundHigh;
        }

        public void setYBounds(int yBoundLow, int yBoundHigh) {
            this.yBoundLow = yBoundLow;
            this.yBoundHigh = yBoundHigh;
        }

        public void setZBounds(int zBoundLow, int zBoundHigh) {
            this.zBoundLow = zBoundLow;
            this.zBoundHigh = zBoundHigh;
        }

        public void setParticleColor(int particleColor) {
            this.particleColor = particleColor;
        }

        public void createParticle() {
            Random random = new Random();
            for (int x = 0; x < this.particleCount; x++) {
                int xBounds = ((this.xBoundLow <= 0 && this.xBoundHigh <= 0) ? 0 : (
                        (this.xBoundLow > 0 && this.xBoundHigh <= 0) ? this.xBoundLow : (
                                (this.xBoundLow <= 0 && this.xBoundHigh > 0) ? RiftUtil.randomInRange(0, this.xBoundHigh) : RiftUtil.randomInRange(this.xBoundLow, this.xBoundHigh)
                        )
                ));
                int yBounds = ((this.yBoundLow <= 0 && this.yBoundHigh <= 0) ? 0 : (
                        (this.yBoundLow > 0 && this.yBoundHigh <= 0) ? this.yBoundLow : (
                                (this.yBoundLow <= 0 && this.yBoundHigh > 0) ? RiftUtil.randomInRange(0, this.yBoundHigh) : RiftUtil.randomInRange(this.yBoundLow, this.yBoundHigh)
                        )
                ));
                int zBounds = ((this.zBoundLow <= 0 && this.zBoundHigh <= 0) ? 0 : (
                        (this.zBoundLow > 0 && this.zBoundHigh <= 0) ? this.zBoundLow : (
                                (this.zBoundLow <= 0 && this.zBoundHigh > 0) ? RiftUtil.randomInRange(0, this.zBoundHigh) : RiftUtil.randomInRange(this.zBoundLow, this.zBoundHigh)
                        )
                ));
                double finalXMotion = this.motionExceptYRandom ? RiftUtil.randomInRange(0.2D, 0.8D) * (random.nextBoolean() ? 1 : -1) : this.motionX;
                double finalZMotion = this.motionExceptYRandom ? RiftUtil.randomInRange(0.2D, 0.8D) * (random.nextBoolean() ? 1 : -1) : this.motionZ;
                RiftMessages.WRAPPER.sendToAll(new RiftShowParticlesOnClient(this.particleName,
                        this.particleColor,
                        this.posX + xBounds * (random.nextBoolean() ? 1 : -1),
                        this.posY + yBounds * (random.nextBoolean() ? 1 : -1),
                        this.posZ + zBounds * (random.nextBoolean() ? 1 : -1),
                        finalXMotion, this.motionY, finalZMotion));
            }
        }
    }
}
