package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.CreatureDomesticationBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DistanceFromUserDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveCommon;
import net.minecraft.entity.EntityLivingBase;

public class DodoBuilder extends RiftCreatureBuilder {
    public DodoBuilder() {
        DistanceFromUserDetectionRule peckRange = new DistanceFromUserDetectionRule(2D);

        this.setName("dodo")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.BIRD)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                .setCreatureElement(Element.NEUTRAL)
                .setTributeItemPartName("beak")
                .setStats(0.5D, 1, 1, 1, 2)
                .setScaleRangeForAge(0.5f, 0.75f)
                .setSpawnEggColors(0x777575, 0x5E5C5C)
                .setMainHitboxSize(0.75f, 0.75f)
                .setDaysUntilAdult(3)
                .setNavigation(new CreatureNavigationBuilder().setCanWalk())
                .setDomestication(new CreatureDomesticationBuilder()
                        .setTamingMethod(RiftCreatureEnums.TamingMethod.FEED)
                        .setLevelupRate(RiftCreatureEnums.LevelupRate.NORMAL)
                        .setInventorySize(9)
                )
                .setFallSlowly()
                .setCanBeKnockedBack()
                .setIsHerder(8)
                .setPanicOnHit()
                //---moves---
                .addMove("peck", CreatureMoveCommon.standardMeleeMove.copy()
                        .setBasePower(40)
                        .setUseLookBasedFacing()
                        .setAnimNames("peck")
                        .setOnMoveHitEffect(creature -> {
                            EntityLivingBase target = creature.getAttackTarget();
                            if (!creature.getEntityWorld().isRemote && target != null && target.isEntityAlive()
                                    && peckRange.targetWithinRange(creature, target)
                            ) {
                                creature.attackEntityAsMob(target);
                            }
                        })
                )
                //---attack ai---
                .setMoveSelector(new CreatureMoveSelectorBuilder()
                        .setMoveRule(
                                new MoveRuleBuilder("peck")
                                        .setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                                        .addDetectionRule(peckRange)
                        )
                )
                //---food---
                .addDefaultFoodItemWhitelistEntry("seed")
                .addDefaultFoodItemWhitelistEntry("fruit");
    }
}