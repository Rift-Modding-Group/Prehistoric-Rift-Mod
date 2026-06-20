package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMoveSelector;
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
    @NotNull
    private final RiftCreature creature;
    private CreatureMoveSelector.MoveRule moveRule;
    private boolean pathingToTargetForMove;
    private boolean hasExecutedMove;

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
        CreatureMoveStorage creatureMoves = this.creature.getCreatureMoves();
        this.moveRule = creatureMoves.getBestMoveRuleUnmounted();
        return this.moveRule != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase target = this.creature.getAttackTarget();
        boolean targetAvailability = target != null && target.isEntityAlive();

        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.USE_MOVE) {
            //use the moverule as basis for checking if the move can be used as its needed for target check
            CreatureMoveBuilder creatureMoveToUseBuilder = this.creature.getCreatureMoves().getUsableMoveBuilder(this.moveRule.name());
            boolean moveBuilderTargetCondition = (creatureMoveToUseBuilder != null && creatureMoveToUseBuilder.getRequireFindTargetToUse()) ? targetAvailability : true;

            //move execution depends on if current move hasnt been reset and if target is gone (requires move to actually require targeting)
            if (this.hasExecutedMove) {
                boolean moveStillInUseCondition = !this.creature.getCurrentMove().isEmpty();
                return moveStillInUseCondition && moveBuilderTargetCondition;
            }
            //if creature hasnt executed move yet, keep it true to keep it running
            //only thing stopping it is if target is gone (if said move requires it)
            else return moveBuilderTargetCondition;
        }
        //when sprinting towards the target, the target should be alive for this to continue executing
        else if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.SPRINT) {
            return this.creature.isSprinting() && targetAvailability;
        }
        else return false;
    }

    @Override
    public void startExecuting() {
        //reset all preexisting pathing if there is a target
        //if (this.creature.getAttackTarget() != null) this.creature.getNavigator().clearPath();

        //other result specific stuff
        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.SPRINT) {
            this.creature.setSprinting(true);
        }
    }

    @Override
    public void resetTask() {
        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.SPRINT) {
            this.creature.sprintToAttackCooldown = MathUtil.randomInRange(this.creature.world.rand, 5, 10) * 20;
            this.creature.setSprinting(false);
        }

        this.creature.getNavigator().clearPath();
        this.moveRule = null;
        this.pathingToTargetForMove = false;
        this.hasExecutedMove = false;
        this.creature.resetCurrentMove();
    }

    /**
     * Mostly to update pathing to target of a move
     * */
    @Override
    public void updateTask() {
        EntityLivingBase target = this.creature.getAttackTarget();
        if (target == null) return;

        //normal move usage involves just proper pathing until it reaches a specific distance
        if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.USE_MOVE) {
            PathNavigate creatureNavigation = this.creature.getNavigator();

            //set path
            if (!this.pathingToTargetForMove) {
                creatureNavigation.setPath(creatureNavigation.getPathToEntityLiving(target), 1D);
                this.pathingToTargetForMove = true;
            }

            //if within specific distance, set the move
            if (this.creature.getDistance(target) <= this.creature.width + 1) {
                this.creature.setCurrentMove(this.moveRule.name());
                this.hasExecutedMove = true;

                //block all further pathing if associated move builder says so
                CreatureMoveBuilder creatureMoveBuilder = this.creature.getCreatureMoves().getMoveBuilderCurrentMove();
                if (creatureMoveBuilder.getUseCanStopMovement()) {
                    this.creature.getNavigator().clearPath();
                    this.pathingToTargetForMove = false;
                }
            }
        }
        //sprinting to target involves directly moving to its target position
        //the 1D speed should not be worried about, speed boost is already taken care of somewhere...
        else if (this.moveRule.moveResult() == CreatureMoveSelector.MoveResult.SPRINT) {
            this.creature.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1D);
        }
    }
}
