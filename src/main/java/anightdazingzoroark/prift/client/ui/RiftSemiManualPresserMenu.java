package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.inventory.SemiManualPresserContainer;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualPresser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class RiftSemiManualPresserMenu extends GuiContainer {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/semi_manual_presser.png");
    private static final ResourceLocation progressBar = new ResourceLocation(RiftInitialize.MODID, "textures/ui/progress_arrow.png");
    private final TileEntitySemiManualPresser semiManualPresser;
    private final IInventory playerInventory;

    public RiftSemiManualPresserMenu(TileEntitySemiManualPresser semiManualPresser, IInventory playerInventory) {
        super(new SemiManualPresserContainer(semiManualPresser, Minecraft.getMinecraft().player));
        this.semiManualPresser = semiManualPresser;
        this.playerInventory = playerInventory;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawProgressBar();
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("tile.semi_automatic_presser.name"), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    private void drawProgressBar() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float progress = (float)this.semiManualPresser.getTopTEntity().getTimeHeld();
        float max = (float)this.semiManualPresser.getTopTEntity().getMaxRecipeTime();
        float fill = progress / max * 21f;
        this.mc.getTextureManager().bindTexture(progressBar);
        drawModalRectWithCustomSizedTexture((this.width - 21)/2 + 1, (this.height - 14) /2 - 40, 0, 0, (int)fill, 14, 21, 14);
    }
}
