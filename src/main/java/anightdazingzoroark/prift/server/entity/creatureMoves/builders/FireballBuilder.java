package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import net.minecraft.entity.EntityLivingBase;

public class FireballBuilder extends CreatureMoveBuilder {
    public FireballBuilder() {
        this.setStaminaCost(0.08f)
                .setBasePower(50)
                .setRequireFindTargetToUse()
                .setElement(Element.FIRE)
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "fireball"))
                .setOnMoveHitEffect(creature -> {
                    ProjectileBuilder fireballProjectile = new ProjectileBuilder().setName("fireball").setUseCubeModel().setRotateAlongPitch();

                    //when ridden, shoot forward
                    if (creature.getControllingPassenger() != null) {
                        creature.launchProjectile(fireballProjectile, 3f, 0f);
                    }
                    //otherwise, hit target
                    else {
                        EntityLivingBase target = creature.getAttackTarget();
                        if (target == null || !target.isEntityAlive()) return;

                        creature.launchProjectile(fireballProjectile, target, 3f, 0f);
                    }
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                .setCooldown(100);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("fireball");
            this.setPriorityPredicate((creature, target) -> {
                        return (target != null && target.isEntityAlive()) ? 0 : -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D, 16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(4D, 16D))
                    .setDontPathToTarget();
        }
    }
}
