package anightdazingzoroark.prift.server.entity.aiNew;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.helper.WeightedListNew;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.CreatureMoveRegistry;
import anightdazingzoroark.prift.server.entity.creaturenew.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;

/**
 * This is for managing a creature being able to use moves as well as other offensive
 * actions that are not moves.
 * */
public class RiftUnmountedUseMoveNew extends EntityAIBase {
    private final RiftCreatureNew creature;
    private MoveResult moveResult;
    private boolean isPathing;

    public RiftUnmountedUseMoveNew(RiftCreatureNew creature) {
        this.creature = creature;
        this.setMutexBits(3);
    }

    //checking if a move could be used happens here
    @Override
    public boolean shouldExecute() {
        //cannot execute if the creature is using a move
        if (!this.creature.getCurrentMove().isEmpty()) return false;

        EntityLivingBase attackTarget = this.creature.getAttackTarget();

        //-----path towards the target, if there is-----
        if (attackTarget != null && !this.isPathing) {
            Path path = this.creature.getNavigator().getPathToEntityLiving(attackTarget);
            if (path != null) {
                this.isPathing = true;
                this.creature.getNavigator().setPath(path, 1D);
            }
        }
        if (attackTarget == null) {
            this.isPathing = false;
            this.creature.getNavigator().clearPath();
        }

        //-----every 5-10 seconds the creature will sprint if the target is too far-----
        if (this.creature.getCreatureType().getCanSprintToAttack() && this.creature.sprintToAttackCooldown <= 0 && attackTarget != null) {
            double distFromTarget = this.creature.getDistance(attackTarget);

            if (distFromTarget <= 16) {
                this.moveResult = MoveResult.SPRINT;
                return true;
            }
        }

        //-----find and use move from current list-----
        CreatureMoveStorage creatureMoves = this.creature.getCreatureMoves();
        FixedSizeList<String> currentUsableMoves = creatureMoves.getAllUsableMoves();
        WeightedListNew<String> weightedMoveList = new WeightedListNew<>();
        for (int index = 0; index < currentUsableMoves.size(); index++) {
            String moveName = currentUsableMoves.get(index);
            if (moveName.isEmpty()) continue;

            CreatureMoveBuilder move = CreatureMoveRegistry.getCreatureMove(moveName);
            boolean moveCanAttackTarget = move.getRequireFindTargetToUse() && attackTarget != null;
            int predicateResult;
            if (moveCanAttackTarget) {
                predicateResult = move.getCanUsePredicate().apply(this.creature, attackTarget);
            }
            else {
                predicateResult = move.getCanUsePredicate().apply(this.creature, null);
            }

            if (predicateResult >= 0 && creatureMoves.moveCurrentCooldown(moveName) <= 0) {
                weightedMoveList.add(predicateResult, moveName);
            }
        }

        String finalMoveToUse = weightedMoveList.next();
        if (finalMoveToUse == null) return false;
        this.creature.setCurrentMove(finalMoveToUse);
        this.moveResult = MoveResult.USE_MOVE;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        //must keep executing as long as there is a current move
        if (this.moveResult == MoveResult.USE_MOVE) return !this.creature.getCurrentMove().isEmpty();
        //when sprinting towards the target, the target should be alive for this to continue executing
        else if (this.moveResult == MoveResult.SPRINT) {
            return this.creature.isSprinting() && this.creature.getAttackTarget() != null && this.creature.getAttackTarget().isEntityAlive();
        }
        else return false;
    }

    @Override
    public void startExecuting() {
        if (this.moveResult == MoveResult.USE_MOVE) {
            CreatureMoveBuilder creatureMoveBuilder = CreatureMoveRegistry.getCreatureMove(this.creature.getCurrentMove());
            if (creatureMoveBuilder.getUseCanStopMovement()) {
                this.creature.getNavigator().clearPath();
                this.isPathing = false;
            }
        }
        else if (this.moveResult == MoveResult.SPRINT) {
            this.creature.setSprinting(true);
        }
    }

    @Override
    public void resetTask() {
        if (this.moveResult == MoveResult.SPRINT) {
            this.creature.sprintToAttackCooldown = RiftUtil.randomInRange(5, 10) * 20;
            this.creature.setSprinting(false);
        }

        this.creature.getNavigator().clearPath();
        this.isPathing = false;
        this.moveResult = null;
    }

    @Override
    public void updateTask() {
        if (this.moveResult == MoveResult.SPRINT) {
            if (this.creature.getAttackTarget() == null) return;
            this.creature.getMoveHelper().setMoveTo(
                    this.creature.getAttackTarget().posX,
                    this.creature.getAttackTarget().posY,
                    this.creature.getAttackTarget().posZ,
                    1D
            );
        }
    }

    private enum MoveResult {
        USE_MOVE,
        SPRINT,
        LEAP
    }
}
