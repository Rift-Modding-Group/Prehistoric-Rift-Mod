package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import net.minecraft.entity.EntityLivingBase;

public class PlateFlingBuilder extends CreatureMoveBuilder {
    public PlateFlingBuilder() {
        this.setStaminaCost(0.08f)
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setBasePower(30)
                .setRequireFindTargetToUse()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                .setOnMoveBeginEffect((creature, target) -> creature.getAnimationData().setVariable("plate_fling_shot", 0D))
                .setOnMoveHitEffect(creature -> {
                    AnimationDataEntityLiving data = creature.getAnimationData();
                    data.setVariable("plate_fling_pitch", Math.clamp(data.getHeadPitch(1f), -15f, 15f));
                    data.setVariable("plate_fling_yaw", Math.clamp(data.getHeadYaw(1f), -15f, 15f));
                    data.setVariable("plate_fling_shot", 1D);

                    //when ridden, shoot forward
                    ProjectileBuilder projectileBuilder = new ProjectileBuilder().setName("thrown_stegosaurus_plate")
                            .setRotateAlongPitch().setProjectileModel(ProjectileBuilder.ProjectileModel.CUSTOM);
                    if (creature.getControllingPassenger() != null) {
                        creature.launchProjectile(projectileBuilder, 3f, 0f);
                    }
                    //otherwise, hit target
                    else {
                        EntityLivingBase target = creature.getAttackTarget();
                        if (target == null || !target.isEntityAlive()) return;

                        creature.launchProjectile(projectileBuilder, target, 3f, 0f);
                    }
                });
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("plate_fling");
            this.setPriorityPredicate((creature, target) -> {
                        return (target != null && target.isEntityAlive()) ? 0 : -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(12D, 16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(4D, 16D))
                    .setDontPathToTarget();
        }
    }
}
