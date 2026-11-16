package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;

public class ScaleShedBuilder extends CreatureMoveBuilder {
    public ScaleShedBuilder() {
        this.setStaminaCost(0.25f)
                .setRequireFindTargetToUse()
                .setElement(Element.GUARD)
                .setMoveType(CreatureMoveBuilder.MoveType.STATUS)
                .setOnMoveBeginEffect((creature, target) -> {
                    RiftLibSoundHelper.playLoopingSound(
                            creature.getAnimationData().getHolder(),
                            RiftSounds.getSound("generic_move.scale_shed"),
                            SoundCategory.NEUTRAL
                    );
                })
                .setOnMoveEndEffect(creature -> {
                    RiftLibSoundHelper.stopLoopingSound(
                            creature.getAnimationData().getHolder(),
                            RiftSounds.getSound("generic_move.scale_shed"),
                            SoundCategory.NEUTRAL
                    );
                    creature.addPotionEffect(new PotionEffect(RiftEffects.DEFENSE_DOWN, 600, 2));
                    creature.addPotionEffect(new PotionEffect(MobEffects.SPEED, 600, 2));
                })
                .setUseLookBasedFacing()
                .setCooldown(900);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("scale_shed", MoveCategory.STATUS);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive());
        }
    }
}
