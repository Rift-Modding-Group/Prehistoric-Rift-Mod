package anightdazingzoroark.prift.api.creature.builder.detectionRule;

import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

/**
 * Determines whether a target can be reached by a move.
 */
public abstract class DetectionRule {
    public abstract boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target);
}
