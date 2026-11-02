package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.server.effect.RiftEffects;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class ScaleShedBuilder extends CreatureMoveBuilder {
    public ScaleShedBuilder() {
        this.setStaminaCost(0.25f)
                .setRequireFindTargetToUse()
                .setElement(Element.GUARD)
                .setMoveType(CreatureMoveBuilder.MoveType.STATUS)
                .setOnMoveHitEffect(creature -> {
                    creature.addPotionEffect(new PotionEffect(RiftEffects.DEFENSE_DOWN, 600, 2));
                    creature.addPotionEffect(new PotionEffect(MobEffects.SPEED, 600, 2));
                })
                .setUseLookBasedFacing()
                .setCooldown(900);
    }
}
