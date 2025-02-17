package anightdazingzoroark.prift.server.entity.creatureMoves;

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
    public MovePriority canBeExecutedUnmounted(RiftCreature user, Entity target) {
        this.packMembers = user.world.getEntitiesWithinAABB(user.getClass(), user.getEntityBoundingBox().grow(12D), new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature input) {
                return input != null && !user.equals(input) && user.isTamed() == input.isTamed() && !input.isBaby();
            }
        });
        if (!this.packMembers.isEmpty()) return MovePriority.HIGH;
        return MovePriority.NONE;
    }

    @Override
    public boolean canBeExecutedMounted(RiftCreature user, Entity target) {
        this.packMembers = user.world.getEntitiesWithinAABB(user.getClass(), user.getEntityBoundingBox().grow(12D), new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature input) {
                return input != null && !user.equals(input) && user.isTamed() == input.isTamed() && !input.isBaby();
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
        user.removeSpeed();
    }

    @Override
    public void whileChargingUp(RiftCreature user) {}

    @Override
    public void whileExecuting(RiftCreature user) {

    }

    @Override
    public void onReachUsePoint(RiftCreature user, Entity target, int useAmount) {
        for (RiftCreature entity : this.packMembers) {
            entity.removeSpeed();
            entity.addPotionEffect(new PotionEffect(MobEffects.SPEED, 90 * 20, 2));
            entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 90 * 20, 2));
        }
        user.addPotionEffect(new PotionEffect(MobEffects.SPEED, 90 * 20, 2));
        user.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 90 * 20, 2));
    }

    @Override
    public void onStopExecuting(RiftCreature user) {
        user.resetSpeed();
        for (RiftCreature entity : this.packMembers) entity.resetSpeed();
    }
}
