package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.ILeapingMob;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftControlledAttack extends EntityAIBase  {
    protected final RiftCreature attacker;
    protected final int attackAnimLength;
    protected final int attackAnimTime;
    protected int animTime;

    public RiftControlledAttack(RiftCreature creature, float attackAnimLength, float attackAnimTime) {
        this.attacker = creature;
        //attackAnimLength and attackAnimTime are in seconds, will convert to ticks automatically here
        this.attackAnimLength = (int)(attackAnimLength * 20);
        this.attackAnimTime = (int)(attackAnimTime * 20);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (this.attacker instanceof ILeapingMob) {
            return this.attacker.isTamed() && this.attacker.isBeingRidden() && this.attacker.isAttacking() && !((ILeapingMob)this.attacker).isLeaping();
        }
        return this.attacker.isTamed() && this.attacker.isBeingRidden() && this.attacker.isAttacking();
    }

    public boolean shouldContinueExecuting() {
        return this.animTime <= this.attackAnimLength;
    }

    public void startExecuting() {
        this.animTime = 0;
        this.attacker.energyActionMod++;
    }

    public void resetTask() {
        this.animTime = 0;
        this.attacker.setAttacking(false);
    }

    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.attackAnimTime) this.attacker.controlAttack();
    }
}
