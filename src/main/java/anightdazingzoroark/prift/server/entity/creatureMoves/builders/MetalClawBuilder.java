package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.ScratchLookUtil;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class MetalClawBuilder extends StandardMeleeBuilder {
    public MetalClawBuilder() {
        this.setUseBlockBreak()
                .setElement(Element.STEEL)
                .setBasePower(50)
                .setCooldown(100)
                .setOnMoveBeginEffect(ScratchLookUtil::captureScratchLook)
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("metal_claw.use"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );

                    creature.createOffenseHitboxesByTag("clawHitZone", 4, hitEntity -> {
                        if (creature.getEntityWorld().rand.nextInt(4) != 0) return;
                        if (!(hitEntity instanceof EntityLivingBase hitLiving)) return;

                        OtherEntityProperties otherEntityProperties = OtherEntityProperties.get(hitLiving);
                        if (otherEntityProperties == null) return;
                        otherEntityProperties.setBleeding(0, 200);
                    });
                })
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.TARGETED_MELEE)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.WIDE);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("metal_claw", MoveCategory.MELEE);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new AnimatedHitboxDetectionRule("clawHitZone", ScratchLookUtil::captureScratchLook));
        }
    }
}
