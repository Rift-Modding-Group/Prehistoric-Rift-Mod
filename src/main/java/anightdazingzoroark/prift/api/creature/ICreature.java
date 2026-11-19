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
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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

    @NotNull
    AxisAlignedBB getAreaAroundCreature(double radius);

    @NotNull
    List<ICreature> getCreaturesInAABB(@NotNull AxisAlignedBB aabb, Predicate<ICreature> creaturePredicate);

    EntityLivingBase getAttackTarget();

    boolean isInAnyEntityLineOfSight();

    boolean attackEntityAsMob(Entity entity);

    void setRecoilDamage(@Nullable Function<Double, Double> recoilDamage);

    void knockbackEntity(@NotNull Entity entity, double horizontalStrength, double verticalStrength);

    Entity getControllingPassenger();

    boolean isEntityAlive();

    boolean isOnGround();

    BlockPos getPosition();

    Vec3d getPositionVector();

    double[] getSize();

    double horizontalDistanceFromEntity(@NotNull Entity entity);

    double verticalDistanceFromEntity(@NotNull Entity entity, boolean useCenter);

    boolean bodyTouchingLiquid();

    boolean aabbIntersectsBoundingBox(@NotNull AxisAlignedBB otherAABB, @NotNull String boundingBoxName);

    boolean aabbIntersectsBoundingBoxTag(@NotNull AxisAlignedBB otherAABB, @NotNull String tagName);

    /** Checks for matching tagged hitboxes or all required attachment locators. */
    boolean hasAttackGeometry(@NotNull AttackGeometry geometry);

    @NotNull
    Vec3d getLocatorWorldPos(@NotNull String name);

    @NotNull
    AnimationDataEntityLiving getAnimationData();

    void createRay(@NotNull RiftLibRayBuilder rayBuilder, @NotNull String locatorName);

    void createMoveGraphic(@NotNull CreatureMoveGraphic graphic, @Nullable EntityLivingBase target);

    @Nullable
    RayTraceResult getCrosshairTargets();

    boolean canSprintToAttack();

    void prepareRush(double speedMultiplier, int maximumTicks);

    void prepareRush(double speedMultiplier, int maximumTicks, boolean includeKnockback);

    void prepareRush(double speedMultiplier, int maximumTicks, boolean includeKnockback, @Nullable Consumer<ICreature> impactEffect);

    void prepareRush(double speedMultiplier, int maximumTicks, boolean includeKnockback, boolean stopOnImpact, @Nullable Consumer<ICreature> impactEffect);

    boolean canLeapToAttack();

    boolean atFrustrationThreshold();

    boolean atRageThreshold();

    boolean isUnableToPathToTarget();

    boolean hasStraightWalkingPathTo(@NotNull EntityLivingBase target);

    boolean canDoHerding();

    boolean isInHerd();

    ICreature getHerdLeader();

    boolean getUseBlockBreak();

    boolean getWasRecentlyHit();

    boolean isTamed();

    boolean isTemporaryAlly();

    int getTicksExisted();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isHerdLeader();

    @Nullable
    CreatureNavigationBuilder getNavigationBuilder();

    <I extends AbstractPropertyValue<?>> I getProperty(String key);

    boolean hasProperty(String key);

    <I> void setProperty(String key, I value);

    @NotNull
    String getCurrentMove();

    //index of the current move's selected animation variant
    int getCurrentMoveAnimationIndex();

    String getPhase();

    void setPhase(String value);

    @NotNull
    RiftCreatureBuilder getCreatureType();

    @NotNull
    RiftCreatureConfig getCreatureConfig();

    void affectEntitiesWithinBounds(double size, @NotNull Consumer<EntityLivingBase> effect);

    int getLevel();

    RiftCreatureEnums.Nature getNature();

    int getAgeInTicks();

    float getHealth();

    float getMaxHealth();

    float getStamina();

    float getMaxStamina();

    boolean hasCollisionHitboxes();

    void addPotionEffect(PotionEffect potionEffect);

    boolean isRelatedToEntity(@Nullable Entity entity);

    void createOffenseHitboxesByTag(@NotNull String tagName, int lifetime);

    void createOffenseHitboxesByTag(@NotNull String tagName, int lifetime, @Nullable Consumer<Entity> entityHitEffect);

    @Nullable
    Vec3d getRiddenAimPosition();

    default void launchProjectile(@NotNull ProjectileBuilder projectileBuilder, float velocity, float inaccuracy) {
        this.launchProjectile(projectileBuilder, velocity, inaccuracy, 0f, 0f);
    }

    void launchProjectile(@NotNull ProjectileBuilder projectileBuilder, float velocity, float inaccuracy, float pitchOffset, float yawOffset);

    default void launchProjectile(@NotNull ProjectileBuilder projectileBuilder, @NotNull EntityLivingBase target, float velocity, float inaccuracy) {
        this.launchProjectile(projectileBuilder, target, velocity, inaccuracy, 0f, 0f);
    }

    void launchProjectile(@NotNull ProjectileBuilder projectileBuilder, @NotNull EntityLivingBase target, float velocity, float inaccuracy, float pitchOffset, float yawOffset);
}
