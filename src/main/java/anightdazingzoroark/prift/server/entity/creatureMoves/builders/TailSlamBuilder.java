package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class TailSlamBuilder extends CreatureMoveBuilder {
    public TailSlamBuilder() {
        this.setUseBlockBreak()
                .setMakesContact()
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.tail"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );
                    creature.createOffenseHitboxesByTag("frontZone", 4);
                })
                .setUseLookBasedFacing()
                .setStaminaCost(0.15f)
                .setBasePower(70)
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                .setCooldown(400);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("tail_slam", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new BoundingBoxDetectionRule("frontZone", true));
        }
    }
}
