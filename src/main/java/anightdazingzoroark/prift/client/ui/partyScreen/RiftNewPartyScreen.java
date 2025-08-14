package anightdazingzoroark.prift.client.ui.partyScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.journalScreen.RiftNewJournalScreen;
import anightdazingzoroark.prift.client.ui.partyScreen.elements.RiftNewPartyMembersSection;
import anightdazingzoroark.prift.client.ui.partyScreen.elements.RiftPartyMemButtonForParty;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftChangePartyMemName;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftNewPartyScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");

    private RiftCreature creatureToDraw;
    private boolean moveManagement;
    private boolean shufflePartyMemsMode;
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
                this.partyMemberManagementSection(),
                this.partyMembersSection()
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
        return new RiftLibUISection("partyMemManagementSection", this.width, this.height, 100, 90, 0, 52, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                if (getPartyMembersSection() != null) {
                    //for getting party member name
                    NBTTagCompound tagCompound = getPartyMembersSection().getPartyMembersNBT().get(partyMemPos);
                    RiftCreatureType creatureType = RiftCreatureType.values()[tagCompound.getByte("CreatureType")];
                    String partyMemName = (tagCompound.hasKey("CustomName") && !tagCompound.getString("CustomName").isEmpty()) ? tagCompound.getString("CustomName") : creatureType.getTranslatedName();
                    String partyMemNameString = I18n.format("journal.party_member.name", partyMemName, tagCompound.getInteger("Level"));

                    //party member name
                    RiftLibUIElement.TextElement creatureName = new RiftLibUIElement.TextElement();
                    creatureName.setSingleLine();
                    creatureName.setText(partyMemNameString);
                    creatureName.setScale(0.75f);
                    creatureName.setBottomSpace(0);
                    creatureName.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    creatureName.setNotLimitedByBounds();
                    toReturn.add(creatureName);

                    //summon or dismiss
                    RiftLibUIElement.ButtonElement summonDismissButton = new RiftLibUIElement.ButtonElement();
                    summonDismissButton.setSize(80, 20);
                    summonDismissButton.setBottomSpace(5);
                    summonDismissButton.setID("summonDismiss");
                    summonDismissButton.setText(I18n.format("journal.party_button.summon"));
                    summonDismissButton.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    toReturn.add(summonDismissButton);

                    //teleport
                    RiftLibUIElement.ButtonElement teleportButton = new RiftLibUIElement.ButtonElement();
                    teleportButton.setSize(80, 20);
                    teleportButton.setBottomSpace(5);
                    teleportButton.setID("teleport");
                    teleportButton.setText(I18n.format("journal.party_button.teleport"));
                    teleportButton.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    toReturn.add(teleportButton);

                    //change name
                    RiftLibUIElement.ButtonElement changeNameButton = new RiftLibUIElement.ButtonElement();
                    changeNameButton.setSize(80, 20);
                    changeNameButton.setBottomSpace(5);
                    changeNameButton.setID("openChangeNamePopup");
                    changeNameButton.setText(I18n.format("journal.party_button.change_name"));
                    changeNameButton.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    toReturn.add(changeNameButton);
                }

                return toReturn;
            }
        };
    }

    //create the party members section
    private RiftLibUISection partyMembersSection() {
        return new RiftNewPartyMembersSection(this.width, this.height, 120, 120, -1, 3, this.fontRenderer, this.mc);
    }

    //get the party members section once its created
    private RiftNewPartyMembersSection getPartyMembersSection() {
        return (RiftNewPartyMembersSection) this.getSectionByID("partyMembersSection");
    }

    @Override
    public void initGui() {
        super.initGui();

        //update creatures upon opening
        //NewPlayerTamedCreaturesHelper.updateAfterOpenPartyScreen(this.mc.player, (int) this.mc.world.getTotalWorldTime());
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

        //set creature to draw
        if (this.partyMemPos >= 0) this.creatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player).get(this.partyMemPos));
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection section, RiftLibUIElement.Element element) {
        //for updating all party members
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

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

                //add overlay text if its not deployable
                if (!deployable) {
                    String cannotSummonLocation = I18n.format("journal.warning.cannot_summon");
                    String cannotSummonDead = I18n.format("journal.warning.cannot_summon_dead");
                    String finalOverlayString = (this.getMemberNBT().hasKey("Health") && this.getMemberNBT().getFloat("Health") <= 0)
                            ? cannotSummonDead
                            : cannotSummonLocation;
                    summonDismissButton.setOverlayText(finalOverlayString);
                }
            }
            else if (element.getID().equals("teleport")) {
                RiftLibUIElement.ButtonElement teleportButton = (RiftLibUIElement.ButtonElement) element;

                //set usability
                boolean teleportable = this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY && NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos);
                this.setButtonUsabilityByID("teleport", teleportable);

                //add overlay text if its not teleportable
                if (!teleportable) teleportButton.setOverlayText(I18n.format("journal.warning.cannot_teleport"));
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
            case "partyMembersSection": {
                //update party members
                NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);
                RiftNewPartyMembersSection partyMembersSection = (RiftNewPartyMembersSection) section;
                partyMembersSection.setPartyMembersNBT(NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player));

                //change position based on if theres a selected creature or not
                int xOffset = this.hasSelectedCreature() ? -125 : -1;
                int yOffset = this.hasSelectedCreature() ? -13 : 3;
                partyMembersSection.repositionSection(xOffset, yOffset);

                break;
            }
        }
        return section;
    }

    private boolean hasSelectedCreature() {
        return this.partyMemPos >= 0 && this.creatureToDraw != null;
    }

    private NBTTagCompound getMemberNBT() {
        if (this.partyMemPos >= 0) {
            //RiftLibUIElement.TableContainerElement partyMemberTable = this.byi
            if (this.getPartyMembersSection() != null) return this.getPartyMembersSection().getPartyMembersNBT().get(this.partyMemPos);
        }
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
        else if (riftLibButton.buttonId.equals("openChangeNamePopup")) this.createPopup(this.changeNamePopup());
        //for setting the name of a tamed creature
        else if (riftLibButton.buttonId.equals("setNewName")) {
            RiftMessages.WRAPPER.sendToServer(new RiftChangePartyMemName(this.mc.player, this.partyMemPos, this.getTextFieldTextByID("newName")));
            this.clearPopup();
        }
        //universal, for exiting popup
        else if (riftLibButton.buttonId.equals("exitPopup")) this.clearPopup();
    }

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {
        if (riftLibClickableSection.getStringID().equals("openJournal")) {
            RiftLibUIHelper.showUI(this.mc.player, new RiftNewJournalScreen());
        }
        if (riftLibClickableSection.getStringID().startsWith("partyMember:")) {
            RiftPartyMemButtonForParty partyMemButtonForParty = (RiftPartyMemButtonForParty) riftLibClickableSection;
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    riftLibClickableSection.getStringID().indexOf(":") + 1
            ));

            //for swapping party members, stuff here works as expected
            if (this.shufflePartyMemsMode) {}
            //when not swapping party members, just select a reature
            else {
                if (clickedPosition != this.partyMemPos) {
                    this.setSelectClickableSectionByID("partyMember:"+this.partyMemPos, false);
                    this.partyMemPos = clickedPosition;
                    if (partyMemButtonForParty.getCreatureNBT() != null && !partyMemButtonForParty.getCreatureNBT().isEmpty()) this.setSelectClickableSectionByID("partyMember:"+this.partyMemPos, true);
                    this.creatureToDraw = partyMemButtonForParty.getCreatureFromNBT();
                }
                else {
                    this.setSelectClickableSectionByID("partyMember:"+this.partyMemPos, false);
                    this.partyMemPos = -1;
                    this.creatureToDraw = null;
                }
            }
        }
        if (riftLibClickableSection.getStringID().equals("shuffleCreatures")) {
            this.shufflePartyMemsMode = !this.shufflePartyMemsMode;
            this.setSelectClickableSectionByID("shuffleCreatures", this.shufflePartyMemsMode);
        }
    }

    private List<RiftLibUIElement.Element> changeNamePopup() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        RiftLibUIElement.TextBoxElement textBox = new RiftLibUIElement.TextBoxElement();
        textBox.setID("newName");
        textBox.setWidth(100);
        textBox.setAlignment(RiftLibUIElement.ALIGN_CENTER);
        if (this.getMemberNBT().hasKey("CustomName") && !this.getMemberNBT().getString("CustomName").isEmpty()) {
            textBox.setDefaultText(this.getMemberNBT().getString("CustomName"));
        }
        toReturn.add(textBox);

        //table for buttons
        RiftLibUIElement.TableContainerElement buttonContainer = new RiftLibUIElement.TableContainerElement();
        buttonContainer.setCellSize(70, 20);
        buttonContainer.setRowCount(2);
        buttonContainer.setAlignment(RiftLibUIElement.ALIGN_CENTER);

        //confirm button
        RiftLibUIElement.ButtonElement confirmButton = new RiftLibUIElement.ButtonElement();
        confirmButton.setSize(60, 20);
        confirmButton.setText(I18n.format("radial.popup_button.confirm"));
        confirmButton.setID("setNewName");
        buttonContainer.addElement(confirmButton);

        //cancel button
        RiftLibUIElement.ButtonElement cancelButton = new RiftLibUIElement.ButtonElement();
        cancelButton.setSize(60, 20);
        cancelButton.setText(I18n.format("radial.popup_button.cancel"));
        cancelButton.setID("exitPopup");
        buttonContainer.addElement(cancelButton);

        toReturn.add(buttonContainer);

        return toReturn;
    }
}
