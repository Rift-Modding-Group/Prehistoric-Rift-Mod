package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftControlledAttack extends EntityAIBase  {
    protected RiftCreature attacker;
    protected int attackAnimLength;
    protected int attackAnimTime;
    protected int animTime;

    public RiftControlledAttack(RiftCreature creature, float attackAnimLength, float attackAnimTime) {
        this.attacker = creature;
        //attackAnimLength and attackAnimTime are in seconds, will convert to ticks automatically here
        this.attackAnimLength = (int)(attackAnimLength * 20);
        this.attackAnimTime = (int)(attackAnimTime * 20);
    }

    @Override
    public boolean shouldExecute() {
        return this.attacker.isTamed() && this.attacker.isBeingRidden() && this.attacker.isAttacking();
    }

    public boolean shouldContinueExecuting() {
        return this.animTime <= this.attackAnimLength;
    }

    public void startExecuting() {
        this.animTime = 0;
        if (this.attacker.isTamed()) this.attacker.energyActionMod++;
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
