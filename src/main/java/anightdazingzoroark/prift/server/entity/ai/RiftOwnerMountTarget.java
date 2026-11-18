package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RiftOwnerMountTarget extends EntityAITarget {
    @NotNull
    private final RiftCreature creature;
    @Nullable
    private EntityLivingBase ownerMount;
    @Nullable
    private EntityLivingBase mountTarget;
    @Nullable
    private RiftCreatureEnums.TameTargeting targetingMode;
    private int lastAttackTime;
    private int lastRevengeTime;
    private int eventTime;

    public RiftOwnerMountTarget(@NotNull RiftCreature creature) {
        super(creature, false);
        this.creature = creature;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.creature.isTamed() || this.creature.getIsSleeping() || this.creature.isBeingRidden()) return false;

        EntityLivingBase owner = this.creature.getOwner();
        if (owner == null || !(owner.getRidingEntity() instanceof EntityLivingBase mount)) return false;

        if (this.ownerMount != mount) {
            this.ownerMount = mount;
            this.lastAttackTime = 0;
            this.lastRevengeTime = 0;
        }

        switch (this.creature.getTameTargeting()) {
            case ASSIST -> {
                this.mountTarget = mount.getLastAttackedEntity();
                this.eventTime = mount.getLastAttackedEntityTime();
                if (this.eventTime == this.lastAttackTime) return false;
            }
            case DEFENSIVE -> {
                this.mountTarget = mount.getRevengeTarget();
                this.eventTime = mount.getRevengeTimer();
                if (this.eventTime == this.lastRevengeTime) return false;
            }
            default -> {
                return false;
            }
        }

        return this.isSuitableTarget(this.mountTarget, false)
                && this.mountTarget != mount
                && this.creature.shouldAttackEntity(this.mountTarget, owner);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.creature.isTamed() && !this.creature.getIsSleeping() && !this.creature.isBeingRidden()
                && this.creature.getTameTargeting() == this.targetingMode
                && super.shouldContinueExecuting();
    }

    @Override
    public void startExecuting() {
        this.creature.setAttackTarget(this.mountTarget);
        this.targetingMode = this.creature.getTameTargeting();
        if (this.targetingMode == RiftCreatureEnums.TameTargeting.ASSIST) {
            this.lastAttackTime = this.eventTime;
        }
        else this.lastRevengeTime = this.eventTime;
        super.startExecuting();
    }
}
