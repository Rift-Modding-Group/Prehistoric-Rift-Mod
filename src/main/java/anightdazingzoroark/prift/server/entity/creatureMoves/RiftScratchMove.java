package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public class RiftScratchMove extends RiftCreatureMove {
    public RiftScratchMove() {
        super(CreatureMove.SCRATCH, 10, 0.5);
    }

    @Override
    public boolean canBeExecuted(RiftCreature user, EntityLivingBase target) {
        return true;
    }

    @Override
    public void onStartExecuting(RiftCreature user) {

    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, EntityLivingBase target) {}

    @Override
    public void onStopExecuting(RiftCreature user) {

    }

    @Override
    public void onHitEntity(RiftCreature user, EntityLivingBase target) {

    }

    @Override
    public void onHitBlock(RiftCreature user, BlockPos targetPos) {

    }
}
