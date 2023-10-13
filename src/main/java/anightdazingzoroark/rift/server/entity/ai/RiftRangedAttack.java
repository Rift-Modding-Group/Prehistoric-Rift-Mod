package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftRangedAttack extends EntityAIBase {
    protected RiftCreature attacker;
    protected boolean canMoveWhenShooting;
    protected int shootAnimLength;
    protected int shootAnimTime;
    protected int animTime;
    double speedTowardsTarget;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
    private int attackCooldown;

    public RiftRangedAttack(RiftCreature creature, double speedIn, float shootAnimLength, float shootAnimTime) {
        this(creature, true, speedIn, shootAnimLength, shootAnimTime);
    }

    public RiftRangedAttack(RiftCreature creature, boolean canMoveWhenShooting, double speedIn, float shootAnimLength, float shootAnimTime) {
        this.attacker = creature;
        this.canMoveWhenShooting = canMoveWhenShooting;
        this.speedTowardsTarget = speedIn;
        this.shootAnimLength = (int)(shootAnimLength * 20);
        this.shootAnimTime = (int)(shootAnimTime * 20);
        this.seeTime = 0;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.attacker.getAttackTarget() == null) return false;
        else if (!this.attacker.getAttackTarget().isEntityAlive()) return false;
        else {
            EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
            double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
            return d0 > this.getAttackReachSqr(entitylivingbase) && d0 <= this.getRangedAttackReachSqr();
        }
    }

    public boolean shouldContinueExecuting() {
        return this.shouldExecute() || !this.attacker.getNavigator().noPath();
    }

    public void startExecuting() {
        this.attackCooldown = 20;
        this.animTime = 0;
    }

    public void resetTask() {
        this.seeTime = 0;
    }

    public void updateTask() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (entitylivingbase != null) {
            double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
            boolean flag = this.attacker.getEntitySenses().canSee(entitylivingbase);
            boolean flag1 = this.seeTime > 0;

            if (flag != flag1) this.seeTime = 0;

            if (flag) ++this.seeTime;
            else --this.seeTime;

            if (d0 <= this.getRangedAttackReachSqr() && this.seeTime >= 20) {
                this.attacker.getNavigator().clearPath();
                ++this.strafingTime;
            }
            else {
                this.attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if ((double)this.attacker.getRNG().nextFloat() < 0.3D) this.strafingClockwise = !this.strafingClockwise;
                if ((double)this.attacker.getRNG().nextFloat() < 0.3D) this.strafingBackwards = !this.strafingBackwards;
                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (d0 > this.getRangedAttackReachSqr() * 0.75D) this.strafingBackwards = false;
                else if (d0 < this.getRangedAttackReachSqr() * 0.25D) this.strafingBackwards = true;

                this.attacker.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.attacker.faceEntity(entitylivingbase, 30.0F, 30.0F);
            }
            else this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);

            if (flag) {
                if (--this.attackCooldown <= 0) {
                    this.attacker.setRangedAttacking(true);
                    if (!this.canMoveWhenShooting) this.attacker.removeSpeed();
                    this.animTime++;
                    if (this.animTime == this.shootAnimTime) ((IRangedAttackMob)(this.attacker)).attackEntityWithRangedAttack(entitylivingbase, 1F);
                    if (this.animTime > this.shootAnimLength) {
                        this.animTime = 0;
                        this.attacker.setRangedAttacking(false);
                        this.attacker.resetSpeed();
                        this.attackCooldown = 20;
                    }
                }
            }
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        return (double)(this.attacker.attackWidth * this.attacker.attackWidth + attackTarget.width);
    }

    protected double getRangedAttackReachSqr() {
        if (this.attacker instanceof IRangedAttackMob) return (double)(this.attacker.rangedWidth * this.attacker.rangedWidth);
        return 0;
    }
}
