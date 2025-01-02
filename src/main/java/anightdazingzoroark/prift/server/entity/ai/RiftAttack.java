package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.SarcosuchusConfig;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.*;
import anightdazingzoroark.prift.server.entity.interfaces.IChargingMob;
import anightdazingzoroark.prift.server.entity.interfaces.ILeapAttackingMob;
import anightdazingzoroark.prift.server.entity.interfaces.IRangedAttacker;
import anightdazingzoroark.prift.server.entity.interfaces.ITurretModeUser;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftSarcosuchusSpinTargeting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;

public class RiftAttack extends EntityAIBase {
    protected RiftCreature attacker;
    protected int attackAnimLength;
    protected int attackAnimTime;
    protected int animTime;
    double speedTowardsTarget;
    protected Path path;
    protected int delayCounter;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected int attackCooldown;

    public RiftAttack(RiftCreature creature, double speedIn, float attackAnimLength, float attackAnimTime) {
        this.attacker = creature;
        this.speedTowardsTarget = speedIn;
        //attackAnimLength and attackAnimTime are in seconds, will convert to ticks automatically here
        this.attackAnimLength = (int)(attackAnimLength * 20);
        this.attackAnimTime = (int)(attackAnimTime * 20);
        this.setMutexBits(3);
    }

    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else {
            this.path = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
            double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);

