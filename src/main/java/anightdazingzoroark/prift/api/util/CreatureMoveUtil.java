package anightdazingzoroark.prift.api.util;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry.MoveDefinition;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder.MoveRule;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DetectionRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registration-time indexes for declared moves and their AI policies.
 * */
public class CreatureMoveUtil {
    /** Resolve the same species animation binding for playback, aiming and hitbox prediction. */
    @NotNull
    public static List<String> getAnimationNames(@NotNull ICreature creature, @NotNull String creaturePhase,
                                                @NotNull String moveName, @Nullable ChargeupPhase chargeupPhase) {
        AbstractCreatureBuilder<?> configuration = creature.getCreatureType();
        if (!creaturePhase.isEmpty()) configuration = creature.getCreatureType().getPhaseBuilderMaps().get(creaturePhase);
        if (configuration == null) return List.of();
        List<String> names;
        if (chargeupPhase == null) names = configuration.getMoveAnimations(moveName);
        else names = configuration.getMoveAnimations(moveName, chargeupPhase);
        if (names.isEmpty() && creature.getAnimationData().getHolder() instanceof ICreature holder && holder != creature) {
            return getAnimationNames(holder, creaturePhase, moveName, chargeupPhase);
        }
        return getAnimationNames(creature.getCreatureType().getName(), configuration, moveName, chargeupPhase);
    }

    @NotNull
    public static List<String> getAnimationNames(@NotNull String creatureName, @NotNull AbstractCreatureBuilder<?> configuration,
                                                @NotNull String moveName, @Nullable ChargeupPhase chargeupPhase) {
        List<String> names;
        if (chargeupPhase == null) names = configuration.getMoveAnimations(moveName);
        else names = configuration.getMoveAnimations(moveName, chargeupPhase);
        return names.stream().map(name -> {
            if (name.startsWith("animation.")) return name;
            return "animation." + creatureName + "." + name;
        }).toList();
    }

    @NotNull
    public static Map<String, MoveDefinition> groupMoveRules(
            @NotNull Map<String, MoveDefinition> declarations, @NotNull List<MoveRule> policies
    ) {
        Map<String, MoveDefinition> moves = new LinkedHashMap<>();
        for (MoveDefinition move : declarations.values()) {
            moves.put(move.name(), new MoveDefinition(move.name(), move.moveBuilder(), List.of()));
        }
        for (MoveRule policy : policies) {
            if (policy.moveResult() != CreatureMoveResult.USE_MOVE) continue;
            MoveRuleBuilder rule = policy.moveRuleBuilder();
            MoveDefinition move = moves.get(rule.getMoveName());
            // Declarations may follow the selector; locking checks any unresolved names.
            if (move == null) continue;
            List<MoveRuleBuilder> rules = new ArrayList<>(move.rules());
            rules.add(rule);
            moves.put(move.name(), new MoveDefinition(move.name(), move.moveBuilder(), rules));
        }
        return Collections.unmodifiableMap(moves);
    }

    @NotNull
    public static Map<DetectionRule, MoveDefinition> indexDetectionOwners(@NotNull Map<String, MoveDefinition> moves) {
        Map<DetectionRule, MoveDefinition> owners = new IdentityHashMap<>();
        for (MoveDefinition move : moves.values()) {
            for (MoveRuleBuilder rule : move.rules()) {
                for (DetectionRule detection : rule.getDetectionRules()) {
                    MoveDefinition previous = owners.putIfAbsent(detection, move);
                    if (previous != null && previous != move) {
                        throw new IllegalArgumentException("A detection rule cannot belong to both " + previous.name() + " and " + move.name());
                    }
                }
            }
        }
        return Collections.unmodifiableMap(owners);
    }
}
