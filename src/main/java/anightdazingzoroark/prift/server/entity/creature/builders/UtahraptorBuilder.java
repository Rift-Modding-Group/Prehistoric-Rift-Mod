package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.*;

import java.util.List;
import java.util.Map;

public class UtahraptorBuilder extends RiftCreatureBuilder {
    public UtahraptorBuilder() {
        this.setName("utahraptor")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                .setCreatureElement(Element.STEEL)
                .setTributeItemPartName("claw")
                .setStats(2, 4, 2, 8, 4)
                .setScaleRangeForAge(0.3f, 1f)
                .setSpawnEggColors(5855577, 10439936)
                .setMainHitboxSize(1.25f, 1.5f)
                .setMaxFallHeight(8)
                .setDaysUntilAdult(3)
                .setHitboxInformation(Map.of(
                        "head", creature -> 1.5D,
                        "tail", creature -> 0.5D
                ))
                .setCanBeKnockedBack()
                .setNavigation(new CreatureNavigationBuilder().setCanWalk()
                        .setCanLeap(4D, 0, 16D)
                        .setCanClimb()
                )
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTameFactor(0.75f)
                        .setInventorySize(9)
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.TRANQ_THEN_FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.FAST)
                        .setControllerRideLocator("controllerPos")
                )
                .setIsHerder(6)
                .setFleeBehavior(
                        16f,
                        (creature, threat) -> {
                            return creature.getHealth() <= creature.getMaxHealth() * 0.15f && !creature.isInHerd()
                                    && (creature.entityInTargetGroup(threat, "human")
                                    || creature.entityInTargetGroup(threat, "carnivoreCreature"));
                        }
                )
                .addBlockBreakLevel("shovel", 0)
                //---moves---
                .setMoveCategorySelector((creature, target) -> {
                    if (target == null || !target.isEntityAlive()) return new MoveRuleBuilder.MoveCategory[0];
                    return new MoveRuleBuilder.MoveCategory[]{MoveRuleBuilder.MoveCategory.MELEE, MoveRuleBuilder.MoveCategory.RANGED};
                })
                .setMaximumMoveSlots(3)
                .setSpawnMoves("scratch", "pack_call", "feather_dart")
                .setLevelupMoves(Map.of(
                        1, List.of("metal_claw", "kick"),
                        4, List.of("backstab"),
                        7, List.of("hyper_cutter"),
                        10, List.of("ally_call")
                ))
                .setMoveAnimations("scratch", "scratch_1", "scratch_2")
                .setMoveAnimations("pack_call", "pack_call")
                .setMoveAnimations("feather_dart", "feather_dart")
                .setMoveAnimations("metal_claw", "scratch_1", "scratch_2")
                .setMoveAnimations("backstab", "scratch_1", "scratch_2")
                .setMoveAnimations("kick", "kick")
                .setMoveAnimations("hyper_cutter", "scratch_1", "scratch_2")
                .setMoveAnimations("ally_call", "pack_call")
                .setMoveSelector(new CreatureMoveSelectorBuilder()
                        .setCanLeapToAttack(6D, 12D, true)
                )
                //---targeting---
                .addDefaultTargetWhitelistEntry("animal")
                .addDefaultTargetWhitelistEntry("human")
                .addDefaultTargetWhitelistEntry("herbivoreCreature")
                .addDefaultFoodItemWhitelistEntry("meat")
                .setRetaliateWhenAttacked()
                .setRememberPlayerAttacker();
    }
}
