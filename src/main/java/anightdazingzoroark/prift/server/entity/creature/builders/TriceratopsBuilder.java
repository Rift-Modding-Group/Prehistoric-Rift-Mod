package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.*;

import java.util.List;
import java.util.Map;

public class TriceratopsBuilder extends RiftCreatureBuilder {
    public TriceratopsBuilder() {
        this.setName("triceratops")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                .setCreatureElement(Element.GUARD)
                .setTributeItemPartName("horn")
                .setStats(6.5D, 4, 2, 6, 1)
                .setScaleRangeForAge(0.3f, 1.75f)
                .setSpawnEggColors(935177, 3631923)
                .setMainHitboxSize(2f, 2f)
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(5)
                .setHitboxInformation(Map.of("head", creature -> 0D))
                .setNavigation(new CreatureNavigationBuilder().setCanWalk()
                        .setCanLeap(4D, 0, 12D)
                )
                .setSprintBasePower(60)
                .setSprintCooldown(100)
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTameFactor(0.75f)
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.NORMAL)
                        .setControllerRideLocator("controllerPos")
                )
                .setIsHerder(8)
                .setFleeBehavior(
                        16f,
                        (creature, threat) -> {
                            return creature.getHealth() <= creature.getMaxHealth() * 0.15f && !creature.isInHerd()
                                    && (creature.entityInTargetGroup(threat, "human")
                                    || creature.entityInTargetGroup(threat, "carnivoreCreature"));
                        }
                )
                .addBlockBreakLevel("axe", 2)
                .addBlockBreakLevel("pickaxe", 0)
                .addBlockBreakLevel("shovel", 0)
                //---moves---
                .setMaximumMoveSlots(4)
                .setSpawnMoves("headbutt", "shield_bash", "charge", "belch")
                .setLevelupMoves(Map.of(
                        1, List.of("stomp"),
                        4, List.of("flame_charge", "volt_tackle", "slush_rush"),
                        6, List.of("tremor", "bide"),
                        8, List.of("heavy_slam", "drill_rush", "sludge_wave")
                ))
                .setMoveAnimations("headbutt", "headbutt")
                .setMoveAnimations("stomp", "stomp")
                .setMoveAnimations("charge", "charge")
                .setMoveAnimations("belch", "belch")
                .setMoveAnimations("shield_bash", "headbutt")
                .setMoveAnimations("tremor", "stomp")
                .setMoveAnimations("bide", "bide")
                .setMoveAnimations("flame_charge", "charge")
                .setMoveAnimations("volt_tackle", "charge")
                .setMoveAnimations("slush_rush", "charge")
                .setMoveAnimations("heavy_slam", "heavy_slam")
                .setMoveAnimations("drill_rush", "drill_rush")
                .setMoveAnimations("sludge_wave", "sludge_wave")
                .setMoveSelector(new CreatureMoveSelectorBuilder()
                        .setCanSprintToAttack(1, 2D, 16D)
                        .setCanLeapToAttack(1, 6D, 12D, false)
                )
                //---food---
                .addDefaultFoodItemWhitelistEntry("fruit")
                .addDefaultFoodItemWhitelistEntry("vegetable")
                .addDefaultFoodItemWhitelistEntry("grass")
                //---targeting---
                .setRetaliateWhenAttacked()
                .setRememberPlayerAttacker();
    }
}
