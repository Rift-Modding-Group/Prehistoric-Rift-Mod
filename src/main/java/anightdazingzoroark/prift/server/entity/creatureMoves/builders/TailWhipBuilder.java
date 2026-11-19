package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class TailWhipBuilder extends CreatureMoveBuilder {
    public TailWhipBuilder() {
        this.setUseBlockBreak()
                .setMakesContact()
                .setMoveType(MoveType.PHYSICAL)
                .setBasePower(20)
                .setRequireFindTargetToUse()
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature))
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.tail"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );
                    creature.createOffenseHitboxesByTag("tail", 4, hitEntity -> {
                        if (!(hitEntity instanceof EntityLivingBase)) return;
                        creature.knockbackEntity(hitEntity, 0.5D, 0.1D);
                    });
                })
                .setUseLookBasedFacing()
                .setStaminaCost(0.02f)
                .setRiddenAimingType(RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("tail_whip", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new AnimatedHitboxDetectionRule("tail", CreatureLookUtil::captureHeadAttackLook));
        }
    }
}
