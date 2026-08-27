package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.api.creature.CreatureMoveResult;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public enum MoveResult {
    USE_MOVE(
            (creature, rule) -> new UseMoveMoveResultTicker(asConcreteCreature(creature), rule),
            (moveBuilder, interval) -> moveBuilder == null ? -1f
                    : moveBuilder.getStaminaCost() + moveBuilder.getStaminaDrainPerSecond() * interval / 20f,
            5
    ),
    SPRINT(
            (creature, rule) -> new SprintMoveResultTicker(asConcreteCreature(creature), rule),
            (moveBuilder, interval) -> 0.01f,
            5
    ),
    LEAP(
            (creature, rule) -> new LeapMoveResultTicker(asConcreteCreature(creature), rule),
            (moveBuilder, interval) -> 0.1f,
            1
    );

    @NotNull
    private final BiFunction<ICreature, MoveRuleBuilder, AbstractMoveResultTicker> moveResultTicker;
    @NotNull
    private final BiFunction<CreatureMoveBuilder, Integer, Float> staminaConsumption;
    private final int staminaConsumptionInterval;

    MoveResult(
            @NotNull BiFunction<ICreature, MoveRuleBuilder, AbstractMoveResultTicker> moveResultTicker,
            @NotNull BiFunction<CreatureMoveBuilder, Integer, Float> staminaConsumption,
            int staminaConsumptionInterval
    ) {
        this.moveResultTicker = moveResultTicker;
        this.staminaConsumption = staminaConsumption;
        this.staminaConsumptionInterval = staminaConsumptionInterval;
    }

    public static AbstractMoveResultTicker createTicker(CreatureMoveResult result, ICreature creature, MoveRuleBuilder rule) {
        MoveResult internalResult = MoveResult.valueOf(result.name());
        return internalResult.moveResultTicker.apply(creature, rule);
    }

    private static RiftCreature asConcreteCreature(ICreature creature) {
        if (creature instanceof RiftCreature riftCreature) return riftCreature;
        throw new IllegalArgumentException("Move result tickers require Prehistoric Rift's creature implementation");
    }

    /**
     * meant for sprinting and leaping only
     * */
    public float staminaConsumption() {
        return this.staminaConsumption(null);
    }

    /**
     * meant for use for moves only
     * */
    public float staminaConsumption(@Nullable CreatureMoveBuilder creatureMoveBuilder) {
        return this.staminaConsumption.apply(creatureMoveBuilder, this.staminaConsumptionInterval);
    }

    /**
     * By how many ticks
     * */
    public int staminaConsumptionInterval() {
        return this.staminaConsumptionInterval;
    }
}
