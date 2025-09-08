package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RiftPackCallMove extends RiftCreatureMove {
    private List<RiftCreature> packMembers = new ArrayList<>();

    public RiftPackCallMove() {
        super(CreatureMove.PACK_CALL);
    }

    @Override
    public boolean canBeExecutedUnmounted(RiftCreature user, Entity target) {
        this.packMembers = user.world.getEntitiesWithinAABB(user.getClass(), user.getEntityBoundingBox().grow(12D), new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature entity) {
                return entity != null
                        && !RiftUtil.checkForNoHerdAssociations(user, entity)
                        && !user.equals(entity)
                        && user.isTamed() == entity.isTamed()
                        && !entity.isBaby();
            }
        });
        return super.canBeExecutedUnmounted(user, target) && !this.packMembers.isEmpty();
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        this.packMembers = user.world.getEntitiesWithinAABB(user.getClass(), user.getEntityBoundingBox().grow(12D), new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature entity) {
                return entity != null && !user.equals(entity) && user.isTamed() == entity.isTamed() && !entity.isBaby();
            }
        });
        return !this.packMembers.isEmpty();
    }

    @Override
    public String cannotExecuteMountedMessage() {
        return "reminder.insufficient_pack_members";
    }

    @Override
    public void onStartExecuting(RiftCreature user, Entity target) {
        user.setCanMove(false);
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        for (RiftCreature entity : this.packMembers) {
            entity.setCanMove(false);
            entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 90 * 20, 2));
            entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 90 * 20, 2));
        }
        user.addPotionEffect(new PotionEffect(MobEffects.SPEED, 90 * 20, 2));
        user.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 90 * 20, 2));
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.setCanMove(true);
        for (RiftCreature entity : this.packMembers) entity.setCanMove(true);
    }
}
