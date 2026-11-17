package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.AttackGeometry;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.ScratchLookUtil;
import anightdazingzoroark.prift.server.entity.creatureMoves.TailAttackLookUtil;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.SoundCategory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HyperCutterBuilder extends CreatureMoveBuilder {
    private static final AttackGeometry CLAW_ATTACK = new AttackGeometry(
            "clawHitZone", List.of("leftClawAim", "rightClawAim"), ScratchLookUtil::captureScratchLook
    );
    private static final AttackGeometry TAIL_ATTACK = new AttackGeometry(
            "thagomizer", List.of("tailEffectLocator"), TailAttackLookUtil::captureTailStabLook
    );

    public HyperCutterBuilder() {
        this.setUseBlockBreak()
                .setStaminaCost(0.08f)
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
                    AttackGeometry geometry = creature.hasAttackGeometry(CLAW_ATTACK) ? CLAW_ATTACK : TAIL_ATTACK;
                    geometry.preparePose().accept(creature, target);

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

                    for (String locator : geometry.particleLocators()) {
                        RiftLibParticleHelper.createParticle(
                                "prift:charge_level_up_looping",
                                locator,
                                (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                                "variable.emission_radius", Double.toString(0.5D),
                                "variable.charge_color", chargeColor
                        );
                    }
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

                    stopChargeParticles(creature);

                    AttackGeometry geometry = creature.hasAttackGeometry(CLAW_ATTACK) ? CLAW_ATTACK : TAIL_ATTACK;
                    creature.createOffenseHitboxesByTag(geometry.hitboxTag(), 4, hitEntity -> {
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

                            EntityLivingBase hitEntityLiving = null;
                            if (hitEntity instanceof MultiPartEntityPart hitHitbox) hitEntityLiving = (EntityLivingBase) hitHitbox.parent;
                            else if (hitEntity instanceof EntityLivingBase) hitEntityLiving = (EntityLivingBase) hitEntity;
                            if (hitEntityLiving != null) {
                                OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(hitEntityLiving);
                                if (otherEntityProperties != null) otherEntityProperties.setBleeding(2, 200);
                            }
                        }
                        else if (creature.getProperty("HyperCutterHit").getValue() == Boolean.FALSE) {
                            hyperCutterCount++;
                            creature.setProperty("HyperCutterCount", hyperCutterCount);
                            creature.setProperty("HyperCutterHit", true);
                        }
                    });
                })
                .setOnMoveEndEffect(creature -> {
                    creature.setProperty("HyperCutterHit", false);
                    stopChargeParticles(creature);
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    private static void stopChargeParticles(@NotNull ICreature creature) {
        AttackGeometry geometry = creature.hasAttackGeometry(CLAW_ATTACK) ? CLAW_ATTACK : TAIL_ATTACK;
        for (String locator : geometry.particleLocators()) {
            RiftLibParticleHelper.killParticle(
                    (Entity & IAnimatable<?>) creature.getAnimationData().getHolder(),
                    "prift:charge_level_up_looping", locator
            );
        }
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("hyper_cutter", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .setAdditionalAIUses(4)
                    .addDetectionRule(new AnimatedHitboxDetectionRule(TAIL_ATTACK.hitboxTag(), TAIL_ATTACK.preparePose()))
                    .addDetectionRule(new AnimatedHitboxDetectionRule(CLAW_ATTACK.hitboxTag(), CLAW_ATTACK.preparePose()));
        }
    }
}
