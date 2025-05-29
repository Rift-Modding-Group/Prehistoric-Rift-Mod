package anightdazingzoroark.prift.client.particle;

import anightdazingzoroark.prift.helper.RiftUtil;
import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftLightBlastParticle extends ParticleCloud {
    public RiftLightBlastParticle(World worldIn, int color, double xCoordIn, double yCoordIn, double zCoordIn, double motX, double motY, double motZ) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, motX, motY, motZ);
        this.particleRed = ((color & 0xFF0000) >> 16) / 255f;
        this.particleGreen = ((color & 0xFF00) >> 8) / 255f;
        this.particleBlue = (color & 0xFF) / 255f;
        this.particleMaxAge = RiftUtil.randomInRange(5, 15);
    }
    @Override
    public int getBrightnessForRender(float partialTick) {
        return 0xF000F0;
    }
}
