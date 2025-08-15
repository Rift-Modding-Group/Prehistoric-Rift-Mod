package anightdazingzoroark.prift.client.ui.partyScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUser;
import anightdazingzoroark.prift.client.ui.journalScreen.RiftNewJournalScreen;
import anightdazingzoroark.prift.client.ui.partyScreen.elements.RiftNewPartyMembersSection;
import anightdazingzoroark.prift.client.ui.partyScreen.elements.RiftPartyMemButtonForParty;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RiftNewPartyScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");

    private RiftCreature creatureToDraw;
    private boolean moveManagement;
    private boolean shufflePartyMemsMode;
    private int partyMemPos = -1;
    private int selectedMovePos = -1;

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
                this.partyMembersSection(),
                this.partyMemberInfoSection(),
                this.informationSection(),
                this.movesSection(),
                this.partyMemberMovesSection(),
                this.moveDescriptionBackgroundSection(),
                this.moveDescriptionSection()
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
                    CreatureNBT tagCompound = getPartyMembersSection().getPartyMembersNBT().get(partyMemPos);

                    //party member name
                    RiftLibUIElement.TextElement creatureName = new RiftLibUIElement.TextElement();
                    creatureName.setSingleLine();
                    creatureName.setText(tagCompound.getCreatureName(true));
                    creatureName.setScale(0.75f);
                    creatureName.setBottomSpace(3);
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

    //create the party members info section
    private RiftLibUISection partyMemberInfoSection() {
        return new RiftUISectionCreatureNBTUser("partyMemberInfoSection", this.getMemberNBT(), this.width, this.height, 115, 119, 124, -16, this.fontRenderer, this.mc ) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                if (!this.nbtTagCompound.nbtIsEmpty()) {
                    //species name
                    RiftLibUIElement.TextElement speciesName = new RiftLibUIElement.TextElement();
                    speciesName.setSingleLine();
                    speciesName.setText(I18n.format("tametrait.species", this.nbtTagCompound.getCreatureType().getTranslatedName()));
                    speciesName.setScale(0.5f);
                    speciesName.setBottomSpace(6);
                    toReturn.add(speciesName);

                    //health
                    float health = this.nbtTagCompound.getCreatureHealth()[0];
                    float maxHealth = this.nbtTagCompound.getCreatureHealth()[1];

                    //health header
                    RiftLibUIElement.TextElement healthHeader = new RiftLibUIElement.TextElement();
                    healthHeader.setSingleLine();
                    healthHeader.setText(I18n.format("tametrait.health", Math.round(health), maxHealth));
                    healthHeader.setScale(0.5f);
                    healthHeader.setBottomSpace(3);
                    toReturn.add(healthHeader);

                    //health bar
                    RiftLibUIElement.ProgressBarElement healthBar = new RiftLibUIElement.ProgressBarElement();
                    healthBar.setPercentage(MathHelper.clamp(health / maxHealth, 0, 1));
                    healthBar.setColors(0xff0000, 0x868686);
                    healthBar.setWidth(100);
                    healthBar.setBottomSpace(6);
                    toReturn.add(healthBar);

                    //energy
                    int energy = this.nbtTagCompound.getCreatureEnergy()[0];
                    int maxEnergy = this.nbtTagCompound.getCreatureEnergy()[1];

                    //energy header
                    RiftLibUIElement.TextElement energyHeader = new RiftLibUIElement.TextElement();
                    energyHeader.setSingleLine();
                    energyHeader.setText(I18n.format("tametrait.energy", energy, maxEnergy));
                    energyHeader.setScale(0.5f);
                    energyHeader.setBottomSpace(3);
                    toReturn.add(energyHeader);

                    //energy bar
                    RiftLibUIElement.ProgressBarElement energyBar = new RiftLibUIElement.ProgressBarElement();
                    energyBar.setPercentage(MathHelper.clamp(energy / maxEnergy, 0, 1));
                    energyBar.setColors(0xffff00, 0x868686);
                    energyBar.setWidth(100);
                    energyBar.setBottomSpace(6);
                    toReturn.add(energyBar);

                    //experience
                    int xp = this.nbtTagCompound.getCreatureXP()[0];
                    int maxXP = this.nbtTagCompound.getCreatureXP()[1];

                    //experience header
                    RiftLibUIElement.TextElement experienceHeader = new RiftLibUIElement.TextElement();
                    experienceHeader.setSingleLine();
                    experienceHeader.setText(I18n.format("tametrait.xp", xp, maxXP));
                    experienceHeader.setScale(0.5f);
                    experienceHeader.setBottomSpace(3);
                    toReturn.add(experienceHeader);

                    //experience bar
                    RiftLibUIElement.ProgressBarElement xpBar = new RiftLibUIElement.ProgressBarElement();
                    xpBar.setPercentage(MathHelper.clamp(xp / maxXP, 0, 1));
                    xpBar.setColors(0x98d06b, 0x868686);
                    xpBar.setWidth(100);
                    xpBar.setBottomSpace(6);
                    toReturn.add(xpBar);

                    //age
                    RiftLibUIElement.TextElement ageText = new RiftLibUIElement.TextElement();
                    ageText.setSingleLine();
                    ageText.setText(I18n.format("tametrait.age", this.nbtTagCompound.getAgeInDays()));
                    ageText.setScale(0.5f);
                    ageText.setBottomSpace(6);
                    toReturn.add(ageText);

                    //acquisition info
                    RiftLibUIElement.TextElement acquisitionText = new RiftLibUIElement.TextElement();
                    acquisitionText.setText(this.nbtTagCompound.getAcquisitionInfoString());
                    acquisitionText.setScale(0.5f);
                    acquisitionText.setBottomSpace(6);
                    toReturn.add(acquisitionText);
                }

                return toReturn;
            }
        };
    }

    //get party member info section once its created
    private RiftUISectionCreatureNBTUser getPartyMemberInfoSection() {
        return (RiftUISectionCreatureNBTUser) this.getSectionByID("partyMemberInfoSection");
    }

    //create the party member moves section
    private RiftUISectionCreatureNBTUser partyMemberMovesSection() {
        return new RiftUISectionCreatureNBTUser("partyMemberMovesSection", this.getMemberNBT(), this.width, this.height, 115, 65, 124, -46, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //shuffle moves button
                RiftLibUIElement.ClickableSectionElement shuffleMoves = new RiftLibUIElement.ClickableSectionElement();
                shuffleMoves.setID("shuffleMoves");
                shuffleMoves.setAlignment(RiftLibUIElement.ALIGN_RIGHT);
                shuffleMoves.setImage(background, 400, 360, 20, 18, 75, 203, 95, 203);
                shuffleMoves.setImageSelectedUV(115, 203);
                shuffleMoves.setSize(19, 17);
                shuffleMoves.setImageScale(0.75f);
                shuffleMoves.setBottomSpace(0);
                toReturn.add(shuffleMoves);

                //for moves
                for (int i = 0; i < this.nbtTagCompound.getMovesListNBT().tagCount(); i++) {
                    CreatureMove move = this.nbtTagCompound.getMovesList().get(i);
                    RiftLibUIElement.ClickableSectionElement moveClickableSection = new RiftLibUIElement.ClickableSectionElement();
                    moveClickableSection.setID("move:"+i);
                    moveClickableSection.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    moveClickableSection.setImage(background, 400, 360, 105, 13, 287, 237, 287, 250);
                    moveClickableSection.setImageSelectedUV(287, 263);
                    moveClickableSection.setTextContent(move.getTranslatedName());
                    moveClickableSection.setTextScale(0.75f);
                    moveClickableSection.setTextOffsets(0, 1);
                    moveClickableSection.setSize(105, 13);
                    moveClickableSection.setBottomSpace(3);
                    toReturn.add(moveClickableSection);
                }

                return toReturn;
            }
        };
    }

    //get party member moves section once its created
    private RiftUISectionCreatureNBTUser getPartyMemberMovesSection() {
        return (RiftUISectionCreatureNBTUser) this.getSectionByID("partyMemberMovesSection");
    }

    private RiftLibUISection informationSection() {
        return new RiftLibUISection("informationSection", this.width, this.height, 54, 11, 94, -85, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //clickable section
                RiftLibUIElement.ClickableSectionElement informationClickableSection = new RiftLibUIElement.ClickableSectionElement();
                informationClickableSection.setID("informationClickableSection");
                informationClickableSection.setTextContent(I18n.format("journal.party_member.info"));
                informationClickableSection.setTextScale(0.75f);
                informationClickableSection.setTextOffsets(0, 1);
                informationClickableSection.setSize(55, 11);
                toReturn.add(informationClickableSection);

                return toReturn;
            }
        };
    }

    private RiftLibUISection movesSection() {
        return new RiftLibUISection("movesSection", this.width, this.height, 54, 11, 152, -85, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //clickable section
                RiftLibUIElement.ClickableSectionElement movesClickableSection = new RiftLibUIElement.ClickableSectionElement();
                movesClickableSection.setID("movesClickableSection");
                movesClickableSection.setTextContent(I18n.format("journal.party_member.moves"));
                movesClickableSection.setTextScale(0.75f);
                movesClickableSection.setTextOffsets(0, 1);
                movesClickableSection.setSize(55, 11);
                toReturn.add(movesClickableSection);

                return toReturn;
            }
        };
    }

    private RiftLibUISection moveDescriptionBackgroundSection() {
        return new RiftLibUISection("moveDescriptionBGSection", this.width, this.height, 113, 55, 124, 15, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //the background image
                RiftLibUIElement.ImageElement backgroundElement = new RiftLibUIElement.ImageElement();
                backgroundElement.setImage(background, 400, 360, 113, 55, 287, 182);
                toReturn.add(backgroundElement);

                return toReturn;
            }
        };
    }

    //create move description section
    private RiftUISectionCreatureNBTUser moveDescriptionSection() {
        return new RiftUISectionCreatureNBTUser("moveDescriptionSection", this.getMemberNBT(), this.width, this.height, 107, 49, 124, 15, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //move description of selected move
                if (selectedMovePos >= 0 && !this.nbtTagCompound.getMovesList().isEmpty()) {
                    RiftLibUIElement.TextElement moveDescription = new RiftLibUIElement.TextElement();
                    moveDescription.setText(this.nbtTagCompound.getMovesList().get(selectedMovePos).getTranslatedDescription());
                    moveDescription.setScale(0.75f);
                    moveDescription.setTextColor(0xFFFFFF);
                    toReturn.add(moveDescription);
                }

                return toReturn;
            }
        };
    }

    //get move description section after its created
    private RiftUISectionCreatureNBTUser getMoveDescription() {
        return (RiftUISectionCreatureNBTUser) this.getSectionByID("moveDescriptionSection");
    }

    @Override
    public void initGui() {
        super.initGui();

        //update creatures upon opening
        //NewPlayerTamedCreaturesHelper.updateAfterOpenPartyScreen(this.mc.player, (int) this.mc.world.getTotalWorldTime());
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

        //set creature to draw
        if (this.partyMemPos >= 0) this.creatureToDraw = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player).get(this.partyMemPos).getCreatureAsNBT(this.mc.world);
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection section, RiftLibUIElement.Element element) {
        //for updating all party members
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

        this.setSelectClickableSectionByID("informationClickableSection", !this.moveManagement);
        this.setSelectClickableSectionByID("movesClickableSection", this.moveManagement);

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
                boolean deployable = this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY || (NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos) && this.getMemberNBT().getCreatureHealth()[0] > 0);
                this.setButtonUsabilityByID("summonDismiss", deployable);

                //add overlay text if its not deployable
                if (!deployable) {
                    String cannotSummonLocation = I18n.format("journal.warning.cannot_summon");
                    String cannotSummonDead = I18n.format("journal.warning.cannot_summon_dead");
                    String finalOverlayString = (this.getMemberNBT().getCreatureHealth()[0] <= 0)
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
        //some sections are visible only based on whether or not there's a selected creature and if its not shuffle party mems mode
        this.setUISectionVisibility("selectedCreatureSection", this.hasSelectedCreature() && !this.shufflePartyMemsMode);
        this.setUISectionVisibility("partyMemManagementSection", this.hasSelectedCreature() && !this.shufflePartyMemsMode);
        this.setUISectionVisibility("informationSection", this.hasSelectedCreature() && !this.shufflePartyMemsMode);
        this.setUISectionVisibility("movesSection", this.hasSelectedCreature() && !this.shufflePartyMemsMode);

        this.setUISectionVisibility("partyMemberInfoSection", this.hasSelectedCreature() && !this.shufflePartyMemsMode && !this.moveManagement);
        this.setUISectionVisibility("partyMemberMovesSection", this.hasSelectedCreature() && !this.shufflePartyMemsMode && this.moveManagement);

        this.setUISectionVisibility("moveDescriptionBGSection", this.hasSelectedCreature() && !this.shufflePartyMemsMode && this.moveManagement && this.selectedMovePos >= 0);
        this.setUISectionVisibility("moveDescriptionSection", this.hasSelectedCreature() && !this.shufflePartyMemsMode && this.moveManagement && this.selectedMovePos >= 0);

        switch (section.id) {
            case "partyLabelSection": {
                int xOffset = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? -124 : 0;
                int YOffset = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? -81 : -65;
                section.repositionSection(xOffset, YOffset);
                break;
            }
            case "shuffleCreaturesSection": {
                int xOffset = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? -74 : 50;
                int YOffset = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? -81 : -65;
                section.repositionSection(xOffset, YOffset);
                break;
            }
            case "openJournalSection": {
                int xOffset = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? -123 : -1;
                int yOffset = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? 54 : 70;
                section.repositionSection(xOffset, yOffset);
                break;
            }
            case "partyMembersSection": {
                //update party members
                NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);
                RiftNewPartyMembersSection partyMembersSection = (RiftNewPartyMembersSection) section;
                partyMembersSection.setPartyMembersNBT(NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player));

                //change position based on if theres a selected creature or not
                int xOffset = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? -125 : -1;
                int yOffset = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? -13 : 3;
                partyMembersSection.repositionSection(xOffset, yOffset);

                break;
            }
            case "partyMemberInfoSection": {
                this.getPartyMemberInfoSection().setNBTTagCompound(this.getMemberNBT());
                break;
            }
            case "partyMemberMovesSection": {
                this.getPartyMemberMovesSection().setNBTTagCompound(this.getMemberNBT());
                break;
            }
            case "moveDescriptionSection": {
                this.getMoveDescription().setNBTTagCompound(this.getMemberNBT());
                break;
            }
        }
        return section;
    }

    private boolean hasSelectedCreature() {
        return this.partyMemPos >= 0 && this.creatureToDraw != null;
    }

    private CreatureNBT getMemberNBT() {
        if (this.partyMemPos >= 0) {
            if (this.getPartyMembersSection() != null) return this.getPartyMembersSection().getPartyMembersNBT().get(this.partyMemPos);
        }
        return new CreatureNBT();
    }

    private PlayerTamedCreatures.DeploymentType getPartyMemDeployment() {
        if (this.getMemberNBT() == null || this.getMemberNBT().nbtIsEmpty()) return PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE;
        return this.getMemberNBT().getDeploymentType();
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
        int uvX = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? 0 : 162;
        int uvY = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? 0 : 182;
        return new int[]{uvX, uvY};
    }

    @Override
    public int[] backgroundSize() {
        int xScreenSize = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? 373 : 125;
        int yScreenSize = (this.hasSelectedCreature() && !this.shufflePartyMemsMode) ? 182 : 151;
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
            if (this.shufflePartyMemsMode) {
                if (this.partyMemPos == -1 && partyMemButtonForParty.getCreatureNBT() != null && !partyMemButtonForParty.getCreatureNBT().nbtIsEmpty()) {
                    this.partyMemPos = clickedPosition;
                    this.setSelectClickableSectionByID("partyMember:"+this.partyMemPos, true);
                }
                else if (this.partyMemPos > -1) {
                    NewPlayerTamedCreaturesHelper.rearrangePartyCreatures(this.mc.player, this.partyMemPos, clickedPosition);
                    this.setSelectClickableSectionByID("partyMember:"+this.partyMemPos, false);
                    this.partyMemPos = -1;
                }
            }
            //when not swapping party members, just select a reature
            else {
                if (clickedPosition != this.partyMemPos) {
                    this.setSelectClickableSectionByID("partyMember:"+this.partyMemPos, false);
                    this.partyMemPos = clickedPosition;
                    if (partyMemButtonForParty.getCreatureNBT() != null && !partyMemButtonForParty.getCreatureNBT().nbtIsEmpty()) this.setSelectClickableSectionByID("partyMember:"+this.partyMemPos, true);
                    this.creatureToDraw = partyMemButtonForParty.getCreatureFromNBT();
                }
                else {
                    this.setSelectClickableSectionByID("partyMember:"+this.partyMemPos, false);
                    this.partyMemPos = -1;
                    this.creatureToDraw = null;
                }
                //reset selected move, if there is
                if (this.selectedMovePos >= 0) {
                    this.setSelectClickableSectionByID("move:"+this.selectedMovePos, false);
                    this.selectedMovePos = -1;
                }
                //reset to creature info mode when in move management mode
                if (this.moveManagement) this.moveManagement = false;
            }
        }
        if (riftLibClickableSection.getStringID().equals("shuffleCreatures")) {
            this.shufflePartyMemsMode = !this.shufflePartyMemsMode;
            this.setSelectClickableSectionByID("shuffleCreatures", this.shufflePartyMemsMode);
            //reset selected move, if there is
            if (this.selectedMovePos >= 0) {
                this.setSelectClickableSectionByID("move:"+this.selectedMovePos, false);
                this.selectedMovePos = -1;
            }
        }
        if (riftLibClickableSection.getStringID().equals("informationClickableSection") && this.moveManagement) {
            this.moveManagement = false;
        }
        if (riftLibClickableSection.getStringID().equals("movesClickableSection") && !this.moveManagement) {
            this.moveManagement = true;
        }
        if (riftLibClickableSection.getStringID().startsWith("move:")) {
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    riftLibClickableSection.getStringID().indexOf(":") + 1
            ));
            if (clickedPosition != this.selectedMovePos) {
                this.setSelectClickableSectionByID("move:"+clickedPosition, true);
                if (this.selectedMovePos >= 0) this.setSelectClickableSectionByID("move:"+this.selectedMovePos, false);
                this.selectedMovePos = clickedPosition;
            }
            else {
                this.setSelectClickableSectionByID("move:"+clickedPosition, false);
                this.selectedMovePos = -1;
            }
        }
    }

    private List<RiftLibUIElement.Element> changeNamePopup() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        RiftLibUIElement.TextBoxElement textBox = new RiftLibUIElement.TextBoxElement();
        textBox.setID("newName");
        textBox.setWidth(100);
        textBox.setAlignment(RiftLibUIElement.ALIGN_CENTER);
        if (!this.getMemberNBT().getCustomName().isEmpty()) {
            textBox.setDefaultText(this.getMemberNBT().getCustomName());
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
