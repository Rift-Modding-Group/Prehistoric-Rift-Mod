package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.TailAttackLookUtil;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.SoundCategory;

public class HyperCutterBuilder extends CreatureMoveBuilder {
    public HyperCutterBuilder() {
        this.setStaminaCost(0.08f)
                .setBasePower(60)
                .setRequireFindTargetToUse()
                .setElement(Element.NEUTRAL)
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setCooldown(creature -> {
                    int hyperCutterCount = (int) creature.getProperty("HyperCutterCount").getValue();
                    return hyperCutterCount == 0 ? 300: 0; //yeah idk how to explain why this shit works
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
                })
                .setOnMoveHitEffect(creature -> {
                    creature.setProperty("HyperCutterHit", false);
                    creature.createOffenseHitboxesByTag("thagomizer", 4, hitEntity -> {
                        int hyperCutterCount = (int) creature.getProperty("HyperCutterCount").getValue();

                        //play sound
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