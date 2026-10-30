package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class TremorBuilder extends CreatureMoveBuilder {
    public TremorBuilder() {
        this.setStaminaCost(0.2f)
                .setBasePower(70)
                .setRequireFindTargetToUse()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setElement(Element.EARTH)
                .setOnMoveHitEffect(creature -> {
                    creature.createRay(
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
                                        if (!(rayCreator.world instanceof WorldServer world)) return;
                                        for (BlockPos hitBlockPos : rayHitResult.hitBlockPositions()) {
                                            // The wave also hits air immediately above the ground.
                                            BlockPos particlePos = hitBlockPos;
                                            if (world.isAirBlock(particlePos)) particlePos = particlePos.down();
                                            IBlockState state = world.getBlockState(particlePos);
                                            if (!state.getMaterial().isSolid()) continue;

                                            world.spawnParticle(
                                                    EnumParticleTypes.BLOCK_CRACK,
                                                    particlePos.getX() + 0.5D,
                                                    particlePos.getY() + state.getBoundingBox(world, particlePos).maxY + 0.1D,
                                                    particlePos.getZ() + 0.5D,
                                                    12, 0.35D, 0.1D, 0.35D, 0.15D,
                                                    Block.getStateId(state)
                                            );
                                        }
                                    }),
                            "stompOrigin"
                    );
                })
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
