package anightdazingzoroark.prift.api.util;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.AbstractCreatureBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.creature.CreatureMoveRegistry.MoveDefinition;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder.MoveRule;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.keyframe.EventKeyFrame;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimationHitboxTimeline;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared move declarations, animation bindings, and AI policy indexes.
 * */
public class CreatureMoveUtil {
    /**
     * Samples existing detection geometry at animation hit cues without running Java hit effects.
     * Animation-declared offense intervals retain their explicitly authored lifetimes.
     */
    @NotNull
    public static List<AnimationHitboxTimeline.HitboxWindow> getHitboxWindows(
            @NotNull ICreature creature, @NotNull CreatureMoveBuilder move, @NotNull List<Animation> animations,
            @NotNull List<AnimatedBoundingBox> hitboxes, @NotNull AnimationDataEntityLiving data
    ) {
        List<AnimationHitboxTimeline.HitboxWindow> windows = new ArrayList<>(
                AnimationHitboxTimeline.getHitboxWindows(animations, hitboxes, data));
        AbstractCreatureBuilder<?> configuration = creature.getCreatureType();
        if (!creature.getPhase().isEmpty()) configuration = creature.getCreatureType().getPhaseBuilderMaps().get(creature.getPhase());
        if (configuration == null) return List.copyOf(windows);
        MoveDefinition definition = configuration.getMoveDefinitions().values().stream()
                .filter(candidate -> candidate.moveBuilder() == move).findFirst().orElse(null);
        if (definition == null) return List.copyOf(windows);
        List<DetectionRule> rules = definition.rules().stream()
                .flatMap(rule -> rule.getDetectionRules().stream()).toList();
        List<AnimatedBoundingBox> detectionHitboxes = hitboxes.stream().filter(hitbox -> rules.stream().anyMatch(rule -> {
            List<String> tags = Arrays.asList(hitbox.getTags());
            return switch (rule) {
                case AnimatedHitboxDetectionRule animated -> tags.contains(animated.getHitboxTag());
                case BoundingBoxDetectionRule bounds -> {
                    if (bounds.getUseTagName()) yield tags.contains(bounds.getBoundingBoxName());
                    yield hitbox.getName().equals(bounds.getBoundingBoxName());
                }
                default -> false;
            };
        })).filter(hitbox -> windows.stream().noneMatch(window -> window.hitbox() == hitbox)).toList();
        windows.addAll(getHitEffectHitboxWindows(animations, detectionHitboxes, data.getDataScope().getValues()));
        return List.copyOf(windows);
    }

    /**
     * Sample the first hit message, matching runtime's once-per-move hit-effect dispatch.
     */
    @NotNull
    public static List<AnimationHitboxTimeline.HitboxWindow> getHitEffectHitboxWindows(
            @NotNull List<Animation> animations, @NotNull List<AnimatedBoundingBox> hitboxes,
            @NotNull Map<String, Double> variables
    ) {
        if (hitboxes.isEmpty()) return List.of();
        HitEffectTimeline parser = new HitEffectTimeline();
        MolangScope scope = new MolangScope();
        scope.replaceValues(variables);
        for (Animation animation : animations) {
            for (EventKeyFrame.CustomInstructionKeyFrame event : animation.customInstructionKeyFrames.stream()
                    .sorted(Comparator.comparingDouble(EventKeyFrame::getStartTick)).toList()) {
                if (event.getStartTick() < 0D || event.getStartTick() > animation.animationLength) continue;
                parser.tick = event.getStartTick();
                try {
                    MolangExpression instruction = parser.parseExpression(event.instruction);
                    parser.withScope(scope, () -> { instruction.get(); });
                }
                catch (MolangException exception) {
                    return List.of();
                }
                if (!parser.hitTicks.isEmpty()) {
                    double tick = parser.hitTicks.getFirst();
                    return hitboxes.stream()
                            .map(hitbox -> new AnimationHitboxTimeline.HitboxWindow(animation, hitbox, tick, tick)).toList();
                }
            }
        }
        return List.of();
    }

    private static class HitEffectTimeline extends MolangParser {
        private double tick;
        @NotNull
        private final List<Double> hitTicks = new ArrayList<>();

        private HitEffectTimeline() {
            this.registerFunction("function.send_message", 1, (arguments, data) -> {
                if (!arguments[0].getString().equals("moveHitEffect")) return 0D;
                this.hitTicks.add(this.tick);
                return 1D;
            });
        }
    }

    /** Select the configured playback variant, or the charge-up sequence, for prediction. */
    @NotNull
    public static List<String> getPreviewAnimationNames(@NotNull ICreature creature, @NotNull String moveName) {
        AbstractCreatureBuilder<?> configuration = creature.getCreatureType();
        if (!creature.getPhase().isEmpty()) configuration = creature.getCreatureType().getPhaseBuilderMaps().get(creature.getPhase());
        if (configuration == null) return List.of();
        MoveDefinition definition = configuration.getMoveDefinitions().get(moveName);
        if (definition == null) return List.of();
        if (definition.moveBuilder().getMoveChargeupBuilder() != null) {
            return Arrays.stream(ChargeupPhase.values())
                    .flatMap(phase -> getAnimationNames(creature, creature.getPhase(), moveName, phase).stream()).toList();
        }
        List<String> names = getAnimationNames(creature, creature.getPhase(), moveName, null);
        if (names.isEmpty()) return List.of();
        int index = 0;
        if (creature.getCurrentMove().equals(moveName)) {
            index = Math.clamp((int) creature.getAnimationData().getVariable("chosenMove"), 0, names.size() - 1);
        }
        return List.of(names.get(index));
    }

    /**
     * Resolve the same species animation binding for playback, aiming and hitbox prediction.
     * */
    @NotNull
    public static List<String> getAnimationNames(
            @NotNull ICreature creature, @NotNull String creaturePhase,
            @NotNull String moveName, @Nullable ChargeupPhase chargeupPhase
    ) {
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
