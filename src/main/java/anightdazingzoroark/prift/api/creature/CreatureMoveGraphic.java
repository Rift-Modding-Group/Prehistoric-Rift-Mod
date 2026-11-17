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
        if (placement == Placement.LOCATOR && (locator == null || locator.isBlank())) {
            throw new IllegalArgumentException("A locator-attached move graphic requires a locator");
        }
    }

    public enum Placement {
        USER,
        TARGET_GROUND,
        /** Follows the locator's position and rotation, scaled with the creature. */
        LOCATOR
    }

    //captured playback context for a graphic started by a move effect
    public record Playback(
            @NotNull CreatureMoveGraphic graphic, @NotNull Vec3d origin,
            @NotNull Vec3d destination, long startTime, int sourceEntityId, long moveUseId
    ) {}
}
