package anightdazingzoroark.prift.client.ui.journalScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.journalScreen.elements.RiftJournalIndexSection;
import anightdazingzoroark.prift.client.ui.journalScreen.elements.RiftNewJournalInfoSection;
import anightdazingzoroark.prift.client.ui.partyScreen.RiftNewPartyScreen;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RiftNewJournalScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/journal_background.png");
    private boolean searchMode;

    public RiftNewJournalScreen() {
        super(0, 0, 0);
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Arrays.asList(
                this.createInfoSection(),
                this.createIndexSection(),
                this.createSearchSection(),
                this.createIndexTabSection(),
                this.createSearchTabSection(),
                this.createBackToPartyButton()
        );
    }

    //this is where creature info is made
    private RiftLibUISection createInfoSection() {
        return new RiftNewJournalInfoSection("journalInfo", this.width, this.height, 60, 7, this.fontRenderer, this.mc);
    }

    //get the info section
    private RiftNewJournalInfoSection getInfoSection() {
        RiftLibUISection sectionToTest = this.getSectionByID("journalInfo");
        if (sectionToTest instanceof RiftNewJournalInfoSection) return (RiftNewJournalInfoSection) this.getSectionByID("journalInfo");
        return null;
    }

    //this is where creatures are indexed
    private RiftLibUISection createIndexSection() {
        return new RiftJournalIndexSection("journalIndex", this.width, this.height, -147, 7, this.fontRenderer, this.mc);
    }

    //get the index section
    private RiftJournalIndexSection getIndexSection() {
        RiftLibUISection sectionToTest = this.getSectionByID("journalIndex");
        if (sectionToTest instanceof RiftJournalIndexSection) return (RiftJournalIndexSection) this.getSectionByID("journalIndex");
        return null;
    }

    //make the clickable section for the index tab
    private RiftLibUISection createIndexTabSection() {
        //since this is gonna be what the player opens the journal to, create the section first
        RiftLibUISection toReturn = new RiftLibUISection("journalIndexTab", this.width, this.height, 101, 12, -147, -112, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement indexClickableSection = new RiftLibUIElement.ClickableSectionElement();
                indexClickableSection.setID("toJournalIndex");
                indexClickableSection.setSize(101, 12);
                indexClickableSection.setTextContent(I18n.format("journal.index_button"));
                indexClickableSection.setTextColor(0xffffff);
                indexClickableSection.setTextHoveredColor(0xffff00);
                indexClickableSection.setTextSelectedColor(0xffff00);
                indexClickableSection.setTextOffsets(0, 2);
                toReturn.add(indexClickableSection);

                return toReturn;
            }
        };
        toReturn.setClickableSectionSelected("toJournalIndex", true);

        return toReturn;
    }

    //make the clickable section for the search tab
    private RiftLibUISection createSearchTabSection() {
        return new RiftLibUISection("journalSearchTab", this.width, this.height, 101, 12, -32, -112, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement indexClickableSection = new RiftLibUIElement.ClickableSectionElement();
                indexClickableSection.setID("toJournalSearch");
                indexClickableSection.setSize(101, 12);
                indexClickableSection.setTextContent(I18n.format("journal.search_button"));
                indexClickableSection.setTextColor(0xffffff);
                indexClickableSection.setTextHoveredColor(0xffff00);
                indexClickableSection.setTextSelectedColor(0xffff00);
                indexClickableSection.setTextOffsets(0, 2);
                toReturn.add(indexClickableSection);

                return toReturn;
            }
        };
    }

    //make the search section
    private RiftLibUISection createSearchSection() {
        RiftLibUISection toReturn = new RiftLibUISection("journalSearch", this.width, this.height, 268, 194, 60, 7, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.TextBoxElement textBoxElement = new RiftLibUIElement.TextBoxElement();
                textBoxElement.setID("searchBox");
                textBoxElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                textBoxElement.setWidth(180);
                toReturn.add(textBoxElement);

                return toReturn;
            }
        };

        this.setUISectionVisibility("journalSearch", false);

        return toReturn;
    }

    //make the lickable section for returning to the party
    private RiftLibUISection createBackToPartyButton() {
        return new RiftLibUISection("backToPartySection", this.width, this.height, 13, 13, 192, -112, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement clickableSection = new RiftLibUIElement.ClickableSectionElement();
                clickableSection.setID("backToParty");
                clickableSection.setImage(
                        background,
                        560,
                        240,
                        13,
                        13,
                        420,
                        136,
                        420,
                        149
                );
                clickableSection.setSize(12, 12);
                toReturn.add(clickableSection);

                return toReturn;
            }
        };
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        //set search term in the index section
        if (this.searchMode && this.getIndexSection() != null) {
            String searchResult = this.getTextFieldTextByID("searchBox");
            this.getIndexSection().setStringForSearch(searchResult);
        }
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection section, RiftLibUIElement.Element element) {
        return element;
    }

    @Override
    public RiftLibUISection modifyUISection(RiftLibUISection section) {
        return section;
    }

    @Override
    public ResourceLocation drawBackground() {
        return this.background;
    }

    @Override
    public int[] backgroundTextureSize() {
        return new int[]{560, 240};
    }

    @Override
    public int[] backgroundUV() {
        return new int[]{0, 0};
    }

    @Override
    public int[] backgroundSize() {
        return new int[]{420, 240};
    }

    @Override
    public void onButtonClicked(RiftLibButton riftLibButton) {
        if (this.buttonInSection("journalIndex", riftLibButton) && this.getInfoSection() != null && this.getIndexSection() != null) {
            if (this.searchMode) {
                //clear search results and go back to default search screen
                if (riftLibButton.buttonId.equals("NULL")) {
                    this.setAllButtonsUsability("journalIndex", true);
                    this.getIndexSection().resetScrollProgress();
                    this.getInfoSection().setEntryType(null);
                    this.getInfoSection().resetScrollProgress();
                    this.setUISectionVisibility("journalInfo", false);
                    this.setUISectionVisibility("journalSearch", true);
                }
                //choose a creature whose entry shall be read
                else {
                    RiftCreatureType typeToChoose = RiftCreatureType.safeValOf(riftLibButton.buttonId);
                    if (typeToChoose != null) {
                        this.getInfoSection().setEntryType(typeToChoose);
                        this.setAllButtonsUsability("journalIndex", true);
                        this.setButtonUsabilityByID(typeToChoose.toString(), false);
                    }
                    this.getInfoSection().resetScrollProgress();
                    this.getInfoSection().getOpenedTabs().clear();
                    this.setUISectionVisibility("journalInfo", true);
                    this.setUISectionVisibility("journalSearch", false);
                }
            }
            else {
                //set a category for when at start of index
                if (this.getIndexSection().getCurrentCategory() == null) {
                    RiftCreatureType.CreatureCategory chosenCategory = RiftCreatureType.CreatureCategory.safeValOf(riftLibButton.buttonId);
                    this.getIndexSection().setCurrentCategory(chosenCategory);
                }
                //set a creature for when indexing thru a category
                else {
                    //go back to index
                    if (riftLibButton.buttonId.equals("NULL")) {
                        this.setAllButtonsUsability("journalIndex", true);
                        this.getIndexSection().setCurrentCategory(null);
                        this.getIndexSection().resetScrollProgress();
                        this.getInfoSection().setEntryType(null);
                        this.getInfoSection().resetScrollProgress();
                    }
                    //choose a creature whose entry shall be read
                    else {
                        RiftCreatureType typeToChoose = RiftCreatureType.safeValOf(riftLibButton.buttonId);
                        this.setAllButtonsUsability("journalIndex", true);
                        if (typeToChoose != null) {
                            this.getInfoSection().setEntryType(typeToChoose);
                            this.setButtonUsabilityByID(typeToChoose.toString(), false);
                        }
                        this.getInfoSection().resetScrollProgress();
                        this.getInfoSection().getOpenedTabs().clear();
                    }
                }
            }
        }
    }

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {
        //switch to search mode
        if (this.clickableSectionInSection("journalSearchTab", riftLibClickableSection) && !this.searchMode) {
            this.searchMode = true;
            this.setSelectClickableSectionByID("toJournalIndex", false);
            this.setAllButtonsUsability("journalIndex", true);
            this.setUISectionVisibility("journalInfo", false);
            this.setUISectionVisibility("journalSearch", true);
            this.setSelectClickableSectionByID("toJournalSearch", true);
            if (this.getIndexSection() != null) {
                this.getIndexSection().setCurrentCategory(null);
                this.getIndexSection().setSearchMode(true);
            }
        }
        //switch to index mode
        if (this.clickableSectionInSection("journalIndexTab", riftLibClickableSection) && this.searchMode) {
            this.searchMode = false;
            this.setSelectClickableSectionByID("toJournalIndex", true);
            this.setAllButtonsUsability("journalIndex", true);
            this.setUISectionVisibility("journalInfo", true);
            this.setUISectionVisibility("journalSearch", false);
            this.setSelectClickableSectionByID("toJournalSearch", false);
            this.setTextFieldTextByID("searchBox", "");
            if (this.getIndexSection() != null) {
                this.getIndexSection().setSearchMode(false);
                this.getIndexSection().setStringForSearch("");
            }
            if (this.getInfoSection() != null) this.getInfoSection().setEntryType(null);
        }
        //go back to party screen
        if (riftLibClickableSection.getStringID().equals("backToParty")) RiftLibUIHelper.showUI(this.mc.player, new RiftNewPartyScreen());
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.getPopupSection() != null) super.keyTyped(typedChar, keyCode);
        else {
            if (keyCode == 1) RiftLibUIHelper.showUI(this.mc.player, new RiftNewPartyScreen());
            else super.keyTyped(typedChar, keyCode);
        }
    }
}
