package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsHelper;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class RiftHypnosisPowderMove extends RiftCreatureMove {
    public RiftHypnosisPowderMove() {
        super(CreatureMove.HYPNOSIS_POWDER);
    }

    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        //return super.canBeExecutedUnmounted(user, target) && user.world.rand.nextInt(4) == 0;
        return false;
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {}

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        AxisAlignedBB area = user.getEntityBoundingBox().grow(3D, 3D, 3D);
        List<Entity> nearbyEntities = user.world.getEntitiesWithinAABB(Entity.class, area, this.generalEntityPredicate(user, false));
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof EntityCreature)) continue;
            EntityCreature entityCreature = (EntityCreature) entity;
            if (!NonPotionEffectsHelper.isHypnotized(entityCreature)
                    && !MobFamilyHelper.entityInMobFamily(entityCreature, "human")
                    && (MobFamilyHelper.entityInMobFamily(entityCreature, "monster") || MobFamilyHelper.entityInMobFamily(entityCreature, "arthropod"))) {
                NonPotionEffectsHelper.setHypnotized(entityCreature, user);
            }
        }
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
    }
}
