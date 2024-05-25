package anightdazingzoroark.prift.client.particle;

import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftPregnancyParticle extends ParticleSpell {
    public RiftPregnancyParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motX, double motY, double motZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motX, motY, motZ);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= -0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.particleRed = (150f/255f);
        this.particleGreen = (75f/255f);
        this.particleBlue = 0f;
        this.setMaxAge(15);
    }
}
