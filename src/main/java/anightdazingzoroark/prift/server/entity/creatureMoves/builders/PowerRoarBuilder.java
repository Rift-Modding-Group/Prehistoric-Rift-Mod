package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRaySphereImpactShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class PowerRoarBuilder extends CreatureMoveBuilder {
    public PowerRoarBuilder() {
        this.setStaminaCost(0.15f)
                .setBasePower(20)
                .setRequireFindTargetToUse()
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setElement(Element.SONIC)
                .setOnMoveHitEffect(creature -> creature.createRay(
                        new RiftLibRayBuilder("prift:power_roar")
                                .setImpactOnly()
                                .setImpactShape(() -> new RiftLibRaySphereImpactShape().topOnly())
                                .setMaxMotionDistance(12D)
                                .setMotionSpeed(1.5D)
                                .setBlockBreakCheck((rayCreator, blockPos) -> {
                                    IBlockState blockState = rayCreator.world.getBlockState(blockPos);
                                    float hardness = blockState.getBlockHardness(rayCreator.world, blockPos);
                                    return hardness <= 1f && hardness >= 0f;
                                })
                                .setImpactCreationInterval(5)
                                .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                    if (!(rayCreator instanceof ICreature source)) return;
                                    for (Entity hitEntity : rayHitResult.hitEntities()) {
                                        if (hitEntity instanceof EntityLivingBase) source.attackEntityAsMob(hitEntity);
                                    }
                                }),
                        "centerPoint"
                ))
                .setOnMoveEndEffect(creature -> RiftLibRayHelper.killRay(
                        creature.getAnimationData().getHolder(), "prift:power_roar"
                ))
                .setCooldown(1200);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("power_roar");
            this.addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D))
                    .setUseWhenFrustrated()
                    .setDontPathToTarget();
        }
    }
}
