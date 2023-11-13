package anightdazingzoroark.rift.server.entity.ai;

import anightdazingzoroark.rift.server.entity.RiftEgg;
import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class RiftMate extends EntityAIBase {
    private final RiftCreature creature;
    private final Class <? extends RiftCreature> mateClass;
    private final World world;
    private RiftCreature targetMate;
    private int spawnBabyDelay;

    public RiftMate(RiftCreature creature) {
        this.creature = creature;
        this.world = creature.world;
        this.mateClass = creature.getClass();
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        if (!this.creature.isInLove()) return false;
        else {
            this.targetMate = this.getNearbyMate();
            return this.targetMate != null;
        }
    }

    public boolean shouldContinueExecuting() {
        return this.targetMate.isEntityAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
    }

    public void resetTask() {
        this.targetMate = null;
        this.spawnBabyDelay = 0;
    }

    public void updateTask() {
        this.creature.getLookHelper().setLookPositionWithEntity(this.targetMate, 10.0F, (float)this.creature.getVerticalFaceSpeed());
        this.creature.getNavigator().tryMoveToEntityLiving(this.targetMate, 1D);
        ++this.spawnBabyDelay;
        System.out.println(this.spawnBabyDelay);
        System.out.println(this.creature.getDistanceSq(this.targetMate));

        if (this.spawnBabyDelay >= 20 && this.creature.getDistanceSq(this.targetMate) <= 24.0D) {
            this.spawnEgg();
        }
    }

    private RiftCreature getNearbyMate() {
        double d0 = Double.MAX_VALUE;
        RiftCreature riftCreature = null;

        for (RiftCreature potentialMate : this.world.<RiftCreature>getEntitiesWithinAABB(this.mateClass, this.creature.getEntityBoundingBox().grow(8.0D))) {
            if (this.creature.canMateWith(potentialMate) && this.creature.getDistanceSq(potentialMate) < d0) {
                riftCreature = potentialMate;
                d0 = this.creature.getDistanceSq(potentialMate);
            }
        }

        return riftCreature;
    }

    private void spawnEgg() {
        RiftEgg egg = (RiftEgg) this.creature.createChild(this.targetMate);

        final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(creature, targetMate, egg);
        final boolean cancelled = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
        if (cancelled) {
            this.creature.resetInLove();
            this.targetMate.resetInLove();
            return;
        }

        if (egg != null) {
            EntityPlayer player = this.creature.getLoveCause();
            if (player == null && this.targetMate.getLoveCause() != null) player = this.targetMate.getLoveCause();
            if (player != null) player.addStat(StatList.ANIMALS_BRED);
            this.creature.resetInLove();
            this.targetMate.resetInLove();
            egg.setCreatureType(this.creature.creatureType);
            if (this.creature.isTamed()) egg.setOwnerId(this.creature.getOwnerId());
            egg.setLocationAndAngles(this.creature.posX, this.creature.posY, this.creature.posZ, 0.0F, 0.0F);
            egg.enablePersistence();
            egg.setHatchTime(this.creature.creatureType.getHatchTime() * 20);
            this.world.spawnEntity(egg);

            Random random = this.creature.getRNG();
            for (int i = 0; i < 17; ++i) {
                double d0 = random.nextGaussian() * 0.02D;
                double d1 = random.nextGaussian() * 0.02D;
                double d2 = random.nextGaussian() * 0.02D;
                double d3 = random.nextDouble() * (double) this.creature.width * 2.0D - (double) this.creature.width;
                double d4 = 0.5D + random.nextDouble() * (double) this.creature.height;
                double d5 = random.nextDouble() * (double) this.creature.width * 2.0D - (double) this.creature.width;
                this.world.spawnParticle(EnumParticleTypes.HEART, this.creature.posX + d3, this.creature.posY + d4, this.creature.posZ + d5, d0, d1, d2);
            }

            if (this.world.getGameRules().getBoolean("doMobLoot")) {
                this.world.spawnEntity(new EntityXPOrb(this.world, this.creature.posX, this.creature.posY, this.creature.posZ, random.nextInt(7) + 1));
            }
        }
    }
}
