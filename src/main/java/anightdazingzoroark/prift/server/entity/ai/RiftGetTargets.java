package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.*;

public class RiftGetTargets extends EntityAITarget {
    private final int targetChance;
    private List<String> targetList;
    protected final RiftGetTargets.Sorter sorter;
    protected final Predicate <? super EntityLivingBase > targetEntitySelector;
    protected final boolean alertOthers;
    protected EntityLivingBase targetEntity;

    public RiftGetTargets(EntityCreature creature, String[] targetList, boolean alertOthers, boolean checkSight) {
        this(creature, targetList, checkSight, alertOthers, false);
    }

    public RiftGetTargets(EntityCreature creature, String[] targetList, boolean alertOthers, boolean checkSight, boolean onlyNearby) {
        this(creature, targetList, alertOthers, 10, checkSight, onlyNearby, (Predicate)null);
    }

    public RiftGetTargets(EntityCreature creature, String[] targetList, boolean alertOthers, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate <? super EntityLivingBase > targetSelector) {
        super(creature, checkSight, onlyNearby);
        this.targetList = Arrays.asList(targetList);
        this.targetChance = chance;
        this.sorter = new RiftGetTargets.Sorter(creature);
        this.setMutexBits(1);
        this.alertOthers = alertOthers;
        this.targetEntitySelector = new Predicate<EntityLivingBase>() {
            public boolean apply(@Nullable EntityLivingBase p_apply_1_)
            {
                if (p_apply_1_ == null) return false;
                else if (targetSelector != null && !targetSelector.apply(p_apply_1_)) return false;
                else {
                    return !EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) ? false : RiftGetTargets.this.isSuitableTarget(p_apply_1_, false);
                }
            }
        };
    }

    @Override
    public boolean shouldExecute() {
        RiftCreature creature = (RiftCreature) this.taskOwner;
        if (creature.isTamed()) return false;
        else if (creature.canDoHerding() && !creature.isHerdLeader() && !creature.getHerdLeader().equals(creature)) return false;
        else {
            List<EntityLivingBase> list = new ArrayList<>();
            for (EntityLivingBase entity : this.taskOwner.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector)) {
                if (!entity.isRiding()) {
                    if (entity instanceof EntityPlayer) {
                        if (this.targetList.contains("minecraft:player")) {
                            EntityPlayer player = (EntityPlayer) entity;
                            if (!player.isSneaking() || !creature.isTamingFood(player.getHeldItemMainhand())) list.add(entity);
                        }
                    }
                    else {
                        if (this.targetList.contains(EntityList.getKey(entity).toString())) {
                            list.add(entity);
                        }
                    }
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

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.taskOwner.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        if (this.alertOthers) {
            List<RiftCreature> allyList = this.taskOwner.world.getEntitiesWithinAABB(((RiftCreature)this.taskOwner).getClass(), ((RiftCreature)this.taskOwner).getHerdBoundingBox(), new Predicate<RiftCreature>() {
                @Override
                public boolean apply(@Nullable RiftCreature input) {
                    return !input.isTamed();
                }
            });
            for (RiftCreature ally : allyList) ally.setAttackTarget(this.targetEntity);
        }
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
