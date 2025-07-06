package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftCreatureWarnTarget extends EntityAIBase {
    private final RiftCreature creature;
    private final int animLength;
    private final int warnSoundTime;
    private int animTick;

    public RiftCreatureWarnTarget(RiftCreature creature, float animLength, float warnSoundTime) {
        this.creature = creature;
        this.animLength = (int) (animLength * 20f);
        this.warnSoundTime = (int) (warnSoundTime * 20f);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        boolean isHerdLeader = this.creature.isHerdLeader();
        boolean isStrayFromHerd = !this.creature.canDoHerding() || !this.creature.isHerdLeader() && !this.creature.hasHerdLeader();

        return this.creature.canWarn() && !this.creature.isWarning()
                && !this.creature.isBeingRidden()
                && this.creature.getRevengeTarget() == null && this.creature.getAttackTarget() != null
                && (!this.creature.canDoHerding() || (isHerdLeader || isStrayFromHerd));
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.animTick <= this.animLength;
    }

    @Override
    public void startExecuting() {
        this.creature.setIsWarning(true);
    }

    @Override
    public void resetTask() {
        this.creature.setIsWarning(false);
        this.creature.setCanWarn(false);
        this.animTick = 0;
    }

    @Override
    public void updateTask() {
        if (this.animTick == this.warnSoundTime) {
            this.creature.playSound(this.creature.getWarnSound(), 1f, 1f);
        }
        this.animTick++;
    }
}