            if (this.path != null) return this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden() && (!(this.attacker instanceof ITurretModeUser) || !((ITurretModeUser) this.attacker).isTurretMode());
            else {
                if (this.attacker instanceof IRangedAttacker) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden() && (!(this.attacker instanceof ITurretModeUser) || !((ITurretModeUser) this.attacker).isTurretMode()) && this.getRangedAttackReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && !this.attacker.isRangedAttacking() && !this.attacker.isActing();
                }
                else if (this.attacker instanceof IChargingMob) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden() && (!(this.attacker instanceof ITurretModeUser) || !((ITurretModeUser) this.attacker).isTurretMode()) && this.getChargeReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && ((IChargingMob) this.attacker).isNotUtilizingCharging() && !this.attacker.isActing();
                }
                else if (this.attacker instanceof ILeapAttackingMob) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden() && (!(this.attacker instanceof ITurretModeUser) || !((ITurretModeUser) this.attacker).isTurretMode()) && this.getLeapReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && !((ILeapAttackingMob)this.attacker).isLeaping() && !this.attacker.isActing();
                }
                return this.getAttackReachSqr(entitylivingbase) >= d0 && this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden() && (!(this.attacker instanceof ITurretModeUser) || !((ITurretModeUser) this.attacker).isTurretMode());
            }
        }
    }

    public boolean shouldContinueExecuting() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (this.attacker.getEnergy() == 0) return false;
        else if (this.attacker.isBeingRidden()) return false;
        else if (this.attacker instanceof IChargingMob && !((IChargingMob) this.attacker).isNotUtilizingCharging()) return false;
        else if (this.attacker instanceof ILeapAttackingMob && ((ILeapAttackingMob) this.attacker).isLeaping()) return false;
        else if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else if (!this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase))) return false;
        else return !(entitylivingbase instanceof EntityPlayer) || !(((EntityPlayer)entitylivingbase).isSpectator() && ((EntityPlayer)entitylivingbase).isCreative());
    }

    @Override
    public void startExecuting() {
        this.attacker.getNavigator().setPath(this.path, this.speedTowardsTarget);
        this.delayCounter = 0;
        this.animTime = 0;
        this.attackCooldown = 0;
    }

    @Override
    public void resetTask() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase instanceof EntityPlayer && (((EntityPlayer)entitylivingbase).isSpectator() || ((EntityPlayer)entitylivingbase).isCreative())) {
            this.attacker.setAttackTarget((EntityLivingBase)null);
        }

        this.attacker.getNavigator().clearPath();
        this.animTime = 0;
        this.attacker.setAttacking(false);
        this.attacker.resetSpeed();
    }

    public void updateTask() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
        this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
        this.targetX = entitylivingbase.posX;
        this.targetY = entitylivingbase.getEntityBoundingBox().minY;
        this.targetZ = entitylivingbase.posZ;
        double d0 = this.attacker.getDistanceSq(this.targetX, this.targetY, this.targetZ);
        --this.delayCounter;

        if (this.attacker.getEntitySenses().canSee(entitylivingbase) && this.delayCounter <= 0) {
            this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);

            if (d0 > 1024.0D) this.delayCounter += 10;
            else if (d0 > 256.0D) this.delayCounter += 5;

            if (d0 >= this.getAttackReachSqr(entitylivingbase)) {
                if (!this.attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget)) this.delayCounter += 15;
            }
            else {
                this.path = null;
                this.attacker.getNavigator().clearPath();
            }
        }
        this.checkAndPerformAttack(entitylivingbase, d0);
    }

    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);

        if (--this.attackCooldown <= 0) {
            if (distToEnemySqr <= d0) this.attacker.setAttacking(true);
            if (this.attacker.isAttacking()) {
                this.animTime++;
                if (this.animTime == this.attackAnimTime) {
                    this.attacker.removeSpeed();
                    if (distToEnemySqr <= d0) this.attacker.attackEntityAsMob(enemy);
                }
                if (this.animTime > this.attackAnimLength + 1) {
                    this.animTime = 0;
                    this.attacker.setAttacking(false);
                    this.attackCooldown = 20;
                    this.attacker.resetSpeed();
                    if (this.attacker.isTamed()) this.attacker.energyActionMod++;
                }
            }
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        return (this.attacker.attackWidth() * this.attacker.attackWidth() + attackTarget.width);
    }

    protected double getRangedAttackReachSqr(EntityLivingBase attackTarget) {
        if (this.attacker instanceof IRangedAttacker) return Math.pow(((IRangedAttacker)this.attacker).rangedWidth(), 2) + attackTarget.width + 25;
        return 0;
    }

    protected double getChargeReachSqr(EntityLivingBase attackTarget) {
        if (this.attacker instanceof IChargingMob) {
            return (double) (((IChargingMob)this.attacker).chargeWidth() * ((IChargingMob)this.attacker).chargeWidth() + attackTarget.width);
        }
        return 0;
    }

    protected double getLeapReachSqr(EntityLivingBase attackTarget) {
        if (this.attacker instanceof ILeapAttackingMob) {
            return (double) (((ILeapAttackingMob)this.attacker).leapWidth() * ((ILeapAttackingMob)this.attacker).leapWidth() + attackTarget.width);
        }
        return 0;
    }

    //subclasses for different creatures
    public static class ApatosaurusAttack extends RiftAttack {
        private final Apatosaurus apatosaurus;
        protected int whipAnimLength;
        protected int whipAnimTime;
        private int attackMode; //0 is stomp, 1 is tail whip

        //apato has two attacks, stomp and tail whip
        //the default attack stuff is for the stomp
        public ApatosaurusAttack(Apatosaurus apatosaurus, double speedIn, float attackAnimLength, float attackAnimTime) {
            super(apatosaurus, speedIn, attackAnimLength, attackAnimTime);
            this.apatosaurus = apatosaurus;
            this.whipAnimLength = (int) (0.6f * 20f);
            this.whipAnimTime = (int) (0.4f * 20f);
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            this.attackMode = RiftUtil.randomInRange(0, 1);
        }

        @Override
        public void resetTask() {
            super.resetTask();
            this.apatosaurus.setTailWhipping(false);
        }

        @Override
        protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
            double d0 = this.getAttackReachSqr(enemy);

            if (--this.attackCooldown <= 0) {
                this.animTime++;

                if (this.attackMode == 0) {
                    if (distToEnemySqr <= d0) this.attacker.setAttacking(true);
                    if (this.animTime == this.attackAnimTime) {
                        if (distToEnemySqr <= d0) this.attacker.attackEntityAsMob(enemy);
                    }
                    if (this.animTime > this.attackAnimLength + 1) {
                        this.animTime = 0;
                        this.attacker.setAttacking(false);
                        this.attackCooldown = 20;
                        this.attackMode = RiftUtil.randomInRange(0, 1);
                    }
                }
                else if (this.attackMode == 1) {
                    if (distToEnemySqr <= d0) this.apatosaurus.setTailWhipping(true);
                    if (this.animTime == this.whipAnimTime) {
                        if (distToEnemySqr <= d0) this.apatosaurus.useWhipAttack();
                    }
                    if (this.animTime > this.whipAnimLength) {
                        this.animTime = 0;
                        this.apatosaurus.setTailWhipping(false);
                        this.attackCooldown = 20;
                        this.attackMode = RiftUtil.randomInRange(0, 1);
                    }
                }
            }
        }
    }

    public static class SarcosuchusAttack extends RiftAttack {
        private final Sarcosuchus sarcosuchus;
        private EntityLivingBase spinVictim;
        private int spinTime;
        private boolean spinFlag;

        public SarcosuchusAttack(Sarcosuchus sarcosuchus, double speedIn, float attackAnimLength, float attackAnimTime) {
            super(sarcosuchus, speedIn, attackAnimLength, attackAnimTime);
            this.sarcosuchus = sarcosuchus;
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            this.spinTime = 0;
            this.spinFlag = true;
        }

        public boolean shouldContinueExecuting() {
            if (!this.spinFlag) return false;
            else if (this.sarcosuchus.isSpinning()) return super.shouldContinueExecuting() && this.sarcosuchus.getEnergy() > 6;
            return super.shouldContinueExecuting();
        }

        @Override
        public void resetTask() {
            super.resetTask();
            this.spinTime = 0;
            this.sarcosuchus.setIsSpinning(false);
            if (this.spinVictim != null) NonPotionEffectsHelper.setCaptured(this.spinVictim, false);;
            this.spinVictim = null;
        }

        @Override
        public void updateTask() {
            if (this.spinVictim == null) super.updateTask();
            else this.manageSpin();
        }

        @Override
        protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
            double d0 = this.getAttackReachSqr(enemy);

            if (--this.attackCooldown <= 0) {
                if (distToEnemySqr <= d0) this.sarcosuchus.setAttacking(true);
                if (this.sarcosuchus.isAttacking()) {
                    this.animTime++;
                    if (this.animTime == this.attackAnimTime) {
                        this.sarcosuchus.removeSpeed();
                        if (distToEnemySqr <= d0) this.sarcosuchus.attackEntityAsMob(enemy);
                    }
                    if (this.animTime > this.attackAnimLength + 1) {
                        this.animTime = 0;
                        this.sarcosuchus.setAttacking(false);
                        this.attackCooldown = 20;
                        if (this.sarcosuchus.isTamed()) this.sarcosuchus.energyActionMod++;

                        if (enemy.isEntityAlive() && this.sarcosuchus.getEnergy() > 6 && !NonPotionEffectsHelper.isCaptured(enemy)) {
                            if (RiftUtil.isAppropriateSize(enemy, MobSize.safeValueOf(((SarcosuchusConfig) RiftConfigHandler.getConfig(RiftCreatureType.SARCOSUCHUS)).general.maximumSpinAttackTargetSize))) {
                                this.sarcosuchus.setIsSpinning(true);
                                this.spinVictim = enemy;
                                NonPotionEffectsHelper.setCaptured(enemy, true);
                            }
                        }
                        else this.sarcosuchus.resetSpeed();
                    }
                }
            }
        }

        protected void manageSpin() {
            if (this.spinVictim.isEntityAlive()) {
                RiftMessages.WRAPPER.sendToServer(new RiftSarcosuchusSpinTargeting(this.sarcosuchus, this.spinVictim));
                if (this.sarcosuchus.isTamed() && this.spinTime % 10 == 0) this.sarcosuchus.setEnergy(this.sarcosuchus.getEnergy() - 1);
                if (this.spinTime >= 100) {
                    this.spinFlag = false;
                    NonPotionEffectsHelper.setCaptured(this.spinVictim, false);
                }
                this.spinTime++;
            }
            else this.sarcosuchus.setIsSpinning(false);
        }
    }

    public static class BaryonyxAttack extends RiftAttack {
        private final Baryonyx baryonyx;
        private final int clawAnimLength;
        private final int clawAnimTime;
        private int attackMode; //0 is bite, 1 is left claw attack, 2 is right claw attack

        public BaryonyxAttack(RiftCreature creature, double speedIn) {
            super(creature, speedIn, 0.52f, 0.24f);
            this.baryonyx = (Baryonyx) creature;
            this.clawAnimLength = (int)(20f * 0.48f);
            this.clawAnimTime = (int)(20f * 0.24f);
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            this.attackMode = RiftUtil.randomInRange(0, 2);
        }

        @Override
        public void resetTask() {
            super.resetTask();
            this.baryonyx.setUsingLeftClaw(false);
            this.baryonyx.setUsingRightClaw(false);
        }

        protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
            double d0 = this.getAttackReachSqr(enemy);

            if (--this.attackCooldown <= 0) {
                if (distToEnemySqr <= d0) {
                    if (this.attackMode == 0) this.attacker.setAttacking(true);
                    else if (this.attackMode == 1) this.baryonyx.setUsingLeftClaw(true);
                    else if (this.attackMode == 2) this.baryonyx.setUsingRightClaw(true);
                }
                if (this.attacker.isAttacking()) {
                    this.animTime++;
                    if (this.animTime == this.attackAnimTime) {
                        this.attacker.removeSpeed();
                        if (distToEnemySqr <= d0) this.attacker.attackEntityAsMob(enemy);
                    }
                    if (this.animTime > this.attackAnimLength + 1) {
                        this.animTime = 0;
                        this.attacker.setAttacking(false);
                        this.attackCooldown = 20;
                        this.attacker.resetSpeed();
                        if (this.attacker.isTamed()) this.attacker.energyActionMod++;
                        this.attackMode = RiftUtil.randomInRange(0, 2);
                    }
                }
                else if (this.baryonyx.isUsingLeftClaw() || this.baryonyx.isUsingRightClaw()) {
                    this.animTime++;
                    if (this.animTime == this.clawAnimTime) {
                        this.attacker.removeSpeed();
                        if (distToEnemySqr <= d0) this.baryonyx.attackUsingClaw(enemy);
                    }
                    if (this.animTime > this.clawAnimLength + 1) {
                        this.animTime = 0;
                        this.baryonyx.setUsingLeftClaw(false);
                        this.baryonyx.setUsingRightClaw(false);
                        this.attackCooldown = 20;
                        this.attacker.resetSpeed();
                        if (this.attacker.isTamed()) this.attacker.energyActionMod++;
                        this.attackMode = RiftUtil.randomInRange(0, 2);
                    }
                }
            }
        }
    }

    public static class DilophosaurusAttack extends RiftAttack {
        private final Dilophosaurus dilophosaurus;
        private final int clawAnimLength;
        private final int clawAnimTime;
        private int attackMode; //0 is left claw attack, 1 is right claw attack

        public DilophosaurusAttack(Dilophosaurus creature, double speedIn) {
            super(creature, speedIn, 0, 0);
            this.dilophosaurus = creature;
            this.clawAnimLength = (int)(20f * 0.48f);
            this.clawAnimTime = (int)(20f * 0.24f);
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            this.attackMode = RiftUtil.randomInRange(0, 1);
        }

        @Override
        public void resetTask() {
            super.resetTask();
            this.dilophosaurus.setUsingLeftClaw(false);
            this.dilophosaurus.setUsingRightClaw(false);
        }

        protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
            double d0 = this.getAttackReachSqr(enemy);

            if (--this.attackCooldown <= 0) {
                if (distToEnemySqr <= d0) {
                    if (this.attackMode == 0) this.dilophosaurus.setUsingLeftClaw(true);
                    else if (this.attackMode == 1) this.dilophosaurus.setUsingRightClaw(true);
                }
                if (this.dilophosaurus.isUsingLeftClaw() || this.dilophosaurus.isUsingRightClaw()) {
                    this.animTime++;
                    if (this.animTime == this.clawAnimTime) {
                        this.attacker.removeSpeed();
                        if (distToEnemySqr <= d0) this.dilophosaurus.attackEntityAsMob(enemy);
                    }
                    if (this.animTime > this.clawAnimLength + 1) {
                        this.animTime = 0;
                        this.dilophosaurus.setUsingLeftClaw(false);
                        this.dilophosaurus.setUsingRightClaw(false);
                        this.attackCooldown = 20;
                        this.attacker.resetSpeed();
                        if (this.attacker.isTamed()) this.attacker.energyActionMod++;
                        this.attackMode = RiftUtil.randomInRange(0, 1);
                    }
                }
            }
        }
    }
}
