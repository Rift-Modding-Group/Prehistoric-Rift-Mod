package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureinterface.IChargingMob;
import anightdazingzoroark.prift.server.entity.creatureinterface.ILeapingMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

public class RiftLeapAttack extends EntityAIBase {
    protected RiftCreature attacker;
    private boolean endFlag;
    protected float leapHeight;

    public RiftLeapAttack(RiftCreature attacker, float leapHeight) {
        this.attacker = attacker;
        this.leapHeight = leapHeight;
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

        if (!this.attacker.onGround) return false;
        else if (this.attacker.isBeingRidden()) return false;
        else if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else {
            double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
            return d0 > this.getAttackReachSqr(entitylivingbase) && d0 <= this.getLeapAttackReachSqr(entitylivingbase);
        }
    }

    @Override
    public void startExecuting() {
        EntityLivingBase target = this.attacker.getAttackTarget();
        double dx = target.posX - this.attacker.posX;
        double dy = target.posY + (double)(target.height / 2.0F) - (this.attacker.posY + (double)(this.attacker.height / 2.0F));
        double dz = target.posZ - this.attacker.posZ;
        double distance = MathHelper.sqrt(dx * dx + dz * dz);
        double dMotion = MathHelper.sqrt(dx * dx + dz * dz + dy * dy);

        double horizontalStrength = distance / dMotion;
        this.attacker.motionX = (dx / distance) * horizontalStrength;
        this.attacker.motionZ = (dz / distance) * horizontalStrength;

        this.attacker.motionY = dy / dMotion + Math.sqrt(this.leapHeight);

        this.attacker.motionX *= 0.5D;
        this.attacker.motionY *= 0.5D;
        this.attacker.motionZ *= 0.5D;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.attacker.onGround;
    }

    public void resetTask() {
        this.attacker.setLeaping(false);
    }

    public void updateTask() {
        this.attacker.setLeaping(!this.attacker.onGround);
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        return (double)(this.attacker.attackWidth * this.attacker.attackWidth + attackTarget.width);
    }

    protected double getLeapAttackReachSqr(EntityLivingBase attackTarget) {
        if (this.attacker instanceof ILeapingMob) return (double)(this.attacker.leapWidth * this.attacker.leapWidth + attackTarget.width);
        return 0;
    }
}
