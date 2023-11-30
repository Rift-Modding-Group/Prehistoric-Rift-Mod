package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Stegosaurus;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftStegosaurusControlledStrongAttack extends EntityAIBase {
    protected Stegosaurus mob;
    protected int attackAnimLength;
    protected int attackAnimTime;
    protected int animTime;

    public RiftStegosaurusControlledStrongAttack(Stegosaurus mob, float attackAnimLength, float attackAnimTime) {
        this.mob = mob;
        //attackAnimLength and attackAnimTime are in seconds, will convert to ticks automatically here
        this.attackAnimLength = (int)(attackAnimLength * 20);
        this.attackAnimTime = (int)(attackAnimTime * 20);
    }

    @Override
    public boolean shouldExecute() {
        return this.mob.isTamed() && this.mob.isBeingRidden() && this.mob.isStrongAttacking();
    }

    public boolean shouldContinueExecuting() {
        return this.animTime <= this.attackAnimLength;
    }

    public void startExecuting() {
        this.animTime = 0;
        this.mob.removeSpeed();
    }

    public void resetTask() {
        this.animTime = 0;
        this.mob.setIsStrongAttacking(false);
        this.mob.resetSpeed();
    }

    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.attackAnimTime) {
            this.mob.strongControlAttack();
            if (this.mob.isTamed()) this.mob.setEnergy(this.mob.getEnergy() - (int)(0.06d * (double)this.mob.strongAttackCharge + 6d));
        }
    }
}
