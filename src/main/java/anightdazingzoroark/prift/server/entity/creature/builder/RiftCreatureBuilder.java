package anightdazingzoroark.prift.server.entity.creature.builder;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.AbstractPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.BooleanPropertyValue;
import anightdazingzoroark.riftlib.nbtStorageUser.propertyValue.IntegerPropertyValue;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class RiftCreatureBuilder extends AbstractCreatureBuilder<RiftCreatureBuilder> {
    private String creatureName;
    private final Map<String, CreaturePhaseBuilder> creaturePhaseBuilderMap = new HashMap<>();
    private Map<String, AbstractPropertyValue<?>> propertyValueMap;
    @Nullable
    private Consumer<RiftCreature> updateEffect;
    private Map<String, Function<RiftCreature, Double>> hitboxTagDamageInfo;

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
        this.creaturePhaseBuilderMap.put(phaseName, phaseBuilder.setPhaseName(phaseName));
        return this;
    }

    public Map<String, CreaturePhaseBuilder> getPhaseBuilderMaps() {
        return this.creaturePhaseBuilderMap;
    }

    //-----for additional values to this creature. they do sync from server to client, but they do not persist.-----
    public RiftCreatureBuilder registerIntegerValue(String name, int initVal) {
        if (this.locked) return this;
        if (this.propertyValueMap == null) this.propertyValueMap = new HashMap<>();
        this.propertyValueMap.put(name, new IntegerPropertyValue(name, initVal));
        return this;
    }

    public RiftCreatureBuilder registerBooleanValue(String name, boolean initVal) {
        if (this.locked) return this;
        if (this.propertyValueMap == null) this.propertyValueMap = new HashMap<>();
        this.propertyValueMap.put(name, new BooleanPropertyValue(name, initVal));
        return this;
    }

    public Map<String, AbstractPropertyValue<?>> getPropertyValueMap() {
        return this.propertyValueMap;
    }
    //-----additional values end here-----
    /**
     * Update effects are extra stuff that happens with the creature every tick
     * */
    public RiftCreatureBuilder registerOnUpdateEffect(Consumer<RiftCreature> onUpdate) {
        this.updateEffect = onUpdate;
        return this;
    }

    @Nullable
    public Consumer<RiftCreature> getUpdateEffect() {
        return this.updateEffect;
    }

    /**
     * Set hitbox information on the creature
     * */
    public RiftCreatureBuilder setHitboxInformation() {
        this.hitboxTagDamageInfo = Map.of();
        return this;
    }

    public RiftCreatureBuilder setHitboxInformation(Map<String, Function<RiftCreature, Double>> hitboxTagDamageInfo) {
        this.hitboxTagDamageInfo = hitboxTagDamageInfo;
        return this;
    }

    public Map<String, Function<RiftCreature, Double>> getHitboxInformation() {
        return this.hitboxTagDamageInfo;
    }
}
