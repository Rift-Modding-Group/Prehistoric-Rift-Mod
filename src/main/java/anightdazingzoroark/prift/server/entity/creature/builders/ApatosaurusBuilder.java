package anightdazingzoroark.prift.server.entity.creature.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;

public class ApatosaurusBuilder extends RiftCreatureBuilder {
    public ApatosaurusBuilder() {
        this.setName("apatosaurus")
                .setCreatureCategory(RiftCreatureEnums.CreatureCategory.DINOSAUR)
                .setCreatureDiet(RiftCreatureEnums.CreatureDiet.HERBIVORE)
                .setCreatureElement(Element.GRASS, Element.WATER)
                .setTributeItemPartName("vertebrae")
                .setStats(9, 6, 6, 4, 1)
                .setScaleRangeForAge(0.3f, 1f)
                .setSpawnEggColors(5855577, 10439936)
                .setMainHitboxSize(1.25f, 1.5f)
                .setMaxFallHeight(4)
                .setFallCreatesImpact()
                .setDaysUntilAdult(7);
    }
}
