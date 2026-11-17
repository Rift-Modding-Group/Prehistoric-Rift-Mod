package anightdazingzoroark.prift.api.creature;

import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Base configuration for an attack's geometry and pose preparation callback.
 * The callback may receive a null target for player-controlled attacks and previews.
 */
public abstract class AttackGeometry {
    @NotNull
    private final BiConsumer<ICreature, @Nullable EntityLivingBase> preparePose;

    protected AttackGeometry(@NotNull BiConsumer<ICreature, @Nullable EntityLivingBase> preparePose) {
        this.preparePose = preparePose;
    }

    @NotNull
    public BiConsumer<ICreature, @Nullable EntityLivingBase> preparePose() {
        return this.preparePose;
    }

    /**
     * Tagged bounding boxes used by an attack and its range detection.
     * */
    public static class HitboxTagAttackGeometry extends AttackGeometry {
        @NotNull
        private final String hitboxTag;

        public HitboxTagAttackGeometry(
                @NotNull String hitboxTag,
                @NotNull BiConsumer<ICreature, @Nullable EntityLivingBase> preparePose
        ) {
            super(preparePose);
            this.hitboxTag = hitboxTag;
        }

        @NotNull
        public String hitboxTag() {
            return this.hitboxTag;
        }
    }

    /**
     * Attachment locators for particles or move graphics, ordered to match the move's animation variants.
     */
    public static class LocatorAttackGeometry extends AttackGeometry {
        @NotNull
        private final List<String> locators;

        public LocatorAttackGeometry(
                @NotNull List<String> locators,
                @NotNull BiConsumer<ICreature, @Nullable EntityLivingBase> preparePose
        ) {
            super(preparePose);
            this.locators = locators;
        }

        @NotNull
        public List<String> locators() {
            return this.locators;
        }

        /**
         * Selects the attachment for the creature's active move animation; a single locator supports every variant.
         * */
        @NotNull
        public static String getActiveLocator(@NotNull LocatorAttackGeometry geometry, @NotNull ICreature creature) {
            if (geometry.locators().isEmpty()) {
                throw new IllegalArgumentException("An attack geometry requires a locator to attach a graphic");
            }
            return geometry.locators().get(Math.clamp(creature.getCurrentMoveAnimationIndex(), 0, geometry.locators().size() - 1));
        }
    }
}
