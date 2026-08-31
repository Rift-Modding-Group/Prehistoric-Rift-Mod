package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveCommon;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRaySphereImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayConeMotionShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//this registers creatures
public class RiftCreatureRegistry {
    public static final String DEFAULT_CREATURE = "tyrannosaurus";

    //all creatures are stored here
    private static final LinkedHashMap<String, RiftCreatureBuilder> creatureBuilderMap = new LinkedHashMap<>();
    private static final LinkedHashMap<String, RiftCreatureBuilder> pendingAddonCreatures = new LinkedHashMap<>();
    private static boolean registeringBuiltInCreatures;
    private static boolean builtInCreaturesRegistered;
    private static boolean registrationFinished;

    public static RiftCreatureBuilder getCreatureBuilder(String name) {
        return creatureBuilderMap.get(name);
    }

    public static boolean hasCreatureBuilder(String name) {
        return creatureBuilderMap.containsKey(name);
    }

    public static List<String> getCreatureNames() {
        return List.copyOf(creatureBuilderMap.keySet());
    }

    public static Map<String, RiftCreatureBuilder> getCreatureBuilders() {
        return Collections.unmodifiableMap(creatureBuilderMap);
    }

    public static boolean creatureUsesHitboxes(String name) {
        RiftCreatureBuilder builder = creatureBuilderMap.get(name);
        return builder != null && builder.getHitboxInformation() != null;
    }

    public static RiftCreature createCreature(World world, String name) {
        String resolvedName = creatureBuilderMap.containsKey(name) ? name : DEFAULT_CREATURE;
        if (creatureUsesHitboxes(resolvedName)) return new RiftCreatureHitboxed(world, resolvedName);
        return new RiftCreature(world, resolvedName);
    }

    public static void registerCreatureType(String name, RiftCreatureBuilder builder) {
        if (registrationFinished) {
            RiftInitialize.logger.warn("Creature type {} was registered after creature registration finished!", name);
            return;
        }
        if (!builtInCreaturesRegistered && !registeringBuiltInCreatures) {
            if (pendingAddonCreatures.putIfAbsent(name, builder) != null) {
                RiftInitialize.logger.warn("Builder for addon creature type {} already exists!", name);
            }
            return;
        }

        //-----verify creature builder first-----
        if (creatureBuilderMap.containsKey(name)) {
            RiftInitialize.logger.warn("Builder for creature type {} already exists!", name);
            return;
        }
        if (!builder.isValid()) {
            RiftInitialize.logger.warn("Builder for creature type {} is invalid!", name);
            return;
        }
        //-----verify creature move builders next-----
        for (ImmutablePair<String, CreatureMoveBuilder> moveEntry : builder.getMoves()) {
            if (!moveEntry.getValue().isValid()) {
                RiftInitialize.logger.warn("Move {} for creature type {} in phase '{}' is invalid!", moveEntry.getKey(), name, moveEntry.getKey());
                return;
            }
        }
        for (Map.Entry<String, CreaturePhaseBuilder> phaseEntry : builder.getPhaseBuilderMaps().entrySet()) {
            for (ImmutablePair<String, CreatureMoveBuilder> moveEntry: phaseEntry.getValue().getMoves()) {
                if (!moveEntry.getValue().isValid()) {
                    RiftInitialize.logger.warn("Move {} for creature type {} in phase '{}' is invalid!", moveEntry.getKey(), name, moveEntry.getKey());
                    return;
                }
            }
        }
        //-----finalize creature info and lock them-----
        builder.setName(name);
        builder.lock();
        creatureBuilderMap.put(name, builder);
    }

