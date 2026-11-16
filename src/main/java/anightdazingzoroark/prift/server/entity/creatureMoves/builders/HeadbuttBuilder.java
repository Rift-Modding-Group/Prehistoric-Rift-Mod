package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class HeadbuttBuilder extends CreatureMoveBuilder {
    public HeadbuttBuilder() {
        this.setUseBlockBreak()
                .setMakesContact()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.02f)
                .setBasePower(50)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature))
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.head"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );

                    creature.createOffenseHitboxesByTag("headAttack", 4);
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("headbutt", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new AnimatedHitboxDetectionRule("headAttack", CreatureLookUtil::captureHeadAttackLook));
        }
    }
}
