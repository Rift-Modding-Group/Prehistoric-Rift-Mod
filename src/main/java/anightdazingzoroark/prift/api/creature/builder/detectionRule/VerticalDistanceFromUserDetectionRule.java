package anightdazingzoroark.prift.api.creature.builder.detectionRule;

import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

public class VerticalDistanceFromUserDetectionRule extends DetectionRule {
    private final double minDistance;
    private final double maxDistance;
    private final boolean useCenter;

    public VerticalDistanceFromUserDetectionRule(double maxDistance) {
        this(-1, maxDistance);
    }

    public VerticalDistanceFromUserDetectionRule(double maxDistance, boolean useCenter) {
        this(-1, maxDistance, useCenter);
    }

    public VerticalDistanceFromUserDetectionRule(double minDistance, double maxDistance) {
        this(minDistance, maxDistance, true);
    }

    public VerticalDistanceFromUserDetectionRule(double minDistance, double maxDistance, boolean useCenter) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.useCenter = useCenter;
    }

    @Override
    public boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target) {
        double dist = user.verticalDistanceFromEntity(target, this.useCenter);

        //if minDistance is negative, it means only maxDistance matters
        if (this.minDistance < 0) return dist <= this.maxDistance;
        else if (this.maxDistance >= this.minDistance) return dist >= minDistance && dist <= this.maxDistance;
        throw new IllegalArgumentException("Given maxDistance is smaller than minDistance!");
    }
}