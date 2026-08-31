package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.api.creature.config.RiftCreatureConfig;
import anightdazingzoroark.prift.server.config.RiftListsConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.util.RiftUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class RiftFindTarget extends EntityAITarget {
    @NotNull
    private final RiftCreature creature;
    protected EntityLivingBase targetEntity;

    public RiftFindTarget(@NotNull RiftCreature creature, boolean checkSight) {
        super(creature, checkSight);
        this.creature = creature;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        //for herders, only herd leaders can find targets
        if (!this.creature.canLeadHerdBehavior()) return false;

        EntityLivingBase existingTarget = this.creature.getAttackTarget();
        if (this.creature.isInHerd() && existingTarget != null && existingTarget.isEntityAlive()) {
            this.targetEntity = existingTarget;
            return true;
        }

        List<EntityLivingBase> list = this.taskOwner.world.getEntitiesWithinAABB(
                EntityLivingBase.class, this.getTargetableArea(this.getTargetDistance()),
                entity -> {
                    if (entity == null) return false;
                    return EntitySelectors.NOT_SPECTATING.apply(entity) && this.isSuitableTarget(entity, false);
                }
        );

        if (list.isEmpty()) return false;
        else {
            list.sort((entityOne, entityTwo) -> {
                double distanceSqOne = this.creature.getDistanceSq(entityOne);
                double distanceSqTwo = this.creature.getDistanceSq(entityTwo);

                if (distanceSqOne < distanceSqTwo) return -1;
                else return distanceSqOne > distanceSqTwo ? 1 : 0;
            });
            this.targetEntity = list.getFirst();
            return true;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (!this.creature.canLeadHerdBehavior()) return false;

        EntityLivingBase existingTarget = this.creature.getAttackTarget();
        if (this.creature.isInHerd() && existingTarget != null && existingTarget != this.targetEntity) {
            this.targetEntity = existingTarget;
        }
        return super.shouldContinueExecuting();
    }

    @Override
    public void startExecuting() {
        this.creature.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }

    @Override
    public void resetTask() {
        if (this.creature.canLeadHerdBehavior() && this.creature.getAttackTarget() == this.targetEntity) {
            super.resetTask();
        }
        else this.target = null;
        this.targetEntity = null;
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.creature.getEntityBoundingBox().grow(targetDistance, 4D, targetDistance);
    }

    //will use creature config info and relation to user to determine target suitability then return super of this method
    @Override
    protected boolean isSuitableTarget(@Nullable EntityLivingBase target, boolean includeInvincibles) {
        if (target == null) return false;

        if (this.creature.isRelatedToEntity(target)) return false;

        if (target instanceof EntityPlayer player && this.creature.isRememberedPlayerTarget(player)) return true;

        if (!this.isAllowedByConfig(target)) return false;

        return super.isSuitableTarget(target, includeInvincibles);
    }

    //---target group stuff from here---
    private boolean isAllowedByConfig(@NotNull EntityLivingBase target) {
        RiftCreatureConfig creatureConfig = this.creature.getCreatureConfig();

        //1. group blacklist
        if (this.matchesTargetGroup(target, creatureConfig.targetBlacklist)) {
            return false;
        }

        //2. individually blacklisted
        if (this.matchesIndividualTarget(target, creatureConfig.targetBlacklist)) {
            return false;
        }

        //3. group whitelist
        if (this.matchesTargetGroup(target, creatureConfig.targetWhitelist)) {
            return true;
        }

        //4. individually whitelisted
        return this.matchesIndividualTarget(target, creatureConfig.targetWhitelist);
    }

    private boolean matchesTargetGroup(@NotNull EntityLivingBase target, @Nullable List<String> entries) {
        if (entries == null) return false;

        RiftListsConfig listsConfig = ServerProxy.jsonConfigParser.getListsConfig();
        for (String entry : entries) {
            List<String> targetGroup = listsConfig.targetGroups.get(entry);

            //skip if not a valid group name
            if (targetGroup == null) continue;

            for (String idInGroup : targetGroup) {
                if (RiftUtil.entityMatchesID(target, idInGroup)) return true;
            }
        }

        return false;
    }

    private boolean matchesIndividualTarget(@NotNull EntityLivingBase target, @Nullable List<String> entries) {
        if (entries == null) return false;

        RiftListsConfig listsConfig = ServerProxy.jsonConfigParser.getListsConfig();
        for (String entry : entries) {
            //skip groups, we only lookin for individual entities here
            if (listsConfig.targetGroups != null && listsConfig.targetGroups.containsKey(entry)) {
                continue;
            }

            if (RiftUtil.entityMatchesID(target, entry)) return true;
        }

        return false;
    }
}
