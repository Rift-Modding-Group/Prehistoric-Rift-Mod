package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Baryonyx;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftBaryonyxControlledClawAttack extends EntityAIBase {
    protected final Baryonyx attacker;
    protected final int attackAnimLength;
    protected final int attackAnimTime;
    protected int animTime;

    public RiftBaryonyxControlledClawAttack(Baryonyx creature) {
        this.attacker = creature;
        //attackAnimLength and attackAnimTime are in seconds, will convert to ticks automatically here
        this.attackAnimLength = (int)(0.48f * 20);
        this.attackAnimTime = (int)(0.24f * 20);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.attacker.isTamed() && this.attacker.isBeingRidden() && (this.attacker.isUsingLeftClaw() || this.attacker.isUsingRightClaw());
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
        this.attacker.setUsingLeftClaw(false);
        this.attacker.setUsingRightClaw(false);
    }

    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.attackAnimTime) this.attacker.controlClawAttack();
    }
}
