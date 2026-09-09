package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.BooleanPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.IntegerPropertyValue;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public class RiftCreatureBuilder extends AbstractCreatureBuilder<RiftCreatureBuilder> {
    private String creatureName;
    private final Map<String, CreaturePhaseBuilder> creaturePhaseBuilderMap = new HashMap<>();
    @Nullable
    private Map<String, AbstractPropertyValue<?>> propertyValueMap;
    @Nullable
    private Consumer<ICreature> updateEffect;
    private Map<String, Function<ICreature, Double>> hitboxTagDamageInfo;
    private float fleeSearchDistance;
    @Nullable
    private BiPredicate<ICreature, EntityLivingBase> fleePredicate;

    /**
     * Set the name of the species of the creature, is to be required
     * */
    public RiftCreatureBuilder setName(String name) {
        this.checkIfLocked();

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
     * Make the creature flee matching nearby entities while the predicate is satisfied.
     * The predicate can include conditions such as health and whether a herder is currently solitary.
     */
    public RiftCreatureBuilder setFleeBehavior(
            float searchDistance, @NotNull BiPredicate<ICreature, EntityLivingBase> fleePredicate
    ) {
        this.checkIfLocked();
        if (searchDistance <= 0f) throw new IllegalArgumentException("Flee search distance must be greater than 0!");

        this.fleeSearchDistance = searchDistance;
        this.fleePredicate = fleePredicate;
        return this;
    }

    public float getFleeSearchDistance() {
        return this.fleeSearchDistance;
    }

    @Nullable
    public BiPredicate<ICreature, EntityLivingBase> getFleePredicate() {
        return this.fleePredicate;
    }

    /**
     * A creature's "phase" implies change in appearance, usable moves, and stats
     * Creatures can change between phases depending on different things
     * The builder that is used to define the creature is treated as a phase called ""
     * */
    public RiftCreatureBuilder addPhase(String phaseName, CreaturePhaseBuilder phaseBuilder) {
        this.checkIfLocked();

        this.creaturePhaseBuilderMap.put(phaseName, phaseBuilder.setPhaseName(phaseName));
        return this;
    }

    public Map<String, CreaturePhaseBuilder> getPhaseBuilderMaps() {
        return this.creaturePhaseBuilderMap;
    }

    //-----for additional values to this creature. they do sync from server to client, but they do not persist.-----
    public RiftCreatureBuilder registerIntegerValue(@NotNull String name, int initVal) {
        this.checkIfLocked();

        if (this.propertyValueMap == null) this.propertyValueMap = new HashMap<>();
        this.propertyValueMap.put(name, new IntegerPropertyValue(name, initVal));
        return this;
    }

    public RiftCreatureBuilder registerBooleanValue(@NotNull String name, boolean initVal) {
        this.checkIfLocked();

        if (this.propertyValueMap == null) this.propertyValueMap = new HashMap<>();
        this.propertyValueMap.put(name, new BooleanPropertyValue(name, initVal));
        return this;
    }

    @Nullable
    public Map<String, AbstractPropertyValue<?>> getPropertyValueMap() {
        return this.propertyValueMap;
    }

    //-----additional values end here-----
    /**
     * Update effects are extra stuff that happens with the creature every tick
     * */
    public RiftCreatureBuilder registerOnUpdateEffect(@NotNull Consumer<ICreature> onUpdate) {
        this.checkIfLocked();

        this.updateEffect = onUpdate;
        return this;
    }

    @Nullable
    public Consumer<ICreature> getUpdateEffect() {
        return this.updateEffect;
    }

    /**
     * Set hitbox information on the creature
     * */
    public RiftCreatureBuilder setHitboxInformation() {
        this.checkIfLocked();

        this.hitboxTagDamageInfo = Map.of();
        return this;
    }

    public RiftCreatureBuilder setHitboxInformation(@NotNull Map<String, Function<ICreature, Double>> hitboxTagDamageInfo) {
        this.checkIfLocked();

        this.hitboxTagDamageInfo = hitboxTagDamageInfo;
        return this;
    }

    @Nullable
    public Map<String, Function<ICreature, Double>> getHitboxInformation() {
        return this.hitboxTagDamageInfo;
    }
}
