package anightdazingzoroark.prift.client.ui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;
import static net.minecraft.client.gui.Gui.drawRect;

public abstract class RiftGuiScrollableSection {
    //width and height of the section
    private final int width;
    private final int height;

    //x and y offsets of the section
    private final int xOffset;
    private final int yOffset;

    //width and height of the screen the section will be put in
    private int guiWidth;
    private int guiHeight;

    //gonna be pretty important for scrolling
    protected int contentHeight;

    //scroll stuff
    protected int scrollOffset = 0;
    protected int maxScroll = 0;
    protected int scrollStep = 10;
    protected final int scrollbarWidth = 2;
    private boolean draggingScrollbar = false;
    private int dragOffsetY = 0;

    //logic involved in disabling buttons
    private final List<String> disabledButtonIds = new ArrayList<>();

    //other important shite
    protected Minecraft minecraft;
    protected final FontRenderer fontRenderer;

    //button stuff
    private final List<RiftGuiSectionButton> activeButtons = new ArrayList<>();

    public RiftGuiScrollableSection(int width, int height, int guiWidth, int guiHeight, int xOffset, int yOffset, FontRenderer fontRenderer, Minecraft minecraft) {
        this.width = width;
        this.height = height;
        this.guiWidth = guiWidth;
        this.guiHeight = guiHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.fontRenderer = fontRenderer;
        this.minecraft = minecraft;
    }

    //width and height will get updated based on screen size
    public void updateGuiSize(int guiWidth, int guiHeight) {
        this.guiWidth = guiWidth;
        this.guiHeight = guiHeight;
    }

    //this is where ui sections are drawn
    public abstract RiftGuiScrollableSectionContents defineSectionContents();

