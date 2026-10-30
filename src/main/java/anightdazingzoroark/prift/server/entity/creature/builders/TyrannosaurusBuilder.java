package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.*;

import java.util.List;
import java.util.Map;

public class TyrannosaurusBuilder extends RiftCreatureBuilder {
    public TyrannosaurusBuilder() {
        this.setName("tyrannosaurus")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                .setCreatureElement(Element.FIRE)
                .setTributeItemPartName("arm")
                .setStats(9, 7, 5, 7, 3)
                .setScaleRangeForAge(0.5f, 3.25f)
                .setSpawnEggColors(3670016, 2428687)
                .setMainHitboxSize(3.25f, 4f)
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(7)
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTameFactor(0.33f)
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.SLOW)
                        .setControllerRideLocator("controllerPos")
                )
                .setRetaliateWhenAttacked()
                .setFleeBehavior(
                        16f,
                        (creature, threat) -> {
                            return creature.getHealth() <= creature.getMaxHealth() * 0.15f
                                    && creature.entityInTargetGroup(threat, "human");
                        }
                )
                .setHitboxInformation(Map.of(
                        "leg", creature -> 0.25D,
                        "body", creature -> 0.75D,
                        "head", creature -> 1.25D,
                        "tail", creature -> 0.5D
                ))
                .setNavigation(new CreatureNavigationBuilder().setCanWalk()
                        .setCanLeap(4D, 0, 12D)
                )
                .addBlockBreakLevel("pickaxe", 2)
                .addBlockBreakLevel("shovel", 2)
                .addBlockBreakLevel("axe", 2)
                //---moves---
                .setMaximumMoveSlots(5)
                .setSpawnMoves("bite", "stomp", "fire_fang", "power_roar", "flamethrower")
                .setLevelupMoves(Map.of(
                        1, List.of("fireball", "thunder_fang"),
                        3, List.of("toxic_haze", "flame_spit"),
                        5, List.of("willowisp", "flame_charge"),
                        6, List.of("tremor")
                ))
                .setMoveAnimations("bite", "bite")
                .setMoveAnimations("fire_fang", "bite")
                .setMoveAnimations("thunder_fang", "bite")
                .setMoveAnimations("stomp", "stomp")
                .setMoveAnimations("flame_spit", "flame_spit")
                .setMoveAnimations("willowisp", "flame_spit")
                .setMoveAnimations("power_roar", "power_roar")
                .setMoveAnimations("flamethrower", "flamethrower")
                .setMoveAnimations("fireball", "flame_spit")
                .setMoveAnimations("toxic_haze", "flamethrower")
                .setMoveAnimations("tremor", "stomp")
                .setMoveAnimations("flame_charge", "charge")
                .setMoveSelector(new CreatureMoveSelectorBuilder()
                        .setCanSprintToAttack(1, 8D, 16D)
                        .setCanLeapToAttack(1, 6D, 12D, false)
                )
                //---targeting---
                .addDefaultTargetWhitelistEntry("animal")
                .addDefaultTargetWhitelistEntry("herbivoreCreature")
                .addDefaultFoodItemWhitelistEntry("meat")
                .setRetaliateWhenAttacked()
                .setRememberPlayerAttacker();
    }
}