package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityTameable;

public class RiftAttackForOwner extends EntityAITarget {
    private final RiftCreature creature;
    private EntityLivingBase hitTarget;
    private int timestamp;

    public RiftAttackForOwner(RiftCreature creature) {
        super(creature, false);
        this.creature = creature;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.creature.isTamed()) return false;
        else if (this.creature.isSleeping()) return false;
        else if (this.creature.getTameBehavior() != TameBehaviorType.ASSIST) return false;
        else {
            EntityLivingBase owner = this.creature.getOwner();

            if (owner == null) return false;
            else if (owner.isRiding()) {
                if (owner.getRidingEntity() instanceof RiftCreature) {
                    RiftCreature riddenCreature = (RiftCreature) owner.getRidingEntity();
                    this.hitTarget = riddenCreature.getLastAttackedEntity();
                    int i = riddenCreature.getLastAttackedEntityTime();
                    return i != this.timestamp && this.isSuitableTarget(this.hitTarget, false) && this.creature.shouldAttackEntity(this.hitTarget, owner) &&  !this.creature.isBeingRidden() && !this.creature.busyAtWork();
                }
            }
            else {
                this.hitTarget = owner.getLastAttackedEntity();
                int i = owner.getLastAttackedEntityTime();
                return i != this.timestamp && this.isSuitableTarget(this.hitTarget, false) && this.creature.shouldAttackEntity(this.hitTarget, owner) && !this.creature.busyAtWork();
            }
        }
        return false;
    }

    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.hitTarget);
        EntityLivingBase entitylivingbase = this.creature.getOwner();

        if (entitylivingbase != null) this.timestamp = entitylivingbase.getLastAttackedEntityTime();

        super.startExecuting();
    }
}
