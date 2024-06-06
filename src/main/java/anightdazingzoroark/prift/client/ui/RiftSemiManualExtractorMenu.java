package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.inventory.SemiManualExtractorContainer;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntitySemiManualExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class RiftSemiManualExtractorMenu extends GuiContainer {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/semi_manual_extractor.png");
    private static final ResourceLocation tankScale = new ResourceLocation(RiftInitialize.MODID, "textures/ui/semi_manual_extractor_scale.png");
    private static final ResourceLocation progressBar = new ResourceLocation(RiftInitialize.MODID, "textures/ui/progress_arrow.png");
    private final TileEntitySemiManualExtractor semiManualExtractor;
    private final IInventory playerInventory;

    public RiftSemiManualExtractorMenu(TileEntitySemiManualExtractor semiManualExtractor, IInventory playerInventory) {
        super(new SemiManualExtractorContainer(semiManualExtractor, Minecraft.getMinecraft().player));
        this.semiManualExtractor = semiManualExtractor;
        this.playerInventory = playerInventory;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawTank();
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
        this.fontRenderer.drawString(I18n.format("tile.semi_automatic_extractor.name"), 8, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    private void drawTank() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //for fluid
        FluidStack fluidStack = this.semiManualExtractor.getTank().getFluid();
        if (fluidStack != null) {
            float fluidHeight = ((float) fluidStack.amount / (float) this.semiManualExtractor.getTank().getCapacity()) * 52f;
            TextureAtlasSprite liquidIcon = this.mc.getTextureMapBlocks().getAtlasSprite(fluidStack.getFluid().getStill().toString());
            this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            int xPos = (this.width - this.xSize) / 2 + 97;
            int yPos = (this.height - this.ySize) / 2 + 70 - (int)fluidHeight;
            for (int i = 0; i < fluidHeight; i += 16) {
                float drawHeight = Math.min(16f, (int)fluidHeight - i);
                this.drawFluidTexture(liquidIcon, xPos, yPos + i, 16, (int)drawHeight);
                this.drawFluidTexture(liquidIcon, xPos + 16, yPos + i, 16, (int)drawHeight);
                this.drawFluidTexture(liquidIcon, xPos + 32, yPos + i, 2, (int)drawHeight);
            }
            GlStateManager.disableBlend();
        }

        //for tank scale
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        this.mc.getTextureManager().bindTexture(tankScale);
        drawModalRectWithCustomSizedTexture((this.width - 34) / 2 + 26, (this.height - 52) / 2 - 39, 0, 0, 34, 52, 34, 52);
        GlStateManager.disableBlend();
    }

    private void drawProgressBar() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float progress = (float)this.semiManualExtractor.getTopTEntity().getTimeHeld();
        float max = (float)this.semiManualExtractor.getTopTEntity().getMaxRecipeTime();
        float fill = progress / max * 21f;
        this.mc.getTextureManager().bindTexture(progressBar);
        drawModalRectWithCustomSizedTexture((this.width - 21)/2 - 9, (this.height - 14) /2 - 40, 0, 0, (int)fill, 14, 21, 14);
    }

    private void drawFluidTexture(TextureAtlasSprite sprite, int x, int y, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();
        float minV = sprite.getMinV();
        float maxV = sprite.getMaxV();
        float vHeight = minV + (maxV - minV) * (height / 16.0f);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(minU, vHeight).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex(maxU, vHeight).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex(maxU, minV).endVertex();
        buffer.pos(x, y, this.zLevel).tex(minU, minV).endVertex();
        tessellator.draw();
    }
}