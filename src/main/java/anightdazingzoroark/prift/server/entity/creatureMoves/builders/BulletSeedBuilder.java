package anightdazingzoroark.prift.server.entity.creatureMoves.builders;

import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.api.creature.builder.MoveRuleBuilder;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.HorizontalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.creature.builder.detectionRule.VerticalDistanceFromUserDetectionRule;
import anightdazingzoroark.prift.api.projectile.ProjectileBuilder;
import anightdazingzoroark.prift.server.sound.RiftSounds;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class BulletSeedBuilder extends CreatureMoveBuilder {
    public BulletSeedBuilder() {
        this.setStaminaCost(0.08f)
                .setStaminaDrainPerSecond(0.0125f)
                .setBasePower(10)
                .setRequireFindTargetToUse()
                .setElement(Element.GRASS)
                .setMoveType(CreatureMoveBuilder.MoveType.PHYSICAL)
                .setMoveChargeupBuilder(new CreatureMoveChargeupBuilder()
                        .setChargeUpWhileUse(true)
                        .setCanLookWhileUsing()
                        .setMaxChargeUp(100)
                        .setReleaseDuringUseEffect((creature, charge, target) -> {
                            if (charge % 5 != 0) return;
                            ProjectileBuilder bulletSeedProjectile = new ProjectileBuilder().setName("bullet_seed").setScale(0.25f)
                                    .setProjectileModel(ProjectileBuilder.ProjectileModel.FLAT);

                            //when ridden, shoot forward
                            if (creature.getControllingPassenger() != null) {
                                Vec3d creaturePos = creature.getPositionVector();
                                creature.getEntityWorld().playSound(
                                        null, creaturePos.x, creaturePos.y, creaturePos.z,
                                        RiftSounds.getSound("generic_move.projectile"),
                                        SoundCategory.NEUTRAL, 1f, 1f
                                );
                                creature.launchProjectile(bulletSeedProjectile, 3f, 16f);
                            }
                            //otherwise, hit target
                            else {
                                if (target == null || !target.isEntityAlive()) return;

                                Vec3d creaturePos = creature.getPositionVector();
                                creature.getEntityWorld().playSound(
                                        null, creaturePos.x, creaturePos.y, creaturePos.z,
                                        RiftSounds.getSound("generic_move.projectile"),
                                        SoundCategory.NEUTRAL, 1f, 1f
                                );
                                creature.launchProjectile(bulletSeedProjectile, target, 3f, 16f);
                            }
                        })
                        .setCooldownMultiplier(1D)
                )
                .setUseLookBasedFacing();
    }

    public static class Rule extends MoveRuleBuilder {
        public Rule() {
            super("bullet_seed");
            this.setPriorityPredicate((creature, target) -> {
                        return (target != null && target.isEntityAlive()) ? 0 : -1;
                    })
                    .addDetectionRule(new HorizontalDistanceFromUserDetectionRule(16D))
                    .addDetectionRule(new VerticalDistanceFromUserDetectionRule(16D));
        }
    }
}
