package anightdazingzoroark.prift.server.entity.creaturenew.builder;

import anightdazingzoroark.prift.server.entity.creaturenew.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiftCreatureBuilder extends AbstractCreatureBuilder<RiftCreatureBuilder> {
    private String creatureName;
    private Map<String, CreaturePhaseBuilder> creaturePhaseBuilderMap;
    private CreatureMoveStorage.LearnableMoveHolder[] learnableMoves;
    private Map<String, List<String>> initMovesPerPhase;

    public RiftCreatureBuilder(Class<? extends RiftCreatureNew> creatureClass) {
        super(creatureClass);
    }

    /**
     * Set the name of the species of the creature, is to be required
     * */
    public RiftCreatureBuilder setName(String name) {
        this.creatureName = name;
        return this.getThis();
    }

    public String getName() {
        return this.creatureName;
    }

    public String getLocalizedName() {
        return I18n.format("entity."+this.creatureName+".name");
    }

    /**
     * A creature's "phase" implies change in appearance, usable moves, and stats
     * Creatures can change between phases depending on different things
     * The builder that is used to define the creature is treated as a phase called ""
     * */
    public RiftCreatureBuilder addPhase(String phaseName, CreaturePhaseBuilder phaseBuilder) {
        if (this.creaturePhaseBuilderMap == null) {
            this.creaturePhaseBuilderMap = new HashMap<>();
        }
        this.creaturePhaseBuilderMap.put(phaseName, phaseBuilder.setPhaseName(phaseName));
        return this;
    }

    public Map<String, CreaturePhaseBuilder> getPhaseBuilderMaps() {
        return this.creaturePhaseBuilderMap;
    }

    public boolean hasPhases() {
        return this.creaturePhaseBuilderMap != null;
    }

    /**
     * Define the moves that the creature can learn
     * */
    public RiftCreatureBuilder setLearnableMoves(CreatureMoveStorage.LearnableMoveHolder... learnableMoves) {
        this.learnableMoves = learnableMoves;
        return this;
    }

    public CreatureMoveStorage.LearnableMoveHolder[] getLearnableMoves() {
        return this.learnableMoves;
    }

    /**
     * Initialize initMovesPerPhase and set the usable move lists for the main phase
     * */
    public RiftCreatureBuilder setInitMainUsableMoves(String... mainMoves) {
        this.initMovesPerPhase = new HashMap<>();
        this.initMovesPerPhase.put("", Arrays.asList(mainMoves));
        return this;
    }

    /**
     * Set usable move lists for each creature phase
     * */
    public RiftCreatureBuilder setUsableMovesForPhase(String phase, String... phaseMoves) {
        if (this.initMovesPerPhase == null || phase.isEmpty()) return this;
        this.initMovesPerPhase.put(phase, Arrays.asList(phaseMoves));
        return this;
    }

    /**
     * Getter for init usable moves
     * */
    public Map<String, List<String>> getInitUsableMovesPerPhase() {
        return this.initMovesPerPhase;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && this.learnableMoves != null && this.initMovesPerPhase != null;
    }
}
