package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyHelper;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.*;

public class RiftTurretModeTargeting extends EntityAITarget {
    private final RiftCreature creature;
    protected final RiftTurretModeTargeting.Sorter sorter;
    protected EntityLivingBase targetEntity;

    public RiftTurretModeTargeting(RiftCreature creature, boolean checkSight) {
        super(creature, checkSight);
        this.creature = creature;
        this.sorter = new RiftTurretModeTargeting.Sorter(creature);
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.canEnterTurretMode() && this.creature.isTurretMode()) {
            List<EntityLivingBase> list = new ArrayList<>();
            for (EntityLivingBase entity : this.taskOwner.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getTargetableArea(this.getTargetDistance()), new Predicate<EntityLivingBase>() {
                @Override
                public boolean apply(@Nullable EntityLivingBase input) {
                    return !(input instanceof RiftEgg) && !(input instanceof RiftLargeWeapon);
                }
            })) {
                if (this.creature.getTurretTargeting().equals(TurretModeTargeting.PLAYERS)) {
                    if (entity instanceof EntityPlayer) {
                        if (!entity.getUniqueID().equals(this.creature.getOwnerId())) {
                            list.add(entity);
                        }
                    }
                }
                else if (this.creature.getTurretTargeting().equals(TurretModeTargeting.PLAYERS_AND_OTHER_TAMES)) {
                    if ((entity instanceof EntityPlayer || entity instanceof EntityTameable) && RiftUtil.checkForNoAssociations(this.creature, entity)) {
                        list.add(entity);
                    }
                }
                else if (this.creature.getTurretTargeting().equals(TurretModeTargeting.HOSTILES)) {
                    List<String> targets = new ArrayList<>(MobFamilyHelper.getMobFamily("monster").getFamilyMembers());
                    targets.addAll(MobFamilyHelper.getMobFamily("carnivoreHostileToHuman").getFamilyMembers());

                    if (entity instanceof EntityPlayer) {
                        if (targets.contains("minecraft:player")) {
                            EntityPlayer player = (EntityPlayer) entity;
                            if (!this.creature.getOwner().equals(player)) list.add(entity);
                        }
                    }
                    else {
                        if (targets.contains(EntityList.getKey(entity).toString())) {
                            if (entity instanceof EntityTameable) {
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
                }
                else if (this.creature.getTurretTargeting().equals(TurretModeTargeting.ALL)) {
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
        return false;
    }

    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        super.startExecuting();
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.taskOwner.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
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
