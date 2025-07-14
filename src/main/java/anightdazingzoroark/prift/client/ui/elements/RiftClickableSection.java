package anightdazingzoroark.prift.client.ui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;

public class RiftClickableSection {
    //width and height of the section
    private final int width;
    private final int height;

    //x and y offsets of the section
    public final int xOffset;
    public final int yOffset;

    //width and height of the screen the section will be put in
    public int guiWidth;
    public int guiHeight;

    private boolean isHovered;
    private boolean isSelected;

    //other important shite
    protected Minecraft minecraft;
    protected final FontRenderer fontRenderer;

    //string related stuff
    private String stringToRender;
    private boolean stringHasShadow;
    private int stringColor;
    private int stringXOffset;
    private int stringYOffset;

    //image related stuff
    private ResourceLocation textureLocation;
    private int textureWidth, textureHeight, uvWidth, uvHeight;
    private int xUV, yUV, hoveredXUV, hoveredYUV;

    public RiftClickableSection(int width, int height, int guiWidth, int guiHeight, int xOffset, int yOffset, FontRenderer fontRenderer, Minecraft minecraft) {
        this.width = width;
        this.height = height;
        this.guiWidth = guiWidth;
        this.guiHeight = guiHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.fontRenderer = fontRenderer;
        this.minecraft = minecraft;
    }

    public void addString(String value, boolean withShadow, int color, int stringXOffset, int stringYOffset) {
        this.stringToRender = value;
        this.stringHasShadow = withShadow;
        this.stringColor = color;
        this.stringXOffset = stringXOffset;
        this.stringYOffset = stringYOffset;
    }

    public void addImage(ResourceLocation texture, int uvWidth, int uvHeight, int textureWidth, int textureHeight, int xUV, int yUV, int hoveredXUV, int hoveredYUV) {
        this.textureLocation = texture;
        this.uvWidth = uvWidth;
        this.uvHeight = uvHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xUV = xUV;
        this.yUV = yUV;
        this.hoveredXUV = hoveredXUV;
        this.hoveredYUV = hoveredYUV;
    }

    public void drawSection(int mouseX, int mouseY) {
        //deal with hovering
        this.isHovered = this.isHovered(mouseX, mouseY);

        //draw string
        if (this.stringToRender != null) {
            int stringWidth = this.fontRenderer.getStringWidth(this.stringToRender);
            if (this.stringHasShadow) {
                this.fontRenderer.drawStringWithShadow(
                        this.stringToRender,
                        (int)((this.guiWidth - stringWidth) / 2 + this.xOffset + this.stringXOffset),
                        (int)((this.guiHeight - this.fontRenderer.FONT_HEIGHT) / 2 + this.yOffset + this.stringYOffset),
                        this.getTextColor()
                );
            }
            else {
                this.fontRenderer.drawString(
                        this.stringToRender,
                        (int)((this.guiWidth - stringWidth) / 2 + this.xOffset + this.stringXOffset),
                        (int)((this.guiHeight - this.fontRenderer.FONT_HEIGHT) / 2 + this.yOffset + this.stringYOffset),
                        this.getTextColor()
                );
            }
        }

        //draw image
        if (this.textureLocation != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int k = (this.guiWidth - this.width) / 2 + this.xOffset;
            int l = (this.guiHeight - this.height) / 2 + this.yOffset;
            int xUVTexture = this.isHovered ? this.hoveredXUV : this.xUV;
            int yUVTexture = this.isHovered ? this.hoveredYUV : this.yUV;
            drawModalRectWithCustomSizedTexture(k, l, xUVTexture, yUVTexture, this.uvWidth, this.uvHeight, this.textureWidth, this.textureHeight);
        }
    }

    public boolean isHovered(int mouseX, int mouseY) {
        int x = (this.guiWidth - this.width) / 2 + this.xOffset;
        int y = (this.guiHeight - this.height) / 2 + this.yOffset;
        return mouseX >= x && mouseX <= x + this.width && mouseY >= y && mouseY <= y + this.height;
    }

    public void setSelected(boolean value) {
        this.isSelected = value;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    private int getTextColor() {
        if (this.isSelected) return 0x5A3B1A; //brown for active
        else if (this.isHovered) return 0xFFFF00; //yellow for hover
        return this.stringColor;
    }
}