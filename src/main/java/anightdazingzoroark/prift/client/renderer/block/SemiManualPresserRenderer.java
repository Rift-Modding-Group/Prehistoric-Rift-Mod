package anightdazingzoroark.prift.client.renderer.block;

import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.client.model.block.RiftSemiManualPresserModel;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualPresser;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualPresserTop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;

public class SemiManualPresserRenderer extends GeoBlockRenderer<TileEntitySemiManualPresser> {
    public SemiManualPresserRenderer() {
        super(new RiftSemiManualPresserModel());
    }

    @Override
    public void render(TileEntitySemiManualPresser animatable, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(animatable, x, y, z, partialTicks, destroyStage, alpha);
        //render items
        TileEntitySemiManualPresserTop topTE = (TileEntitySemiManualPresserTop)animatable.getTopTEntity();
        float recipeTRatio = topTE != null ? (float)topTE.getTimeHeld()/(float)topTE.getMaxRecipeTime() : -1;

        IItemHandler itemStackHandler = animatable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        boolean canRenderItemFlag = itemStackHandler != null && (!itemStackHandler.getStackInSlot(0).isEmpty() || !itemStackHandler.getStackInSlot(1).isEmpty());
        ItemStack itemToRender =  itemStackHandler != null && itemStackHandler.getStackInSlot(0).isEmpty() ? itemStackHandler.getStackInSlot(1) : itemStackHandler.getStackInSlot(0);
        if (canRenderItemFlag && recipeTRatio >= 0 && recipeTRatio < 0.75 && !topTE.getMustBeReset() && !animatable.canDoResetAnim()) {
            GL11.glPushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableLighting();
            GL11.glTranslated(x + 0.5, y + 1.25, z + 0.5);
            GL11.glScaled(0.25, 0.25, 0.25);
            Minecraft.getMinecraft().getRenderItem().renderItem(itemToRender, ItemCameraTransforms.TransformType.NONE);
            GL11.glPopMatrix();
        }
    }

    @Override
    public void render(GeoModel model, TileEntitySemiManualPresser animatable, float partialTicks, float red, float green, float blue, float alpha) {
        //animations
        TileEntitySemiManualPresserTop topTE = (TileEntitySemiManualPresserTop)animatable.getTopTEntity();
        if (topTE != null) {
            float recipeTRatio = (float)topTE.getTimeHeld()/(float)topTE.getMaxRecipeTime();
            if (recipeTRatio > 0) {
                model.getBone("arm").get().setPositionY(-93f * recipeTRatio);
                model.getBone("arm").get().setScaleY(3f * recipeTRatio + 1f);
                model.getBone("squeezer").get().setPositionY(-8f * recipeTRatio);
            }
            if (topTE.getMustBeReset()) {
                model.getBone("arm").get().setPositionY(-93f);
                model.getBone("arm").get().setScaleY(4f);
                model.getBone("squeezer").get().setPositionY(-8f);
            }
            if (animatable.canDoResetAnim()) {
                model.getBone("arm").get().setPositionY(RiftUtil.clamp(18.6f * animatable.getResetAnimTime() - 186f, -93f, 0));
                model.getBone("arm").get().setScaleY(RiftUtil.clamp(-0.6f * animatable.getResetAnimTime() + 7f, 1f, 4f));
                model.getBone("squeezer").get().setPositionY(RiftUtil.clamp(1.6f * animatable.getResetAnimTime() - 16f, -8f, 0));
            }
        }
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }
}
