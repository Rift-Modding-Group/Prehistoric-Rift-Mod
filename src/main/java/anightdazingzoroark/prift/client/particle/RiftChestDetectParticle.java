package anightdazingzoroark.prift.client.particle;

import anightdazingzoroark.prift.server.items.RiftItems;
import net.minecraft.client.particle.Barrier;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftChestDetectParticle extends Barrier {
    public RiftChestDetectParticle(World worldIn, double p_i46286_2_, double p_i46286_4_, double p_i46286_6_) {
        super(worldIn, p_i46286_2_, p_i46286_4_, p_i46286_6_, RiftItems.CHEST_DETECT_ALERT);
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();

        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);

        GlStateManager.popMatrix();
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return 15728880;
    }
}
