package anightdazingzoroark.prift.api.creature;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Species-independent move definitions. Register addon moves before creature registration.
 */
public class CreatureMoveRegistry {
    private static final Map<String, MoveDefinition> moves = new LinkedHashMap<>();
    private static boolean registrationFinished;

    public static void registerMove(@NotNull String name, @NotNull CreatureMoveBuilder builder, @NotNull MoveRuleBuilder... rules) {
        if (registrationFinished) throw new IllegalStateException("Move registration has finished");
        if (name.isBlank()) throw new IllegalArgumentException("A move must have a name");
        if (moves.containsKey(name)) throw new IllegalArgumentException("Move " + name + " is already registered");
        if (!builder.isValid()) throw new IllegalArgumentException("Move " + name + " is invalid");
        for (MoveRuleBuilder rule : rules) {
            if (!name.equals(rule.getMoveName())) {
                throw new IllegalArgumentException("A move's rules must reference its registered name: " + name);
            }
        }
        builder.lock();
        for (MoveRuleBuilder rule : rules) rule.lock();
        moves.put(name, new MoveDefinition(name, builder, List.of(rules)));
    }

    @Nullable
    public static MoveDefinition getMove(@NotNull String name) {
        return moves.get(name);
    }

    @NotNull
    public static Map<String, MoveDefinition> getMoves() {
        return Collections.unmodifiableMap(moves);
    }

    public record MoveDefinition(
            @NotNull String name, @NotNull CreatureMoveBuilder moveBuilder, @NotNull List<MoveRuleBuilder> rules
    ) {
        public MoveDefinition {
            rules = List.copyOf(rules);
        }
    }

    public static void finishRegistration() {
        registrationFinished = true;
    }
}
