package anightdazingzoroark.prift.client.ui.partyScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.journalScreen.RiftNewJournalScreen;
import anightdazingzoroark.prift.client.ui.partyScreen.elements.RiftPartyMemButtonForParty;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftTeleportPartyMemToPlayer;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftNewPartyScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");
    private final List<RiftPartyMemButtonForParty> partyMemButtons = new ArrayList<>();

    private RiftCreature creatureToDraw;
    private boolean moveManagement;
    private int partyMemPos = -1;

    public RiftNewPartyScreen() {
        super(0, 0, 0);
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Arrays.asList(
                this.partyLabelSection(),
                this.shuffleCreaturesSection(),
                this.selectedCreatureSection(),
                this.openJournalSection(),
                this.partyMemberManagementSection()
        );
    }

    private RiftLibUISection partyLabelSection() {
        return new RiftLibUISection("partyLabelSection", this.width, this.height, 115, 9, 0, -65, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.TextElement label = new RiftLibUIElement.TextElement();
                label.setText(I18n.format("journal.party_label.party"));
                label.setID("partyLabel");
                toReturn.add(label);

                return toReturn;
            }
        };
    }

    private RiftLibUISection shuffleCreaturesSection() {
        return new RiftLibUISection("shuffleCreaturesSection", this.width, this.height, 20, 18, 50, -65, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement clickableSection = new RiftLibUIElement.ClickableSectionElement();
                clickableSection.setID("shuffleCreatures");
                clickableSection.setImage(background, 400, 360, 20, 18, 75, 203, 95, 203);
                clickableSection.setSize(19, 17);
                clickableSection.setImageSelectedUV(115, 203);
                clickableSection.setImageScale(0.75f);
                clickableSection.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                toReturn.add(clickableSection);

                return toReturn;
            }
        };
    }

    private RiftLibUISection selectedCreatureSection() {
        return new RiftLibUISection("selectedCreatureSection", RiftNewPartyScreen.this.width, RiftNewPartyScreen.this.height, 99, 60, 0, -31, RiftNewPartyScreen.this.fontRenderer, RiftNewPartyScreen.this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.RenderedEntityElement entityToRender = new RiftLibUIElement.RenderedEntityElement();
                entityToRender.setID("selectedCreature");
                entityToRender.setNotLimitedByBounds();
                entityToRender.setScale(20f);
                entityToRender.setAdditionalSize(0, 40);
                entityToRender.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                entityToRender.setRotationAngle(150);
                toReturn.add(entityToRender);

                return toReturn;
            }
        };
    }

    private RiftLibUISection openJournalSection() {
        return new RiftLibUISection("openJournalSection", this.width, this.height, 54, 11, -1, 70, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement journalSection = new RiftLibUIElement.ClickableSectionElement();
                journalSection.setID("openJournal");
                journalSection.setTextContent(I18n.format("journal.party_button.journal"));
                journalSection.setTextScale(0.75f);
                journalSection.setSize(54, 11);
                toReturn.add(journalSection);

                return toReturn;
            }
        };
    }

    private RiftLibUISection partyMemberManagementSection() {
        return new RiftLibUISection("partyMemManagementSection", this.width, this.height, 80, 70, 0, 45, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //summon or dismiss
                RiftLibUIElement.ButtonElement summonDismissButton = new RiftLibUIElement.ButtonElement();
                summonDismissButton.setSize(80, 20);
                summonDismissButton.setBottomSpace(5);
                summonDismissButton.setID("summonDismiss");
                summonDismissButton.setText(I18n.format("journal.party_button.summon"));
                toReturn.add(summonDismissButton);

                //teleport
                RiftLibUIElement.ButtonElement teleportButton = new RiftLibUIElement.ButtonElement();
                teleportButton.setSize(80, 20);
                teleportButton.setBottomSpace(5);
                teleportButton.setID("teleport");
                teleportButton.setText(I18n.format("journal.party_button.teleport"));
                toReturn.add(teleportButton);

                //change name
                RiftLibUIElement.ButtonElement changeNameButton = new RiftLibUIElement.ButtonElement();
                changeNameButton.setSize(80, 20);
                changeNameButton.setBottomSpace(5);
                changeNameButton.setID("changeName");
                changeNameButton.setText(I18n.format("journal.party_button.change_name"));
                toReturn.add(changeNameButton);

                return toReturn;
            }
        };
    }

    @Override
    public void initGui() {
        super.initGui();

        //update creatures upon opening
        //NewPlayerTamedCreaturesHelper.updateAfterOpenPartyScreen(this.mc.player, (int) this.mc.world.getTotalWorldTime());
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

        //create party buttons
        this.partyMemButtons.clear();
        FixedSizeList<NBTTagCompound> playerPartyNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player);
        for (int x = 0; x < playerPartyNBT.size(); x++) {
            int partyMemXOffset = -154 + (x % 2) * 60;
            int partyMemYOffset = -41 + (x / 2) * 40;
            RiftPartyMemButtonForParty partyMemButton = new RiftPartyMemButtonForParty(playerPartyNBT.get(x), this.width, this.height, partyMemXOffset, partyMemYOffset, this.fontRenderer, this.mc);
            this.partyMemButtons.add(partyMemButton);
        }

        //set creature to draw
        if (this.partyMemPos >= 0) this.creatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, playerPartyNBT.get(this.partyMemPos));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        //constantly update party members as long as this screen is opened
        this.updateAllPartyMems();

        //draw the party buttons
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButtonForParty partyMemButton = this.partyMemButtons.get(x);
            int selectedXOffset = this.hasSelectedCreature() ? 0 : 124;
            int selectedYOffset = this.hasSelectedCreature() ? -13 : 3;
            partyMemButton.setAdditionalOffset(selectedXOffset, selectedYOffset);
            partyMemButton.drawSection(mouseX, mouseY);
            partyMemButton.setSelected(x == this.partyMemPos);
        }
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection section, RiftLibUIElement.Element element) {
        if (section.id.equals("selectedCreatureSection") && element.getID().equals("selectedCreature")) {
            RiftLibUIElement.RenderedEntityElement renderedEntityElement = (RiftLibUIElement.RenderedEntityElement) element;
            if (this.creatureToDraw != null) renderedEntityElement.setEntity(this.creatureToDraw);
        }
        if (section.id.equals("partyMemManagementSection")) {
            //change summon/dismiss button based on if creature is deployed or not
            if (element.getID().equals("summonDismiss")) {
                RiftLibUIElement.ButtonElement summonDismissButton = (RiftLibUIElement.ButtonElement) element;

                //change text
                String displayString = this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY ? I18n.format("journal.party_button.dismiss") : I18n.format("journal.party_button.summon");;
                summonDismissButton.setText(displayString);

                //set usability based on if is deployable in area
                boolean deployable = this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY || (NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos) && (!this.getMemberNBT().hasKey("Health") || this.getMemberNBT().getFloat("Health") > 0));
                this.setButtonUsabilityByID("summonDismiss", deployable);
            }
            else if (element.getID().equals("teleport")) {
                //set usability
                boolean teleportable = this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY && NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos);
                this.setButtonUsabilityByID("teleport", teleportable);
            }
        }
        return element;
    }

    @Override
    public RiftLibUISection modifyUISection(RiftLibUISection section) {
        //some sections are visible only based on whether or not there's a selected creature
        this.setUISectionVisibility("selectedCreatureSection", this.hasSelectedCreature());
        this.setUISectionVisibility("partyMemManagementSection", this.hasSelectedCreature());

        switch (section.id) {
            case "partyLabelSection": {
                int xOffset = this.hasSelectedCreature() ? -124 : 0;
                int YOffset = this.hasSelectedCreature() ? -81 : -65;
                section.repositionSection(xOffset, YOffset);
                break;
            }
            case "shuffleCreaturesSection": {
                int xOffset = this.hasSelectedCreature() ? -74 : 50;
                int YOffset = this.hasSelectedCreature() ? -81 : -65;
                section.repositionSection(xOffset, YOffset);
                break;
            }
            case "openJournalSection": {
                int xOffset = this.hasSelectedCreature() ? -123 : -1;
                int yOffset = this.hasSelectedCreature() ? 54 : 70;
                section.repositionSection(xOffset, yOffset);
                break;
            }
        }
        return section;
    }

    private boolean hasSelectedCreature() {
        return this.partyMemPos >= 0 && this.creatureToDraw != null;
    }

    private void updateAllPartyMems() {
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player); //update nbt from deployed creatures
        //update creatures while ui is opened
        FixedSizeList<NBTTagCompound> newPartyNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player);
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButtonForParty button = this.partyMemButtons.get(x);
            button.setCreatureNBT(newPartyNBT.get(x));
        }
    }

    private NBTTagCompound getMemberNBT() {
        if (this.partyMemPos >= 0) return this.partyMemButtons.get(this.partyMemPos).getCreatureNBT();
        return new NBTTagCompound();
    }

    private PlayerTamedCreatures.DeploymentType getPartyMemDeployment() {
        if (this.getMemberNBT().hasKey("DeploymentType")) return PlayerTamedCreatures.DeploymentType.values()[this.getMemberNBT().getByte("DeploymentType")];
        return PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE;
    }

    @Override
    public ResourceLocation drawBackground() {
        return this.background;
    }

    @Override
    public int[] backgroundTextureSize() {
        return new int[]{400, 360};
    }

    @Override
    public int[] backgroundUV() {
        int uvX = this.hasSelectedCreature() ? 0 : 162;
        int uvY = this.hasSelectedCreature() ? 0 : 182;
        return new int[]{uvX, uvY};
    }

    @Override
    public int[] backgroundSize() {
        int xScreenSize = this.hasSelectedCreature() ? 373 : 125;
        int yScreenSize = this.hasSelectedCreature() ? 182 : 151;
        return new int[]{xScreenSize, yScreenSize};
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //party related stuff stays here cos yes
        for (int i = 0; i < this.partyMemButtons.size(); i++) {
            RiftPartyMemButtonForParty partyMemButton = this.partyMemButtons.get(i);
            if (partyMemButton.isHovered(mouseX, mouseY)) {
                this.partyMemPos = i;
                this.creatureToDraw = partyMemButton.getCreatureFromNBT();
                this.playPressSound();
            }
        }
    }

    @Override
    public void onButtonClicked(RiftLibButton riftLibButton) {
        if (riftLibButton.buttonId.equals("summonDismiss")) {
            if (this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY) {
                if (NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos)) {
                    NewPlayerTamedCreaturesHelper.deployCreatureFromParty(this.mc.player, this.partyMemPos, false);
                }
            }
            else if (this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                NewPlayerTamedCreaturesHelper.deployCreatureFromParty(this.mc.player, this.partyMemPos, true);
            }
        }
        else if (riftLibButton.buttonId.equals("teleport")) {
            if (NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos)) {
                RiftMessages.WRAPPER.sendToServer(new RiftTeleportPartyMemToPlayer(this.mc.player, this.partyMemPos));
            }
        }
    }

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {
        if (riftLibClickableSection.getStringID().equals("openJournal")) {
            RiftLibUIHelper.showUI(this.mc.player, new RiftNewJournalScreen());
        }
    }
}
