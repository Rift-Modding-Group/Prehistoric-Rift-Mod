package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class StompBuilder extends CreatureMoveBuilder {
    public StompBuilder() {
        this.setStaminaCost(0.02f)
                .setBasePower(30)
                .setRequireFindTargetToUse()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setOnMoveHitEffect(creature -> creature.createRay(
                        new RiftLibRayBuilder("prift:stomp")
                                .setImpactOnly()
                                .setImpactShape(() -> new RiftLibRayEllipsoidImpactShape(1D, 0.2D, 1D).topOnly())
                                .setMaxMotionDistance(5D)
                                .setOnlyOneSegment()
                                .setMotionSpeed(1.5D)
                                .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                    if (!(rayCreator instanceof ICreature source)) return;
                                    for (Entity hitEntity : rayHitResult.hitEntities()) {
                                        if (hitEntity instanceof EntityLivingBase) source.attackEntityAsMob(hitEntity);
                                    }
                                }),
                        "stompOrigin"
                ));
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("stomp");
            this.setPriorityPredicate((creature, target) -> {
                        if (target != null && target.isEntityAlive() && !creature.bodyTouchingLiquid()
                                && creature.isOnGround()
                                && creature.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), "stompHitZone")
                        ) {
                            return 0;
                        }
                        return -1;
                    })
                    .addDetectionRule(new BoundingBoxDetectionRule("stompHitZone"))
                    .setDontPathToTarget();
        }
    }
}
