package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.TailAttackLookUtil;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleCameraMode;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class HyperCutterBuilder extends CreatureMoveBuilder {
    public HyperCutterBuilder() {
        this.setStaminaCost(0.08f)
                .setBasePower(60)
                .setRequireFindTargetToUse()
                .setElement(Element.NEUTRAL)
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setCooldown(creature -> {
                    boolean hyperCutterReset = (boolean) creature.getProperty("HyperCutterReset").getValue();
                    creature.setProperty("HyperCutterReset", false);
                    return hyperCutterReset ? 300: 0;
                })
                .setMovePowerMultiplier((creature, hitTarget) -> {
                    int hyperCutterCount = (int) creature.getProperty("HyperCutterCount").getValue();
                    return 1D + hyperCutterCount * 0.25D;
                })
                .setOnMoveBeginEffect((creature, target) -> {
                    //look
                    TailAttackLookUtil.captureTailStabLook(creature, target);

                    //hyper cutter count set
                    if (!creature.hasProperty("HyperCutterCount")) creature.setProperty("HyperCutterCount", 0);
                    if (!creature.hasProperty("HyperCutterHit")) creature.setProperty("HyperCutterHit", false);
                    if (!creature.hasProperty("HyperCutterReset")) creature.setProperty("HyperCutterReset", false);

                    //particle effect
                    int hyperCutterCount = (int) creature.getProperty("HyperCutterCount").getValue();
                    String chargeColor;
                    if (hyperCutterCount == 0 || hyperCutterCount == 1) chargeColor = "#FFFFFF";
                    else if (hyperCutterCount == 2 || hyperCutterCount == 3) chargeColor = "#FFFF00";
                    else if (hyperCutterCount == 4) chargeColor = "#FF0000";
                    else chargeColor = "#FFFFFF";

                    RiftLibParticleHelper.createParticle(
                            "prift:charge_level_up_looping",
                            "tailEffectLocator",
                            (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                            "variable.emission_radius", Double.toString(0.5D),
                            "variable.charge_color", chargeColor
                    );
                })
                .setOnMoveHitEffect(creature -> {
                    creature.setProperty("HyperCutterHit", false);
                    creature.getEntityWorld().playSound(
                            null,
                            creature.getPositionVector().x,
                            creature.getPositionVector().y,
                            creature.getPositionVector().z,
                            RiftSounds.getSound("generic_move.slash"),
                            SoundCategory.NEUTRAL,
                            1f, 1f
                    );

                    RiftLibParticleHelper.killParticle(
                            (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                            "prift:charge_level_up_looping",
                            "tailEffectLocator"
                    );

                    creature.createOffenseHitboxesByTag("thagomizer", 4, hitEntity -> {
                        int hyperCutterCount = (int) creature.getProperty("HyperCutterCount").getValue();

                        if (hyperCutterCount == 0 || hyperCutterCount == 1) {
                            creature.getEntityWorld().playSound(
                                    null,
                                    creature.getPositionVector().x,
                                    creature.getPositionVector().y,
                                    creature.getPositionVector().z,
                                    RiftSounds.getSound("generic_move.chargeup_weak"),
                                    SoundCategory.NEUTRAL,
                                    1f, 1f
                            );
                        }
                        else if (hyperCutterCount == 2 || hyperCutterCount == 3) {
                            creature.getEntityWorld().playSound(
                                    null,
                                    creature.getPositionVector().x,
                                    creature.getPositionVector().y,
                                    creature.getPositionVector().z,
                                    RiftSounds.getSound("generic_move.chargeup_medium"),
                                    SoundCategory.NEUTRAL,
                                    1f, 1f
                            );
                        }
                        else if (hyperCutterCount == 4) {
                            creature.getEntityWorld().playSound(
                                    null,
                                    creature.getPositionVector().x,
                                    creature.getPositionVector().y,
                                    creature.getPositionVector().z,
                                    RiftSounds.getSound("generic_move.chargeup_strong"),
                                    SoundCategory.NEUTRAL,
                                    1f, 1f
                            );
                        }

                        //increase or reset count
                        if (hyperCutterCount >= 4) {
                            creature.setProperty("HyperCutterCount", 0);
                            creature.setProperty("HyperCutterHit", true);
                            creature.setProperty("HyperCutterReset", true);

                            EntityLivingBase hitEntityLiving;
                            if (hitEntity instanceof MultiPartEntityPart hitHitbox) hitEntityLiving = (EntityLivingBase) hitHitbox.parent;
                            else hitEntityLiving = (EntityLivingBase) hitEntity;
                            OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(hitEntityLiving);
                            if (otherEntityProperties != null) otherEntityProperties.setBleeding(2, 200);
                        }
                        else if (creature.getProperty("HyperCutterHit").getValue() == Boolean.FALSE) {
                            hyperCutterCount++;
                            creature.setProperty("HyperCutterCount", hyperCutterCount);
                            creature.setProperty("HyperCutterHit", true);
                        }
                    });
                })
                .setOnMoveEndEffect(creature -> creature.setProperty("HyperCutterHit", false))
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("hyper_cutter");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(new AnimatedHitboxDetectionRule("thagomizer", TailAttackLookUtil::captureTailStabLook))
                    .setUseBlockBreak();
        }
    }
}