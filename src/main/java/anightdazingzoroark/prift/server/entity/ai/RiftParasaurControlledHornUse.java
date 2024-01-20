package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.Parasaurolophus;
import net.minecraft.entity.ai.EntityAIBase;

public class RiftParasaurControlledHornUse extends EntityAIBase {
    private Parasaurolophus parasaurolophus;
    protected final int hornAnimLength;
    protected int animTime;

    public RiftParasaurControlledHornUse(Parasaurolophus parasaurolophus) {
        this.parasaurolophus = parasaurolophus;
        this.hornAnimLength = (int)(1.04D * 20D);
    }

    @Override
    public boolean shouldExecute() {
        return this.parasaurolophus.isTamed() && this.parasaurolophus.isBeingRidden() && this.parasaurolophus.isUsingHorn();
    }

    public boolean shouldContinueExecuting() {
        return this.animTime <= this.hornAnimLength;
    }

    public void startExecuting() {
        this.animTime = 0;
        this.parasaurolophus.setEnergy(this.parasaurolophus.getEnergy() - 1);
        this.parasaurolophus.useScareHorn();
        System.out.println("use scare horn");
    }

    public void resetTask() {
        this.animTime = 0;
        this.parasaurolophus.setUsingHorn(false);
    }

    public void updateTask() {
        this.animTime++;
    }
}
