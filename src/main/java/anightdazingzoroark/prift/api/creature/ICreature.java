package anightdazingzoroark.prift.api.creature;

import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.creature.config.RiftCreatureConfig;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * api version of RiftCreature
 */
public interface ICreature {
    World getEntityWorld();

    Random getRNG();

    AxisAlignedBB getEntityBoundingBox();

    EntityLivingBase getAttackTarget();

    boolean attackEntityAsMob(Entity entity);

    boolean isEntityAlive();

    boolean isOnGround();

    boolean bodyTouchingLiquid();

    boolean aabbIntersectsBoundingBox(@NotNull AxisAlignedBB otherAABB, @NotNull String boundingBoxName);

    @NotNull
    Vec3d getLocatorWorldPos(@NotNull String name);

    @NotNull
    AnimationDataEntity getAnimationData();

    /**
     * Returns this creature as a RiftLib ray creator.
     */
    default IRayCreator<?> asRayCreator() {
        if (this instanceof IRayCreator<?> rayCreator) return rayCreator;
        throw new IllegalStateException("This creature does not support rays");
    }

    int getSprintToAttackCooldown();

    boolean canLeapToAttack();

    boolean atFrustrationThreshold();

    boolean atRageThreshold();

    boolean isUnableToPathToTarget();

    boolean hasStraightWalkingPathTo(@NotNull EntityLivingBase target);

    boolean canDoHerding();

    boolean isInHerd();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isHerdLeader();

    @NotNull
    CreatureNavigationBuilder getNavigationBuilder();

    <I> I getProperty(String key);

    <I> void setProperty(String key, I value);

    String getPhase();

    void setPhase(String value);

    @NotNull
    RiftCreatureBuilder getCreatureType();

    @NotNull
    RiftCreatureConfig getCreatureConfig();

    int getLevel();

    RiftCreatureEnums.Nature getNature();

    int getAgeInTicks();

    float getHealth();

    float getMaxHealth();

    float getStamina();

    float getMaxStamina();
}
