package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

public class BideBuilder extends CreatureMoveBuilder {
    public BideBuilder() {
        this.setMakesContact()
                .setElement(Element.GUARD)
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setRequireFindTargetToUse()
                .setStaminaCost(0.08f)
                .setStaminaDrainPerSecond(0.02f)
                .setPreventMovement()
                .setOnMoveBeginEffect((creature, target) -> creature.setProperty("BideReceivedDamage", 0D))
                .setOnMoveHitEffect(creature -> {
                    double releasedDamage = (double) creature.getProperty("BideReceivedDamage").getValue() * 2D;
                    creature.affectEntitiesWithinBounds(4, entity -> {
                        entity.attackEntityFrom(
                                DamageSource.causeMobDamage((EntityLivingBase) creature),
                                (float) releasedDamage
                        );
                    });
                })
                .setOnMoveEndEffect(creature -> creature.setProperty("BideReceivedDamage", 0D))
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeUpThenRelease()
                        .setReleaseOnEndWindup()
                        .setMaxChargeUp(100)
                );
    }
}