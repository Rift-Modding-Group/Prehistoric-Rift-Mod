package anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * A move rule defines how moves are executed by creature AI
 * */
public class MoveRuleBuilder {
    //extremely important
    protected boolean locked;

    @NotNull
    private final String moveName;
    @NotNull
    private BiFunction<RiftCreature, EntityLivingBase, Integer> priorityPredicate = (creature, target) -> -1;
    @NotNull
    private CreatureMoveSelectorBuilder.DetectionRule detectionRule = new CreatureMoveSelectorBuilder.DistanceFromUserDetectionRule("", 8D);
    private boolean canUseWhenFrustrated;
    private boolean dontPathToTarget;

    public MoveRuleBuilder(@NotNull String moveName) {
        this.moveName = moveName;
    }

    /**
     * This locks this object so that when accessing any instances of this, it can never be modified ever
     * */
    public void lock() {
        this.locked = true;
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
        this.checkIfLocked();

        return this.setPriorityPredicate(((creature, target) -> (target != null && target.isEntityAlive()) ? priority : -1));
    }

    public MoveRuleBuilder setPriorityPredicate(@NotNull BiFunction<RiftCreature, EntityLivingBase, Integer> priorityPredicate) {
        this.checkIfLocked();

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
    public MoveRuleBuilder setDetectionRule(@NotNull CreatureMoveSelectorBuilder.DetectionRule detectionRule) {
        this.checkIfLocked();

        this.detectionRule = detectionRule;
        return this;
    }

    @NotNull
    public CreatureMoveSelectorBuilder.DetectionRule getDetectionRule() {
        return this.detectionRule;
    }

    /**
     * Allows a creature to instantly use this move when it's frustrated
     * */
    public MoveRuleBuilder setUseWhenFrustrated() {
        this.checkIfLocked();

        this.canUseWhenFrustrated = true;
        return this;
    }

    public boolean getUseWhenFrustrated() {
        return this.canUseWhenFrustrated;
    }

    /**
     * Make it so creature will not path to target nor look at them when the move is selected
     * */
    public MoveRuleBuilder setDontPathToTarget() {
        this.checkIfLocked();

        this.dontPathToTarget = true;
        return this;
    }

    public boolean getDontPathToTarget() {
        return this.dontPathToTarget;
    }

    /**
     * for debugging lol
     * */
    @Override
    public String toString() {
        return "MoveBuilder:"+this.moveName;
    }

    /**
     * Put this on every setter in builder to protect from post-creation editing
     * */
    protected void checkIfLocked() {
        if (this.locked) throw new IllegalCallerException("A setter for a move rule builder cannot be called after the move rule is registered!");
    }
}
