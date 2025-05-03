package anightdazingzoroark.prift.client.particle;

import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftTrapParticle extends ParticleSpell {
    public RiftTrapParticle(World worldIn, int color, double xCoordIn, double yCoordIn, double zCoordIn, double motX, double motY, double motZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motX, motY, motZ);
        this.motionX *= 1.5D;
        this.motionY *= -1.5D;
        this.motionZ *= 1.5D;
        this.particleRed = ((color >> 16) & 0xFF)/255f;
        this.particleGreen = ((color >> 8) & 0xFF)/255f;
        this.particleBlue =  (color & 0xFF)/255f;
        this.setMaxAge(15);
    }
}
