package anightdazingzoroark.prift.server.entity.creatureMoves;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Creature-level AI policy for choosing moves.
 * */
//todo: add move combos
public class CreatureMoveSelector {
    //general storage of move rules
    private final Map<MoveRule, BiFunction<RiftCreature, EntityLivingBase, Integer>> moveRules = new HashMap<>();

    public CreatureMoveSelector setMoveRule(String name, BiFunction<RiftCreature, EntityLivingBase, Integer> predicate) {
        this.moveRules.put(new MoveRule(MoveResult.USE_MOVE, name), predicate);
        return this;
    }

    /**
     * Make it so that sprinting can be used as an attack by this creature.
     * Note that its only for when its on its own, when controlled by a rider
     * it can spring to attack when commanded to (by simply sprinting lol)
     * */
    public CreatureMoveSelector setCanSprintToAttack() {
        this.moveRules.put(new MoveRule(MoveResult.SPRINT, ""), (creature, target) -> {
            if (target == null) return -1;
            double distFromTarget = creature.getDistance(target);
            double minReach = creature.width + 3; //is temporary
            boolean sprintCondition = distFromTarget <= 16D && distFromTarget >= minReach && creature.sprintToAttackCooldown <= 0;
            return sprintCondition ? 1 : -1;
        });
        return this;
    }

    public CreatureMoveSelector setCanLeapToAttack(BiFunction<RiftCreature, EntityLivingBase, Integer> predicate) {
        this.moveRules.put(new MoveRule(MoveResult.LEAP, ""), predicate);
        return this;
    }

    public Map<MoveRule, BiFunction<RiftCreature, EntityLivingBase, Integer>> getMoveRules() {
        return this.moveRules;
    }

    public enum MoveResult {
        USE_MOVE,
        USE_MOVE_COMBO,
        SPRINT,
        LEAP
    }

    public record MoveRule(@NotNull MoveResult moveResult, @NotNull String name) {
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof MoveRule(MoveResult result, String otherName))) return false;

            if (result != MoveResult.USE_MOVE) return result == this.moveResult;
            else return otherName.equals(this.name);
        }
    }
}
