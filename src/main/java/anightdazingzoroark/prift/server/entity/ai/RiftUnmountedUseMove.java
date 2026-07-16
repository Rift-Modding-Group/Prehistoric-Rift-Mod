package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveHelper;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.AbstractMoveResultTicker;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveResult.MoveResult;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.CreatureMoveSelectorBuilder;
import anightdazingzoroark.prift.util.MathUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import org.jetbrains.annotations.NotNull;

/**
 * This is for managing a creature being able to use moves as well as other offensive
 * actions that are not moves.
 * */
public class RiftUnmountedUseMove extends EntityAIBase {
    private static final double TARGET_MOVED_REPATH_DISTANCE_SQ = 1D;
    private static final int DIRECT_TARGET_MOVE_STALL_TICKS = 8;
    private static final int CLOSE_TARGET_STRAFE_TICKS = 10;

    @NotNull
    private final RiftCreature creature;
    private CreatureMoveSelectorBuilder.MoveRule moveRule;
    private AbstractMoveResultTicker moveResultTicker;

    public RiftUnmountedUseMove(@NotNull RiftCreature creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    //checking if a move could be used happens here
    @Override
    public boolean shouldExecute() {
        //cannot execute if the creature has no moves
        if (!this.creature.getCreatureMoves().isInitialized()) return false;

        //cannot execute if the creature is using a move
        if (!this.creature.getCurrentMove().isEmpty()) return false;

        //-----find and use a move from current list-----
        this.moveRule = this.createMoveRule();
        return this.moveRule != null;
    }

    @Override
    public void startExecuting() {
        this.moveResultTicker = this.moveRule.moveResult().moveResultTicker.apply(this.creature, this.moveRule.moveRuleBuilder());
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.moveResultTicker != null && this.moveResultTicker.canContinueTicking();
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
        if (this.moveResultTicker != null) this.moveResultTicker.onUpdate();
    }

    private CreatureMoveSelectorBuilder.MoveRule createMoveRule() {
        CreatureMoveStorage creatureMoves = this.creature.getCreatureMoves();
        return creatureMoves.getBestMoveRuleUnmounted();
    }
}
