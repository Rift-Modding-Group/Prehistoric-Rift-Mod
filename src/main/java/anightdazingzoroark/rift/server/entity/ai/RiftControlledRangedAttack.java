package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftControlledRangedAttack extends EntityAIBase {
    protected RiftCreature attacker;
    protected int shootAnimLength;
    protected int shootAnimTime;
    protected int animTime;

    public RiftControlledRangedAttack(RiftCreature creature, float shootAnimLength, float shootAnimTime) {
        this.attacker = creature;
        //attackAnimLength and attackAnimTime are in seconds, will convert to ticks automatically here
        this.shootAnimLength = (int)(shootAnimLength * 20);
        this.shootAnimTime = (int)(shootAnimTime * 20);
    }

    @Override
    public boolean shouldExecute() {
        return this.attacker.isTamed() && this.attacker.isBeingRidden() && this.attacker.isRangedAttacking() && this.attacker instanceof IRangedAttackMob;
    }

    public boolean shouldContinueExecuting() {
        return this.animTime <= this.shootAnimLength;
    }

    public void startExecuting() {
        this.animTime = 0;
    }

    public void resetTask() {
        this.animTime = 0;
        this.attacker.setRangedAttacking(false);
    }

    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.shootAnimTime) this.attacker.controlRangedAttack(0);
    }
}
