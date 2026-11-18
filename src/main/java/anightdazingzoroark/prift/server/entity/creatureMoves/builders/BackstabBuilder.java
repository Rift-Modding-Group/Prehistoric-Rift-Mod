package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import net.minecraft.client.resources.I18n;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.ScratchLookUtil;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class BackstabBuilder extends StandardMeleeBuilder {
    public BackstabBuilder() {
        this.setElement(Element.DARK)
                .setStaminaCost(0.2f)
                .setBasePower(120)
                .setCooldown(900)
                .setCanUse(creature -> !creature.isInAnyEntityLineOfSight(), I18n.format("move.creature.backstab.cannot_use"))
                .setOnMoveBeginEffect((creature, target) -> {
                    ScratchLookUtil.captureScratchLook(creature, target);
                    creature.createMoveGraphic(new CreatureMoveGraphic("knife", "animation.move_graphic.backstab"), target);
                    RiftLibSoundHelper.playLoopingSound(
                            creature.getAnimationData().getHolder(),
                            RiftSounds.getSound("generic_move.weakness_exploiter_1"),
                            SoundCategory.NEUTRAL
                    );
                })
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    RiftLibSoundHelper.stopLoopingSound(
                            creature.getAnimationData().getHolder(),
                            RiftSounds.getSound("generic_move.weakness_exploiter_1"),
                            SoundCategory.NEUTRAL
                    );

                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.weakness_exploiter_2"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );

                    creature.createOffenseHitboxesByTag("clawHitZone", 4);
                })
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("backstab", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new AnimatedHitboxDetectionRule("clawHitZone", ScratchLookUtil::captureScratchLook));
        }
    }
}
