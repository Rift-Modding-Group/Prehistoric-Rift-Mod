package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRayEllipsoidImpactShape;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class HeavySlamBuilder extends CreatureMoveBuilder {
    public HeavySlamBuilder() {
        this.setStaminaCost(0.2f)
                .setBasePower(120)
                .setCooldown(400)
                .setRequireFindTargetToUse()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setOnMoveHitEffect(creature -> {
                    double impactRadius = 5D;
                    creature.createRay(
                            new RiftLibRayBuilder("prift:heavy_slam")
                                    .setImpactOnly()
                                    .setImpactShape(() -> new RiftLibRayEllipsoidImpactShape(1D, 0.2D, 1D).topOnly())
                                    .setMaxMotionDistance(impactRadius)
                                    .setOnlyOneSegment()
                                    .setMotionSpeed(1.5D)
                                    .setHitHandler((rayCreator, rayOrigin, rayHitResult) -> {
                                        if (!(rayCreator instanceof ICreature source)) return;
                                        for (Entity hitEntity : rayHitResult.hitEntities()) {
                                            if (!(hitEntity instanceof EntityLivingBase hitLiving)) continue;
                                            source.attackEntityAsMob(hitEntity);

                                            int paralysisChance = creature.getEntityWorld().rand.nextInt(3);
                                            if (paralysisChance != 0) continue;
                                            hitLiving.addPotionEffect(new PotionEffect(RiftEffects.PARALYSIS, 300, 1));
                                        }
                                    }),
                            "stompOrigin"
                    );

                    if (!(creature.getEntityWorld() instanceof WorldServer world)) return;
                    Vec3d rayOrigin = creature.getLocatorWorldPos("stompOrigin");
                    BlockPos sourcePos = new BlockPos(rayOrigin);
                    if (world.isAirBlock(sourcePos)) sourcePos = sourcePos.down();
                    IBlockState sourceState = world.getBlockState(sourcePos);
                    if (!sourceState.getMaterial().isSolid()) return;

                    double particleRadius = impactRadius + 1D;
                    double particleY = sourcePos.getY() + sourceState.getBoundingBox(world, sourcePos).maxY + 0.1D;
                    int blockStateId = Block.getStateId(sourceState);
                    for (int particle = 0; particle < 256; particle++) {
                        double radius = Math.sqrt(world.rand.nextDouble()) * particleRadius;
                        double angle = world.rand.nextDouble() * Math.TAU;
                        world.spawnParticle(
                                EnumParticleTypes.BLOCK_CRACK,
                                rayOrigin.x + Math.cos(angle) * radius,
                                particleY,
                                rayOrigin.z + Math.sin(angle) * radius,
                                0, 0D, 1D, 0D, 1.5D,
                                blockStateId
                        );
                    }
                });
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("heavy_slam", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> {
                        return target != null && target.isEntityAlive() && !creature.bodyTouchingLiquid()
                                && creature.isOnGround()
                                && creature.aabbIntersectsBoundingBox(target.getEntityBoundingBox(), "stompHitZone");
                    })
                    .addDetectionRule(new BoundingBoxDetectionRule("stompHitZone"))
                    .setDontPathToTarget();
        }
    }
}
