package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class CrunchBuilder extends StandardMeleeBuilder {
    public CrunchBuilder() {
        this.setBasePower(70)
                .setStaminaCost(0.2f)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "bite"))
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    creature.createOffenseHitboxesByTag("jaw", 4);
                })
                .setOnHitTargetEffect((creature, target) -> {
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
                    .addDetectionRule(new AnimatedHitboxDetectionRule("jaw", creature -> CreatureLookUtil.captureHeadAttackLook(creature, "bite")))
                    .setUseBlockBreak();
        }
    }
}
