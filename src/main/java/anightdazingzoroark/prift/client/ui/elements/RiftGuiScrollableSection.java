package anightdazingzoroark.prift.client.ui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;
import static net.minecraft.client.gui.Gui.drawRect;

public abstract class RiftGuiScrollableSection {
    //width and height of the section
    public final int width;
    public final int height;

    //x and y offsets of the section
    public final int xOffset;
    public final int yOffset;

    //width and height of the screen the section will be put in
    public int guiWidth;
    public int guiHeight;

    //gonna be pretty important for scrolling
    protected int contentHeight;

    //scroll stuff
    protected int scrollOffset = 0;
    protected int maxScroll = 0;
    protected int scrollStep = 10;
    protected final int scrollbarWidth = 2;
    private boolean draggingScrollbar = false;
    private int dragOffsetY = 0;
    protected int scrollbarXOffset;
    protected int scrollbarYOffset;

    //logic involved in disabling buttons
    private final List<String> disabledButtonIds = new ArrayList<>();

    //other important shite
    protected Minecraft minecraft;
    protected final FontRenderer fontRenderer;

    //button stuff
    private final List<RiftGuiSectionButton> activeButtons = new ArrayList<>();

    //tab stuff
    private final Map<String, String> activeTabs = new HashMap<>(); //1st string is id for tabs list, 2nd string is name of tab
    private final List<TabClickRegion> activeTabRegions = new ArrayList<>();

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
        this.activeTabRegions.clear();

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

        //elements are drawn here
        for (int i = 0; i < this.defineSectionContents().getContents().size(); i++) {
            RiftGuiScrollableSectionContents.Element element = this.defineSectionContents().getContents().get(i);

            //draw the elements and add up their height
            totalHeight += this.drawElement(element,  sectionX, drawY + totalHeight, mouseX, mouseY, partialTicks);

            //extra bottom height for certain elements
            if (i < this.defineSectionContents().getContents().size() - 1) totalHeight += this.elementBottomSpace(element);
        }

