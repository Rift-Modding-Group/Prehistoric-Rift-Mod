package anightdazingzoroark.prift.api.creature.builder;

import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * A move rule defines how moves are executed by creature AI.
 */
public class MoveRuleBuilder {
    protected boolean locked;

    @NotNull
    private final String moveName;
    @NotNull
    private BiFunction<ICreature, EntityLivingBase, Integer> priorityPredicate = (creature, target) -> -1;
    @NotNull
    private CreatureMoveSelectorBuilder.DetectionRule detectionRule = new CreatureMoveSelectorBuilder.DistanceFromUserDetectionRule("", 8D);
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

    public MoveRuleBuilder setDetectionRule(@NotNull CreatureMoveSelectorBuilder.DetectionRule detectionRule) {
        this.checkIfLocked();
        this.detectionRule = detectionRule;
        return this;
    }

    @NotNull
    public CreatureMoveSelectorBuilder.DetectionRule getDetectionRule() {
        return this.detectionRule;
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
     * Make it so this move can be used in clearing paths
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
