package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureinterface.IChargingMob;
import anightdazingzoroark.prift.server.entity.creatureinterface.ILeapingMob;
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
    Path path;
    private int delayCounter;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int failedPathFindingPenalty = 0;
    private int attackCooldown;

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

            if (this.path != null) return true;
            else {
                if (this.attacker instanceof IRangedAttackMob) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.getRangedAttackReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && !this.attacker.isRangedAttacking();
                }
                else if (this.attacker instanceof IChargingMob) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.getChargeReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && !this.attacker.isUtilizingCharging();
                }
                else if (this.attacker instanceof ILeapingMob) {
                    return this.getAttackReachSqr(entitylivingbase) >= d0 && this.getLeapReachSqr(entitylivingbase) < this.getAttackReachSqr(entitylivingbase) && !this.attacker.isLeaping();
                }
                return this.getAttackReachSqr(entitylivingbase) >= d0;
            }
        }
    }

    public boolean shouldContinueExecuting() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (this.attacker.isUtilizingCharging()) return false;
        else if (this.attacker.isLeaping()) return false;
        else if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else if (!this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase))) return false;
        else {
            return !(entitylivingbase instanceof EntityPlayer) || !(((EntityPlayer)entitylivingbase).isSpectator() && ((EntityPlayer)entitylivingbase).isCreative());
        }
    }

    public void startExecuting() {
        this.attacker.getNavigator().setPath(this.path, this.speedTowardsTarget);
        this.delayCounter = 0;
        this.animTime = 0;
        this.attackCooldown = 0;
        if (this.attacker.isTamed()) this.attacker.energyActionMod++;
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
            if (distToEnemySqr <= d0) {
                this.attacker.setAttacking(true);
                this.animTime++;
                if (this.animTime == this.attackAnimTime) this.attacker.attackEntityAsMob(enemy);
                if (this.animTime > this.attackAnimLength) {
                    this.animTime = 0;
                    this.attacker.setAttacking(false);
                    this.attackCooldown = 20;
                }
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
}
