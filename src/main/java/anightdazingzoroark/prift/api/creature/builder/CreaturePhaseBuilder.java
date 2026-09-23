package anightdazingzoroark.prift.api.creature.builder;

public class CreaturePhaseBuilder extends AbstractCreatureBuilder<CreaturePhaseBuilder> {
    private String phaseName;

    public CreaturePhaseBuilder setPhaseName(String name) {
        this.phaseName = name;
        return this;
    }

    public String getPhaseName() {
        return this.phaseName;
    }
}
