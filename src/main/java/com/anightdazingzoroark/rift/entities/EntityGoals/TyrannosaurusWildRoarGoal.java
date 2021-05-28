package com.anightdazingzoroark.rift.entities.EntityGoals;

import com.anightdazingzoroark.rift.entities.Creatures.TyrannosaurusEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class TyrannosaurusWildRoarGoal extends Goal {
    private static final Predicate<Entity> IMMUNE_TO_ROAR = (entity) -> entity.isAlive() && !(entity instanceof TyrannosaurusEntity);
    protected final PathAwareEntity mob;
    Random rand = new Random();

    public TyrannosaurusWildRoarGoal(PathAwareEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if ((this.mob.getAttacker() instanceof Entity) && (this.mob.hurtTime == 0)) {
            int roarChance = rand.nextInt(15);
            System.out.println("roar chance is "+roarChance);
            if ((roarChance == 0) && (this.mob.hurtTime == 0)) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public boolean shouldContinue() {
        if (this.mob.getAttacker() == null) {
            return false;
        }
        else if (this.mob.hurtTime > 0) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void start() {
        System.out.println("start");
        List<Entity> list = this.mob.world.getEntitiesByClass(LivingEntity.class, this.mob.getBoundingBox().expand(25.0D), IMMUNE_TO_ROAR);

        Entity entity;
        for(Iterator var2 = list.iterator(); var2.hasNext(); this.knockback(entity)) {
            entity = (Entity)var2.next();
            if (!(entity instanceof TyrannosaurusEntity)) {
                entity.damage(DamageSource.mob(this.mob), 2.0F);
            }
        }

        Vec3d vec3d = this.mob.getBoundingBox().getCenter();
        for(int i = 0; i < 40; ++i) {
            double d = this.mob.getRandom().nextGaussian() * 0.2D;
            double e = this.mob.getRandom().nextGaussian() * 0.2D;
            double f = this.mob.getRandom().nextGaussian() * 0.2D;
            this.mob.world.addParticle(ParticleTypes.POOF, vec3d.x, vec3d.y, vec3d.z, d, e, f);
        }
    }

    @Override
    public void stop() {
        System.out.println("stop");
    }

    public void knockback(Entity entity) {
        double d = entity.getX() - this.mob.getX();
        double e = entity.getZ() - this.mob.getZ();
        double f = Math.max(d * d + e * e, 0.001D);
        entity.addVelocity(d / f * 15.0D, 0.0125D, e / f * 15.0D);
    }
}
