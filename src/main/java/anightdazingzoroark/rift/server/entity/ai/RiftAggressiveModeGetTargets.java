package anightdazingzoroark.rift.server.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RiftAggressiveModeGetTargets extends EntityAITarget {
    private final int targetChance;
    protected final RiftAggressiveModeGetTargets.Sorter sorter;
    protected EntityLivingBase targetEntity;

    public RiftAggressiveModeGetTargets(EntityCreature creature, boolean checkSight) {
        this(creature, checkSight, false);
    }

    public RiftAggressiveModeGetTargets(EntityCreature creature, boolean checkSight, boolean onlyNearby) {
        this(creature, 10, checkSight, onlyNearby);
    }

    public RiftAggressiveModeGetTargets(EntityCreature creature, int chance, boolean checkSight, boolean onlyNearby) {
        super(creature, checkSight, onlyNearby);
        this.targetChance = chance;
        this.sorter = new RiftAggressiveModeGetTargets.Sorter(creature);
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
            return false;
        }
        else {
            List<EntityLivingBase> list = new ArrayList<>();
            for (EntityLivingBase entity : this.taskOwner.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getTargetableArea(this.getTargetDistance()), null)) {
                if (!entity.isRiding()) {
                    if (entity instanceof EntityPlayer) {
                        if (!entity.getUniqueID().equals(((EntityTameable) this.taskOwner).getOwnerId())) {
                            list.add(entity);
                        }
                    }
                    else if (entity instanceof EntityTameable) {
                        if (!(((EntityTameable) entity).getOwnerId().equals(((EntityTameable) this.taskOwner).getOwnerId())) && ((EntityTameable) entity).isTamed()) {
                            list.add(entity);
                        }
                    }
                    else {
                        list.add(entity);
                    }
                }
            }

            if (list.isEmpty()) {
                return false;
            }
            else {
                Collections.sort(list, this.sorter);
                this.targetEntity = list.get(0);
                return true;
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
