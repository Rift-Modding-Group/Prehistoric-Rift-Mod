package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public class RiftBiteMove extends RiftCreatureMove {
    public RiftBiteMove() {
        super(CreatureMove.BITE, 20, 0.6D);
    }

    @Override
    public boolean canBeExecuted(RiftCreature user, EntityLivingBase target) {
        return true;
    }

    @Override
    public void onStartExecuting(RiftCreature user) {
        user.setAttacking(true);
        user.removeSpeed();
    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, EntityLivingBase target) {
        user.attackEntityAsMob(target);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setAttacking(false);
        user.resetSpeed();
        if (user.isTamed()) user.energyActionMod++;
    }

    @Override
    public void onHitEntity(RiftCreature user, EntityLivingBase target) {

    }

    @Override
    public void onHitBlock(RiftCreature user, BlockPos targetPos) {

    }
}
