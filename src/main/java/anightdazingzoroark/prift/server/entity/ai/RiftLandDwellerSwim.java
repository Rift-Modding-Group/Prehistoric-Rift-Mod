package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.ai.pathfinding.PathNavigateRiftClimber;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigateGround;

public class RiftLandDwellerSwim extends EntityAIBase {
    private final RiftCreature creature;

    public RiftLandDwellerSwim(RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(4);

        if (creature.getNavigator() instanceof PathNavigateGround) {
            ((PathNavigateGround)creature.getNavigator()).setCanSwim(true);
        }
        else if (creature.getNavigator() instanceof PathNavigateRiftClimber) {
            ((PathNavigateRiftClimber)creature.getNavigator()).setCanSwim(true);
        }
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.bodyPart != null) return this.creature.bodyPart.isInWater() || this.creature.bodyPart.isInLava();
        else return false;
    }

    @Override
    public void startExecuting() {
        this.creature.isFloatingOnWater = true;
    }

    @Override
    public void resetTask() {
        this.creature.isFloatingOnWater = false;
    }
}
