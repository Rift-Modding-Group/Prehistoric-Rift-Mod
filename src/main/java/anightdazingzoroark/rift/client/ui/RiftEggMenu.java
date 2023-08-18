package anightdazingzoroark.rift.client.ui;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.RiftEgg;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraftforge.fml.common.StartupQuery.reset;

@SideOnly(Side.CLIENT)
public class RiftEggMenu extends GuiScreen {
    protected int xSize = 176;
    protected int ySize = 166;
    public final int xGui = 176;
    public final int yGui = 166;
    protected int guiLeft;
    protected int guiTop;
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/generic_screen.png");
    private String renderText = "";

    public RiftEggMenu() {
        super();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        int centerX = (this.width - this.xGui) / 2;
        int centerY = (this.height - this.yGui) / 2;
        super.initGui();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        RiftEgg egg = (RiftEgg) RiftInitialize.EGG;
        if (!this.mc.player.isEntityAlive() || this.mc.player.isDead) {
            this.mc.player.closeScreen();
        }
        if (egg.getHatchTime() <= 0) {
            this.mc.player.closeScreen();
        }
        renderText = "";
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        if (mc != null && mc.world != null) {
            this.drawDefaultBackground();
        }
        else {
            return;
        }
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawGuiContainerBackgroundLayer(p_73863_3_, p_73863_1_, p_73863_2_);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) k, (float) l, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        this.drawGuiContainerForegroundLayer(p_73863_1_, p_73863_2_);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        if (!renderText.isEmpty()) {
            this.drawHoveringText(Lists.newArrayList(renderText), p_73863_1_, p_73863_2_, fontRenderer);
        }
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        reset();
        int wordLength = 90;
        RiftEgg egg = (RiftEgg) RiftInitialize.EGG;
        GlStateManager.pushMatrix();
        String s = I18n.format(egg.creatureType.friendlyName + " " + I18n.format("rift.egg"));
        GlStateManager.scale(1F, 1F, 1F);
        printStringXY(s, (-this.fontRenderer.getStringWidth(s) / 2) + 65, 60, 66, 48, 36);
        GlStateManager.popMatrix();
        {
            int minutes = egg.getHatchTimeMinutes()[0];
            int seconds = egg.getHatchTimeMinutes()[1];
            String minutesString = minutes > 0 ? minutes + " " + I18n.format("rift.egg.minutes") : "";
            String s1 = I18n.format("rift.egg.time") + ": " + minutesString + " " + seconds + " " + I18n.format("rift.egg.seconds");
            printStringXY(s1, wordLength / 2, 120, 157, 126, 103);
        }
    }

    public void printStringXY(String str0, int x0, int y0, int r, int g, int b) {
        int col = (r << 16) | (g << 8) | b;
        this.fontRenderer.drawString(str0, x0, y0, col);
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, (176F), (166F));
        GlStateManager.pushMatrix();
        renderEgg((RiftEgg) RiftInitialize.EGG, k + 100, l + 80, 50, 0, 0);
        GlStateManager.popMatrix();
    }

    public static void renderEgg(RiftEgg egg, int posX, int posY, int scaleValue, float renderYaw, float renderPitch) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) (scaleValue), (float) scaleValue, (float) scaleValue);
        float f3 = egg.rotationYaw;
        float f4 = egg.rotationPitch;
        GlStateManager.rotate(-45.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan(renderPitch / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        egg.rotationYaw = (float) Math.atan(renderYaw / 40.0F) * 40.0F;
        egg.rotationPitch = -((float) Math.atan(renderPitch / 40.0F)) * 20.0F;
        GlStateManager.translate(0.0F, (float) egg.getYOffset(), 0.0F);
        GlStateManager.rotate(egg.ticksExisted, 0.0F, 1.0F, 0.0F);
        Minecraft.getMinecraft().getRenderManager().playerViewY = 180.0F;
        Minecraft.getMinecraft().getRenderManager().renderEntity(egg, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
        egg.rotationYaw = f3;
        egg.rotationPitch = f4;
        GlStateManager.disableDepth();

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
