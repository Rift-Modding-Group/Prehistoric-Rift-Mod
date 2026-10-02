package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.config.RiftCreatureFood;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CreatureFoodResult(@Nullable RiftCreatureFood creatureFood) {
    public void applyEffect(@NotNull RiftCreature creature) {}
}
