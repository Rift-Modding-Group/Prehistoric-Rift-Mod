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
    private BiFunction<ICreature, EntityLivingBase, Boolean> priorityPredicate = (creature, target) -> false;
    @NotNull
    private MoveCategory moveCategory;
    @NotNull
    private final List<DetectionRule> detectionRules = new ArrayList<>();
    private boolean canUseWhenFrustrated;
    private boolean dontPathToTarget;
    private int additionalAIUses;

    public MoveRuleBuilder(@NotNull String moveName, @NotNull MoveCategory moveCategory) {
        this.moveName = moveName;
        this.moveCategory = moveCategory;
    }

    public void lock() {
        this.locked = true;
    }

    @NotNull
    public String getMoveName() {
        return this.moveName;
    }

    @NotNull
    public MoveRuleBuilder setMoveCategory(@NotNull MoveCategory moveCategory) {
        this.checkIfLocked();
        this.moveCategory = moveCategory;
        return this;
    }

    @NotNull
    public MoveCategory getMoveCategory() {
        return this.moveCategory;
    }

    @NotNull
    public MoveRuleBuilder setPriorityPredicate(@NotNull BiFunction<ICreature, EntityLivingBase, Boolean> priorityPredicate) {
        this.checkIfLocked();
        this.priorityPredicate = priorityPredicate;
        return this;
    }

    @NotNull
    public BiFunction<ICreature, EntityLivingBase, Boolean> getPriorityPredicate() {
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
     * Prefer this move for this many additional AI uses after its initial use.
     * Each use must still be eligible. Zero leaves ordinary move selection unchanged.
     */
    @NotNull
    public MoveRuleBuilder setAdditionalAIUses(int additionalAiUses) {
        this.checkIfLocked();
        if (additionalAiUses < 0) {
            throw new IllegalArgumentException("Additional AI uses must be nonnegative!");
        }
        this.additionalAIUses = additionalAiUses;
        return this;
    }

    public int getAdditionalAIUses() {
        return this.additionalAIUses;
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

    public enum MoveCategory {
        MELEE,
        RANGED,
        STATUS
    }
}
