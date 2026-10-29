package anightdazingzoroark.prift.api.creature;

import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.creature.config.RiftCreatureConfig;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.List;

/**
 * api version of RiftCreature
 */
public interface ICreature {
    World getEntityWorld();

    Random getRNG();

    @NotNull
    List<String> getMoves();

    void setMoves(@NotNull List<String> moves);

    int getMaximumMoveSlots();

    boolean entityInTargetGroup(@NotNull Entity entity, @NotNull String group);

    AxisAlignedBB getEntityBoundingBox();

    EntityLivingBase getAttackTarget();

    boolean attackEntityAsMob(Entity entity);

    Entity getControllingPassenger();

    boolean isEntityAlive();

    boolean isOnGround();

    BlockPos getPosition();

    Vec3d getPositionVector();

    double horizontalDistanceFromEntity(@NotNull Entity entity);

    double verticalDistanceFromEntity(@NotNull Entity entity);

    boolean bodyTouchingLiquid();

    boolean aabbIntersectsBoundingBox(@NotNull AxisAlignedBB otherAABB, @NotNull String boundingBoxName);

    boolean aabbIntersectsBoundingBoxTag(@NotNull AxisAlignedBB otherAABB, @NotNull String tagName);

    @NotNull
    Vec3d getLocatorWorldPos(@NotNull String name);

    @NotNull
    AnimationDataEntityLiving getAnimationData();

    /** Create a ray from an explicit definition at one of this entity's animated locators. */
    void createRay(@NotNull RiftLibRayBuilder rayBuilder, @NotNull String locatorName);

    boolean canSprintToAttack();

    void prepareRush(double speedMultiplier, int maximumTicks);

    boolean canLeapToAttack();

    boolean atFrustrationThreshold();

    boolean atRageThreshold();

    boolean isUnableToPathToTarget();

    boolean hasStraightWalkingPathTo(@NotNull EntityLivingBase target);

    boolean canDoHerding();

    boolean isInHerd();

    boolean getUseBlockBreak();

    boolean getWasRecentlyHit();

    boolean isTamed();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isHerdLeader();

    @Nullable
    CreatureNavigationBuilder getNavigationBuilder();

    <I extends AbstractPropertyValue<?>> I getProperty(String key);

    <I> void setProperty(String key, I value);

    @NotNull
    String getCurrentMove();

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

    @Nullable
    Vec3d getRiddenAimPosition();

    void launchProjectile(@NotNull ProjectileBuilder projectileBuilder, float velocity, float inaccuracy);

    void launchProjectile(@NotNull ProjectileBuilder projectileBuilder, @NotNull EntityLivingBase target, float velocity, float inaccuracy);
}
