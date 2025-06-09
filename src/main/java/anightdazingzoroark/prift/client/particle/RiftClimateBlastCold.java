package anightdazingzoroark.prift.client.particle;

import anightdazingzoroark.prift.helper.RiftUtil;
import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftClimateBlastCold extends ParticleCloud {
    public RiftClimateBlastCold(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double motX, double motY, double motZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motX, motY, motZ);
        this.particleRed = 1f;
        this.particleGreen = 1f;
        this.particleBlue =  1f;
        this.particleMaxAge = RiftUtil.randomInRange(5, 15);
    }
}
