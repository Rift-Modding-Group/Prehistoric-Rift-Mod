package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public class RiftTailSlapMove extends RiftCreatureMove {
    public RiftTailSlapMove() {
        super(CreatureMove.TAIL_SLAP);
    }

    @Override
    public MovePriority canBeExecuted(RiftCreature user, EntityLivingBase target) {
        if (user.getDistance(target) <= user.attackWidth()) {
            user.getNavigator().tryMoveToEntityLiving(target, 1.0D);
            user.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            return MovePriority.LOW;
        }
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user) {
        user.removeSpeed();
    }

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, EntityLivingBase target, int useAmount) {
        if (target != null) user.attackEntityAsMob(target);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
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
