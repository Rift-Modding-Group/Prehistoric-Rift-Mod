package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.TailAttackLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class TailStabBuilder extends StandardMeleeBuilder {
    public TailStabBuilder() {
        this.setUseBlockBreak()
                .setBasePower(50)
                .setOnMoveBeginEffect(TailAttackLookUtil::captureTailStabLook)
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.tail"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );
                    creature.createOffenseHitboxesByTag("thagomizer", 4);
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("tail_stab", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new AnimatedHitboxDetectionRule("thagomizer", TailAttackLookUtil::captureTailStabLook));
        }
    }
}