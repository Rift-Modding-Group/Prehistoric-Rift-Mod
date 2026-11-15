package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class ShieldBashBuilder extends CreatureMoveBuilder {
    public ShieldBashBuilder() {
        this.setMakesContact()
                .setMoveType(MoveType.PHYSICAL)
                .setElement(Element.GUARD)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.1f)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature))
                .setCooldown(200)
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.shield_bash"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );

                    creature.createOffenseHitboxesByTag("headAttack", 4, hitEntity -> {
                        if (!(hitEntity instanceof EntityLivingBase)) return;
                        creature.knockbackEntity(hitEntity, 2D, 0.5D);
                    });
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("shield_bash");
            this.setPriorityPredicate((creature, target) -> target != null ? 3 : -1)
                    .addDetectionRule(new AnimatedHitboxDetectionRule("headAttack", CreatureLookUtil::captureHeadAttackLook))
                    .setUseBlockBreak();
        }
    }
}
