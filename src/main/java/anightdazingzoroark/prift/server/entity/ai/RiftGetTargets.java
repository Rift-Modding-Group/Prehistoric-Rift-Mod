package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
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
    protected final RiftCreature creature;
    protected final int targetChance;
    protected final RiftGetTargets.Sorter sorter;
    protected final Predicate <? super EntityLivingBase > targetEntitySelector;
    protected final boolean alertOthers;
    protected EntityLivingBase targetEntity;

    public RiftGetTargets(EntityCreature creature, boolean alertOthers, boolean checkSight) {
        this(creature, checkSight, alertOthers, false);
    }

    public RiftGetTargets(EntityCreature creature, boolean alertOthers, boolean checkSight, boolean onlyNearby) {
        this(creature, alertOthers, 10, checkSight, onlyNearby, (Predicate)null);
    }

    public RiftGetTargets(EntityCreature creature, boolean alertOthers, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate <? super EntityLivingBase > targetSelector) {
        super(creature, checkSight, onlyNearby);
        this.creature = (RiftCreature)creature;
        this.targetChance = chance;
        this.sorter = new RiftGetTargets.Sorter(creature);
        this.setMutexBits(1);
        this.alertOthers = alertOthers;
        this.targetEntitySelector = new Predicate<EntityLivingBase>() {
            public boolean apply(@Nullable EntityLivingBase entity) {
                if (entity == null) return false;
                else if (targetSelector != null && !targetSelector.apply(entity)) return false;
                else {
                    return !EntitySelectors.NOT_SPECTATING.apply(entity) ? false : RiftGetTargets.this.isSuitableTarget(entity, false);
                }
            }
        };
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.isSleeping() || this.creature.isForcedAwake()) return false;
        else if (this.creature.isTamed()) return false;
        else {
            List<EntityLivingBase> list = new ArrayList<>();
            for (EntityLivingBase entity : this.creature.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector)) {
                if (entity instanceof EntityPlayer) {
                    if (this.creature.creatureType.getBehaviors().contains(RiftCreatureType.Behavior.AGGRESSIVE_TO_HUMANS) && this.creature.getTargetList().contains("minecraft:player")) {
                        EntityPlayer player = (EntityPlayer) entity;
                        if (!RiftUtil.playerIgnorableByCreature(this.creature, player)) list.add(entity);
                    }
                }
                else {
                    if (this.creature.getTargetList().contains(EntityList.getKey(entity).toString())) {
                        list.add(entity);
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
        return this.creature.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    public void startExecuting() {
        this.creature.setAttackTarget(this.targetEntity);
        if (this.alertOthers && this.creature.isHerdLeader()) {
            List<RiftCreature> allyList = creature.world.getEntitiesWithinAABB(this.creature.getClass(), this.creature.herdBoundingBox(), new Predicate<RiftCreature>() {
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

            if (d0 < d1) return -1;
            else return d0 > d1 ? 1 : 0;
        }
    }

    public static class RiftGetTargetsWater extends RiftGetTargets {
        public RiftGetTargetsWater(EntityCreature creature, boolean alertOthers, boolean checkSight) {
            super(creature, checkSight, alertOthers, false);
        }

        public RiftGetTargetsWater(EntityCreature creature, boolean alertOthers, boolean checkSight, boolean onlyNearby) {
            super(creature, alertOthers, 10, checkSight, onlyNearby, (Predicate)null);
        }

        public RiftGetTargetsWater(EntityCreature creature, boolean alertOthers, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate <? super EntityLivingBase > targetSelector) {
            super(creature, alertOthers, chance, checkSight, onlyNearby, targetSelector);
        }

        @Override
        public boolean shouldExecute() {
            if (this.creature.isInWater()) {
                if (this.creature.isTamed()) return false;
                else {
                    List<EntityLivingBase> list = new ArrayList<>();

                    for (EntityLivingBase entity : this.creature.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector)) {
                        if ((!RiftUtil.isRidingBoat(entity) && entity.isInWater()) || RiftUtil.isRidingBoat(entity)) {
                            if (entity instanceof EntityPlayer) {
                                if (this.creature.getTargetList().contains("minecraft:player")) {
                                    EntityPlayer player = (EntityPlayer) entity;
                                    if (!RiftUtil.playerIgnorableByCreature(this.creature, player)) list.add(entity);
                                }
                            }
                            else {
                                if (this.creature.getTargetList().contains(EntityList.getKey(entity).toString())) {
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
            return false;
        }
    }
}
