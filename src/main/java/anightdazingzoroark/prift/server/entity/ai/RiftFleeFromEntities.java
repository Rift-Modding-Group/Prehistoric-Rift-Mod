package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.mobFamily.MobFamily;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;

public class RiftFleeFromEntities extends EntityAIBase {
    private final RiftCreature creature;
    private final float fleeSpeed;
    private Path path;

    public RiftFleeFromEntities(RiftCreature creature, float fleeSpeed) {
        this.creature = creature;
        this.fleeSpeed = fleeSpeed;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        boolean isHerdLeader = this.creature.isHerdLeader();
        boolean isStrayFromHerd = !this.creature.canDoHerding() || !this.creature.isHerdLeader() && !this.creature.hasHerdLeader();
        if (this.creature.isTamed() || !this.creature.fleesFromDanger() || !(isHerdLeader || isStrayFromHerd)) return false;

        //find nearby entities to flee from
        List<EntityLivingBase> entityList = this.creature.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getTargetableArea(this.getTargetDistance()), this.targetSelector());
        if (entityList.isEmpty()) return false;
        else {
            EntityLivingBase entityToRunFrom = entityList.get(0);
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 7, new Vec3d(entityToRunFrom.posX, entityToRunFrom.posY, entityToRunFrom.posZ));

            if (vec3d == null) return false;
            else if (entityToRunFrom.getDistanceSq(vec3d.x, vec3d.y, vec3d.z) < entityToRunFrom.getDistanceSq(this.creature)) return false;
            else {
                this.path = this.creature.getNavigator().getPathToXYZ(vec3d.x, vec3d.y, vec3d.z);
                return this.path != null;
            }
        }
    }

    private Predicate<EntityLivingBase> targetSelector() {
        return new Predicate<EntityLivingBase>() {
            @Override
            public boolean apply(@Nullable EntityLivingBase entityLivingBase) {
                if (entityLivingBase == null) return false;
                else if (!RiftUtil.checkForNoAssociations(creature, entityLivingBase)) return false;
                else {
                    List<String> toFleeFrom = RiftConfigHandler.getConfig(creature.creatureType).general.mobsToRunFrom;
                    if (toFleeFrom == null || toFleeFrom.isEmpty()) return false;
                    for (String entry : toFleeFrom) {
                        //check if it is string for mob family first
                        if (RiftUtil.isMobFamilyString(entry)) {
                            MobFamily mobFamily = RiftUtil.getMobFamilyFromString(entry);
                            if (mobFamily == null) return false;
                            if (entityLivingBase instanceof EntityPlayer) {
                                EntityPlayer playerToCheck = (EntityPlayer) entityLivingBase;
                                if (mobFamily.getFamilyMembers().contains("minecraft:player")
                                    && !RiftUtil.playerIgnorableByCreature(creature, playerToCheck)) return true;
                            }
                            else {
                                ResourceLocation key = EntityList.getKey(entityLivingBase);
                                if (key == null) return false;
                                if (mobFamily.getFamilyMembers().contains(key.toString())) return true;
                            }
                        }
                        //check if its just an entity
                        else {
                            if (entityLivingBase instanceof EntityPlayer) {
                                EntityPlayer playerToCheck = (EntityPlayer) entityLivingBase;
                                if (toFleeFrom.contains("minecraft:player")
                                        && !RiftUtil.playerIgnorableByCreature(creature, playerToCheck)) return true;
                            }
                            else {
                                ResourceLocation key = EntityList.getKey(entityLivingBase);
                                if (key == null) return false;
                                if (toFleeFrom.contains(key.toString())) return true;
                            }
                        }
                    }
                }
                return false;
            }
        };
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        return this.creature.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
    }

    private double getTargetDistance() {
        IAttributeInstance iattributeinstance = this.creature.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
    }

    public void startExecuting() {
        this.creature.getNavigator().setPath(this.path, this.fleeSpeed);
        if (this.path.getFinalPathPoint() != null)
            this.creature.getLookHelper().setLookPosition(this.path.getFinalPathPoint().x, this.path.getFinalPathPoint().y, this.path.getFinalPathPoint().z, 30, 30);
    }

    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }
}
