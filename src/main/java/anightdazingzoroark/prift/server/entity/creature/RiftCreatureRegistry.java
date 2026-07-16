package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.builder.CreaturePhaseBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.Element;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveCommon;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.CreatureMoveSelector;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.RiftCreatureEnums;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.MoveRuleBuilder;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRaySphereImpactShape;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayConeMotionShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;

//this registers creatures
public class RiftCreatureRegistry {
    public static final String DEFAULT_CREATURE = "tyrannosaurus";

    //all creatures are stored here
    public static final HashMap<String, RiftCreatureBuilder> creatureBuilderMap = new HashMap<>();
    private static final HashMap<String, Class<? extends RiftCreature>> creatureRegistryClassMap = new HashMap<>();

    public static RiftCreatureBuilder getCreatureBuilder(String name) {
        return creatureBuilderMap.get(name);
    }

    public static Class<? extends RiftCreature> getCreatureRegistryClass(String name) {
        return creatureRegistryClassMap.get(name);
    }

    public static RiftCreature createCreature(World world, String name) {
        String resolvedName = creatureBuilderMap.containsKey(name) ? name : DEFAULT_CREATURE;
        Class<? extends RiftCreature> registryClass = getCreatureRegistryClass(resolvedName);
        if (registryClass == RiftCreatureHitboxed.class) return new RiftCreatureHitboxed(world, resolvedName);
        return new RiftCreature(world, resolvedName);
    }

    public static void registerCreatureType(String name, RiftCreatureBuilder builder) {
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
        //-----set registry class-----
        Class<? extends RiftCreature> registryClass = builder.getHitboxInformation() != null ? RiftCreatureHitboxed.class : RiftCreature.class;

        //-----finalize creature info and lock them-----
        builder.setName(name);
        builder.lock();
        creatureBuilderMap.put(name, builder);
        creatureRegistryClassMap.put(name, registryClass);
    }

    public static void createCreatures() {
        registerCreatureType(
                "tyrannosaurus",
                new RiftCreatureBuilder()
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                        .setStats(9, 7, 5, 7, 3)
                        .setScaleRangeForAge(0.5f, 3.25f)
                        .setSpawnEggColors(3670016, 2428687)
                        .setMainHitboxSize(3.25f, 4f)
                        .setDaysUntilAdult(7)
                        .setHostileToHumans()
                        .setRetaliateWhenAttacked()
                        .setHitboxInformation()
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
                                        /*.setImpactCreationSpeed(0.25D)*/,
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
                                                0.1D,
                                                0.35D,
                                                0.25D,
                                                3,
                                                12,
                                                false,
                                                false
                                        ))
                                        .setMotionSpeed(1D)
                                        .setMaxMotionDistance(16D),
                                (creature, rayOrigin, rayHitResult) -> {
                                    for (Entity hitEntity : rayHitResult.hitEntities()) {
                                        if (!(hitEntity instanceof EntityLivingBase hitEntityLivingBase)) continue;
                                        creature.attackEntityAsMob(hitEntityLivingBase);
                                    }
                                }
                        )
                        //---moves---
                        .addMove("bite", CreatureMoveCommon.standardMeleeMove.copy()
                                .setBasePower(50)
                                .setAnimNames("bite")
                        )
                        .addMove("stomp", new CreatureMoveBuilder()
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
                                .setBasePower(20)
                                .setRequireFindTargetToUse()
                                .setElemental(Element.SONIC, 0)
                                .setOnMoveHitEffect(creature -> {
                                    if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                                    RiftLibRayHelper.createRay(rayCreator, "roarRay", "roarOrigin");
                                })
                                .setOnMoveEndEffect(creature -> {
                                    if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                                    RiftLibRayHelper.killRay(rayCreator, "roarRay");
                                })
                                .setAnimNames("power_roar")
                                .setCooldown(1200)
                        )
                        .addMove("flamethrower", new CreatureMoveBuilder()
                                .setBasePower(90)
                                .setRequireFindTargetToUse()
                                .setElemental(Element.FIRE, 0)
                                .setOnMoveBeginEffect((creature, target) -> {
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
                                    creature.getAnimationData().setVariable("flamethrower_head_bend", Math.clamp(angle, -35, 35));
                                })
                                .setOnMoveHitEffect(creature -> {
                                    if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                                    RiftLibRayHelper.createRay(rayCreator, "flamethrowerRay", "flameLocator");
                                })
                                .setOnMoveEndEffect(creature -> {
                                    if (!(creature instanceof IRayCreator<?> rayCreator)) return;
                                    RiftLibRayHelper.killRay(rayCreator, "flamethrowerRay");
                                    creature.getAnimationData().setVariable("flamethrower_head_bend", 0);
                                })
                                .setAnimNames("flamethrower")
                                .setCooldown(1200)
                        )
                        //---attack ai---
                        .setMoveSelector(new CreatureMoveSelector()
                                .setMoveRule(
                                        new MoveRuleBuilder("bite")
                                                .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                                .setDetectionRule(new CreatureMoveSelector.BoundingBoxDetectionRule("jawHitZone"))
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("stomp")
                                                .setPriorityPredicate((creature, target) -> {
                                                    return target != null
                                                            && target.isEntityAlive()
                                                            && creature.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), "stompHitZone") ?
                                                            0 : -1;
                                                })
                                                .setDetectionRule(new CreatureMoveSelector.BoundingBoxDetectionRule("stompHitZone"))
                                                .setDontPathToTarget()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("power_roar")
                                                .setDetectionRule(new CreatureMoveSelector.DistanceFromUserDetectionRule(12D))
                                                .setUseWhenFrustrated()
                                                .setDontPathToTarget()
                                )
                                .setMoveRule(
                                        new MoveRuleBuilder("flamethrower")
                                                .setPriorityPredicate((creature, target) -> target != null ? 0 : -1)
                                                .setDetectionRule(new CreatureMoveSelector.DistanceFromUserDetectionRule(4D, 16D))
                                                .setUseWhenFrustrated()
                                )
                                .setCanSprintToAttack()
                        )
        );
        /*
        registerCreatureType(
                "stegosaurus",
                RiftCreature.class,
                new RiftCreatureBuilder()
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setStats(5.5, 5, 2, 4, 2)
                        .setScaleRangeForAge(0.3f, 2.125f)
                        .setSpawnEggColors(1731840, 16743424)
                        .setMainHitboxSize(2.125f, 2.5f)
                        .setDaysUntilAdult(3)
                        .setIsHerder()
                        .setRetaliateWhenAttacked(true)
                        //.setCanSprintToAttack()
                        .setMoves(
                                new CreatureMoveStorage.MoveHolder(CreatureMoveNew.THAGOMIZE, "tail_attack")
                        )
                        .setInitMainUsableMoves(CreatureMoveNew.THAGOMIZE)
        );
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
    }
}
