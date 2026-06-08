package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.util.WeightedList;
import net.minecraft.entity.EntityLivingBase;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Creature-level AI policy for choosing moves.
 * */
public class CreatureMoveSelector {
    private final Map<String, MoveRule> moveRules = new HashMap<>();

    //-----self move stuff. self moves are to be used on oneself-----
    public CreatureMoveSelector addSelfMove(@NotNull String moveName, @NotNull Function<RiftCreature, Integer> priority) {
        this.moveRules.put(moveName, new MoveRule(MoveUseTarget.SELF, (creature, target, moveBuilder) -> {
            return (int) priority.apply(creature);
        }));
        return this;
    }

    public CreatureMoveSelector addSelfMove(@NotNull String moveName, int priority) {
        return this.addSelfMove(moveName, creature -> priority);
    }

    //-----melee move stuff-----
    public CreatureMoveSelector addMeleeMove(@NotNull String moveName, @NotNull BiFunction<RiftCreature, EntityLivingBase, Integer> priority) {
        this.moveRules.put(moveName, new MoveRule(MoveUseTarget.TARGET, (creature, target, moveBuilder) -> {
            if (target == null || creature.getDistance(target) > creature.getCreatureType().getPhysicalReach()) return -1;

            return (int) priority.apply(creature, target);
        }));
        return this;
    }

    public CreatureMoveSelector addMeleeMove(@NotNull String moveName, int priority) {
        return this.addMeleeMove(moveName, (creature, target) -> priority);
    }

    //-----ranged move stuff-----
    public CreatureMoveSelector addRangedMove(@NotNull String moveName, @NotNull BiFunction<RiftCreature, EntityLivingBase, Integer> priority, double maxDistance) {
        this.moveRules.put(moveName, new MoveRule(MoveUseTarget.TARGET, (creature, target, moveBuilder) -> {
            if (target == null) return -1;

            double distance = creature.getDistance(target);
            if (distance <= creature.getCreatureType().getPhysicalReach() || distance > maxDistance) return -1;

            return (int) priority.apply(creature, target);
        }));
        return this;
    }

    public CreatureMoveSelector addRangedMove(@NotNull String moveName, int priority, double maxDistance) {
        return this.addRangedMove(moveName, (creature, target) -> priority, maxDistance);
    }

    @Nullable
    public String selectMove(
            @NotNull RiftCreature creature,
            @Nullable EntityLivingBase target,
            @NotNull CreatureMoveStorage creatureMoves
    ) {
        WeightedList<String> weightedMoveList = new WeightedList<>();

        for (ImmutablePair<String, CreatureMoveBuilder> movePair : creatureMoves.getUsableMoves()) {
            if (movePair == null) continue;

            String moveName = movePair.getLeft();
            CreatureMoveBuilder moveBuilder = movePair.getRight();
            if (moveName == null || moveName.isEmpty() || moveBuilder == null) continue;
            if (creatureMoves.moveCurrentCooldown(moveName) > 0) continue;

            int moveWeight = this.getMoveWeight(creature, target, moveName, moveBuilder);
            if (moveWeight > 0) weightedMoveList.add(moveWeight, moveName);
        }

        return weightedMoveList.next();
    }

    protected int getMoveWeight(
            @NotNull RiftCreature creature,
            @Nullable EntityLivingBase target,
            @NotNull String moveName,
            @NotNull CreatureMoveBuilder moveBuilder
    ) {
        MoveRule moveRule = this.moveRules.get(moveName);
        if (moveRule == null) return -1;
        if (moveRule.moveUseTarget() == MoveUseTarget.TARGET && target == null) return -1;
        if (moveBuilder.getRequireFindTargetToUse() && target == null) return -1;
        return moveRule.moveWeight().getWeight(creature, target, moveBuilder);
    }

    private enum MoveUseTarget {
        TARGET,
        SELF
    }

    @FunctionalInterface
    public interface MoveWeight {
        int getWeight(
                @NotNull RiftCreature creature,
                @Nullable EntityLivingBase target,
                @NotNull CreatureMoveBuilder moveBuilder
        );
    }

    private record MoveRule(MoveUseTarget moveUseTarget, MoveWeight moveWeight) {}
}
