package anightdazingzoroark.prift.api.creature.builder.detectionRule;

import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

public class HorizontalDistanceFromUserDetectionRule extends DetectionRule {
    private final double minDistance;
    private final double maxDistance;

    public HorizontalDistanceFromUserDetectionRule(double maxDistance) {
        this(-1, maxDistance);
    }

    public HorizontalDistanceFromUserDetectionRule(double minDistance, double maxDistance) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    @Override
    public boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target) {
        double dist = user.horizontalDistanceFromEntity(target);

        //if minDistance is negative, it means only maxDistance matters
        if (this.minDistance < 0) return dist <= this.maxDistance;
        else if (this.maxDistance >= this.minDistance) return dist >= minDistance && dist <= this.maxDistance;
        throw new IllegalArgumentException("Given maxDistance is smaller than minDistance!");
    }
}
