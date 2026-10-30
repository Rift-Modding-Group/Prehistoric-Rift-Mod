package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeOnceComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class SnarlBuilder extends CreatureMoveBuilder {
    public SnarlBuilder() {
        this.setStaminaCost(0.15f)
                .setRequireFindTargetToUse()
                .setElement(Element.DARK)
                .setMoveType(MoveType.STATUS)
                .setOnMoveHitEffect(creature -> {
                    //make aabb where effects take place
                    double[] creatureSize = creature.getSize();
                    double radius = 6D;
                    AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                            creature.getPositionVector().x - (radius + creatureSize[0] / 2D),
                            creature.getPositionVector().y,
                            creature.getPositionVector().z - (radius + creatureSize[0] / 2D),
                            creature.getPositionVector().x + (radius + creatureSize[0] / 2D),
                            creature.getPositionVector().y + (radius * 2) + creatureSize[1],
                            creature.getPositionVector().z + (radius + creatureSize[0] / 2D)
                    );
                    List<EntityLivingBase> hitEntities = creature.getEntityWorld().getEntitiesWithinAABB(
                            EntityLivingBase.class, axisAlignedBB
                    );
                    for (EntityLivingBase entity : hitEntities) {
                        if (creature.isRelatedToEntity(entity)) continue;
                        entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 200));
                    }
                })
                .setUseLookBasedFacing()
                .setCooldown(300);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("snarl");
            this.setPriorityPredicate((creature, target) -> {
                        if (target != null && target.isEntityAlive() && !creature.bodyTouchingLiquid() && creature.isOnGround()) {
                            return 0;
                        }
                        return -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D))
                    .setDontPathToTarget();
        }
    }
}
