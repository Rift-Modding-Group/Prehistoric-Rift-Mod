package anightdazingzoroark.prift.api.creature;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * A graphic for moves to highlight their impact more.
 */
public record CreatureMoveGraphic(@NotNull String name, @NotNull String animation) {
    public CreatureMoveGraphic {
        if (name.isBlank() || animation.isBlank()) {
            throw new IllegalArgumentException("A move graphic requires a model name and an animation name");
        }
    }

    //captured playback context for a graphic started by a move effect
    public record Playback(
            @NotNull CreatureMoveGraphic graphic, @NotNull Vec3d origin,
            @NotNull Vec3d destination, long startTime
    ) {}
}
