package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HitboxDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.server.entity.RiftMoveExplosion;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class SeedBombBuilder extends CreatureMoveBuilder {
    public SeedBombBuilder() {
        this.setStaminaCost(0.08f)
                .setBasePower(70)
                .setRequireFindTargetToUse()
                .setElement(Element.GRASS)
                .setMoveType(CreatureMoveBuilder.MoveType.SPECIAL)
                .setOnMoveBeginEffect((creature, target) -> CreatureLookUtil.captureHeadAttackLook(creature))
                .setOnMoveHitEffect(creature -> {
                    ProjectileBuilder seedBombProjectile = new ProjectileBuilder().setName("seed_bomb")
                            .setProjectileModel(ProjectileBuilder.ProjectileModel.CUBE)
                            .setOnImpactFromCreatureEffect((iCreature, projectile, hitEntity, hitPos) ->
                                    RiftMoveExplosion.create(projectile, hitEntity, hitPos, 3f)
                            );

                    //when ridden, shoot forward
                    if (creature.getControllingPassenger() != null) {
                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_move.fire"),
                                SoundCategory.NEUTRAL, 1f, 1f
                        );
                        creature.launchProjectile(seedBombProjectile, 3f, 0f);
                    }
                    //otherwise, hit target
                    else {
                        EntityLivingBase target = creature.getAttackTarget();
                        if (target == null || !target.isEntityAlive()) return;

                        Vec3d creaturePos = creature.getPositionVector();
                        creature.getEntityWorld().playSound(
                                null, creaturePos.x, creaturePos.y, creaturePos.z,
                                RiftSounds.getSound("generic_move.fire"),
                                SoundCategory.NEUTRAL, 1.5f, 1f
                        );
                        creature.launchProjectile(seedBombProjectile, target, 3f, 0f);
                    }
                })
                .setUseLookBasedFacing()
                .setRiddenAimingType(CreatureMoveBuilder.RiddenAimingType.RANGED)
                .setRiddenPitchBounds(-75f, 35f)
                .setRiddenCrosshairType(CreatureMoveBuilder.RiddenCrosshairType.POINT)
                //.setCooldown(400)
        ;
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("seed_bomb", MoveCategory.RANGED);
            this.setPriorityPredicate((creature, target) -> target != null && target.isEntityAlive())
                    .addDetectionRule(new HitboxDistanceFromUserDetectionRule(4D))
                    .setDontPathToTarget();
        }
    }
}
