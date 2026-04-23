package anightdazingzoroark.prift.server.entity.creaturenew.builder;

import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;

public class CreaturePhaseBuilder extends AbstractCreatureBuilder<CreaturePhaseBuilder> {
    private String phaseName;

    public CreaturePhaseBuilder(Class<? extends RiftCreatureNew> creatureClass) {
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
