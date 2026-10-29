package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.CreatureDomesticationBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;

public class DodoBuilder extends RiftCreatureBuilder {
    public DodoBuilder() {
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
                .setMaximumMoveSlots(1)
                .setSpawnMoves("peck")
                .setMoveAnimations("peck", "peck")
                //---food---
                .addDefaultFoodItemWhitelistEntry("seed")
                .addDefaultFoodItemWhitelistEntry("fruit");
    }
}
