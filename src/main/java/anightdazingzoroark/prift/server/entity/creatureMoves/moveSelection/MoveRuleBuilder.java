package anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * A move rule defines how moves are executed by creature AI
 * */
public class MoveRuleBuilder {
    @NotNull
    private final String moveName;
    @NotNull
    private BiFunction<RiftCreature, EntityLivingBase, Integer> priorityPredicate = (creature, target) -> -1;
    @NotNull
    private CreatureMoveSelector.DetectionRule detectionRule = new CreatureMoveSelector.DistanceFromUserDetectionRule("", 8D);

    public MoveRuleBuilder(@NotNull String moveName) {
        this.moveName = moveName;
    }

    @NotNull
    public String getMoveName() {
        return this.moveName;
    }

    /**
     * A priority predicate represents the priority depending on whatever relationship
     * the creature currently has with the target.
     * */
    public MoveRuleBuilder setPriorityPredicate(int priority) {
        return this.setPriorityPredicate(((creature, target) -> (target != null && target.isEntityAlive()) ? priority : -1));
    }

    public MoveRuleBuilder setPriorityPredicate(@NotNull BiFunction<RiftCreature, EntityLivingBase, Integer> priorityPredicate) {
        this.priorityPredicate = priorityPredicate;
        return this;
    }

    @NotNull
    public BiFunction<RiftCreature, EntityLivingBase, Integer> getPriorityPredicate() {
        return this.priorityPredicate;
    }

    /**
     * A detection rule defines when a creature can use its selected move on its target.
     * Mostly for determining range.
     * */
    public MoveRuleBuilder setDetectionRule(@NotNull CreatureMoveSelector.DetectionRule detectionRule) {
        this.detectionRule = detectionRule;
        return this;
    }

    @NotNull
    public CreatureMoveSelector.DetectionRule getDetectionRule() {
        return this.detectionRule;
    }
}
