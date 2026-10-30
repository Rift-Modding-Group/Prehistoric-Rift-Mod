package anightdazingzoroark.prift.api.creature.builder.detectionRule;

import anightdazingzoroark.prift.api.creature.ICreature;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;

public class BoundingBoxDetectionRule extends DetectionRule {
    @NotNull
    private final String boundingBoxName;
    private final boolean useTagName;

    public BoundingBoxDetectionRule(@NotNull String boundingBoxName) {
        this(boundingBoxName, false);
    }

    public BoundingBoxDetectionRule(@NotNull String boundingBoxName, boolean useTagName) {
        this.boundingBoxName = boundingBoxName;
        this.useTagName = useTagName;
    }

    @NotNull
    public String getBoundingBoxName() {
        return this.boundingBoxName;
    }

    public boolean getUseTagName() {
        return this.useTagName;
    }

    @Override
    public boolean targetWithinRange(@NotNull ICreature user, @NotNull EntityLivingBase target) {
        //test hitboxes first
        if (target instanceof IEntityMultiPart) {
            for (Entity part : target.getParts()) {
                if (!(part instanceof MultiPartEntityPart multiPartEntityPart)) continue;
                if (this.getUseTagName() && user.aabbIntersectsBoundingBoxTag(multiPartEntityPart.getEntityBoundingBox(), this.getBoundingBoxName())) {
                    return true;
                }
                else if (user.aabbIntersectsBoundingBox(multiPartEntityPart.getEntityBoundingBox(), this.getBoundingBoxName())) {
                    return true;
                }
            }
        }

        //back to entity
        if (this.getUseTagName()) return user.aabbIntersectsBoundingBoxTag(target.getEntityBoundingBox(), this.getBoundingBoxName());
        return user.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), this.getBoundingBoxName());
    }
}
