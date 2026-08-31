package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.AbstractMoveResultTicker;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.MoveResult;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveSelectorBuilder;
import net.minecraft.entity.ai.EntityAIBase;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This is for managing a creature being able to use moves as well as other offensive
 * actions that are not moves.
 * */
public class RiftUnmountedUseMove extends EntityAIBase {
    @NotNull
    private final RiftCreature creature;
    private ImmutablePair<CreatureMoveSelectorBuilder.MoveRule, Integer> moveRulePair;
    private AbstractMoveResultTicker moveResultTicker;

    public RiftUnmountedUseMove(@NotNull RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    //checking if a move could be used happens here
    @Override
    public boolean shouldExecute() {
        if (this.creature.isRetreating()) return false;

        //cannot execute if the creature has no moves
        if (!this.creature.getCreatureMoves().isInitialized()) return false;

        //cannot execute if the creature is using a move
        if (!this.creature.getCurrentMove().isEmpty()) return false;

        //cannot use moves when leaping
        if (this.creature.isLeaping()) return false;

        //-----find and use a move from current list-----
        this.moveRulePair = this.createMoveRule();
        return this.moveRulePair != null;
    }

    @Override
    public void startExecuting() {
        CreatureMoveSelectorBuilder.MoveRule moveRule = this.moveRulePair.getLeft();
        this.moveResultTicker = MoveResult.createTicker(moveRule.moveResult(), this.creature, moveRule.moveRuleBuilder());
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.isRetreating()
                && (!this.creature.isLeaping() || this.isUsingLeapAttack())
                && this.moveResultTicker != null
                && (!this.moveResultTicker.isOverridableWhileUsed() || this.createMoveRule() != null)
                && this.moveResultTicker.canContinueTicking();
    }

    @Override
    public void resetTask() {
        if (this.moveResultTicker != null) this.moveResultTicker.onEndTicker();
        this.moveResultTicker = null;
    }

    /**
     * Mostly to update pathing to target of a move
     * */
    @Override
    public void updateTask() {
        if (this.creature.isLeaping() && !this.isUsingLeapAttack()) return;

        //switch before the current ticker can request a pathfinding leap
        if (!this.creature.isLeaping()) this.trySwitchToBestMove();

        if (this.moveResultTicker != null) this.moveResultTicker.onUpdate();
    }

    private void trySwitchToBestMove() {
        ImmutablePair<CreatureMoveSelectorBuilder.MoveRule, Integer> newMoveRulePair = this.createMoveRule();
        if (newMoveRulePair == null
                || (this.moveResultTicker != null && !this.moveResultTicker.isOverridableWhileUsed())
                || newMoveRulePair.getLeft().equals(this.moveRulePair.getLeft())
        ) {
            return;
        }

        if (this.moveResultTicker != null) this.moveResultTicker.onEndTicker();
        this.moveRulePair = newMoveRulePair;
        CreatureMoveSelectorBuilder.MoveRule moveRule = this.moveRulePair.getLeft();
        this.moveResultTicker = MoveResult.createTicker(
                moveRule.moveResult(), this.creature, moveRule.moveRuleBuilder()
        );
    }

    @Nullable
    private ImmutablePair<CreatureMoveSelectorBuilder.MoveRule, Integer> createMoveRule() {
        CreatureMoveStorage creatureMoves = this.creature.getCreatureMoves();
        CreatureMoveSelectorBuilder.MoveRule preferredMoveRule = this.moveRulePair == null ? null : this.moveRulePair.getLeft();
        return creatureMoves.getBestMoveRuleUnmounted(preferredMoveRule);
    }

    private boolean isUsingLeapAttack() {
        return this.moveRulePair != null && this.moveRulePair.getLeft().moveResult() == CreatureMoveResult.LEAP;
    }
}
