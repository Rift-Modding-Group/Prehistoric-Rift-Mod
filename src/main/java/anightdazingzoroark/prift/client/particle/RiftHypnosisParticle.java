package anightdazingzoroark.prift.client.particle;

import net.minecraft.client.particle.ParticleHeart;
import net.minecraft.world.World;

public class RiftHypnosisParticle extends ParticleHeart {
    public RiftHypnosisParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motX, double motY, double motZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motX, motY, motZ);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.setMaxAge(60);
    }
}
