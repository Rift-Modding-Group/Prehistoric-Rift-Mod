package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IRangedAttacker;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftControlledRangedAttack extends EntityAIBase {
    protected RiftCreature attacker;
    protected int shootAnimLength;
    protected int shootAnimTime;
    protected int animTime;
    protected int cooldown;

    public RiftControlledRangedAttack(RiftCreature creature, float shootAnimLength, float shootAnimTime, int cooldown) {
        this.attacker = creature;
        //attackAnimLength and attackAnimTime are in seconds, will convert to ticks automatically here
        this.shootAnimLength = (int)(shootAnimLength * 20);
        this.shootAnimTime = (int)(shootAnimTime * 20);
        this.cooldown = cooldown;
    }

    @Override
    public boolean shouldExecute() {
        return this.attacker.isTamed() && this.attacker.isBeingRidden() && this.attacker.isRangedAttacking() && this.attacker instanceof IRangedAttacker;
    }

    public boolean shouldContinueExecuting() {
        return this.animTime <= this.shootAnimLength;
    }

    public void startExecuting() {
        this.attacker.playSound(((IRangedAttacker)this.attacker).rangedAttackSound(), 2, 1);
        this.animTime = 0;
    }

    public void resetTask() {
        this.animTime = 0;
        this.attacker.setRangedAttacking(false);
        this.attacker.setRightClickCooldown(this.cooldown);
    }

    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.shootAnimTime) this.attacker.controlRangedAttack(0);
    }
}
