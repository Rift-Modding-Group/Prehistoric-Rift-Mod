package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;

import java.util.function.BiFunction;

public enum MoveResult {
    USE_MOVE((creature, rule) -> new UseMoveMoveResultTicker(asConcreteCreature(creature), rule)),
    USE_MOVE_COMBO(null),
    SPRINT((creature, rule) -> new SprintMoveResultTicker(asConcreteCreature(creature), rule)),
    LEAP((creature, rule) -> new LeapMoveResultTicker(asConcreteCreature(creature), rule));

    private final BiFunction<ICreature, MoveRuleBuilder, AbstractMoveResultTicker> moveResultTicker;

    MoveResult(BiFunction<ICreature, MoveRuleBuilder, AbstractMoveResultTicker> moveResultTicker) {
        this.moveResultTicker = moveResultTicker;
    }

    public static AbstractMoveResultTicker createTicker(
            CreatureMoveResult result,
            ICreature creature,
            MoveRuleBuilder rule
    ) {
        MoveResult internalResult = MoveResult.valueOf(result.name());
        if (internalResult.moveResultTicker == null) {
            throw new UnsupportedOperationException("Move result " + result + " is not implemented");
        }
        return internalResult.moveResultTicker.apply(creature, rule);
    }

    private static RiftCreature asConcreteCreature(ICreature creature) {
        if (creature instanceof RiftCreature riftCreature) return riftCreature;
        throw new IllegalArgumentException("Move result tickers require Prehistoric Rift's creature implementation");
    }
}
