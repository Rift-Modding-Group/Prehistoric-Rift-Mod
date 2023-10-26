package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftResetAnimatedPose extends EntityAIBase {
    private RiftCreature creature;
    protected int animLength;
    protected int animTime;
    protected int mouse;

    public RiftResetAnimatedPose(RiftCreature creature, float animLength, int mouse) {
        this.creature = creature;
        this.animLength = (int)(animLength * 20);
        this.mouse = mouse;
    }

    @Override
    public boolean shouldExecute() {
        if (this.mouse == 0) return this.creature.getLeftClickUse() > 0 && this.creature.isActing();
        else if (this.mouse == 1) return this.creature.getRightClickUse() > 0 && this.creature.isActing();
        else return false;
    }

    public boolean shouldContinueExecuting() {
        return this.animTime <= this.animLength;
    }

    public void startExecuting() {
        this.animTime = 0;
    }

    public void resetTask() {
        this.creature.setActing(false);
        if (this.mouse == 0) this.creature.setLeftClickUse(0);
        else if (this.mouse == 1) this.creature.setRightClickUse(0);
    }

    public void updateTask() {
        this.animTime++;
    }
}
