package anightdazingzoroark.prift.server.entity.creature.builder;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;

public class CreaturePhaseBuilder extends AbstractCreatureBuilder<CreaturePhaseBuilder> {
    private String phaseName;

    public CreaturePhaseBuilder(Class<? extends RiftCreature> creatureClass) {
        super(creatureClass);
    }

    public CreaturePhaseBuilder setPhaseName(String name) {
        this.phaseName = name;
        return this;
    }

    public String getPhaseName() {
        return this.phaseName;
    }
}
