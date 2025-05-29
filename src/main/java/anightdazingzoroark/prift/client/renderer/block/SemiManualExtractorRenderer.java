package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.model.block.RiftSemiManualExtractorModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractor;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractorTop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class SemiManualExtractorRenderer extends GeoBlockRenderer<TileEntitySemiManualExtractor> {
    public SemiManualExtractorRenderer() {
        super(new RiftSemiManualExtractorModel());
    }

    @Override
    public void func_192841_a(TileEntitySemiManualExtractor animatable, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.func_192841_a(animatable, x, y, z, partialTicks, destroyStage, alpha);
        //render items
        TileEntitySemiManualExtractorTop topTE = (TileEntitySemiManualExtractorTop)animatable.getTopTEntity();
        float recipeTRatio = topTE != null ? (float)topTE.getTimeHeld()/(float)topTE.getMaxRecipeTime() : -1;

        IItemHandler itemStackHandler = animatable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        boolean canRenderItemFlag = itemStackHandler != null && !itemStackHandler.getStackInSlot(0).isEmpty();
        if (canRenderItemFlag && recipeTRatio >= 0 && recipeTRatio < 0.75 && !topTE.getMustBeReset() && !animatable.canDoResetAnim()) {
            ItemStack stack = itemStackHandler.getStackInSlot(0);
            GL11.glPushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableLighting();
            GL11.glTranslated(x + 0.5, y + 0.8, z + 0.5);
            GL11.glScaled(0.25, 0.25, 0.25);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
            GL11.glPopMatrix();
        }

        //render liquids
        FluidStack fluidStack = animatable.getTank().getFluid();
        if (fluidStack != null) {
            double fluidHeight = fluidStack.amount / (double) animatable.getTank().getCapacity() * 0.75;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x - 0.05, y, z - 0.05);

            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStack.getFluid().getStill().toString());
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            int color = fluidStack.getFluid().getColor(fluidStack);
            float r = (color >> 16 & 0xFF) / 255.0F;
            float g = (color >> 8 & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            GlStateManager.color(r, g, b, 1.0F);
            GlStateManager.disableCull();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            // Top
            buffer.pos(0.1, 0.1 + fluidHeight + 0.5, 0.1).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + fluidHeight + 0.5, 0.1).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + fluidHeight + 0.5, 0.1 + 0.9).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1, 0.1 + fluidHeight + 0.5, 0.1 + 0.9).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();

            // Sides
            buffer.pos(0.1, 0.1 + 0.5, 0.1).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
            buffer.pos(0.1, 0.1 + fluidHeight + 0.5, 0.1).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1, 0.1 + fluidHeight + 0.5, 0.1 + 0.9).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1, 0.1 + 0.5, 0.1 + 0.9).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();

            buffer.pos(0.1 + 0.9, 0.1 + 0.5, 0.1).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + fluidHeight + 0.5, 0.1).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + fluidHeight + 0.5, 0.1 + 0.9).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + 0.5, 0.1 + 0.9).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();

            buffer.pos(0.1, 0.1 + 0.5, 0.1).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + 0.5, 0.1).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + fluidHeight + 0.5, 0.1).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1, 0.1 + fluidHeight + 0.5, 0.1).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();

            buffer.pos(0.1, 0.1 + 0.5, 0.1 + 0.9).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + 0.5, 0.1 + 0.9).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1 + 0.9, 0.1 + fluidHeight + 0.5, 0.1 + 0.9).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
            buffer.pos(0.1, 0.1 + fluidHeight + 0.5, 0.1 + 0.9).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();

            tessellator.draw();
            GlStateManager.enableCull();


            GlStateManager.popMatrix();
        }
    }

    @Override
    public void render(GeoModel model, TileEntitySemiManualExtractor animatable, float partialTicks, float red, float green, float blue, float alpha) {
        //animations
        TileEntitySemiManualExtractorTop topTE = (TileEntitySemiManualExtractorTop)animatable.getTopTEntity();
        if (topTE != null) {
            float recipeTRatio = (float)topTE.getTimeHeld()/(float)topTE.getMaxRecipeTime();
            if (recipeTRatio > 0) {
                model.getBone("arm").get().setPositionY(-155f * recipeTRatio);
                model.getBone("arm").get().setScaleY(5f * recipeTRatio + 1f);
                model.getBone("squeezer").get().setPositionY(-14f * recipeTRatio);
            }
            if (topTE.getMustBeReset()) {
                model.getBone("arm").get().setPositionY(-155f);
                model.getBone("arm").get().setScaleY(6f);
                model.getBone("squeezer").get().setPositionY(-14f);
            }
            if (animatable.canDoResetAnim()) {
                model.getBone("arm").get().setPositionY(RiftUtil.clamp(31f * animatable.getResetAnimTime() - 310f, -155f, 0));
                model.getBone("arm").get().setScaleY(RiftUtil.clamp(-animatable.getResetAnimTime() + 11f, 1f, 6f));
                model.getBone("squeezer").get().setPositionY(RiftUtil.clamp(2.8f * animatable.getResetAnimTime() - 28f, -14f, 0));
            }
        }

        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
