package anightdazingzoroark.prift.server.entity.ai.pathfinding;

import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimTarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Inputs to the shared rider-leap entry point
 * Null approval plan explicitly rejects an aimed request.
 * */
public sealed interface RiddenLeapRequest {
    record Charged(int chargeTicks, boolean movingForward) implements RiddenLeapRequest {}

    record Aimed(@NotNull RiddenAimTarget target) implements RiddenLeapRequest {}

    record Approval(@Nullable CreatureLeapPlan plan) implements RiddenLeapRequest {}
}