package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.client.model.block.RiftLeadPoweredCrankModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class LeadPoweredCrankRenderer extends GeoBlockRenderer<TileEntityLeadPoweredCrank> {
    public LeadPoweredCrankRenderer() {
        super(new RiftLeadPoweredCrankModel());
    }

    @Override
    public void render(GeoModel model, TileEntityLeadPoweredCrank animatable, float partialTicks, float red, float green, float blue, float alpha) {
        model.getBone("lead").get().setHidden(!animatable.getHasLead());

        GeckoLibCache.getInstance().parser.setValue("rotation", animatable.getRotation());

        if (animatable.getHasLead()) this.renderLeash(animatable.getWorker(), -0.5, 0, -0.5, 0, 0, 0, partialTicks, animatable.getPos());

        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }

    @Override
    protected void rotateBlock(EnumFacing facing) {}

    protected void renderLeash(RiftCreature entity, double ox, double oy, double oz, double x, double y, double z, float partialTicks, BlockPos pos) {
        if (entity != null) {
            oy = oy - 0.7D;
            double d2 = 0.0D;
            double d3 = 0.0D;
            double d4 = -1.0D;

            double d9 = this.interpolateValue(entity.prevRenderYawOffset, entity.renderYawOffset, (double)partialTicks) * 0.01745329238474369D + (Math.PI / 2D);
            d2 = Math.cos(d9) * (double)entity.width * 0.4D;
            d3 = Math.sin(d9) * (double)entity.width * 0.4D;
            double d6 = (this.interpolateValue(entity.prevPosX, entity.posX, (double)partialTicks)) + d2;
            double d7 = this.interpolateValue(entity.prevPosY + entity.getEyeHeight() * 1.1D, entity.posY + entity.getEyeHeight() * 1.1D, (double)partialTicks) - d4 * 0.5D - 0.25D - y;
            double d8 = (this.interpolateValue(entity.prevPosZ, entity.posZ, (double)partialTicks)) + d3;

            d2 = 0.5D;
            d3 = 0.5D;
            double d10 = pos.getX() + d2;
            double d11 = pos.getY();
            double d12 = pos.getZ() + d3;
            ox += d2 + x;
            oz += d3 + z;
            oy += y;

            this.renderLeach(d6, d7, d8, ox, oy, oz, d10, d11, d12);
        }
    }

    private double interpolateValue(double start, double end, double pct) {
        return start + (end - start) * pct;
    }

    protected void renderLeach(double x1, double y1, double z1, double ox, double oy, double oz, double x2, double y2, double z2) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();

        double d13 = (double)((float)(x1 - x2));
        double d14 = (double)((float)(y1 - y2));
        double d15 = (double)((float)(z1 - z2));

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        vertexbuffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

        for (int j = 0; j <= 24; ++j)
        {
            float f = 0.5F;
            float f1 = 0.4F;
            float f2 = 0.3F;

            if (j % 2 == 0)
            {
                f *= 0.7F;
                f1 *= 0.7F;
                f2 *= 0.7F;
            }

            float f3 = (float)j / 24.0F;
            vertexbuffer.pos(ox + d13 * (double)f3 + 0.0D, oy + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)j) / 18.0F + 0.125F), oz + d15 * (double)f3).color(f, f1, f2, 1.0F).endVertex();
            vertexbuffer.pos(ox + d13 * (double)f3 + 0.025D, oy + d14 * (double)(f3 * f3 + f3) * 0.5D + (double)((24.0F - (float)j) / 18.0F + 0.125F) + 0.025D, oz + d15 * (double)f3).color(f, f1, f2, 1.0F).endVertex();
        }

        tessellator.draw();
        vertexbuffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

        for (int k = 0; k <= 24; ++k)
        {
            float f4 = 0.5F;
            float f5 = 0.4F;
            float f6 = 0.3F;

            if (k % 2 == 0)
            {
                f4 *= 0.7F;
                f5 *= 0.7F;
                f6 *= 0.7F;
            }

            float f7 = (float)k / 24.0F;
            vertexbuffer.pos(ox + d13 * (double)f7 + 0.0D, oy + d14 * (double)(f7 * f7 + f7) * 0.5D + (double)((24.0F - (float)k) / 18.0F + 0.125F) + 0.025D, oz + d15 * (double)f7).color(f4, f5, f6, 1.0F).endVertex();
            vertexbuffer.pos(ox + d13 * (double)f7 + 0.025D, oy + d14 * (double)(f7 * f7 + f7) * 0.5D + (double)((24.0F - (float)k) / 18.0F + 0.125F), oz + d15 * (double)f7 + 0.025D).color(f4, f5, f6, 1.0F).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
    }
}
