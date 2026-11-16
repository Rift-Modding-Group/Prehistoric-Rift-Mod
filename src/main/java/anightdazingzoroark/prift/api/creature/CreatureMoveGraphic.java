package anightdazingzoroark.prift.api.creature;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A graphic for moves to highlight their impact more.
 */
public record CreatureMoveGraphic(
        @NotNull String name, @NotNull String animation, @Nullable String locator, @NotNull Placement placement
) {
    public CreatureMoveGraphic(@NotNull String name, @NotNull String animation) {
        this(name, animation, null, Placement.USER);
    }

    public CreatureMoveGraphic(@NotNull String name, @NotNull String animation, @Nullable String locator) {
        this(name, animation, locator, Placement.USER);
    }

    public CreatureMoveGraphic {
        if (name.isBlank() || animation.isBlank()) {
            throw new IllegalArgumentException("A move graphic requires a model name and an animation name");
        }
    }

    public enum Placement {
        USER,
        TARGET_GROUND
    }

    //captured playback context for a graphic started by a move effect
    public record Playback(
            @NotNull CreatureMoveGraphic graphic, @NotNull Vec3d origin,
            @NotNull Vec3d destination, long startTime, int sourceEntityId, long moveUseId
    ) {}
}
