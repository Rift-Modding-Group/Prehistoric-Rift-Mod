package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class CrunchBuilder extends StandardMeleeBuilder {
    public CrunchBuilder() {
        this.setBasePower(70)
                .setStaminaCost(0.2f)
                .setOnMoveBeginEffect((creature, target) -> {
                    CreatureLookUtil.captureHeadAttackLook(creature);
                    creature.createMoveGraphic(
                            new CreatureMoveGraphic(
                                    "jaws", "animation.move_graphic.crunch",
                                    null, CreatureMoveGraphic.Placement.TARGET_GROUND
                            ),
                            target
                    );
                })
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    creature.createOffenseHitboxesByTag("jaw", 4);
                })
                .setOnHitTargetEffect((creature, target, damage) -> {
                    if (!(target instanceof EntityLivingBase entityLivingBase)) return;
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 300, 2));
                    entityLivingBase.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 300, 2));
                })
                .setRiddenAimingType(RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(RiddenCrosshairType.WIDE)
                .setCooldown(400);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("crunch");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(new AnimatedHitboxDetectionRule("jaw", CreatureLookUtil::captureHeadAttackLook))
                    .setUseBlockBreak();
        }
    }
}
