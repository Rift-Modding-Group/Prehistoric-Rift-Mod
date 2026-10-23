package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.builder.detectionRule.DetectionRule;
import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Configuration for choosing and executing a creature move.
 */
public class MoveRuleBuilder {
    protected boolean locked;

    @NotNull
    private final String moveName;
    @NotNull
    private BiFunction<ICreature, EntityLivingBase, Integer> priorityPredicate = (creature, target) -> -1;
    @NotNull
    private final List<DetectionRule> detectionRules = new ArrayList<>();
    private boolean canUseWhenFrustrated;
    private boolean dontPathToTarget;
    private boolean useBlockBreak;

    public MoveRuleBuilder(@NotNull String moveName) {
        this.moveName = moveName;
    }

    public void lock() {
        this.locked = true;
    }

    @NotNull
    public String getMoveName() {
        return this.moveName;
    }

    public MoveRuleBuilder setPriorityPredicate(int priority) {
        this.checkIfLocked();
        return this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive() ? priority : -1);
    }

    public MoveRuleBuilder setPriorityPredicate(@NotNull BiFunction<ICreature, EntityLivingBase, Integer> priorityPredicate) {
        this.checkIfLocked();
        this.priorityPredicate = priorityPredicate;
        return this;
    }

    @NotNull
    public BiFunction<ICreature, EntityLivingBase, Integer> getPriorityPredicate() {
        return this.priorityPredicate;
    }

    /**
     * Add an alternative detection rule. A target only needs to match one rule.
     */
    public MoveRuleBuilder addDetectionRule(@NotNull DetectionRule detectionRule) {
        this.checkIfLocked();
        this.detectionRules.add(detectionRule);
        return this;
    }

    @NotNull
    public List<DetectionRule> getDetectionRules() {
        return List.copyOf(this.detectionRules);
    }

    public MoveRuleBuilder setUseWhenFrustrated() {
        this.checkIfLocked();
        this.canUseWhenFrustrated = true;
        return this;
    }

    public boolean getUseWhenFrustrated() {
        return this.canUseWhenFrustrated;
    }

    public MoveRuleBuilder setDontPathToTarget() {
        this.checkIfLocked();
        this.dontPathToTarget = true;
        return this;
    }

    public boolean getDontPathToTarget() {
        return this.dontPathToTarget;
    }

    /**
     * Make it so this move can be used in clearing paths when pathfinding
     */
    public MoveRuleBuilder setUseBlockBreak() {
        this.checkIfLocked();
        this.useBlockBreak = true;
        return this;
    }

    public boolean getUseBlockBreak() {
        return this.useBlockBreak;
    }

    @Override
    public String toString() {
        return "MoveBuilder:"+this.moveName;
    }

    protected void checkIfLocked() {
        if (this.locked) {
            throw new IllegalCallerException("A setter for a move rule builder cannot be called after the move rule is registered!");
        }
    }
}
