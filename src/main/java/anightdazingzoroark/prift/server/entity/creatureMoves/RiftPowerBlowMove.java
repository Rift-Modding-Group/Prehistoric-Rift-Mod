package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.DamageSource;

import java.util.List;

public class RiftPowerBlowMove extends RiftCreatureMove {
    public RiftPowerBlowMove() {
        super(CreatureMove.POWER_BLOW);
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
        user.disableCanRotateMounted();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        List<Entity> targets = user.getAllTargetsInFront(true);
        float blowStrength = RiftUtil.slopeResult(useAmount, true, 0, this.creatureMove.maxUse, 2f, 8f);
        for (Entity entity : targets) {
            if (entity instanceof MultiPartEntityPart
                    && ((MultiPartEntityPart)entity).parent instanceof EntityLivingBase)
                this.knockback(user, (EntityLivingBase)((MultiPartEntityPart)entity).parent, blowStrength);
            else if (entity instanceof EntityLivingBase) this.knockback(user, (EntityLivingBase) entity, blowStrength);
        }
    }

    public void knockback(RiftCreature user, EntityLivingBase entity, float strength) {
        if (RiftUtil.isAppropriateSize(entity, RiftUtil.getMobSize(user))) {
            double d0 = user.posX - entity.posX;
            double d1 = user.posZ - entity.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            entity.knockBack(user, strength, d0 / d2 * 8.0D, d1 / d2 * 8.0D);
        }
        entity.attackEntityFrom(DamageSource.causeMobDamage(user), 0);
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        user.enableCanRotateMounted();
    }

    @Override
    public int[] unmountedChargeBounds() {
        return new int[]{0, (int)(this.creatureMove.maxUse * 0.3)};
    }
}
