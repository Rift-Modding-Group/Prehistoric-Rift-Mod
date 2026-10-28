package anightdazingzoroark.prift.api.creature.builder.detectionRule;

import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

/**
 * Checks three-dimensional distance between positions, independent of entity size.
 */
public class DistanceFromUserDetectionRule extends DetectionRule {
    private double maximumDistanceSquared;

    public DistanceFromUserDetectionRule(double maximumDistance) {
        if (maximumDistance < 0D) throw new IllegalArgumentException("Maximum distance must be nonnegative!");
        this.maximumDistanceSquared = maximumDistance * maximumDistance;
    }

    @Override
    public boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target) {
        return user.getPositionVector().squareDistanceTo(target.getPositionVector()) <= this.maximumDistanceSquared;
    }
}
