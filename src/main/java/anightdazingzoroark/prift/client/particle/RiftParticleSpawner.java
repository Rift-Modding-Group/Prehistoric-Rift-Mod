package anightdazingzoroark.prift.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;

public class RiftParticleSpawner {
    public void spawnParticle(Particle particleID, boolean ignoreRange, boolean ignoreLimit, boolean minParticles, double xCoord, double yCoord, double zCoord) {
        try {
            this.spawnParticle0(particleID, ignoreRange, ignoreLimit, minParticles, xCoord, yCoord, zCoord);
        }
        catch (Throwable ignored) {}
    }

    private Particle spawnParticle0(Particle particleID, boolean ignoreRange, boolean ignoreLimit, boolean minParticles, double xCoord, double yCoord, double zCoord) {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

        if (entity != null && Minecraft.getMinecraft().effectRenderer != null) {
            int k1 = this.calculateParticleLevel(minParticles, ignoreLimit);
            double d3 = entity.posX - xCoord;
            double d4 = entity.posY - yCoord;
            double d5 = entity.posZ - zCoord;
            if (ignoreRange) return spawnEffectParticle(particleID);
            else if (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D) return null;
            else return k1 > 1 ? null : spawnEffectParticle(particleID);
        }
        else return null;
    }

    @Nullable
    public Particle spawnEffectParticle(Particle particle) {
        if (particle != null) {
            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
            Minecraft.getMinecraft().effectRenderer.renderParticles(Minecraft.getMinecraft().getRenderViewEntity(), 100);
            return particle;
        }
        return null;
    }

    private int calculateParticleLevel(boolean minimiseLevel, boolean ignoreLimit) {
        if (Minecraft.getMinecraft().world == null) return 2;
        int k1 = Minecraft.getMinecraft().gameSettings.particleSetting;
        if (minimiseLevel && k1 == 2 && Minecraft.getMinecraft().world.rand.nextInt(10) == 0) k1 = 1;
        if (k1 == 1 && Minecraft.getMinecraft().world.rand.nextInt(3) == 0) k1 = 2;
        return k1;
    }
}
