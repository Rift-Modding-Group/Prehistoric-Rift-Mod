package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.MoveRuleBuilder;

import java.util.function.BiFunction;

public enum MoveResult {
    USE_MOVE(UseMoveMoveResultTicker::new),
    USE_MOVE_COMBO(null),
    SPRINT(SprintMoveResultTicker::new),
    LEAP(LeapMoveResultTicker::new);

    public final BiFunction<RiftCreature, MoveRuleBuilder, AbstractMoveResultTicker> moveResultTicker;

    MoveResult(BiFunction<RiftCreature, MoveRuleBuilder, AbstractMoveResultTicker> moveResultTicker) {
        this.moveResultTicker = moveResultTicker;
    }
}
