package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.projectile.ProjectileTrail;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import net.minecraft.entity.EntityLivingBase;

public class BelchBuilder extends CreatureMoveBuilder {
    public BelchBuilder() {
        this.setStaminaCost(0.2f)
                .setElement(Element.POISON)
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setBasePower(50)
                .setRequireFindTargetToUse()
                .setOnMoveBeginEffect((creature, target) -> {
                    AnimationDataEntityLiving data = creature.getAnimationData();
                    CreatureLookUtil.LookPose look = CreatureLookUtil.getAttackLookPose(creature);
                    data.setVariable("head_attack_pitch", look.pitch());
                    data.setVariable("head_attack_yaw", look.yaw());
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                .setOnMoveHitEffect(creature -> {
                    ProjectileBuilder belchProjectile = new ProjectileBuilder().setName("belch")
                            .setRotateAlongPitch().setProjectileModel(ProjectileBuilder.ProjectileModel.CUBE)
                            .setProjectileTrail(ProjectileTrail.makeBuilderForParticleTrail()
                                    .addParticleComponent(ProjectileTrail.DUST_APPEARANCE.setSize(0.5D, 0.5D))
                                    .addParticleComponent(new AppearanceTintingComponent.Builder().setColor(0x3B5817, false))
                            );

                    //when ridden, shoot forward
                    if (creature.getControllingPassenger() != null) {
                        creature.launchProjectile(belchProjectile, 3f, 0f);
                    }
                    //otherwise, hit target
                    else {
                        EntityLivingBase target = creature.getAttackTarget();
                        if (target == null || !target.isEntityAlive()) return;

                        creature.launchProjectile(belchProjectile, target, 3f, 0f);
                    }
                })
                .setCooldown(100);
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("belch");
            this.setPriorityPredicate((creature, target) -> {
                        return target != null && target.isEntityAlive() ? 0 : -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(6D, 16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(5D, 16D))
                    .setUseWhenFrustrated();
        }
    }
}
