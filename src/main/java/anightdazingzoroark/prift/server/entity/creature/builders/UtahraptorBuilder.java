package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;

public class UtahraptorBuilder extends RiftCreatureBuilder {
    public UtahraptorBuilder() {
        this.setName("utahraptor")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.CARNIVORE)
                .setCreatureElement(Element.STEEL)
                .setTributeItemPartName("claw")
                .setStats(2, 4, 2, 8, 4)
                .setScaleRangeForAge(0.3f, 1.75f)
                .setSpawnEggColors(5855577, 10439936)
                .setMainHitboxSize(2f, 2f)
                .setMaxFallHeight(8)
                .setDaysUntilAdult(3)
                .setNavigation(new CreatureNavigationBuilder().setCanWalk()
                        .setCanLeap(4D, 0, 16D)
                );
    }
}
