package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.model.block.RiftSemiManualHammererModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualHammerer;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualHammererTop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;
import anightdazingzoroark.riftlib.geo.render.built.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;

public class SemiManualHammererRenderer extends GeoBlockRenderer<TileEntitySemiManualHammerer> {
    public SemiManualHammererRenderer() {
        super(new RiftSemiManualHammererModel());
    }

    @Override
    public void render(TileEntitySemiManualHammerer animatable, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(animatable, x, y, z, partialTicks, destroyStage, alpha);
        //render items
        TileEntitySemiManualHammererTop topTE = (TileEntitySemiManualHammererTop)animatable.getTopTEntity();
        float recipeTRatio = topTE != null ? (float)topTE.getTimeHeld()/(float)topTE.getMaxHammererTime() : -1;

        IItemHandler itemStackHandler = animatable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        boolean canRenderItemFlag = itemStackHandler != null && (!itemStackHandler.getStackInSlot(0).isEmpty() || !itemStackHandler.getStackInSlot(1).isEmpty());
        ItemStack itemToRender =  itemStackHandler != null && itemStackHandler.getStackInSlot(0).isEmpty() ? itemStackHandler.getStackInSlot(1) : itemStackHandler.getStackInSlot(0);
        if (canRenderItemFlag && recipeTRatio >= 0 && recipeTRatio > 0.25 && !topTE.getMustBeReset() && !animatable.canDoResetAnim()) {
            GL11.glPushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableLighting();
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            GL11.glScaled(0.5, 0.5, 0.5);
            Minecraft.getMinecraft().getRenderItem().renderItem(itemToRender, ItemCameraTransforms.TransformType.NONE);
            GL11.glPopMatrix();
        }
    }

    @Override
    public void render(GeoModel model, TileEntitySemiManualHammerer animatable, float partialTicks, float red, float green, float blue, float alpha) {
        //animations
        TileEntitySemiManualHammererTop topTE = (TileEntitySemiManualHammererTop)animatable.getTopTEntity();
        if (topTE != null) {
            float recipeTRatio = (float)topTE.getTimeHeld()/(float)topTE.getMaxHammererTime();
            if (recipeTRatio > 0) {
                model.getBone("arm").get().setPositionY(170.5f * recipeTRatio - 170.5f);
                model.getBone("arm").get().setScaleY(-5.5f * recipeTRatio + 6.5f);
                model.getBone("hammerer").get().setPositionY(16f * recipeTRatio - 16f);
            }
            else {
                if (!topTE.getMustBeReset() && !animatable.canDoResetAnim()) {
                    model.getBone("arm").get().setPositionY(-170.5f);
                    model.getBone("arm").get().setScaleY(6.5f);
                    model.getBone("hammerer").get().setPositionY(-16f);
                }
                else if (topTE.getMustBeReset() && !animatable.canDoResetAnim()) {
                    model.getBone("arm").get().setPositionY(0);
                    model.getBone("arm").get().setScaleY(1f);
                    model.getBone("hammerer").get().setPositionY(0);
                }
                else if (!topTE.getMustBeReset() && animatable.canDoResetAnim()) {
                    model.getBone("arm").get().setPositionY(RiftUtil.clamp(-34.1f * animatable.getResetAnimTime() + 170.5f, -170.5f, 0));
                    model.getBone("arm").get().setScaleY(RiftUtil.clamp(1.1f * animatable.getResetAnimTime() -4.5f, 1f, 6.5f));
                    model.getBone("hammerer").get().setPositionY(RiftUtil.clamp(-3.2f * animatable.getResetAnimTime() + 16f, -16f, 0));
                }
            }
        }
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
