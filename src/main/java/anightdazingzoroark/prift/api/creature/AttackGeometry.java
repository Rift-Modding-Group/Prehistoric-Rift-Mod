package anightdazingzoroark.prift.api.creature;

import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Shared attack hitbox tag, particle attachment locators, and pose preparation callback.
 * The callback may receive a null target for player-controlled attacks and previews.
 */
public record AttackGeometry(
        @NotNull String hitboxTag, @NotNull List<String> particleLocators,
        @NotNull BiConsumer<ICreature, @Nullable EntityLivingBase> preparePose
) {
    public AttackGeometry {
        particleLocators = List.copyOf(particleLocators);
    }
}
