package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftClickableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiSectionButton;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class RiftJournalScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/journal_background.png");
    private RiftJournalInfoSection journalInfoSection;
    private RiftJournalEntriesSection journalEntriesSection;
    private RiftClickableSection indexClickableSection;
    private RiftClickableSection searchClickableSection;
    private RiftClickableSection partyClickableSection;
    private RiftCreatureType lastEntry;
    private RiftCreatureType.CreatureCategory lastCategory;
    private int savedInfoScroll = 0;
    private int savedEntriesScroll = 0;
    private Map<String, String> savedInfoTabs = new HashMap<>();
    private Map<String, String> savedEntriesTabs = new HashMap<>();
    protected final int xSize = 420;
    protected final int ySize = 240;
    private boolean searchMode;
    private String searchText = "";

    @Override
    public void initGui() {
        super.initGui();

        //save current scroll progress and tabs
        if (this.journalInfoSection != null) {
            this.savedInfoScroll = this.journalInfoSection.getScrollOffset();
            this.savedInfoTabs = this.journalInfoSection.getActiveTabs();
        }
        if (this.journalEntriesSection != null) {
            this.savedEntriesScroll = this.journalEntriesSection.getScrollOffset();
            this.savedEntriesTabs = this.journalEntriesSection.getActiveTabs();
        }

        //create the sections
        this.journalInfoSection = new RiftJournalInfoSection(this.width, this.height, this.fontRenderer, this.mc);
        this.journalEntriesSection = new RiftJournalEntriesSection(this.width, this.height, this.fontRenderer, this.mc);

        //restore search mode in the sections
        this.journalInfoSection.setSearchMode(this.searchMode);
        this.journalEntriesSection.setSearchMode(this.searchMode);

        //restore tabs
        this.journalInfoSection.setActiveTabs(this.savedInfoTabs);
        this.journalEntriesSection.setActiveTabs(this.savedEntriesTabs);

        //restore based on if search mode was on or off
        if (this.searchMode) {
            this.journalInfoSection.setTextFieldString("searchBox", this.searchText);
            if (this.lastEntry != null) {
                this.journalInfoSection.setEntryType(this.lastEntry);
                this.journalEntriesSection.disableButtonById(this.lastEntry.toString());
                this.journalEntriesSection.setStringForSearch(this.searchText);
            }
        }
        else {
            //restore category and entry
            if (this.lastCategory != null) {
                this.journalEntriesSection.setCurrentCategory(this.lastCategory);
                if (this.lastEntry != null) {
                    this.journalInfoSection.setEntryType(this.lastEntry);
                    this.journalEntriesSection.disableButtonById(this.lastEntry.toString());
                }
            }
        }

        //restore scroll
        this.journalInfoSection.setScrollOffset(this.savedInfoScroll);
        this.journalEntriesSection.setScrollOffset(this.savedEntriesScroll);

        //create the clickable sections
        this.indexClickableSection = new RiftClickableSection(101, 12, this.width, this.height, -147, -112, this.fontRenderer, this.mc);
        this.indexClickableSection.addString(
                I18n.format("journal.index_button"),
                true,
                0xffffff,
                0,
                1
        );
        this.indexClickableSection.setSelected(true); //index is the first thing, so here

        this.searchClickableSection = new RiftClickableSection(101, 12, this.width, this.height, -32, -112, this.fontRenderer, this.mc);
        this.searchClickableSection.addString(
                I18n.format("journal.search_button"),
                true,
                0xffffff,
                0,
                1
        );

        this.partyClickableSection = new RiftClickableSection(12, 12, this.width, this.height, 191, -112, this.fontRenderer, this.mc);
        this.partyClickableSection.addImage(
                background,
                13,
                13,
                560,
                240,
                420,
                136,
                420,
                149
        );

        //restore certain clickable section stuff based on search mode
        if (this.searchMode) {
            this.indexClickableSection.setSelected(false);
            this.searchClickableSection.setSelected(true);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        //update sections
        this.journalInfoSection.updateGuiSize(this.width, this.height);
        this.journalEntriesSection.updateGuiSize(this.width, this.height);

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //draw sections
        this.journalInfoSection.drawSectionContents(mouseX, mouseY, partialTicks);
        this.journalEntriesSection.drawSectionContents(mouseX, mouseY, partialTicks);

        //draw clickable regions on the top
        //index is the default portion,
        this.indexClickableSection.drawSection(mouseX, mouseY);
        this.searchClickableSection.drawSection(mouseX, mouseY);
        this.partyClickableSection.drawSection(mouseX, mouseY);

        //change journal entries section depending on what was searched for
        if (this.searchMode) {
            //make sure search text can be changed as long as the text field can be seen
            if (this.journalInfoSection.getEntryType() == null) this.searchText = this.journalInfoSection.getTextFieldString("searchBox");
            if (!this.journalInfoSection.getTextFields().isEmpty() && this.journalInfoSection.getTextFields().containsKey("searchBox")) {
                //reset scroll progress when a change is made
                if (!this.journalEntriesSection.getStringForSearch().equals(this.searchText)) this.journalEntriesSection.resetScrollProgress();
                this.journalEntriesSection.setStringForSearch(this.searchText);
            }
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        //hover detection and tooltip rendering for items in item lists
        ItemStack hoveredStack = null;

        //check which section (if any) has a hovered item
        ItemStack hoveredFromInfo = this.journalInfoSection.getHoveredItemStack(mouseX, mouseY);
        ItemStack hoveredFromEntries = this.journalEntriesSection.getHoveredItemStack(mouseX, mouseY);

        if (hoveredFromInfo != null) {
            hoveredStack = hoveredFromInfo;
        }
        else if (hoveredFromEntries != null) {
            hoveredStack = hoveredFromEntries;
        }

        //if hovering an item, render its tooltip above all
        if (hoveredStack != null) {
            List<String> tooltip = new ArrayList<>();

            tooltip.add(hoveredStack.getDisplayName());
            if (Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) {
                tooltip.add(I18n.format("journal.open_in_jei"));
            }
            this.drawHoveringText(tooltip, mouseX, mouseY);
        }

        //mining level related stuff
        RiftGuiScrollableSection.MiningLevelRegion miningLevelRegion = this.journalInfoSection.getHoveredMiningLevel(mouseX, mouseY);
        if (miningLevelRegion != null) {
            this.drawHoveringText(I18n.format("journal.mining_info", miningLevelRegion.miningTool , miningLevelRegion.miningLevel), mouseX, mouseY);
        }
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, 560F, 240F);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int delta = Mouse.getEventDWheel();
        if (delta != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            this.journalInfoSection.handleMouseInput(mouseX, mouseY, delta);
            this.journalEntriesSection.handleMouseInput(mouseX, mouseY, delta);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //manually move scroll bar
        this.journalInfoSection.mouseClicked(mouseX, mouseY, mouseButton);
        this.journalEntriesSection.mouseClicked(mouseX, mouseY, mouseButton);

        //manage clickable sections
        //transition out of search mode
        if (this.indexClickableSection.isHovered(mouseX, mouseY) && !this.indexClickableSection.isSelected()) {
            this.lastEntry = null;
            this.indexClickableSection.setSelected(true);
            this.searchClickableSection.setSelected(false);
            this.searchMode = false;
            this.journalInfoSection.setSearchMode(false);
            this.journalInfoSection.resetTextFields();
            this.journalInfoSection.setEntryType(null);
            this.journalInfoSection.getTempTextField().clear();
            this.searchText = "";
            this.journalEntriesSection.setSearchMode(false);
            this.journalEntriesSection.setStringForSearch("");
            this.journalEntriesSection.reenableAllButtons();
        }
        //transition to search mode
        else if (this.searchClickableSection.isHovered(mouseX, mouseY) && !this.searchClickableSection.isSelected()) {
            this.lastEntry = null;
            this.indexClickableSection.setSelected(false);
            this.searchClickableSection.setSelected(true);
            this.searchMode = true;
            this.journalInfoSection.setSearchMode(true);
            this.journalInfoSection.setEntryType(null);
            this.journalEntriesSection.setSearchMode(true);
            this.journalEntriesSection.setCurrentCategory(null);
            this.journalEntriesSection.reenableAllButtons();
        }

        //manage buttons
        //all the additional logic here is for ensuring clicking out of bounds results in nothing
        int sectionTop = (this.journalEntriesSection.guiHeight - this.journalEntriesSection.height) / 2 + this.journalEntriesSection.yOffset;
        int sectionBottom = sectionTop + this.journalEntriesSection.height;
        for (RiftGuiSectionButton b : this.journalEntriesSection.getActiveButtons()) {
            int buttonTop = b.y;
            int buttonBottom = b.y + b.height;
            boolean clickWithinVisiblePart = mouseY >= Math.max(buttonTop, sectionTop) && mouseY <= Math.min(buttonBottom, sectionBottom);
            if (clickWithinVisiblePart && b.mousePressed(this.mc, mouseX, mouseY)) {
                b.playPressSound(this.mc.getSoundHandler());
                this.onButtonClicked(b);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.journalInfoSection.mouseReleased(mouseX, mouseY, state);
        this.journalEntriesSection.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.journalInfoSection.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.journalEntriesSection.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    //for button click
    private void onButtonClicked(RiftGuiSectionButton button) {
        if (this.searchMode) {
            //clear search results and go back to default search screen
            if (button.buttonId.equals("NULL")) {
                this.lastEntry = null;
                this.lastCategory = null;
                this.journalEntriesSection.reenableAllButtons();
                this.journalEntriesSection.resetScrollProgress();
                this.journalInfoSection.setEntryType(null);
                this.journalInfoSection.resetScrollProgress();
            }
            //choose a creature whose entry shall be read
            else {
                RiftCreatureType typeToChoose = RiftCreatureType.safeValOf(button.buttonId);
                if (typeToChoose != null && typeToChoose != this.lastEntry) {
                    this.journalInfoSection.setEntryType(typeToChoose);
                    if (this.lastEntry != null) this.journalEntriesSection.reenableButtonById(this.lastEntry.toString());
                    this.journalEntriesSection.disableButtonById(typeToChoose.toString());
                    this.lastEntry = typeToChoose;
                }
                this.journalInfoSection.resetScrollProgress();
                this.journalInfoSection.resetTabs();
            }
        }
        else {
            if (this.journalEntriesSection.getActiveButtons().contains(button)) {
                //choose a category
                if (this.journalEntriesSection.getCurrentCategory() == null) {
                    RiftCreatureType.CreatureCategory chosenCategory = RiftCreatureType.CreatureCategory.safeValOf(button.buttonId);
                    this.lastCategory = chosenCategory;
                    this.journalEntriesSection.setCurrentCategory(chosenCategory);
                }
                else {
                    //go back to index
                    if (button.buttonId.equals("NULL")) {
                        this.lastEntry = null;
                        this.lastCategory = null;
                        this.journalEntriesSection.reenableAllButtons();
                        this.journalEntriesSection.setCurrentCategory(null);
                        this.journalEntriesSection.resetScrollProgress();
                        this.journalInfoSection.setEntryType(null);
                        this.journalInfoSection.resetScrollProgress();
                    }
                    //choose a creature whose entry shall be read
                    else {
                        RiftCreatureType typeToChoose = RiftCreatureType.safeValOf(button.buttonId);
                        if (typeToChoose != null && typeToChoose != this.lastEntry) {
                            this.journalInfoSection.setEntryType(typeToChoose);
                            if (this.lastEntry != null) this.journalEntriesSection.reenableButtonById(this.lastEntry.toString());
                            this.journalEntriesSection.disableButtonById(typeToChoose.toString());
                            this.lastEntry = typeToChoose;
                        }
                        this.journalInfoSection.resetScrollProgress();
                        this.journalInfoSection.resetTabs();
                    }
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) this.mc.player.closeScreen();

        //deal with text box's input
        if (this.journalInfoSection != null) {
            for (Map.Entry<String, GuiTextField> textBoxEntry : this.journalInfoSection.getTextFields().entrySet()) {
                if (textBoxEntry.getValue() != null) {
                    textBoxEntry.getValue().textboxKeyTyped(typedChar, keyCode);
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        //deal with text boxes
        if (this.journalInfoSection != null) {
            for (Map.Entry<String, GuiTextField> textBoxEntry : this.journalInfoSection.getTextFields().entrySet()) {
                if (textBoxEntry.getValue() != null) {
                    textBoxEntry.getValue().updateCursorCounter();
                }
            }
        }
    }
}