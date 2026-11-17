package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.BoundingBoxDetectionRule;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class KickBuilder extends CreatureMoveBuilder {
    public KickBuilder() {
        this.setUseBlockBreak()
                .setMakesContact()
                .setMoveType(CreatureMoveBuilder.MoveType.STATUS)
                .setRequireFindTargetToUse()
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("generic_move.kick"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );
                    creature.createOffenseHitboxesByTag("frontZone", 4, hitEntity -> {
                        if (!(hitEntity instanceof EntityLivingBase)) return;
                        creature.knockbackEntity(hitEntity, 0.5D, 0.1D);
                    });
                })
                .setUseLookBasedFacing()
                .setStaminaCost(0.15f)
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE)
                .setCooldown(100);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("kick", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new BoundingBoxDetectionRule("frontZone", true));
        }
    }
}
