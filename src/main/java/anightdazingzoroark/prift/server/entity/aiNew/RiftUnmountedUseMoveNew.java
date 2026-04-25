package anightdazingzoroark.prift.server.entity.aiNew;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.WeightedListNew;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creatureMovesNew.CreatureMoveRegistry;
import anightdazingzoroark.prift.server.entity.creaturenew.CreatureMoveStorage;
import anightdazingzoroark.prift.server.entity.creaturenew.RiftCreatureNew;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;

public class RiftUnmountedUseMoveNew extends EntityAIBase {
    private final RiftCreatureNew creature;
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

        CreatureMoveStorage creatureMoves = this.creature.getCreatureMoves();
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
            if (!this.creature.getNavigator().noPath()) this.creature.getNavigator().clearPath();
        }

        //-----find and use move that has 0 priority (must use no matter the current loadout)-----
        FixedSizeList<String> allUsableMoves = creatureMoves.getAllUsableMoves();
        for (int index = 0; index < allUsableMoves.size(); index++) {
            String moveName = allUsableMoves.get(index);
            if (moveName.isEmpty()) continue;

            CreatureMoveBuilder move = CreatureMoveRegistry.getCreatureMove(moveName);
            boolean moveCanAttackTarget = move.getRequireFindTargetToUse() && attackTarget != null;

            if (moveCanAttackTarget) {
                if (move.getCanUsePredicate().apply(this.creature, attackTarget) == 0
                    && creatureMoves.moveCurrentCooldown(moveName) <= 0
                ) {
                    this.creature.setCurrentMove(moveName);
                    return true;
                }
            }
            else {
                if (move.getCanUsePredicate().apply(this.creature, null) == 0
                        && creatureMoves.moveCurrentCooldown(moveName) <= 0
                ) {
                    this.creature.setCurrentMove(moveName);
                    return true;
                }
            }
        }

        //-----find and use move from current list-----
        FixedSizeList<String> currentUsableMoves = creatureMoves.getCurrentUsableMoves();
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
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        System.out.println("current move: "+this.creature.getCurrentMove());
        boolean flag = !this.creature.getCurrentMove().isEmpty();
        System.out.println("flag: "+flag);
        //must keep executing as long as there is a current move
        return flag;
    }

    @Override
    public void resetTask() {
        this.creature.setAttackTarget(null);
        this.creature.getNavigator().clearPath();
        this.isPathing = false;
    }
}
