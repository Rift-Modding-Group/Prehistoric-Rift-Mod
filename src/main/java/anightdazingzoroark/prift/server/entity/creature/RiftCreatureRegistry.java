package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.util.FixedSizeList;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveCommon;
import anightdazingzoroark.prift.server.entity.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.server.entity.creature.info.RiftCreatureEnums;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//this registers creatures
public class RiftCreatureRegistry {
    public static final HashMap<String, RiftCreatureBuilder> creatureBuilderMap = new HashMap<>();

    public static RiftCreatureBuilder getCreatureBuilder(String name) {
        return creatureBuilderMap.get(name);
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
        for (Map.Entry<String, FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>>> moveEntry : builder.getMoves().entrySet()) {
            for (int index = 0; index < moveEntry.getValue().size(); index++) {
                ImmutablePair<String, CreatureMoveBuilder> movePair = moveEntry.getValue().get(index);
                if (movePair == null) continue;
                if (!movePair.getRight().isValid()) {
                    RiftInitialize.logger.warn("Move {} for creature type {} in phase '{}' is invalid!", movePair.getLeft(), name, moveEntry.getKey());
                    return;
                }
            }
        }
        //-----finalize creature info and lock them-----
        builder.setName(name);
        builder.lock();
        creatureBuilderMap.put(name, builder);
    }

    public static void createCreatures() {
        registerCreatureType(
                "tyrannosaurus",
                new RiftCreatureBuilder(Tyrannosaurus.class)
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                        .setStats(9, 7, 5, 7, 3)
                        .setScaleRangeForAge(0.5f, 3.25f)
                        .setSpawnEggColors(3670016, 2428687)
                        .setMainHitboxSize(3.25f, 4f)
                        .setDaysUntilAdult(7)
                        .setHostileToHumans()
                        .setRetaliateWhenAttacked()
                        .setPhysicalReach(5)
                        .setCanSprintToAttack()
                        .setMoves(new FixedSizeList.Builder<ImmutablePair<String, CreatureMoveBuilder>>(CreatureMoveStorage.usableMoveCount)
                                .put(new ImmutablePair<>("bite", CreatureMoveCommon.standardMeleeMove.copy()
                                        .setBasePower(50)
                                        .setAnimNames("bite")
                                ))
                                .put(new ImmutablePair<>("stomp", new CreatureMoveBuilder()
                                        .setBasePower(30)
                                        .setRequireFindTargetToUse()
                                        .setPhysical()
                                        .setUseCanStopMovement()
                                        .setCanUsePredicate(CreatureMoveCommon.generalMeleePredicate)
                                        .setOnMoveHitEffect(creature -> {
                                            AxisAlignedBB creatureAABB = creature.getEntityBoundingBox();
                                            AxisAlignedBB stompRangeAABB = new AxisAlignedBB(
                                                    creatureAABB.minX - creature.getCreatureType().getPhysicalReach(),
                                                    creatureAABB.minY,
                                                    creatureAABB.minZ - creature.getCreatureType().getPhysicalReach(),
                                                    creatureAABB.maxX + creature.getCreatureType().getPhysicalReach(),
                                                    creatureAABB.minY + 1,
                                                    creatureAABB.maxZ + creature.getCreatureType().getPhysicalReach()
                                            );

                                            List<EntityLivingBase> entitiesInStompRange = creature.world.getEntitiesWithinAABB(
                                                    EntityLivingBase.class, stompRangeAABB
                                            );
                                            for (EntityLivingBase entity : entitiesInStompRange) {
                                                if (creature.isRelatedToEntity(entity)) continue;
                                                if (entity.equals(creature)) continue;
                                                creature.attackEntityAsMob(entity);
                                            }
                                        })
                                        .setAnimNames("stomp")
                                ))
                                .build()
                        )
                        /*
                        .setMoves(
                                new CreatureMoveStorage.MoveHolder(CreatureMoveNew.BITE, "bite"),
                                new CreatureMoveStorage.MoveHolder(CreatureMoveNew.STOMP, "stomp")
                        )
                        .setInitMainUsableMoves(CreatureMoveNew.BITE, CreatureMoveNew.STOMP)
                         */
        );
        registerCreatureType(
                "stegosaurus",
                new RiftCreatureBuilder(Stegosaurus.class)
                        .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                        .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                        .setStats(5.5, 5, 2, 4, 2)
                        .setScaleRangeForAge(0.3f, 2.125f)
                        .setSpawnEggColors(1731840, 16743424)
                        .setMainHitboxSize(2.125f, 2.5f)
                        .setDaysUntilAdult(3)
                        .setIsHerder()
                        .setRetaliateWhenAttacked(true)
                        .setPhysicalReach(5)
                        //.setCanSprintToAttack()
                        /*
                        .setMoves(
                                new CreatureMoveStorage.MoveHolder(CreatureMoveNew.THAGOMIZE, "tail_attack")
                        )
                        .setInitMainUsableMoves(CreatureMoveNew.THAGOMIZE)
                         */
        );
        /*
        registerCreatureType(
                "dodo",
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
