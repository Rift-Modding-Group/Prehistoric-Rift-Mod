package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.CreatureDomesticationBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;

import java.util.List;
import java.util.Map;

public class ApatosaurusBuilder extends RiftCreatureBuilder {
    public ApatosaurusBuilder() {
        this.setName("apatosaurus")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                .setCreatureElement(Element.GRASS, Element.WATER)
                .setTributeItemPartName("vertebrae")
                .setStats(9, 6, 6, 4, 1)
                .setScaleRangeForAge(0.35f, 2.25f)
                .setSpawnEggColors(3160621, 16748800)
                .setMainHitboxSize(4f, 3f)
                .setHitboxInformation()
                .setNavigation(new CreatureNavigationBuilder().setCanWalk())
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(7)
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTameFactor(0.33f)
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.VERY_SLOW)
                        .setControllerRideLocator("controllerPos")
                        .setPlatformSaddle("platformCenter", 9, 7, 11, 4, 1, 5)
                )
                .setRetaliateWhenAttacked()
                .setFleeBehavior(
                        16f,
                        (creature, threat) -> {
                            return creature.getHealth() <= creature.getMaxHealth() * 0.15f && !creature.isInHerd()
                                    && (creature.entityInTargetGroup(threat, "human")
                                    || creature.entityInTargetGroup(threat, "carnivoreCreature"));
                        }
                )
                .addBlockBreakLevel("pickaxe", 2)
                .addBlockBreakLevel("shovel", 2)
                .addBlockBreakLevel("axe", 2)
                //---moves---
                .setMoveCategorySelector((creature, target) -> {
                    if (target == null || !target.isEntityAlive()) return new MoveRuleBuilder.MoveCategory[0];
                    if (creature.isUnableToPathToTarget()) return new MoveRuleBuilder.MoveCategory[]{MoveRuleBuilder.MoveCategory.RANGED};
                    return new MoveRuleBuilder.MoveCategory[]{MoveRuleBuilder.MoveCategory.MELEE, MoveRuleBuilder.MoveCategory.RANGED};
                })
                .setMaximumMoveSlots(4)
                .setSpawnMoves("stomp", "tail_whip", "root_spike", "hydro_pump")
                .setMoveAnimations("stomp", "stomp")
                .setMoveAnimations("root_spike", "root_spike")
                .setMoveAnimations("hydro_pump", "hydro_pump")
                .setMoveAnimations("bide", "bide")
                .setMoveAnimations("tail_whip", "tail_whip")
                .setMoveAnimations("leaf_dart", "tail_whip")
                .setMoveAnimations("belch", "spit")
                .setMoveAnimations("vine_cage", "root_spike")
                .setMoveAnimations("acid_water", "spit")
                .setMoveAnimations("heavy_slam", "heavy_slam")
                .setMoveAnimations("seed_bomb", "spit")
                .setMoveAnimations("tremor", "stomp")
                .setMoveAnimations("bubble_bomb", "root_spike")
                .setMoveAnimations("sludge_wave", "hydro_pump")
                .setMoveAnimations("solar_beam", "hydro_pump")
                .setMoveAnimations("petal_storm", "root_spike")
                .setLevelupMoves(Map.of(
                        1, List.of("bide", "leaf_dart", "belch"),
                        3, List.of("vine_cage" , "acid_water"),
                        8, List.of("heavy_slam", "seed_bomb", "tremor", "bubble_bomb"),
                        10, List.of("solar_beam", "sludge_wave", "petal_storm")
                ))
                //---targeting---
                .addDefaultFoodItemWhitelistEntry("leaves")
                .addDefaultFoodItemWhitelistEntry("fruit")
                .setRetaliateWhenAttacked()
                .setRememberPlayerAttacker();
    }
}
