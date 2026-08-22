package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public enum MoveResult {
    USE_MOVE((creature, rule) -> new UseMoveMoveResultTicker(asConcreteCreature(creature), rule)),
    SPRINT((creature, rule) -> new SprintMoveResultTicker(asConcreteCreature(creature), rule)),
    LEAP((creature, rule) -> new LeapMoveResultTicker(asConcreteCreature(creature), rule));

    @NotNull
    private final BiFunction<ICreature, MoveRuleBuilder, AbstractMoveResultTicker> moveResultTicker;

    MoveResult(@NotNull BiFunction<ICreature, MoveRuleBuilder, AbstractMoveResultTicker> moveResultTicker) {
        this.moveResultTicker = moveResultTicker;
    }

    public static AbstractMoveResultTicker createTicker(CreatureMoveResult result, ICreature creature, MoveRuleBuilder rule) {
        MoveResult internalResult = MoveResult.valueOf(result.name());
        return internalResult.moveResultTicker.apply(creature, rule);
    }

    private static RiftCreature asConcreteCreature(ICreature creature) {
        if (creature instanceof RiftCreature riftCreature) return riftCreature;
        throw new IllegalArgumentException("Move result tickers require Prehistoric Rift's creature implementation");
    }
}