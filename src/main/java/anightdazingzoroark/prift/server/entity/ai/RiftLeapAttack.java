package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureinterface.IChargingMob;
import anightdazingzoroark.prift.server.entity.creatureinterface.ILeapingMob;
import anightdazingzoroark.prift.server.entity.creatureinterface.IPackHunter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

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
        this.setMutexBits(3);
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
        else if (entitylivingbase.posY - this.attacker.posY > this.leapHeight) return false;
        else {
            double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
            if (this.attacker instanceof IPackHunter) {
                return d0 > this.getAttackReachSqr(entitylivingbase) && d0 <= this.getLeapAttackReachSqr(entitylivingbase) && !((IPackHunter)this.attacker).isPackBuffing() && this.attacker.canMove() && this.canLeapToArea(entitylivingbase);
            }
            return d0 > this.getAttackReachSqr(entitylivingbase) && d0 <= this.getLeapAttackReachSqr(entitylivingbase) && this.attacker.canMove() && this.canLeapToArea(entitylivingbase);
        }
    }

    @Override
    public void startExecuting() {
        this.target = this.attacker.getAttackTarget();
        if (this.target != null) {
            double dx = this.target.posX - this.attacker.posX;
            double dz = this.target.posZ - this.attacker.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            double velY = Math.sqrt(2 * RiftUtil.gravity * this.leapHeight);
            double totalTime = velY / RiftUtil.gravity;
            double velXZ = dist * 2 / totalTime;

            double angleToTarget = Math.atan2(dz, dx);

            this.attacker.motionX = velXZ * Math.cos(angleToTarget);
            this.attacker.motionZ = velXZ * Math.sin(angleToTarget);
            this.attacker.motionY = velY;
            this.attacker.setActing(true);
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
            AxisAlignedBB leapHithbox = this.attacker.getEntityBoundingBox().grow(0.75D);
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

    protected boolean canLeapToArea(EntityLivingBase entitylivingbase) {
        //gravity constant in minecraft is 0.08D
        double g = 0.08D;
        double dx = entitylivingbase.posX - this.attacker.posX;
        double dz = entitylivingbase.posZ - this.attacker.posZ;
        double velY = Math.sqrt(2 * g * this.leapHeight);
        double totalTime = velY / g;

        for (int i = 0; i <= this.attacker.leapWidth; i++) {
            double fraction = (double) i / this.attacker.leapWidth;
            double time = fraction * totalTime;
            double xToCheck = this.attacker.posX + dx * fraction;
            double zToCheck = this.attacker.posZ + dz * fraction;
            double yToCheck = this.attacker.posY + velY * time - 0.5 * g * time * time;
            BlockPos posToCheck = new BlockPos(xToCheck, yToCheck, zToCheck);
            if (!areBlocksClear(this.attacker.world, posToCheck)) return false;
        }

        return true;
    }

    private boolean areBlocksClear(World world, BlockPos pos) {
        // Check the block at the given coordinates and its adjacent blocks.
        // Here, 'isAirBlock' is used as an example to check if a block is not solid.
        return world.isAirBlock(pos)
                && world.isAirBlock(pos.up())
                && world.isAirBlock(pos.north())
                && world.isAirBlock(pos.south())
                && world.isAirBlock(pos.east())
                && world.isAirBlock(pos.west());
    }
}