    //default creatures
    public static void createCreatures() {
        if (builtInCreaturesRegistered || registeringBuiltInCreatures) return;
        registeringBuiltInCreatures = true;

        registerCreatureType(
                "tyrannosaurus",
                new RiftCreatureBuilder()
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                        .setTributeItemPartName("arm")
                        .setStats(9, 7, 5, 7, 3)
                        .setScaleRangeForAge(0.5f, 3.25f)
                        .setSpawnEggColors(3670016, 2428687)
                        .setMainHitboxSize(3.25f, 4f)
                        .setMaxFallHeight(4)
                        .setFallCreatesImpact()
                        .setDaysUntilAdult(7)
                        .setRetaliateWhenAttacked()
                        .setCanRetreat()
                        .setHitboxInformation()
                        .setNavigation(new CreatureNavigationBuilder().setCanWalk()
                                .setCanLeap(4D, 0, 12D)
                        )
                        .addBlockBreakLevel("pickaxe", 2)
                        .addBlockBreakLevel("shovel", 2)
                        .addBlockBreakLevel("axe", 2)
                        //---rays---
                        .addUsableRay(
                                "footStompRay",
                                new RiftLibRayBuilder()
                                        .setImpactOnly()
                                        .setImpactShape(() -> new RiftLibRayEllipsoidImpactShape(1D, 0.2D, 1D).topOnly())
                                        .setMaxMotionDistance(5D)
                                        .setOnlyOneSegment()
                                        .setMotionSpeed(1.5D),
                                (creature, rayOrigin, rayHitResult) -> {
                                    for (Entity hitEntity : rayHitResult.hitEntities()) {
                                        if (!(hitEntity instanceof EntityLivingBase hitEntityLivingBase)) continue;
                                        creature.attackEntityAsMob(hitEntityLivingBase);
                                    }
                                }
                        )
                        .addUsableRay(
                                "roarRay",
                                new RiftLibRayBuilder()
                                        .setImpactOnly()
                                        .setImpactShape(() -> new RiftLibRaySphereImpactShape().topOnly())
                                        .setMaxMotionDistance(12D)
                                        .setMotionSpeed(1.5D)
                                        .setBlockBreakCheck((rayCreator, blockPos) -> {
                                            IBlockState blockState = rayCreator.getRayCreator().world.getBlockState(blockPos);
                                            float hardness = blockState.getBlockHardness(rayCreator.getRayCreator().world, blockPos);
                                            return hardness <= 1f && hardness >= 0f;
                                        })
                                        .setImpactCreationInterval(5),
                                (creature, rayOrigin, rayHitResult) -> {
                                    for (Entity hitEntity : rayHitResult.hitEntities()) {
                                        if (!(hitEntity instanceof EntityLivingBase hitEntityLivingBase)) continue;
                                        creature.attackEntityAsMob(hitEntityLivingBase);
                                    }
                                }
                        )
                        .addUsableRay(
                                "flamethrowerRay",
                                new RiftLibRayBuilder()
                                        .setMotionOnly()
                                        .setMovementShape(() -> new RiftLibRayConeMotionShape(
                                                0.1D, 0.35D, 0.25D,
                                                3, 12, false, false
                                        ))
                                        .setMotionSpeed(1D)
                                        .setMaxMotionDistance(16D)
                                        .setBlockBreakCheck(((rayCreator, pos) -> {
                                            World world = rayCreator.getRayCreator().world;
                                            IBlockState blockState = world.getBlockState(pos);
                                            return blockState.getBlock().isFlammable(world, pos, EnumFacing.UP);
                                        })),
                                (creature, rayOrigin, rayHitResult) -> {
                                    for (Entity hitEntity : rayHitResult.hitEntities()) {
                                        if (!(hitEntity instanceof EntityLivingBase hitEntityLivingBase)) continue;
                                        creature.attackEntityAsMob(hitEntityLivingBase);
                                    }
                                    for (BlockPos hitBlockPos : rayHitResult.hitBlockPositions()) {
                                        //allow for snow
                                        World world = creature.getEntityWorld();
                                        if (world.getBlockState(hitBlockPos).getBlock() == Blocks.SNOW_LAYER
                                                && Blocks.FIRE.canPlaceBlockAt(world, hitBlockPos)
                                                && creature.getRNG().nextInt(4) == 0) {
                                            world.setBlockState(hitBlockPos, Blocks.FIRE.getDefaultState(), 3);
                                        }
                                        //allow for well almost anything else
                                        else if (!world.isAirBlock(hitBlockPos)
                                                && world.isAirBlock(hitBlockPos.up())
                                                && Blocks.FIRE.canPlaceBlockAt(world, hitBlockPos.up())
                                                && creature.getRNG().nextInt(4) == 0) {
                                            world.setBlockState(hitBlockPos.up(), Blocks.FIRE.getDefaultState(), 3);
                                        }
                                    }
                                }
                        )
                        //---moves---
                        .addMove("bite", CreatureMoveCommon.standardMeleeMove.copy()
                                .setBasePower(50)
                                .setAnimNames("bite")
                        )
                        .addMove("stomp", new CreatureMoveBuilder()
                                .setStaminaCost(0.02f)
                                .setBasePower(30)
                                .setRequireFindTargetToUse()
                                .setPhysical()
                                .setOnMoveHitEffect(creature -> {
                                    if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                                    RiftLibRayHelper.createRay(rayCreator, "footStompRay", "stompOrigin");
                                })
                                .setAnimNames("stomp")
                        )
                        .addMove("power_roar", new CreatureMoveBuilder()
                                .setStaminaCost(0.15f)
                                .setBasePower(20)
                                .setRequireFindTargetToUse()
                                .setElemental(Element.SONIC, 0)
                                .setOnMoveHitEffect(creature -> {
                                    if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                                    RiftLibRayHelper.createRay(rayCreator, "roarRay", "centerPoint");
                                })
                                .setOnMoveEndEffect(creature -> {
                                    if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                                    RiftLibRayHelper.killRay(rayCreator, "roarRay");
                                })
                                .setAnimNames("power_roar")
                                .setCooldown(1200)
                        )
                        .addMove("flamethrower", new CreatureMoveBuilder()
                                .setStaminaCost(0.08f)
                                .setStaminaDrainPerSecond(0.0125f)
                                .setBasePower(90)
                                .setRequireFindTargetToUse()
                                .setElemental(Element.FIRE, 0)
                                .setWhileMoveUseEffect((creature, target) -> {
                                    if (target == null || !target.isEntityAlive()) return;

                                    //get distance between the locator and the target
                                    Vec3d fireDistVec = creature.getLocatorWorldPos("fireDistPoint");
                                    Vec3d targetCenter = target.getPositionVector();
                                    double distToTarget = fireDistVec.distanceTo(targetCenter);
                                    if (distToTarget <= 1E-4D) return;

                                    //convert into angle using trig magic
                                    double verticalDist = (targetCenter.y + target.height / 2D) - fireDistVec.y;
                                    double angle = Math.toDegrees(Math.asin(Math.clamp(verticalDist / distToTarget, -1D, 1D)));

                                    //now set variable
                                    creature.getAnimationData().setVariable("flamethrower_head_bend", Math.clamp(angle, -60, 35));
                                })
                                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                                        .setChargeUpWhileUse(true)
                                        .setMaxChargeUp(300)
                                        .setPrereleaseEndEffect(creature -> {
                                            RiftLibRayHelper.createRay(creature.asRayCreator(), "flamethrowerRay", "flameLocator");
                                        })
                                        .setReleaseEndEffect(creature -> {
                                            RiftLibRayHelper.killRay(creature.asRayCreator(), "flamethrowerRay");
                                        })
                                )
                                .setOnMoveEndEffect(creature -> {
                                    creature.getAnimationData().setVariable("flamethrower_head_bend", 0);
                                })
                        )
                        //---attack ai---
                        .setMoveSelector(new CreatureMoveSelectorBuilder()
                                .setMoveRule(
                                        new MoveRuleBuilder("bite")
                                                .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                                .setDetectionRule(new CreatureMoveSelectorBuilder.BoundingBoxDetectionRule("jawHitZone"))
                                                .setUseBlockBreak()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("stomp")
                                                .setPriorityPredicate((creature, target) -> {
                                                    return (target != null && target.isEntityAlive() && !creature.bodyTouchingLiquid()
                                                            && creature.isOnGround()
                                                            && creature.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), "stompHitZone")) ?
                                                            0 : -1;
                                                })
                                                .setDetectionRule(new CreatureMoveSelectorBuilder.BoundingBoxDetectionRule("stompHitZone"))
                                                .setDontPathToTarget()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("power_roar")
                                                .setDetectionRule(new CreatureMoveSelectorBuilder.DistanceFromUserDetectionRule(12D))
                                                .setUseWhenFrustrated()
                                                .setDontPathToTarget()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("flamethrower")
                                                .setPriorityPredicate((creature, target) -> {
                                                    return (target != null && target.isEntityAlive() && (creature.atRageThreshold() || creature.isUnableToPathToTarget())) ? 0 : -1;
                                                })
                                                .setDetectionRule(new CreatureMoveSelectorBuilder.DistanceFromUserDetectionRule(4D, 16D))
                                                .setUseWhenFrustrated()
                                )
                                .setCanSprintToAttack(1, 8D, 16D)
                                .setCanLeapToAttack(1, 6D, 12D, false)
                        )
                        //---targeting---
                        .addDefaultTargetWhitelistEntry("animal")
                        .addDefaultTargetWhitelistEntry("herbivoreCreature")
                        .setRetaliateWhenAttacked()
                        .setRememberPlayerAttacker()
        );
        registerCreatureType(
                "stegosaurus",
                new RiftCreatureBuilder()
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setTributeItemPartName("plate")
                        .setStats(5.5, 5, 2, 4, 2)
                        .setScaleRangeForAge(0.3f, 2.125f)
                        .setSpawnEggColors(1731840, 16743424)
                        .setMainHitboxSize(2.125f, 2.5f)
                        .setMaxFallHeight(4)
                        .setFallCreatesImpact()
                        .setDaysUntilAdult(3)
                        .setHitboxInformation()
                        .setIsHerder(8)
                        .setCanRetreat()
                        .addBlockBreakLevel("axe", 2)
                        //---moves---
                        .addMove("tail_stab", CreatureMoveCommon.standardMeleeMove.copy()
                                .setBasePower(50)
                                .setAnimNames("tail_stab")
                        )
                        .addMove("tail_sweep", CreatureMoveCommon.standardMeleeMove.copy()
                                .setStaminaCost(0.02f)
                                .setBasePower(30)
                                .setAnimNames("tail_sweep")
                        )
                        .addMove("plate_fling", new CreatureMoveBuilder()
                                .setStaminaCost(0.12f)
                                .setPhysical()
                                .setBasePower(30)
                                .setRequireFindTargetToUse()
                                .setOnMoveHitEffect(creature -> {
                                    EntityLivingBase target = creature.getAttackTarget();
                                    if (target == null || !target.isEntityAlive()) return;

                                    creature.launchProjectile(
                                            new ProjectileBuilder().setName("thrown_stegosaurus_plate").setRotateAlongPitch(),
                                            target, 3f, 0f
                                    );
                                })
                                .setAnimNames("plate_fling")
                        )
                        //---attack ai---
                        .setMoveSelector(new CreatureMoveSelectorBuilder()
                                .setMoveRule(
                                        new MoveRuleBuilder("tail_stab")
                                                .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                                .setDetectionRule(new CreatureMoveSelectorBuilder.BoundingBoxDetectionRule("tailHitZone"))
                                                .setUseBlockBreak()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("tail_sweep")
                                                .setPriorityPredicate((creature, target) -> {
                                                    return (target != null && target.isEntityAlive()
                                                            && creature.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), "spinHitZone")) ?
                                                            0 : -1;
                                                })
                                                .setDetectionRule(new CreatureMoveSelectorBuilder.BoundingBoxDetectionRule("spinHitZone"))
                                                .setDontPathToTarget()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("plate_fling")
                                                .setPriorityPredicate((creature, target) -> {
                                                    return (target != null && target.isEntityAlive()) ? 0 : -1;
                                                })
                                                .setDetectionRule(new CreatureMoveSelectorBuilder.DistanceFromUserDetectionRule(8D, 16D))
                                )
                        )
                        //---targeting---
                        .setRetaliateWhenAttacked()
                        .setRememberPlayerAttacker()
        );
        /*
        registerCreatureType(
                "dodo",
                RiftCreature.class,
                new RiftCreatureBuilder().setCreatureCategory(RiftCreatureEnums.CreatureCategory.BIRD)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setMovementOptions(RiftCreatureEnums.Movement.SLOW_FALL)
                        .setStats(20, 10, 5, 30)
                        .setScaleRangeForAge(0.5f, 0.75f)
                        .setSpawnEggColors(7828853, 6184028)
                        .setIsHerder()
        );
        registerCreatureType(
                "triceratops",
                RiftCreature.class,
                new RiftCreatureBuilder().setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setStats(120, 80, 30, 120)
                        .setScaleRangeForAge(0.3f, 1.75f)
                        .setSpawnEggColors(935177, 3631923)
                        .setDaysUntilAdult(3)
                        .setIsHerder()
                        .setRetaliateWhenAttacked(true)
        );
        registerCreatureType(
                "utahraptor",
                RiftCreature.class,
                new RiftCreatureBuilder().setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                        .setStats(50, 40, 20, 60)
                        .setScaleRangeForAge(0.3f, 1f)
                        .setSpawnEggColors(5855577, 10439936)
                        .setDaysUntilAdult(3)
                        .setIsHerder()
                        .setHostileToHumans()
                        .setRetaliateWhenAttacked(true)
        );
         */

        registeringBuiltInCreatures = false;
        builtInCreaturesRegistered = true;
        for (Map.Entry<String, RiftCreatureBuilder> entry : pendingAddonCreatures.entrySet()) {
            registerCreatureType(entry.getKey(), entry.getValue());
        }
        pendingAddonCreatures.clear();
    }

    /**
     * Closes the registration window after addon registration listeners have run.
     */
    public static void finishCreatureRegistration() {
        if (!builtInCreaturesRegistered) {
            throw new IllegalStateException("Built-in creatures must be registered before addon registration can finish");
        }
        registrationFinished = true;
    }
}
