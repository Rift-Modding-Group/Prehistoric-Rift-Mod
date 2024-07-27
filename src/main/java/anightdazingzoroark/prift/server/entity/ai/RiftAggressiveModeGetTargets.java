package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RiftAggressiveModeGetTargets extends EntityAITarget {
    private final int targetChance;
    protected final RiftAggressiveModeGetTargets.Sorter sorter;
    private final RiftCreature creature;
    protected EntityLivingBase targetEntity;

    public RiftAggressiveModeGetTargets(RiftCreature creature, boolean checkSight) {
        this(creature, checkSight, false);
    }

    public RiftAggressiveModeGetTargets(RiftCreature creature, boolean checkSight, boolean onlyNearby) {
        this(creature, 10, checkSight, onlyNearby);
    }

    public RiftAggressiveModeGetTargets(RiftCreature creature, int chance, boolean checkSight, boolean onlyNearby) {
        super(creature, checkSight, onlyNearby);
        this.creature = creature;
        this.targetChance = chance;
        this.sorter = new RiftAggressiveModeGetTargets.Sorter(creature);
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.creature.isTamed()) return false;
        else if (this.creature.isSleeping()) return false;
        else if (this.creature.isBeingRidden()) return false;
        else if (!this.creature.getTameBehavior().equals(TameBehaviorType.AGGRESSIVE)) return false;
        else if (this.creature.busyAtWork()) return false;
        else if (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) return false;
        else {
            if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
                return false;
            }
            else {
                List<EntityLivingBase> list = new ArrayList<>();
                for (EntityLivingBase entity : this.taskOwner.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getTargetableArea(this.getTargetDistance()), new Predicate<EntityLivingBase>() {
                    @Override
                    public boolean apply(@Nullable EntityLivingBase input) {
                        return !(input instanceof RiftEgg) && !(input instanceof RiftLargeWeapon);
                    }
                })) {
                    if (!entity.isRiding()) {
                        if (entity instanceof EntityPlayer) {
                            if (!entity.getUniqueID().equals(this.creature.getOwnerId())) {
                                list.add(entity);
                            }
                        }
                        else if (entity instanceof EntityTameable) {
                            if ((((EntityTameable) entity).isTamed())) {
                                if (this.creature.getOwner() != null) {
                                    if (!((EntityTameable) entity).getOwner().equals(this.creature.getOwner())) {
                                        list.add(entity);
                                    }
                                }
                            }
                            else list.add(entity);
                        }
                        else list.add(entity);
                    }
                }

                if (list.isEmpty()) return false;
                else {
                    Collections.sort(list, this.sorter);
                    this.targetEntity = list.get(0);
                    return true;
                }
            }
        }
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.taskOwner.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }

    public static class Sorter implements Comparator<Entity> {
        private final Entity entity;

        public Sorter(Entity entityIn)
        {
            this.entity = entityIn;
        }

        public int compare(Entity p_compare_1_, Entity p_compare_2_) {
            double d0 = this.entity.getDistanceSq(p_compare_1_);
            double d1 = this.entity.getDistanceSq(p_compare_2_);

            if (d0 < d1) {
                return -1;
            }
            else {
                return d0 > d1 ? 1 : 0;
            }
        }
    }
}