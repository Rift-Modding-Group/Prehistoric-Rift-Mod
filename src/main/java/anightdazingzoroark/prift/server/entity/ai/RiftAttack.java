package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IChargingMob;
import anightdazingzoroark.prift.server.entity.interfaces.ILeapingMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
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
    private Path path;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int failedPathFindingPenalty = 0;
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

            if (this.path != null) return this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden();
            else {
                if (this.attacker instanceof IRangedAttackMob) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden() && this.getRangedAttackReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && !this.attacker.isRangedAttacking() && !this.attacker.isActing();
                }
                else if (this.attacker instanceof IChargingMob) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden() && this.getChargeReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && !this.attacker.isUtilizingCharging() && !this.attacker.isActing();
                }
                else if (this.attacker instanceof ILeapingMob) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden() && this.getLeapReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && !this.attacker.isLeaping() && !this.attacker.isActing();
                }
                return this.getAttackReachSqr(entitylivingbase) >= d0 && this.attacker.getEnergy() > 0 && !this.attacker.isBeingRidden();
            }
        }
    }

    public boolean shouldContinueExecuting() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (!this.attacker.isAttacking()) return false;
        else if (this.attacker.getEnergy() == 0) return false;
        else if (this.attacker.isBeingRidden()) return false;
        else if (this.attacker.isUtilizingCharging()) return false;
        else if (this.attacker.isLeaping()) return false;
        else if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else if (!this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase))) return false;
        else {
            return !(entitylivingbase instanceof EntityPlayer) || !(((EntityPlayer)entitylivingbase).isSpectator() && ((EntityPlayer)entitylivingbase).isCreative());
        }
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
            this.animTime++;
            if (this.animTime == this.attackAnimTime) {
                if (distToEnemySqr <= d0) this.attacker.attackEntityAsMob(enemy);
            }
            if (this.animTime > this.attackAnimLength + 1) {
                this.animTime = 0;
                this.attacker.setAttacking(false);
                this.attackCooldown = 20;
                if (this.attacker.isTamed()) this.attacker.energyActionMod++;
            }
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        return (double)(this.attacker.attackWidth * this.attacker.attackWidth + attackTarget.width);
    }

    protected double getRangedAttackReachSqr(EntityLivingBase attackTarget) {
        if (this.attacker instanceof IRangedAttackMob) return (double)(this.attacker.rangedWidth * this.attacker.rangedWidth + attackTarget.width);
        return 0;
    }

    protected double getChargeReachSqr(EntityLivingBase attackTarget) {
        if (this.attacker instanceof IChargingMob) return (double)(this.attacker.chargeWidth * this.attacker.chargeWidth + attackTarget.width);
        return 0;
    }

    protected double getLeapReachSqr(EntityLivingBase attackTarget) {
        if (this.attacker instanceof IChargingMob) return (double)(this.attacker.leapWidth * this.attacker.leapWidth + attackTarget.width);
        return 0;
    }

    //subclasses for different creatures

    public static class ApatosaurusAttack extends RiftAttack {
        protected int whipAnimLength;
        protected int whipAnimTime;
        private int attackMode; //0 is stomp, 1 is tail whip

        //apato has two attacks, stomp and tail whip
        //the default attack stuff is for the stomp
        public ApatosaurusAttack(RiftCreature creature, double speedIn, float attackAnimLength, float attackAnimTime) {
            super(creature, speedIn, attackAnimLength, attackAnimTime);
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
            ((Apatosaurus) (this.attacker)).setTailWhipping(false);
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
                } else if (this.attackMode == 1) {
                    Apatosaurus apatosaurus = (Apatosaurus) this.attacker;
                    if (distToEnemySqr <= d0) apatosaurus.setTailWhipping(true);
                    if (this.animTime == this.whipAnimTime) {
                        if (distToEnemySqr <= d0) apatosaurus.useWhipAttack();
                    }
                    if (this.animTime > this.whipAnimLength) {
                        this.animTime = 0;
                        apatosaurus.setTailWhipping(false);
                        this.attackCooldown = 20;
                        this.attackMode = RiftUtil.randomInRange(0, 1);
                    }
                }
            }
        }
    }
}
