package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.ScratchLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class ScratchBuilder extends StandardMeleeBuilder {
    public ScratchBuilder() {
        this.setUseBlockBreak()
                .setBasePower(50)
                .setOnMoveBeginEffect(ScratchLookUtil::captureScratchLook)
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.scratch"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );

                    creature.createOffenseHitboxesByTag("clawHitZone", 4);
                })
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("scratch", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new AnimatedHitboxDetectionRule("clawHitZone", ScratchLookUtil::captureScratchLook));
        }
    }
}
