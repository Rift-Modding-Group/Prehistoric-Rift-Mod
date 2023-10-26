package anightdazingzoroark.rift.client.particle;

import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftBleedParticle extends ParticleCloud {
    public RiftBleedParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motX, double motY, double motZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motX, motY, motZ);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= -0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.particleRed = (134f/255f);
        this.particleGreen = (22f/255f);
        this.particleBlue =  (22f/255f);
        this.setMaxAge(100);
    }
}
