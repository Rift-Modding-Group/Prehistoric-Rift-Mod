package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public class RiftThagomizeMove extends RiftCreatureMove {
    public RiftThagomizeMove() {
        super(CreatureMove.THAGOMIZE);
    }

    @Override
    public MovePriority canBeExecuted(RiftCreature user, EntityLivingBase target) {
        if (user.world.rand.nextInt(4) == 0 && user.getDistance(target) <= user.attackWidth()) {
            user.getNavigator().tryMoveToEntityLiving(target, 1.0D);
            user.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            return MovePriority.HIGH;
        }
        return MovePriority.NONE;
    }

    @Override
    public void onStartExecuting(RiftCreature user) {
        user.removeSpeed();
    }

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, EntityLivingBase target, int useAmount) {
        if (target != null) {
            user.attackEntityAsMob(target, (float) RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 0, 4) * 5f);
            NonPotionEffectsHelper.setBleeding(target,
                    (int) RiftUtil.slopeResult(user.getLevel(), true, 0, 100, 0, 4),
                    (int) RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 100, 600));
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
    }

    @Override
    public void onHitEntity(RiftCreature user, EntityLivingBase target) {

    }

    @Override
    public void onHitBlock(RiftCreature user, BlockPos targetPos) {

    }
}
