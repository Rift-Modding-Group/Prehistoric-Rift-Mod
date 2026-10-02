package anightdazingzoroark.prift.api.creature;

import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Fired after Prehistoric Rift has registered its built-in creatures and before
 * creature entities and JSON defaults are created.
 *
 * <p>Addons should register every creature from a subscriber to this event.
 * Registrations are retained in call order after all built-in creatures.</p>
 */
public final class RiftCreatureRegistrationEvent extends Event {
    private final BiConsumer<String, RiftCreatureBuilder> registrar;

    public RiftCreatureRegistrationEvent(@NotNull BiConsumer<String, RiftCreatureBuilder> registrar) {
        this.registrar = Objects.requireNonNull(registrar, "registrar");
    }

    public void registerCreature(@NotNull String name, @NotNull RiftCreatureBuilder builder) {
        this.registrar.accept(name, builder);
    }
}
