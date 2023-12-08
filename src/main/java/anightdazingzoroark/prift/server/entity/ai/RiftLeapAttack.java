package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureinterface.IChargingMob;
import anightdazingzoroark.prift.server.entity.creatureinterface.ILeapingMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class RiftLeapAttack extends EntityAIBase {
    protected RiftCreature attacker;
    private EntityLivingBase target;
    private boolean endFlag;
    private boolean leapAttackFlag;
    protected float leapHeight;
    private int cooldown;

    public RiftLeapAttack(RiftCreature attacker, float leapHeight, int cooldown) {
        this.attacker = attacker;
        this.leapHeight = leapHeight;
        this.cooldown = cooldown;
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (this.attacker.leapCooldown > 0) return false;
        else if (!this.attacker.onGround) return false;
        else if (this.attacker.isBeingRidden()) return false;
        else if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else if (!this.attacker.canEntityBeSeen(entitylivingbase)) return false;
        else {
            double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
            return d0 > this.getAttackReachSqr(entitylivingbase) && d0 <= this.getLeapAttackReachSqr(entitylivingbase);
        }
    }

    @Override
    public void startExecuting() {
        this.target = this.attacker.getAttackTarget();
        if (this.target != null) {
            double dx = this.target.posX - this.attacker.posX;
            double dy = this.target.posY - this.attacker.posY;
            double dz = this.target.posZ - this.attacker.posZ;
            double distance = MathHelper.sqrt(dx * dx + dz * dz);
            double dMotion = MathHelper.sqrt(dx * dx + dz * dz + dy * dy);

            double horizontalStrength = distance / dMotion;
            this.attacker.motionX = (dx / distance) * horizontalStrength;
            this.attacker.motionZ = (dz / distance) * horizontalStrength;
            this.attacker.motionY = dy / dMotion + Math.sqrt(this.leapHeight);
        }
        this.leapAttackFlag = false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.attacker.onGround;
    }

    public void resetTask() {
        this.attacker.setLeaping(false);
        this.attacker.leapCooldown = cooldown;
    }

    public void updateTask() {
        this.attacker.setLeaping(!this.attacker.onGround);
        if (!this.leapAttackFlag) {
            AxisAlignedBB leapHithbox = this.attacker.getEntityBoundingBox().grow(0.5D);
            List<EntityLivingBase> leapedEntities = this.attacker.world.getEntitiesWithinAABB(EntityLivingBase.class, leapHithbox, null);
            if (leapedEntities.contains(this.target)) {
                this.attacker.attackEntityAsMob(this.target);
                this.leapAttackFlag = true;
            }
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        return (double)(this.attacker.attackWidth * this.attacker.attackWidth + attackTarget.width);
    }

    protected double getLeapAttackReachSqr(EntityLivingBase attackTarget) {
        if (this.attacker instanceof ILeapingMob) return (double)(this.attacker.leapWidth * this.attacker.leapWidth + attackTarget.width);
        return 0;
    }
}
