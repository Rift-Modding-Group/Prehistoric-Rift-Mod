package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.AttackGeometry.HitboxTagAttackGeometry;
import anightdazingzoroark.prift.api.creature.AttackGeometry.LocatorAttackGeometry;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.AnimatedHitboxDetectionRule;
import anightdazingzoroark.prift.server.entity.creatureMoves.ScratchLookUtil;
import anightdazingzoroark.prift.server.properties.OtherEntityProperties;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class MetalClawBuilder extends StandardMeleeBuilder {
    private static final HitboxTagAttackGeometry CLAW_ATTACK = new HitboxTagAttackGeometry(
            "clawHitZone", ScratchLookUtil::captureScratchLook
    );
    private static final LocatorAttackGeometry CLAW_LOCATORS = new LocatorAttackGeometry(
            List.of("leftClawAim", "rightClawAim"), CLAW_ATTACK.preparePose()
    );

    public MetalClawBuilder() {
        this.setUseBlockBreak()
                .setElement(Element.STEEL)
                .setBasePower(50)
                .setCooldown(100)
                .setOnMoveBeginEffect((creature, target) -> {
                    CLAW_LOCATORS.preparePose().accept(creature, target);
                    if (!creature.hasAttackGeometry(CLAW_LOCATORS)) return;
                    String locator = LocatorAttackGeometry.getActiveLocator(CLAW_LOCATORS, creature);

                    creature.createMoveGraphic(new CreatureMoveGraphic(
                            "metal_claw", "animation.move_graphic.metal_claw", locator, CreatureMoveGraphic.Placement.LOCATOR
                    ), target);
                })
                .setUseLookBasedFacing()
                .setOnMoveHitEffect(creature -> {
                    Vec3d creaturePos = creature.getPositionVector();
                    creature.getEntityWorld().playSound(
                            null, creaturePos.x, creaturePos.y, creaturePos.z,
                            RiftSounds.getSound("metal_claw.use"),
                            SoundCategory.NEUTRAL, 1f, 1f
                    );

                    creature.createOffenseHitboxesByTag(CLAW_ATTACK.hitboxTag(), 4, hitEntity -> {
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
                    .addDetectionRule(new AnimatedHitboxDetectionRule(CLAW_ATTACK.hitboxTag(), CLAW_ATTACK.preparePose()));
        }
    }
}
