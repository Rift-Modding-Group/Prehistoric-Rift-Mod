package anightdazingzoroark.prift.server.entity.creature.builder;

import anightdazingzoroark.prift.util.FixedSizeList;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;

public class RiftCreatureBuilder extends AbstractCreatureBuilder<RiftCreatureBuilder> {
    private String creatureName;
    private Map<String, CreaturePhaseBuilder> creaturePhaseBuilderMap;
    private final Map<String, FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>>> movesPerPhase = new HashMap<>();

    public RiftCreatureBuilder(Class<? extends RiftCreature> creatureClass) {
        super(creatureClass);
    }

    /**
     * Set the name of the species of the creature, is to be required
     * */
    public RiftCreatureBuilder setName(String name) {
        this.creatureName = name;
        return this;
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
        if (this.locked) return this;

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
     * Initialize move list for the main phase
     * */
    public RiftCreatureBuilder setMoves(FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>> mainMoves) {
        if (this.locked) return this;

        this.movesPerPhase.put("", mainMoves);
        return this;
    }

    /**
     * Set usable move lists for each creature phase
     * */
    public RiftCreatureBuilder setMovesForPhase(String phase, FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>> phaseMoves) {
        if (this.locked) return this;
        if (phase.isEmpty()) return this;

        this.movesPerPhase.put(phase, phaseMoves);
        return this;
    }

    /**
     * Getter for usable moves per each phase
     * */
    public Map<String, FixedSizeList<ImmutablePair<String, CreatureMoveBuilder>>> getMoves() {
        return this.movesPerPhase;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && !this.movesPerPhase.isEmpty();
    }
}
