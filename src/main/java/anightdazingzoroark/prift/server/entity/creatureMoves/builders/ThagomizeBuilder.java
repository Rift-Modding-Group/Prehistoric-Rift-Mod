package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.entity.creatureMoves.TailAttackLookUtil;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;

public class ThagomizeBuilder extends CreatureMoveBuilder {
    public ThagomizeBuilder() {
        this.setMakesContact()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.08f)
                .setStaminaDrainPerSecond(0.02f)
                .setBasePower(50)
                .setPreventMovement()
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                .setOnMoveBeginEffect(TailAttackLookUtil::captureThagomizeLook)
                .setOnMoveHitEffect(creature -> creature.createOffenseHitboxesByTag("thagomizer", 4))
                .setOnMoveEndEffect(creature -> creature.setProperty("ChargeUpLevel", 0))
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeUpThenRelease()
                        .setCanRotateWhileCharging(true)
                        .setWindupEndEffect((creature, charge) -> TailAttackLookUtil.captureThagomizeLook(creature, creature.getAttackTarget()))
                        .setMaxChargeUp(100)
                        .setCooldownMultiplier(6D)
                        .setBasePowerMultiplier((creature, basePower, chargeUp) -> {
                            int chargeIntMultiplier = (int) Math.floor(chargeUp / 20D) + 1;
                            return MathUtil.slopeResult(chargeIntMultiplier, true, 1, 6, 1D, 2D);
                        })
                        .setWindupEffect((creature, chargeUp) -> {
                            int chargeUpLevel = (int) creature.getProperty("ChargeUpLevel").getValue();
                            if (chargeUpLevel >= 5 || chargeUp <= 0 || chargeUp > 100 || chargeUp % 20D != 0)
                                return;

                            //set chargeup level
                            chargeUpLevel++;
                            creature.setProperty("ChargeUpLevel", chargeUpLevel);

                            //show particles
                            AxisAlignedBB creatureAABB = creature.getEntityBoundingBox();
                            double width = creatureAABB.maxX - creatureAABB.minX;
                            double height = creatureAABB.maxY - creatureAABB.minY;
                            RiftLibParticleHelper.createParticle(
                                    "prift:charge_level_up",
                                    creature.getPositionVector().x, creature.getPositionVector().y + height / 2D, creature.getPositionVector().z,
                                    0, 0,
                                    "variable.emission_radius", Double.toString(width * 0.005D)
                            );

                            //play sound
                            if (chargeUpLevel == 1 || chargeUpLevel == 2) {
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
                            else if (chargeUpLevel == 3 || chargeUpLevel == 4) {
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
                            else if (chargeUpLevel == 5) {
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
                        })
                        .setOnHitEntityDuringRelease((creature, chargeUp, hitEntity) -> {
                            if (!(hitEntity instanceof EntityLivingBase hitEntityLiving)) return;
                            OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(hitEntityLiving);
                            if (otherEntityProperties == null) return;
                            int bleedStrength = (int) MathUtil.slopeResult(chargeUp, true, 0, 100, 0, 4);
                            otherEntityProperties.setBleeding(bleedStrength, 100);
                        })
                );
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("thagomize");
            this.setPriorityPredicate((creature, target) -> {
                        return target != null && target.isEntityAlive() && creature.atRageThreshold() ? 0 : -1;
                    })
                    .addDetectionRule(new AnimatedHitboxDetectionRule("thagomizer", TailAttackLookUtil::captureThagomizeLook));
        }
    }
}
