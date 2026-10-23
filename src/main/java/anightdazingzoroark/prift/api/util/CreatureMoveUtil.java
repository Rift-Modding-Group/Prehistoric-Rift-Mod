package anightdazingzoroark.prift.api.util;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.builder.AbstractCreatureBuilder.MoveDefinition;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder.MoveRule;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DetectionRule;
import org.jetbrains.annotations.NotNull;

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
