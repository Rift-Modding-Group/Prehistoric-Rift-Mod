package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.helper.RiftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;
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

    //for item lists
    private final List<ItemClickRegion> itemClickRegions = new ArrayList<>();

    //for mining level lists
    private final List<MiningLevelRegion> miningLevelRegions = new ArrayList<>();

    //other important shite
    protected Minecraft minecraft;
    protected final FontRenderer fontRenderer;

    //button stuff
    private final List<RiftGuiSectionButton> activeButtons = new ArrayList<>();

    //clickable sections
    private final List<RiftClickableSection> clickableSections = new ArrayList<>();
    private final List<String> selectedClickableSections = new ArrayList<>();

    //tab stuff
    private final Map<String, String> activeTabs = new HashMap<>(); //1st string is id for tabs list, 2nd string is name of tab
    private final List<TabClickRegion> activeTabRegions = new ArrayList<>();

    //text box stuff
    private final Map<String, GuiTextField> textFields = new HashMap<>();
    private final Map<String, String> tempTextField = new HashMap<>();

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
        this.itemClickRegions.clear();
        this.miningLevelRegions.clear();
        this.clickableSections.clear();

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
            totalHeight += this.drawElement(element, this.width, sectionX, drawY + totalHeight, mouseX, mouseY, partialTicks);

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
    private int drawElement(RiftGuiScrollableSectionContents.Element element, int elementWidth, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (element instanceof RiftGuiScrollableSectionContents.TextElement) {
            RiftGuiScrollableSectionContents.TextElement text = (RiftGuiScrollableSectionContents.TextElement) element;

            if (text.getBackground() != null) {
                GlStateManager.color(1f, 1f, 1f, 1f);
                this.minecraft.getTextureManager().bindTexture(text.getBackground());
                GlStateManager.pushMatrix();
                int bgX = text.getBGCentered() ? x + (elementWidth - text.getBGUVSize()[0]) / 2 : x;
                GlStateManager.translate(bgX, y, 0);
                drawModalRectWithCustomSizedTexture(0, 0,
                        text.getBGImageUV()[0],
                        text.getBGImageUV()[1],
                        text.getBGUVSize()[0],
                        text.getBGUVSize()[1],
                        text.getBGImageSize()[0],
                        text.getBGImageSize()[1]
                );
                GlStateManager.popMatrix();
            }

            float scale = text.getScale();
            int lines = 1;

            if (scale != 1f) {
                int totalTextX = text.getTextCentered() ? x + text.getWidthOffset() - (elementWidth / 2) : x + text.getWidthOffset();
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);
                this.fontRenderer.drawSplitString(
                        text.getContents(),
                        (int) (totalTextX / scale),
                        (int) ((y + text.getHeightOffset()) / scale),
                        (int) ((elementWidth - text.getWidthOffset()) / scale),
                        text.getTextColor()
                );
                GlStateManager.popMatrix();
            }
            else {
                int finalTextWidth = Math.min(elementWidth, this.fontRenderer.getStringWidth(text.getContents()));
                int totalTextX = text.getTextCentered() ? x + (elementWidth - finalTextWidth) / 2 : x;
                this.fontRenderer.drawSplitString(text.getContents(), totalTextX, y, elementWidth, text.getTextColor());
                lines = this.fontRenderer.listFormattedStringToWidth(text.getContents(), elementWidth).size();
            }

            return lines * this.fontRenderer.FONT_HEIGHT + text.getBottomSpace();
        }
        else if (element instanceof RiftGuiScrollableSectionContents.ImageElement) {
            RiftGuiScrollableSectionContents.ImageElement img = (RiftGuiScrollableSectionContents.ImageElement) element;
            this.minecraft.getTextureManager().bindTexture(img.getImageLocation());

            int imgW = img.getImageSize()[0];
            int imgH = img.getImageSize()[1];
            float scale = img.getImageScale();

            int scaledW = (int)(imgW * scale);
            int scaledH = (int)(imgH * scale);
            int imgX = x + (elementWidth - scaledW) / 2;

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
            int buttonX = x + (elementWidth - buttonW) / 2;
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
        else if (element instanceof RiftGuiScrollableSectionContents.ItemListElement) {
            RiftGuiScrollableSectionContents.ItemListElement itemList = (RiftGuiScrollableSectionContents.ItemListElement) element;

            //make label
            int headerHeight = this.fontRenderer.FONT_HEIGHT;
            this.fontRenderer.drawSplitString(itemList.getHeaderText(), x, y, elementWidth, itemList.getHeaderTextColor());

            //now make the item list
            int itemsPerRow = 8;
            int itemSize = 16;
            int itemSpacing = 4;
            int fullItemSize = itemSize + itemSpacing;

            int totalItems = itemList.getItemsById().size();
            int totalRows = (int) Math.ceil(totalItems / (float) itemsPerRow);
            int gridYOffset = y + headerHeight + 4;

            for (int i = 0; i < totalItems; i++) {
                String itemId = itemList.getItemsById().get(i);
                ItemStack itemStack = RiftUtil.getItemStackFromString(itemId);

                int col = i % itemsPerRow;
                int row = i / itemsPerRow;

                int itemX = x + col * fullItemSize;
                int itemY = gridYOffset + row * fullItemSize;

                this.renderItem(itemStack, itemX, itemY);
                this.itemClickRegions.add(new ItemClickRegion(itemX, itemY, itemStack));
            }

            return headerHeight + 4 + (totalRows * fullItemSize);

        }
        else if (element instanceof RiftGuiScrollableSectionContents.MiningLevelListElement) {
            RiftGuiScrollableSectionContents.MiningLevelListElement miningLevelList = (RiftGuiScrollableSectionContents.MiningLevelListElement) element;

            //make label
            int headerHeight = this.fontRenderer.FONT_HEIGHT;
            this.fontRenderer.drawSplitString(miningLevelList.getHeaderText(), x, y, elementWidth, miningLevelList.getHeaderTextColor());

            //now visualize the mining levels using wooden tools and a number
            //why wooden tools? well theres mods that let u modify mining levels of tools and am not wanna deal with
            //that shit, so just 1 tool type would make it easier
            int itemsPerRow = 8;
            int itemSize = 16;
            int itemSpacing = 4;
            int fullItemSize = itemSize + itemSpacing;

            int totalItems = miningLevelList.getMiningLevels().size();
            int totalRows = (int) Math.ceil(totalItems / (float) itemsPerRow);
            int gridYOffset = y + headerHeight + 4;

            for (int i = 0; i < totalItems; i++) {
                //get tool type and mining level from string
                String miningLevel = miningLevelList.getMiningLevels().get(i);
                String tool = miningLevel.substring(0, miningLevel.indexOf(":"));
                int level = Integer.parseInt(miningLevel.substring(miningLevel.indexOf(":")+1));

                //get wooden tool associated with tool type
                ItemStack itemStack = this.getToolTypeForMiningLevel(tool);

                int col = i % itemsPerRow;
                int row = i / itemsPerRow;

                int itemX = x + col * fullItemSize;
                int itemY = gridYOffset + row * fullItemSize;

                //render tool
                this.renderItem(itemStack, itemX, itemY);
                this.miningLevelRegions.add(new MiningLevelRegion(itemX, itemY, tool, level));

                //render mining level
                this.fontRenderer.renderString(String.valueOf(level), itemX + 9, itemY + 12, 0, false);
            }

            return headerHeight + 4 + (totalRows * fullItemSize);
        }
        else if (element instanceof RiftGuiScrollableSectionContents.TextBoxElement) {
            RiftGuiScrollableSectionContents.TextBoxElement textBox = (RiftGuiScrollableSectionContents.TextBoxElement) element;

            int textBoxX = x + (elementWidth - textBox.getWidth()) / 2;
            int textBoxY = y + 5;

            //create text field if it doesn't exist
            if (!this.textFields.containsKey(textBox.getId())) {
                GuiTextField textField = new GuiTextField(0, this.fontRenderer, textBoxX, textBoxY, textBox.getWidth(), 20);
                textField.setFocused(true);
                textField.drawTextBox();
                if (!this.tempTextField.isEmpty() && this.tempTextField.containsKey(textBox.getId())) {
                    textField.setText(this.tempTextField.get(textBox.getId()));
                    //this.tempTextField.remove(textBox.getId());
                }
                this.textFields.put(textBox.getId(), textField);
            }
            //just draw it if it does exist
            else this.textFields.get(textBox.getId()).drawTextBox();

            return 25 + this.fontRenderer.FONT_HEIGHT;
        }
        else if (element instanceof RiftGuiScrollableSectionContents.ProgressBarElement) {
            RiftGuiScrollableSectionContents.ProgressBarElement pBar = (RiftGuiScrollableSectionContents.ProgressBarElement) element;

            String label = pBar.getHeaderText();
            float scale = pBar.getHeaderScale();
            int scaledHeaderHeight = (int) (this.fontRenderer.FONT_HEIGHT * scale);

            //draw label with scaling
            if (scale != 1f) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);
                this.fontRenderer.drawString(
                        label,
                        (int)(x / scale),
                        (int)(y / scale),
                        pBar.getHeaderTextColor()
                );
                GlStateManager.popMatrix();
            }
            else this.fontRenderer.drawString(label, x, y, pBar.getHeaderTextColor());

            //progress bar position below scaled label
            int progressX = x;
            int progressY = y + scaledHeaderHeight + 2;

            //draw frame
            this.drawRectOutline(progressX, progressY, pBar.getWidth() + 2, 3, 0xFF000000);

            //draw background and overlay
            this.drawRectOutline(progressX + 1, progressY + 1, pBar.getWidth(), 1, (0xFF000000 | pBar.getBackgroundColor()));
            if (pBar.getPercentage() > 0) {
                float clampedPercentage = pBar.getFactor() > 0 ? MathHelper.clamp(
                        this.clampToFactor(pBar.getPercentage(), pBar.getFactor()),
                        0,
                        1f
                ) : pBar.getPercentage();
                this.drawRectOutline(progressX + 1, progressY + 1, (int) (pBar.getWidth() * clampedPercentage), 1, (0xFF000000 | pBar.getOverlayColor()));
            }

            return scaledHeaderHeight + 3 + this.fontRenderer.FONT_HEIGHT;
        }
        else if (element instanceof RiftGuiScrollableSectionContents.ClickableSectionElement) {
            RiftGuiScrollableSectionContents.ClickableSectionElement clickableSectionElement = (RiftGuiScrollableSectionContents.ClickableSectionElement) element;

            int secX = clickableSectionElement.getSectionCentered() ? x + (elementWidth - clickableSectionElement.getSize()[0]) / 2 : x;

            RiftClickableSection clickableSection = new RiftClickableSection(
                    clickableSectionElement.getSize()[0],
                    clickableSectionElement.getSize()[1],
                    clickableSectionElement.getSize()[0],
                    clickableSectionElement.getSize()[1],
                    secX,
                    y,
                    this.fontRenderer,
                    this.minecraft
            );

            if (clickableSectionElement.getImage() != null) {
                clickableSection.addImage(
                        clickableSectionElement.getImage(),
                        clickableSectionElement.getUVSize()[0],
                        clickableSectionElement.getUVSize()[1],
                        clickableSectionElement.getImageSize()[0],
                        clickableSectionElement.getImageSize()[1],
                        clickableSectionElement.getImageUV()[0],
                        clickableSectionElement.getImageUV()[1],
                        clickableSectionElement.getImageHoveredUV()[0],
                        clickableSectionElement.getImageHoveredUV()[1]
                );
                if (clickableSectionElement.getImageSelectedUV() != null) clickableSection.setSelectedUV(clickableSectionElement.getImageSelectedUV()[0], clickableSectionElement.getImageSelectedUV()[1]);
            }

            if (clickableSectionElement.getTextContent() != null) {
                clickableSection.addString(
                        clickableSectionElement.getTextContent(),
                        false,
                        clickableSectionElement.getTextColor(),
                        clickableSectionElement.getTextOffsets()[0],
                        clickableSectionElement.getTextOffsets()[1],
                        clickableSectionElement.getTextScale()
                );
                if (clickableSectionElement.getTextHoveredColor() >= 0) clickableSection.setStringHoveredColor(clickableSectionElement.getTextHoveredColor());
                if (clickableSectionElement.getTextSelectedColor() >= 0) clickableSection.setStringSelectedColor(clickableSectionElement.getTextSelectedColor());
            }

            clickableSection.setID(clickableSectionElement.getID());
            if (this.selectedClickableSections.contains(clickableSectionElement.getID())) clickableSection.setSelected(true);
            clickableSection.drawSection(mouseX, mouseY);
            this.clickableSections.add(clickableSection);

            return clickableSectionElement.getSize()[1] + clickableSectionElement.getBottomSpace();
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
                if (isActive) textColor = 0x5A3B1A; //brown for active
                else if (isHovered) textColor = 0xFFFF00; //yellow for hover

                //draw outline
                this.drawRectOutline(tabX, tabY, tabWidth, tabHeight, 0xFF000000);

                //draw text
                this.fontRenderer.drawStringWithShadow(I18n.format("gui.tab_header."+tabName), tabX + tabPadding, tabY + 5, textColor);

                tabX += tabWidth + 4;
            }

            //draw tab content box
            int contentBoxY = tabY + tabHeight + 4;
            int contentPadding = 4;

            //draw contents
            String activeTab = this.activeTabs.computeIfAbsent(tab.getId(), id -> tabs.get(0));
            List<RiftGuiScrollableSectionContents.Element> content = tab.getTabContents().get(activeTab);

            int contentInnerHeight = 0;
            if (content != null && !content.isEmpty()) {
                int contentInnerX = x + contentPadding;
                int contentInnerY = contentBoxY + contentPadding;

                for (int i = 0; i < content.size(); i++) {
                    RiftGuiScrollableSectionContents.Element elementInTab = content.get(i);

                    int usedHeight = this.drawElement(elementInTab, this.getTabContentWidth(tab), contentInnerX, contentInnerY, mouseX, mouseY, partialTicks);
                    contentInnerY += usedHeight;
                    contentInnerHeight += usedHeight;

                    //extra bottom height for certain elements
                    if (i < content.size() - 1) {
                        int bottomSpace = this.elementBottomSpace(elementInTab);
                        contentInnerY += bottomSpace;
                        contentInnerHeight += bottomSpace;
                    }
                }

                //draw content outline box after measuring
                this.drawRectOutline(x, contentBoxY, this.getTabContentWidth(tab) + contentPadding * 2, contentInnerHeight + contentPadding * 2, 0xFF000000);
            }

            return tabHeight + 4 + contentInnerHeight + contentPadding * 2; //total vertical space used
        }
        else if (element instanceof RiftGuiScrollableSectionContents.ButtonRowElement) {
            RiftGuiScrollableSectionContents.ButtonRowElement buttonRowElement = (RiftGuiScrollableSectionContents.ButtonRowElement) element;

            //get width of entire row
            int totalButtonWidth = 0;
            int spacing = 5;

            for (RiftGuiScrollableSectionContents.ButtonElement button : buttonRowElement.getButtonElements()) {
                totalButtonWidth += button.getSize()[0];
            }
            totalButtonWidth += spacing * (buttonRowElement.getButtonElements().size() - 1);

            //calculate start X to center the row
            int startX = x + (elementWidth - totalButtonWidth) / 2;

            //get highest height amongst buttons, this will determine the height of the element
            int maxButtonH = 0;
            for (RiftGuiScrollableSectionContents.ButtonElement buttonElement : buttonRowElement.getButtonElements()) {
                if (maxButtonH < buttonElement.getSize()[1]) maxButtonH = buttonElement.getSize()[1];
            }

            //section bounds
            int sectionTop = (this.guiHeight - this.height) / 2 + this.yOffset;
            int sectionBottom = sectionTop + this.height;

            //render each button using the existing ButtonElement rendering logic
            int currentX = startX;
            for (RiftGuiScrollableSectionContents.ButtonElement but : buttonRowElement.getButtonElements()) {
                int buttonW = but.getSize()[0];
                int buttonH = but.getSize()[1];
                int buttonY = y + (maxButtonH - buttonH) / 2;

                if ((buttonY + buttonH) > sectionTop && buttonY < sectionBottom) {
                    RiftGuiSectionButton button = new RiftGuiSectionButton(but.getId(), currentX, buttonY, buttonW, buttonH, but.getName());
                    button.scrollTop = sectionTop;
                    button.scrollBottom = sectionBottom;

                    if (this.disabledButtonIds.contains(but.getId())) {
                        button.enabled = false;
                    }

                    button.drawButton(this.minecraft, mouseX, mouseY, partialTicks);
                    this.activeButtons.add(button);
                }

                currentX += buttonW + spacing;
            }

            return maxButtonH;
        }

        return 0;
    }

    private ItemStack getToolTypeForMiningLevel(String value) {
        String fullTool = "minecraft:wooden_"+value;
        return RiftUtil.getItemStackFromString(fullTool);
    }

    private int getTabContentWidth(RiftGuiScrollableSectionContents.TabElement tabElement) {
        if (tabElement.getWidth() > 0) return tabElement.getWidth();
        return this.width;
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
            if (region.isHovered(mouseX, mouseY) && !region.isActive(this.activeTabs)) {
                if (this.activeTabs.containsKey(region.tabElement.getId())) this.activeTabs.replace(region.tabElement.getId(), region.tabName);
                else this.activeTabs.put(region.tabElement.getId(), region.tabName);

                //change in tab should lead to scroll progress being reset
                this.resetScrollProgress();

                break;
            }
        }

        //for item clicking
        for (ItemClickRegion region : this.itemClickRegions) {
            if (region.isHovered(mouseX, mouseY)) {
                if (Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) {
                    RiftJEI.showRecipesForItemStack(region.stack, false);
                }
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

    public void resetScrollProgress() {
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

    public List<RiftClickableSection> getClickableSections() {
        return this.clickableSections;
    }

    public void selectClickableSectionById(String value) {
        this.selectedClickableSections.add(value);
    }

    public void unselectClickableSectionById(String value) {
        this.selectedClickableSections.remove(value);
    }

    public void unselectAllClickableSections() {
        this.selectedClickableSections.clear();
    }

    public void resetTabs() {
        this.activeTabs.clear();
    }

    public int getScrollOffset() {
        return this.scrollOffset;
    }

    public void setScrollOffset(int value) {
        this.scrollOffset = value;
    }

    public Map<String, String> getActiveTabs() {
        return new HashMap<>(this.activeTabs); // defensive copy
    }

    public Map<String, GuiTextField> getTextFields() {
        return this.textFields;
    }

    public void setTextFieldString(String id, String value) {
        if (this.textFields.isEmpty()) this.tempTextField.put(id, value);
        else {
            for (Map.Entry<String, GuiTextField> entry : this.textFields.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equals(id)) {
                    GuiTextField textField = entry.getValue();
                    textField.setText(value);
                    this.textFields.replace(id, textField);
                }
            }
        }
    }

    public String getTextFieldString(String id) {
        for (Map.Entry<String, GuiTextField> entry : this.textFields.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equals(id)) {
                return entry.getValue().getText();
            }
        }
        return "";
    }

    public Map<String, String> getTempTextField() {
        return this.tempTextField;
    }

    public void resetTextFields() {
        this.textFields.clear();
    }

    public void setActiveTabs(Map<String, String> tabs) {
        this.activeTabs.clear();
        if (tabs != null) this.activeTabs.putAll(tabs);
    }

    private void drawRectOutline(int x, int y, int w, int h, int color) {
        drawRect(x, y, x + w, y + 1, color);             // top
        drawRect(x, y + h - 1, x + w, y + h, color);     // bottom
        drawRect(x, y, x + 1, y + h, color);             // left
        drawRect(x + w - 1, y, x + w, y + h, color);     // right
    }

    private void renderItem(ItemStack stack, int x, int y) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.renderItemAndEffectIntoGUI(stack, x, y);
        renderItem.renderItemOverlayIntoGUI(this.fontRenderer, stack, x, y, null);
    }

    public ItemStack getHoveredItemStack(int mouseX, int mouseY) {
        for (RiftGuiScrollableSection.ItemClickRegion region : this.itemClickRegions) {
            if (region.isHovered(mouseX, mouseY)) {
                return region.stack;
            }
        }
        return null;
    }

    public MiningLevelRegion getHoveredMiningLevel(int mouseX, int mouseY) {
        for (RiftGuiScrollableSection.MiningLevelRegion region : this.miningLevelRegions) {
            if (region.isHovered(mouseX, mouseY)) {
                return region;
            }
        }
        return null;
    }

    private float clampToFactor(float rawPercent, float factor) {
        return MathHelper.clamp((float) Math.floor(rawPercent / factor) * factor, 0f, 1f);
    }

    private static class TabClickRegion {
        private final int x, y, w, h;
        private final RiftGuiScrollableSectionContents.TabElement tabElement;
        private final String tabName;

        public TabClickRegion(int x, int y, int w, int h, RiftGuiScrollableSectionContents.TabElement tabElement, String tabName) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.tabElement = tabElement;
            this.tabName = tabName;
        }

        private boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.w && mouseY >= y && mouseY < this.y + this.h;
        }

        private boolean isActive(Map<String, String> activeTabs) {
            return this.tabName.equals(activeTabs.getOrDefault(tabElement.getId(), tabElement.getTabOrder().get(0)));
        }
    }

    private static class ItemClickRegion {
        private final int x, y, size = 16;
        private final ItemStack stack;

        private ItemClickRegion(int x, int y, ItemStack stack) {
            this.x = x;
            this.y = y;
            this.stack = stack;
        }

        private boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.size && mouseY >= this.y && mouseY < this.y + this.size;
        }
    }

    public static class MiningLevelRegion {
        private final int x, y, size = 16;
        public final String miningTool;
        public final int miningLevel;

        private MiningLevelRegion(int x, int y, String miningTool, int miningLevel) {
            this.x = x;
            this.y = y;
            this.miningTool = miningTool;
            this.miningLevel = miningLevel;
        }

        private boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= this.x && mouseX < this.x + this.size && mouseY >= this.y && mouseY < this.y + this.size;
        }
    }
}
