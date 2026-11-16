package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import net.minecraft.util.SoundCategory;

public class DrillRushBuilder extends CreatureMoveBuilder {
    public DrillRushBuilder() {
        this.setMakesContact()
                .setElement(Element.STEEL)
                .setMoveType(MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.25f)
                .setStaminaDrainPerSecond(0.0125f)
                .setBasePower(70)
                .setRiddenAimingType(RiddenAimingType.CHARGE)
                .setRiddenPitchBounds(0f, 0f)
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeType(CreatureMoveChargeupBuilder.ChargeType.CHARGE_UP_WHILE_USE)
                        .setCanRotateWhileCharging()
                        .setMaxChargeUp(20)
                        .setCooldownMultiplier(30D)
                        .setPrereleaseEndEffect(creature -> {
                            creature.prepareRush(4D, 60, false, false, null);
                            creature.createMoveGraphic(new CreatureMoveGraphic(
                                    "drill", "animation.move_graphic.drill_rush", "drillRush"
                            ), null);
                            RiftLibSoundHelper.playLoopingSound(
                                    creature.getAnimationData().getHolder(),
                                    RiftSounds.getSound("generic_move.drill"),
                                    SoundCategory.NEUTRAL
                            );
                        })
                        .setReleaseEndEffect(creature -> RiftLibSoundHelper.stopLoopingSound(
                                creature.getAnimationData().getHolder(),
                                RiftSounds.getSound("generic_move.drill"),
                                SoundCategory.NEUTRAL
                        ))
                )
                .setOnMoveEndEffect(creature -> RiftLibSoundHelper.stopLoopingSound(
                        creature.getAnimationData().getHolder(),
                        RiftSounds.getSound("generic_move.drill"),
                        SoundCategory.NEUTRAL
                ));
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("drill_rush", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> {
                        return target != null && target.isEntityAlive() && creature.isOnGround()
                                && !creature.bodyTouchingLiquid() && creature.hasStraightWalkingPathTo(target);
                    })
                    .setDontPathToTarget();
        }
    }
}
