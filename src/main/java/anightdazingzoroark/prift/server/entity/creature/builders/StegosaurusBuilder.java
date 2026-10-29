package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.*;

import java.util.Map;

public class StegosaurusBuilder extends RiftCreatureBuilder {
    public StegosaurusBuilder() {
        this.setName("stegosaurus")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                .setCreatureElement(Element.GUARD)
                .setTributeItemPartName("plate")
                .setStats(5.5, 5, 2, 4, 2)
                .setScaleRangeForAge(0.3f, 2.125f)
                .setSpawnEggColors(1731840, 16743424)
                .setMainHitboxSize(2.125f, 2.5f)
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(3)
                .setNavigation(new CreatureNavigationBuilder().setCanWalk())
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTameFactor(0.75f)
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.NORMAL)
                        .setControllerRideLocator("controllerPos")
                )
                .setHitboxInformation(Map.of(
                        "leg", creature -> 0.25D,
                        "body", creature -> 0.75D,
                        "head", creature -> 1.25D,
                        "tail", creature -> 0.25D
                ))
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
                .registerIntegerValue("ChargeUpLevel", 0)
                //---moves---
                .setMaximumMoveSlots(4)
                .setSpawnMoves("tail_stab", "tail_slam", "thagomize", "plate_fling")
                .setMoveAnimations("tail_stab", "tail_stab")
                .setMoveAnimations("tail_slam", "tail_slam")
                .setMoveAnimations("thagomize", "thagomize")
                .setMoveAnimations("plate_fling", "plate_fling")
                //---targeting---
                .addDefaultFoodItemWhitelistEntry("fruit")
                .addDefaultFoodItemWhitelistEntry("vegetable")
                .addDefaultFoodItemWhitelistEntry("grass")
                .setRetaliateWhenAttacked()
                .setRememberPlayerAttacker();
    }

}
