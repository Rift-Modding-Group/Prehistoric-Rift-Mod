package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class TremorBuilder extends CreatureMoveBuilder {
    public TremorBuilder() {
        this.setStaminaCost(0.2f)
                .setBasePower(70)
                .setRequireFindTargetToUse()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setElement(Element.EARTH)
                .setOnMoveHitEffect(creature -> creature.createRay(
                        new RiftLibRayBuilder("prift:tremor")
                                .setImpactOnly()
                                .setImpactShape(() -> new RiftLibRayEllipsoidImpactShape(1D, 0.2D, 1D).topOnly())
                                .setMaxMotionDistance(8D)
                                .setOnlyOneSegment()
                                .setMotionSpeed(1.5D)
                                .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                    if (!(rayCreator instanceof ICreature source)) return;
                                    for (Entity hitEntity : rayHitResult.hitEntities()) {
                                        if (hitEntity instanceof EntityLivingBase) source.attackEntityAsMob(hitEntity);
                                    }
                                }),
                        "stompOrigin"
                ))
                .setCooldown(300);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("tremor");
            this.setPriorityPredicate((creature, target) -> {
                        if (target != null && target.isEntityAlive() && !creature.bodyTouchingLiquid() && creature.isOnGround()) {
                            return 0;
                        }
                        return -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(8D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(1D, false))
                    .setDontPathToTarget()
                    .setUseWhenFrustrated();
        }
    }
}