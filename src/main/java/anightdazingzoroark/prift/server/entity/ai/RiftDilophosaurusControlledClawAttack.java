package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Dilophosaurus;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftDilophosaurusControlledClawAttack extends EntityAIBase {
    private final Dilophosaurus dilophosaurus;
    protected final int attackAnimLength;
    protected final int attackAnimTime;
    protected int animTime;

    public RiftDilophosaurusControlledClawAttack(Dilophosaurus dilophosaurus) {
        this.dilophosaurus = dilophosaurus;
        //attackAnimLength and attackAnimTime are in seconds, will convert to ticks automatically here
        this.attackAnimLength = (int)(0.48f * 20);
        this.attackAnimTime = (int)(0.24f * 20);
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        return this.dilophosaurus.isTamed() && this.dilophosaurus.isBeingRidden() && (this.dilophosaurus.isUsingLeftClaw() || this.dilophosaurus.isUsingRightClaw());
    }

    public boolean shouldContinueExecuting() {
        return this.animTime <= this.attackAnimLength;
    }

    public void startExecuting() {
        this.animTime = 0;
        this.dilophosaurus.energyActionMod++;
    }

    public void resetTask() {
        this.animTime = 0;
        this.dilophosaurus.setUsingLeftClaw(false);
        this.dilophosaurus.setUsingRightClaw(false);
    }

    public void updateTask() {
        this.animTime++;
        if (this.animTime == this.attackAnimTime) this.dilophosaurus.controlAttack();
    }
}