    //this is where contents of the section are placed
    public void drawSectionContents(int mouseX, int mouseY, float partialTicks) {
        this.activeButtons.clear();

        int sectionX = (this.guiWidth - this.width) / 2 + this.xOffset;
        int sectionY = (this.guiHeight - this.height) / 2 + this.yOffset;

        //scissor setup
        ScaledResolution res = new ScaledResolution(this.minecraft);
        int scaleFactor = res.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(sectionX * scaleFactor, (this.minecraft.displayHeight - (sectionY + this.height) * scaleFactor), this.width * scaleFactor, this.height * scaleFactor);

        int drawY = sectionY - this.scrollOffset;
        int totalHeight = 0;
        this.contentHeight = 0;

        for (int i = 0; i < this.defineSectionContents().getContents().size(); i++) {
            RiftGuiScrollableSectionContents.Element element = this.defineSectionContents().getContents().get(i);
            if (element instanceof RiftGuiScrollableSectionContents.TextElement) {
                RiftGuiScrollableSectionContents.TextElement text = (RiftGuiScrollableSectionContents.TextElement) element;
                this.fontRenderer.drawSplitString(
                        text.getContents(),
                        sectionX,
                        drawY + totalHeight,
                        this.width,
                        text.getTextColor()
                );
                int lines = this.fontRenderer.listFormattedStringToWidth(text.getContents(), this.width).size();
                int textHeight = lines * this.fontRenderer.FONT_HEIGHT;
                totalHeight += textHeight;
                if (i < this.defineSectionContents().getContents().size() - 1) totalHeight += this.fontRenderer.FONT_HEIGHT;
            }
            else if (element instanceof RiftGuiScrollableSectionContents.ImageElement) {
                RiftGuiScrollableSectionContents.ImageElement img = (RiftGuiScrollableSectionContents.ImageElement) element;
                this.minecraft.getTextureManager().bindTexture(img.getImageLocation());

                int imgW = img.getImageSize()[0];
                int imgH = img.getImageSize()[1];
                float scale = img.getImageScale();

                int scaledW = (int) (imgW * scale);
                int scaledH = (int) (imgH * scale);
                int imgX = sectionX + (this.width - scaledW) / 2;
                int imgY = drawY + totalHeight;

                GlStateManager.pushMatrix();
                GlStateManager.translate(imgX, imgY, 0);
                GlStateManager.scale(scale, scale, scale);

                drawModalRectWithCustomSizedTexture(
                        0, 0,
                        0, 0,
                        imgW, imgH,
                        imgW, imgH
                );

                GlStateManager.popMatrix();
                totalHeight += scaledH;
            }
            else if (element instanceof RiftGuiScrollableSectionContents.ButtonElement) {
                RiftGuiScrollableSectionContents.ButtonElement but = (RiftGuiScrollableSectionContents.ButtonElement) element;

                int buttonW = but.getSize()[0];
                int buttonH = but.getSize()[1];
                int buttonX = sectionX + (this.width - buttonW) / 2;
                int buttonY = drawY + totalHeight;

                RiftGuiSectionButton button = new RiftGuiSectionButton(
                        but.getId(),
                        buttonX,
                        buttonY,
                        buttonW,
                        buttonH,
                        but.getName()
                );

                if (this.disabledButtonIds.contains(but.getId())) button.enabled = false;

                button.drawButton(this.minecraft, mouseX, mouseY, partialTicks);
                this.activeButtons.add(button);

                totalHeight += buttonH;
                if (i < this.defineSectionContents().getContents().size() - 1) totalHeight += but.getBottomSpaceSize();
            }
        }

        this.contentHeight = totalHeight;
        this.maxScroll = Math.max(0, this.contentHeight - this.height);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //draw scrollbar
        if (this.contentHeight > this.height) {
            float ratio = (float) this.scrollOffset / (float) this.maxScroll;
            int thumbHeight = Math.max(20, (int)((float) this.height * this.height / (float) this.contentHeight));
            int thumbY = sectionY + (int)((height - thumbHeight) * ratio);
            int scrollX = sectionX + this.width - this.scrollbarWidth;

            drawRect(scrollX, sectionY, scrollX + this.scrollbarWidth, sectionY + this.height, 0xFF333333);
            drawRect(scrollX, thumbY, scrollX + this.scrollbarWidth, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    public void handleMouseInput(int mouseX, int mouseY, int delta) {
        int x = (this.guiWidth - this.width) / 2 + this.xOffset;
        int y = (this.guiHeight - this.height) / 2 + this.yOffset;

        //check if mouse is within scrollable section
        if (mouseX >= x && mouseX <= x + this.width && mouseY >= y && mouseY <= y + this.height) {
            this.maxScroll = Math.max(0, this.contentHeight - this.height);
            this.scrollOffset = MathHelper.clamp(this.scrollOffset - Integer.signum(delta) * this.scrollStep, 0, this.maxScroll);
        }
    }

    //mainly to help out in dragg
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || contentHeight <= height) return;

        int sectionX = (this.guiWidth - this.width) / 2 + this.xOffset;
        int sectionY = (this.guiHeight - this.height) / 2 + this.yOffset;
        int scrollX = sectionX + this.width - this.scrollbarWidth;

        float ratio = (float) this.scrollOffset / this.maxScroll;
        int thumbHeight = Math.max(20, (int)((float) this.height * this.height / this.contentHeight));
        int thumbY = sectionY + (int)((this.height - thumbHeight) * ratio);

        if (mouseX >= scrollX && mouseX <= scrollX + this.scrollbarWidth &&
                mouseY >= sectionY && mouseY <= sectionY + height) {
            if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                // Start dragging
                this.draggingScrollbar = true;
                this.dragOffsetY = mouseY - thumbY;
            }
            else {
                int clickedY = mouseY - sectionY - thumbHeight / 2;
                float scrollRatio = (float) clickedY / (float)(this.height - thumbHeight);
                this.scrollOffset = MathHelper.clamp((int)(scrollRatio * this.maxScroll), 0, this.maxScroll);
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0) this.draggingScrollbar = false;
    }

    public void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (!this.draggingScrollbar || this.contentHeight <= this.height || button != 0) return;

        int sectionY = (this.guiHeight - this.height) / 2 + this.yOffset;
        int thumbHeight = Math.max(20, (int)((float) this.height * this.height / this.contentHeight));

        int dragY = mouseY - sectionY - this.dragOffsetY;
        float scrollRatio = (float) dragY / (float)(this.height - thumbHeight);
        this.scrollOffset = MathHelper.clamp((int)(scrollRatio * this.maxScroll), 0, this.maxScroll);
    }

    public List<RiftGuiSectionButton> getActiveButtons() {
        return this.activeButtons;
    }

    public void disableButtonById(String value) {
        this.disabledButtonIds.add(value);
    }

    public void reenableButtonById(String value) {
        this.disabledButtonIds.remove(value);
    }

    public void reenableAllButtons() {
        this.disabledButtonIds.clear();
    }
}
