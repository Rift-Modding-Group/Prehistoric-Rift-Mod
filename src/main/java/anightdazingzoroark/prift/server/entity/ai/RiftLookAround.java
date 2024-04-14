package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAILookIdle;

public class RiftLookAround extends EntityAILookIdle {
    private final RiftCreature creature;

    public RiftLookAround(EntityLiving entitylivingIn) {
        super(entitylivingIn);
        this.creature = (RiftCreature) entitylivingIn;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        return !this.creature.isBeingRidden() && !this.creature.isUsingWorkstation() && super.shouldExecute() && !this.creature.isSleeping();
    }
}
