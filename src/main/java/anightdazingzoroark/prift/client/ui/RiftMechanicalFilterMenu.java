package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.mysticalmechanics.inventory.MechanicalFilterContainer;
import anightdazingzoroark.prift.compat.mysticalmechanics.inventory.MillstoneContainer;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMechanicalFilter;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMillstone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class RiftMechanicalFilterMenu extends GuiContainer {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/millstone.png");
    private final TileEntityMechanicalFilter mechanicalFilter;
    private final IInventory playerInventory;

    public RiftMechanicalFilterMenu(TileEntityMechanicalFilter mechanicalFilter, IInventory playerInventory) {
        super(new MechanicalFilterContainer(mechanicalFilter, Minecraft.getMinecraft().player));
        this.mechanicalFilter = mechanicalFilter;
        this.playerInventory = playerInventory;
        this.ySize = 187;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        //background
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        //progress bar
        float progress = (float)this.mechanicalFilter.getTimeHeld();
        float max = (float)this.mechanicalFilter.getMaxRecipeTime();
        float fill = progress / max * 22f;
        int k = (this.width - 14) / 2 - 1;
        int l = (this.height - 22) / 2 - 38;
        this.drawTexturedModalRect(k, l,176, 0, 14, (int)fill);
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("tile.mechanical_filter.name"), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }
}