        //scroll management is dealt with here
        this.contentHeight = totalHeight;
        this.maxScroll = Math.max(0, this.contentHeight - this.height);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //draw scrollbar
        if (this.contentHeight > this.height) {
            float ratio = (float) this.scrollOffset / (float) this.maxScroll;
            int thumbHeight = Math.max(20, (int)((float) this.height * this.height / (float) this.contentHeight));
            int thumbY = sectionY + (int)((this.height - thumbHeight) * ratio);
            int scrollX = sectionX + this.width - this.scrollbarWidth + this.scrollbarXOffset;

            drawRect(scrollX, sectionY, scrollX + this.scrollbarWidth, sectionY + this.height, 0xFF333333);
            drawRect(scrollX, thumbY, scrollX + this.scrollbarWidth, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    //return value is the total height created by these elements
    private int drawElement(RiftGuiScrollableSectionContents.Element element, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (element instanceof RiftGuiScrollableSectionContents.TextElement) {
            RiftGuiScrollableSectionContents.TextElement text = (RiftGuiScrollableSectionContents.TextElement) element;
            this.fontRenderer.drawSplitString(text.getContents(), x, y, this.width, text.getTextColor());
            int lines = this.fontRenderer.listFormattedStringToWidth(text.getContents(), this.width).size();
            return lines * this.fontRenderer.FONT_HEIGHT;
        }
        else if (element instanceof RiftGuiScrollableSectionContents.ImageElement) {
            RiftGuiScrollableSectionContents.ImageElement img = (RiftGuiScrollableSectionContents.ImageElement) element;
            this.minecraft.getTextureManager().bindTexture(img.getImageLocation());

            int imgW = img.getImageSize()[0];
            int imgH = img.getImageSize()[1];
            float scale = img.getImageScale();

            int scaledW = (int)(imgW * scale);
            int scaledH = (int)(imgH * scale);
            int imgX = x + (this.width - scaledW) / 2;

            GlStateManager.pushMatrix();
            GlStateManager.translate(imgX, y, 0);
            GlStateManager.scale(scale, scale, scale);

            drawModalRectWithCustomSizedTexture(0, 0, 0, 0, imgW, imgH, imgW, imgH);
            GlStateManager.popMatrix();

            return scaledH;
        }
        else if (element instanceof RiftGuiScrollableSectionContents.ButtonElement) {
            RiftGuiScrollableSectionContents.ButtonElement but = (RiftGuiScrollableSectionContents.ButtonElement) element;

            int buttonW = but.getSize()[0];
            int buttonH = but.getSize()[1];
            int buttonX = x + (this.width - buttonW) / 2;
            int buttonY = y;

            //compute scrollable section bounds
            int sectionTop = (this.guiHeight - this.height) / 2 + this.yOffset;
            int sectionBottom = sectionTop + this.height;

            //only render and register button if it’s within visible bounds
            if ((buttonY + buttonH) > sectionTop && buttonY < sectionBottom) {
                RiftGuiSectionButton button = new RiftGuiSectionButton(but.getId(), buttonX, buttonY, buttonW, buttonH, but.getName());
                button.scrollTop = sectionTop;
                button.scrollBottom = sectionBottom;

                if (this.disabledButtonIds.contains(but.getId())) button.enabled = false;

                button.drawButton(this.minecraft, mouseX, mouseY, partialTicks);
                this.activeButtons.add(button);
            }

            return buttonH;
        }
        else if (element instanceof RiftGuiScrollableSectionContents.TabElement) {
            RiftGuiScrollableSectionContents.TabElement tab = (RiftGuiScrollableSectionContents.TabElement) element;
            List<String> tabs = tab.getTabOrder();

            int tabX = x;
            int tabY = y;
            int tabHeight = 18;
            int tabPadding = 6;

            //draw tabs
            for (String tabName : tabs) {
                int tabWidth = this.fontRenderer.getStringWidth(I18n.format("gui.tab_header."+tabName)) + tabPadding * 2;

                //save region for hover and click
                TabClickRegion region = new TabClickRegion(tabX, tabY, tabWidth, tabHeight, tab, tabName);
                this.activeTabRegions.add(region);

                //detect hover
                boolean isHovered = region.isHovered(mouseX, mouseY);
                boolean isActive = tabName.equals(this.activeTabs.getOrDefault(tab.getId(), tabs.get(0)));

                //text color logic
                int textColor = 0xFFFFFFFF; // normal white;
                if (isHovered) textColor = 0xFFFF00; //yellow for hover
                else if (isActive) textColor = 0x0000FF; //blue for active

                //draw outline
                this.drawRectOutline(tabX, tabY, tabWidth, tabHeight, 0xFF000000);

                //draw text
                this.fontRenderer.drawStringWithShadow(I18n.format("gui.tab_header."+tabName), tabX + tabPadding, tabY + 5, textColor);

                tabX += tabWidth + 4;
            }

            //draw tab content box
            int contentBoxY = tabY + tabHeight + 4;
            int contentPadding = 4;

            String activeTab = this.activeTabs.computeIfAbsent(tab.getId(), id -> tabs.get(0));
            RiftGuiScrollableSectionContents.Element content = tab.getTabContents().get(activeTab);

            int contentInnerHeight = 0;
            if (content != null) {
                int contentInnerX = x + contentPadding;
                int contentInnerY = contentBoxY + contentPadding;

                contentInnerHeight = this.drawElement(content, contentInnerX, contentInnerY, mouseX, mouseY, partialTicks);

                // Draw content outline box after measuring
                this.drawRectOutline(x, contentBoxY, this.width, contentInnerHeight + contentPadding * 2, 0xFF000000);
            }

            return tabHeight + 4 + contentInnerHeight + contentPadding * 2; //total vertical space used
        }

        return 0;
    }

    private int elementBottomSpace(RiftGuiScrollableSectionContents.Element element) {
        if (element instanceof RiftGuiScrollableSectionContents.TextElement) {
            return this.fontRenderer.FONT_HEIGHT;
        }
        else if (element instanceof RiftGuiScrollableSectionContents.ImageElement) {
            RiftGuiScrollableSectionContents.ImageElement imageElement = (RiftGuiScrollableSectionContents.ImageElement) element;
            return imageElement.getBottomSpace();
        }
        else if (element instanceof RiftGuiScrollableSectionContents.ButtonElement) {
            RiftGuiScrollableSectionContents.ButtonElement buttonElement = (RiftGuiScrollableSectionContents.ButtonElement) element;
            return buttonElement.getBottomSpaceSize();
        }
        return 0;
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

    public void mouseClicked(int mouseX, int mouseY, int button) {
        //for tab clicking
        for (TabClickRegion region : this.activeTabRegions) {
            if (region.isHovered(mouseX, mouseY)) {
                if (this.activeTabs.containsKey(region.tabElement.getId())) this.activeTabs.replace(region.tabElement.getId(), region.tabName);
                else this.activeTabs.put(region.tabElement.getId(), region.tabName);

                //change in tab should lead to scroll progress being reset
                this.resetScroll();

                break;
            }
        }

        //for dealing with dragging the cursor of the scrollbar
        if (button == 0 && this.contentHeight > this.height) {
            int sectionX = (this.guiWidth - this.width) / 2 + this.xOffset;
            int sectionY = (this.guiHeight - this.height) / 2 + this.yOffset;
            int scrollX = sectionX + this.width - this.scrollbarWidth + this.scrollbarXOffset;

            float ratio = (float) this.scrollOffset / this.maxScroll;
            int thumbHeight = Math.max(20, (int)((float) this.height * this.height / this.contentHeight));
            int thumbY = sectionY + (int)((this.height - thumbHeight) * ratio);

            if (mouseX >= scrollX && mouseX <= scrollX + this.scrollbarWidth &&
                    mouseY >= sectionY && mouseY <= sectionY + height) {
                if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                    //start dragging
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

    public void resetScroll() {
        this.scrollOffset = 0;
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

    public void resetTabs() {
        this.activeTabs.clear();
    }

    private void drawRectOutline(int x, int y, int w, int h, int color) {
        drawRect(x, y, x + w, y + 1, color);             // top
        drawRect(x, y + h - 1, x + w, y + h, color);     // bottom
        drawRect(x, y, x + 1, y + h, color);             // left
        drawRect(x + w - 1, y, x + w, y + h, color);     // right
    }

    private static class TabClickRegion {
        int x, y, w, h;
        RiftGuiScrollableSectionContents.TabElement tabElement;
        String tabName;

        TabClickRegion(int x, int y, int w, int h, RiftGuiScrollableSectionContents.TabElement tabElement, String tabName) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.tabElement = tabElement;
            this.tabName = tabName;
        }

        boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        }
    }
}
