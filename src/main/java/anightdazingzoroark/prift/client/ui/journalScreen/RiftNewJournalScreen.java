package anightdazingzoroark.prift.client.ui.journalScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.journalScreen.elements.RiftJournalIndexSection;
import anightdazingzoroark.prift.client.ui.journalScreen.elements.RiftNewJournalInfoSection;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;

public class RiftNewJournalScreen extends RiftLibUI {
    private boolean searchMode;

    public RiftNewJournalScreen() {
        super(0, 0, 0);
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Arrays.asList(this.createInfoSection(), this.createIndexSection());
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
        return new RiftJournalIndexSection("journalIndex", this.width, this.height, -149, 7, this.fontRenderer, this.mc);
    }

    //get the index section
    private RiftJournalIndexSection getIndexSection() {
        RiftLibUISection sectionToTest = this.getSectionByID("journalIndex");
        if (sectionToTest instanceof RiftJournalIndexSection) return (RiftJournalIndexSection) this.getSectionByID("journalIndex");
        return null;
    }

    //private RiftLibUISection searchSection() {}

    @Override
    public ResourceLocation drawBackground() {
        return new ResourceLocation(RiftInitialize.MODID, "textures/ui/journal_background.png");
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
        //for changing entry
        if (this.buttonInSection("journalIndex", riftLibButton) && this.getInfoSection() != null && this.getIndexSection() != null) {
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

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {

    }
}
