package anightdazingzoroark.prift.client.particle;

import anightdazingzoroark.prift.helper.RiftUtil;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftClimateBlastWarm extends ParticleFlame {
    public RiftClimateBlastWarm(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.particleMaxAge = RiftUtil.randomInRange(5, 15);
    }
}
