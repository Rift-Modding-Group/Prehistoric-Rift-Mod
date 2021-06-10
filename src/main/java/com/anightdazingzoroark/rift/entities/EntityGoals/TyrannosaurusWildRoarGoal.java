package com.anightdazingzoroark.rift.entities.EntityGoals;

import com.anightdazingzoroark.rift.entities.Creatures.TyrannosaurusEntity;
import com.anightdazingzoroark.rift.entities.RiftCreature;
import com.anightdazingzoroark.rift.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class TyrannosaurusWildRoarGoal extends Goal {
    private static final Predicate<Entity> IMMUNE_TO_ROAR = (entity) -> entity.isAlive() && !(entity instanceof TyrannosaurusEntity);
    protected final RiftCreature mob;
    private int wildRoarTick;
    Random rand = new Random();

    public TyrannosaurusWildRoarGoal(RiftCreature mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if ((this.mob.getAttacker() instanceof Entity) && this.wildRoarTick == 0) {
            return true;
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
        else if (this.wildRoarTick > 60) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void start() {
        int roarChance = rand.nextInt(4);
        if (roarChance == 0) {
            this.mob.setRoaring(true);
            this.mob.playSound(ModSounds.ROAR_TYRANNOSAURUS_EVENT, 1, 1);
            List<? extends LivingEntity> list = this.mob.world.getEntitiesByClass(LivingEntity.class, this.mob.getBoundingBox().expand(25.0D), IMMUNE_TO_ROAR);

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
    }

    @Override
    public void tick() {
        this.wildRoarTick++;
        System.out.println(this.wildRoarTick);
    }

    @Override
    public void stop() {
        this.mob.setRoaring(false);
        this.wildRoarTick = 0;
    }

    public void knockback(Entity entity) {
        double d = entity.getX() - this.mob.getX();
        double e = entity.getZ() - this.mob.getZ();
        double f = Math.max(d * d + e * e, 0.001D);
        entity.addVelocity(d / f * 15.0D, 0.0125D, e / f * 15.0D);
    }
}
