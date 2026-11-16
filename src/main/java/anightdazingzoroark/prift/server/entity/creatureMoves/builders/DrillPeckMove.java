package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.DistanceFromUserDetectionRule;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;

public class DrillPeckMove extends StandardMeleeBuilder {
    private static final DistanceFromUserDetectionRule peckRange = new DistanceFromUserDetectionRule(3D);

    public DrillPeckMove() {
        this.setBasePower(70)
                .setCooldown(200)
                .setUseLookBasedFacing()
                .setOnMoveBeginEffect((creature, target) -> {
                    creature.createMoveGraphic(new CreatureMoveGraphic("drill", "animation.move_graphic.drill_peck"), target);
                    RiftLibSoundHelper.playLoopingSound(
                            creature.getAnimationData().getHolder(),
                            RiftSounds.getSound("generic_move.drill"),
                            SoundCategory.NEUTRAL
                    );
                })
                .setOnMoveHitEffect(creature -> {
                    EntityLivingBase target = creature.getAttackTarget();
                    if (!creature.getEntityWorld().isRemote && target != null && target.isEntityAlive()
                            && peckRange.targetWithinRange(creature, target)
                    ) {
                        creature.attackEntityAsMob(target);
                    }

                    RiftLibSoundHelper.stopLoopingSound(
                            creature.getAnimationData().getHolder(),
                            RiftSounds.getSound("generic_move.drill"),
                            SoundCategory.NEUTRAL
                    );
                })
                .setOnMoveEndEffect(creature -> RiftLibSoundHelper.stopLoopingSound(
                        creature.getAnimationData().getHolder(),
                        RiftSounds.getSound("generic_move.drill"),
                        SoundCategory.NEUTRAL
                ));
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("drill_peck", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null)
                    .addDetectionRule(peckRange);
        }
    }
}
