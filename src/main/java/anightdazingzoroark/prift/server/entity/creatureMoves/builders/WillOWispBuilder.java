package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import net.minecraft.entity.EntityLivingBase;

public class WillOWispBuilder extends CreatureMoveBuilder {
    public WillOWispBuilder() {
        this.setStaminaCost(0.02f)
                .setRequireFindTargetToUse()
                .setElement(Element.FIRE)
                .setMoveType(MoveType.STATUS)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature, "willowisp"))
                .setOnMoveHitEffect(creature -> {
                    ProjectileBuilder fireballProjectile = new ProjectileBuilder().setName("willowisp").setUseCubeModel()
                            .setRotateAlongPitch()
                            .setOnImpactFromCreatureEffect((iCreature, projectile, hitEntity, hitPos) -> {
                                if (hitEntity != null) hitEntity.setFire(15);
                            });

                    //when ridden, shoot forward
                    if (creature.getControllingPassenger() != null) {
                        for (int i = 0; i < 4; i++) creature.launchProjectile(fireballProjectile, 3f, 16f);
                    }
                    //otherwise, hit target
                    else {
                        EntityLivingBase target = creature.getAttackTarget();
                        if (target == null || !target.isEntityAlive()) return;

                        for (int i = 0; i < 4; i++) creature.launchProjectile(fireballProjectile, target, 3f, 16f);
                    }
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                .setCooldown(400);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("willowisp");
            this.setPriorityPredicate((creature, target) -> {
                        return (target != null && target.isEntityAlive()) ? 0 : -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D, 16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(4D, 16D))
                    .setDontPathToTarget();
        }
    }
}
