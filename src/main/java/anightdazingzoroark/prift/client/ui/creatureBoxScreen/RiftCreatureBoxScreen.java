package anightdazingzoroark.prift.client.ui.creatureBoxScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.CommonUISections;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.creatureBoxInfoScreen.RiftCreatureBoxInfoScreen;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements.RiftBoxDeployedMembersSection;
import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUser;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements.RiftBoxMembersSection;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements.RiftPartyMembersSection;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureBoxStorage;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.tileentities.RiftNewTileEntityCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBoxHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftCreatureBoxScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png");
    private SelectedCreatureInfo selectedCreatureInfo;
    private SelectedCreatureInfo selectedDropInvTest;
    private int currentBox;
    private RiftCreature creatureToDraw;
    private boolean shufflePartyMemsMode;

    public RiftCreatureBoxScreen(BlockPos pos) {
        this(pos, null);
    }

    public RiftCreatureBoxScreen(BlockPos pos, SelectedCreatureInfo selectedCreatureInfo) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.selectedCreatureInfo = selectedCreatureInfo;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (this.creatureToDraw == null && this.selectedCreatureInfo != null) {
            this.selectNewCreature(this.selectedCreatureInfo);
        }
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Arrays.asList(
                this.createPartyHeaderSection(),
                this.createPartyMembersSection(),
                this.createBoxMembersSection(),
                this.createBoxHeaderSection(),
                this.createLeftButtonHeaderSection(),
                this.createRightButtonHeaderSection(),
                this.createShuffleCreaturesButtonSection(),
                this.createShowCreatureBoxInfoSection(),
                this.createDeathBGSelectedCreature(),
                this.createCreatureToDrawSection(),
                this.createSelectedCreatureInfoSection(),
                this.createCreatureBoxDeployedHeaderSection(),
                this.createCreatureBoxDeployedSection()
        );
    }

    private RiftLibUISection createPartyHeaderSection() {
        return new RiftLibUISection("partyHeaderSection", this.width, this.height, 40, 9, -88, -96, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.TextElement headerElement = new RiftLibUIElement.TextElement();
                headerElement.setText(I18n.format("journal.party_label.party"));
                toReturn.add(headerElement);

                return toReturn;
            }
        };
    }

    private RiftLibUISection createPartyMembersSection() {
        return new RiftPartyMembersSection(this.width, this.height, 44, 66, -90, -55, this.fontRenderer, this.mc);
    }

    private RiftPartyMembersSection getPartyMembersSection() {
        return (RiftPartyMembersSection) this.getSectionByID("partyMembersSection");
    }

    private RiftLibUISection createBoxMembersSection() {
        return new RiftBoxMembersSection(this.width, this.height, 175, 140, 23, -35, this.fontRenderer, this.mc);
    }

    private RiftBoxMembersSection getBoxMembersSection() {
        return (RiftBoxMembersSection) this.getSectionByID("boxMembersSection");
    }

    private RiftLibUISection createBoxHeaderSection() {
        return new RiftLibUISection("boxHeaderSection", this.width, this.height, 96, 13, 23, -114, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement boxHeaderElement = new RiftLibUIElement.ClickableSectionElement();
                boxHeaderElement.setID("boxHeader");
                boxHeaderElement.setSize(96, 13);
                boxHeaderElement.setTextContent("");
                boxHeaderElement.setTextOffsets(0, 1);
                boxHeaderElement.setImage(background, 400, 360, 96, 13, 100, 255, 196, 255);
                toReturn.add(boxHeaderElement);

                return toReturn;
            }
        };
    }

    private RiftLibUISection createLeftButtonHeaderSection() {
        return new RiftLibUISection("leftButtonHeaderSection", this.width, this.height, 13, 13, -37, -114, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement leftButtonElement = new RiftLibUIElement.ClickableSectionElement();
                leftButtonElement.setID("leftButton");
                leftButtonElement.setSize(13, 13);
                leftButtonElement.setImage(background, 400, 360, 13, 13, 173, 268, 199, 268);
                toReturn.add(leftButtonElement);

                return toReturn;
            }
        };
    }

    private RiftLibUISection createRightButtonHeaderSection() {
        return new RiftLibUISection("rightButtonHeaderSection", this.width, this.height, 13, 13, 83, -114, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement rightButtonElement = new RiftLibUIElement.ClickableSectionElement();
                rightButtonElement.setID("rightButton");
                rightButtonElement.setSize(13, 13);
                rightButtonElement.setImage(background, 400, 360, 13, 13, 160, 268, 186, 268);
                toReturn.add(rightButtonElement);

                return toReturn;
            }
        };
    }

    private RiftLibUISection createShuffleCreaturesButtonSection() {
        return new RiftLibUISection("shuffleCreaturesButtonSection", this.width, this.height, 20, 18, 100, -113, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement shuffleButtonElement = new RiftLibUIElement.ClickableSectionElement();
                shuffleButtonElement.setID("shuffleCreaturesButton");
                shuffleButtonElement.setSize(20, 18);
                shuffleButtonElement.setImage(background, 400, 360, 20, 18, 160, 282, 180, 282);
                shuffleButtonElement.setImageSelectedUV(200, 282);
                shuffleButtonElement.setImageScale(0.75f);
                toReturn.add(shuffleButtonElement);

                return toReturn;
            }
        };
    }

    private RiftLibUISection createShowCreatureBoxInfoSection() {
        return new RiftLibUISection("showCreatureBoxInfoButtonSection", this.width, this.height, 20, 18, -54, -113, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement shuffleButtonElement = new RiftLibUIElement.ClickableSectionElement();
                shuffleButtonElement.setID("creatureBoxInfoButton");
                shuffleButtonElement.setSize(20, 18);
                shuffleButtonElement.setImage(background, 400, 360, 20, 18, 160, 300, 180, 300);
                shuffleButtonElement.setImageSelectedUV(200, 282);
                shuffleButtonElement.setImageScale(0.75f);
                toReturn.add(shuffleButtonElement);

                return toReturn;
            }
        };
    }

    private RiftLibUISection createCreatureToDrawSection() {
        return new RiftLibUISection("creatureToDrawSection", this.width, this.height, 99, 60, 117, -62, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                if (creatureToDraw != null) {
                    RiftLibUIElement.RenderedEntityElement creatureElement = new RiftLibUIElement.RenderedEntityElement();
                    creatureElement.setID("creatureToDraw");
                    creatureElement.setScale(20f);
                    creatureElement.setEntity(creatureToDraw);
                    creatureElement.setNotLimitedByBounds();
                    creatureElement.setAdditionalSize(0, 40);
                    creatureElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    creatureElement.setRotationAngle(150);
                    toReturn.add(creatureElement);
                }

                return toReturn;
            }
        };
    }

    private RiftLibUISection createDeathBGSelectedCreature() {
        return new RiftLibUISection("deathBGSelectedCreatureSection", this.width, this.height, 105, 66, 113, -62, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ImageElement backgroundElement = new RiftLibUIElement.ImageElement();
                backgroundElement.setImage(background, 400, 360, 105, 66, 220, 282);
                toReturn.add(backgroundElement);

                return toReturn;
            }
        };
    }

    private RiftUISectionCreatureNBTUser createSelectedCreatureInfoSection() {
        return new RiftUISectionCreatureNBTUser("selectedCreatureInfoSection", new CreatureNBT(), this.width, this.height, 100, 114, 110, 35, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                if (this.nbtTagCompound != null && !this.nbtTagCompound.nbtIsEmpty()) {
                    //name and level
                    RiftLibUIElement.TextElement nameElement = new RiftLibUIElement.TextElement();
                    nameElement.setText(this.nbtTagCompound.getCreatureName(true));
                    nameElement.setScale(0.5f);
                    toReturn.add(nameElement);

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
                    healthBar.setPercentage(health / maxHealth);
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
                    energyBar.setPercentage(energy / (float) maxEnergy);
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
                    xpBar.setPercentage(xp / (float) maxXP);
                    xpBar.setColors(0x98d06b, 0x868686);
                    xpBar.setWidth(100);
                    xpBar.setBottomSpace(6);
                    toReturn.add(xpBar);

                    //more info button
                    RiftLibUIElement.ButtonElement moreInfoButtonElement = new RiftLibUIElement.ButtonElement();
                    moreInfoButtonElement.setID("moreInfoButton");
                    moreInfoButtonElement.setSize(100, 20);
                    moreInfoButtonElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    moreInfoButtonElement.setText(I18n.format("creature_box.more_info"));
                    moreInfoButtonElement.setBottomSpace(3);
                    toReturn.add(moreInfoButtonElement);

                    //release button
                    RiftLibUIElement.ButtonElement releaseButtonElement = new RiftLibUIElement.ButtonElement();
                    releaseButtonElement.setID("openChangeNamePopup");
                    releaseButtonElement.setSize(100, 20);
                    releaseButtonElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    releaseButtonElement.setText(I18n.format("creature_box.change_name"));
                    toReturn.add(releaseButtonElement);
                }

                return toReturn;
            }
        };
    }

    private RiftUISectionCreatureNBTUser getSelectedCreatureInfoSection() {
        return (RiftUISectionCreatureNBTUser) this.getSectionByID("selectedCreatureInfoSection");
    }

    private RiftLibUISection createCreatureBoxDeployedHeaderSection() {
        return new RiftLibUISection("creatureBoxDeployedHeaderSection", this.width, this.height, 172, 9, 23, 43, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.TextElement headerElement = new RiftLibUIElement.TextElement();
                headerElement.setText(I18n.format("creature_box.deployed_creatures"));
                toReturn.add(headerElement);

                return toReturn;
            }
        };
    }

    private RiftBoxDeployedMembersSection createCreatureBoxDeployedSection() {
        return new RiftBoxDeployedMembersSection(this.width, this.height, 172, 70, 21, 87, this.fontRenderer, this.mc);
    }

    private RiftBoxDeployedMembersSection getCreatureBoxDeployedSection() {
        return (RiftBoxDeployedMembersSection) this.getSectionByID("creatureBoxDeployedSection");
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
        return new int[]{0, 0};
    }

    @Override
    public int[] backgroundSize() {
        int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? 124 : 0;
        //int yOffset = this.selectedCreatureInfo != null ? 196 : 0;
        return new int[]{227 + xOffset, 246};
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {
        if (riftLibUISection.id.equals("boxHeaderSection") && element.getID().equals("boxHeader")) {
            RiftLibUIElement.ClickableSectionElement boxHeaderElement = (RiftLibUIElement.ClickableSectionElement) element;
            boxHeaderElement.setTextContent(NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player).getBoxName(this.currentBox));
        }
        else if (riftLibUISection.id.equals("creatureToDrawSection") && element.getID().equals("creatureToDraw")) {
            RiftLibUIElement.RenderedEntityElement creatureElement = (RiftLibUIElement.RenderedEntityElement) element;
            creatureElement.setEntity(this.creatureToDraw);
        }
        return element;
    }

    @Override
    public RiftLibUISection modifyUISection(RiftLibUISection riftLibUISection) {
        this.setUISectionVisibility("deathBGSelectedCreatureSection", !this.shufflePartyMemsMode && this.selectedCreatureInfo != null && this.selectedCreatureInfo.getCreatureNBT(this.mc.player).getCreatureHealth()[0] <= 0);
        this.setUISectionVisibility("creatureToDrawSection", !this.shufflePartyMemsMode);

        switch (riftLibUISection.id) {
            case "partyMembersSection": {
                //update party members
                NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);
                this.getPartyMembersSection().setPartyMembersNBT(NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player));

                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? -152 : -90;
                this.getPartyMembersSection().repositionSection(xOffset, -55);
                break;
            }
            case "boxMembersSection": {
                this.getBoxMembersSection().setBoxMembersNBT(
                        NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player).getBoxContents(this.currentBox),
                        this.currentBox
                );

                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? -39 : 23;
                this.getBoxMembersSection().repositionSection(xOffset, -35);
                break;
            }
            case "creatureBoxDeployedSection": {
                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? -41 : 21;
                this.getCreatureBoxDeployedSection().repositionSection(xOffset, 87);
                this.getCreatureBoxDeployedSection().setBoxDeployedMembersNBT(this.getCreatureBox().getDeployedCreatures());
                break;
            }
            case "selectedCreatureInfoSection": {
                if (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) this.getSelectedCreatureInfoSection().setNBTTagCompound(this.selectedCreatureInfo.getCreatureNBT(this.mc.player));
                else this.getSelectedCreatureInfoSection().setNBTTagCompound(new CreatureNBT());
                break;
            }
            case "partyHeaderSection": {
                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? -150 : -88;
                this.getSectionByID("partyHeaderSection").repositionSection(xOffset, -96);
                break;
            }
            case "boxHeaderSection": {
                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? -39 : 23;
                this.getSectionByID("boxHeaderSection").repositionSection(xOffset, -114);
                break;
            }
            case "creatureBoxDeployedHeaderSection": {
                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? -39 : 23;
                this.getSectionByID("creatureBoxDeployedHeaderSection").repositionSection(xOffset, 43);
                break;
            }
            case "leftButtonHeaderSection": {
                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? -99 : -37;
                this.getSectionByID("leftButtonHeaderSection").repositionSection(xOffset, -114);
                break;
            }
            case "rightButtonHeaderSection": {
                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? 21 : 83;
                this.getSectionByID("rightButtonHeaderSection").repositionSection(xOffset, -114);
                break;
            }
            case "shuffleCreaturesButtonSection": {
                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? 38 : 100;
                this.getSectionByID("shuffleCreaturesButtonSection").repositionSection(xOffset, -113);
                break;
            }
            case "showCreatureBoxInfoButtonSection": {
                int xOffset = (!this.shufflePartyMemsMode && this.selectedCreatureInfo != null) ? -116 : -54;
                this.getSectionByID("showCreatureBoxInfoButtonSection").repositionSection(xOffset, -113);
                break;
            }
        }
        return riftLibUISection;
    }

    @Override
    public void onButtonClicked(RiftLibButton riftLibButton) {
        if (riftLibButton.buttonId.equals("moreInfoButton")) {
            RiftLibUIHelper.showUI(this.mc.player, new RiftCreatureBoxInfoScreen(new BlockPos(this.x, this.y, this.z), this.selectedCreatureInfo));
        }
        if (riftLibButton.buttonId.equals("openChangeNamePopup")) {
            this.createPopup(CommonUISections.changeNamePopup(this.selectedCreatureInfo.getCreatureNBT(this.mc.player)));
        }
        if (riftLibButton.buttonId.equals("setNewBoxName")) {
            NewPlayerTamedCreaturesHelper.changeBoxName(this.mc.player, this.currentBox, this.getTextFieldTextByID("newBoxName"));
            this.clearPopup();
        }
        if (riftLibButton.buttonId.equals("setNewName")) {
            NewPlayerTamedCreaturesHelper.setSelectedCreatureName(this.mc.player, this.selectedCreatureInfo, this.getTextFieldTextByID("newName"));
            this.clearPopup();
        }
        if (riftLibButton.buttonId.equals("confirmInventoryDrop")) {
            if (!this.selectedCreatureInfo.getCreatureNBT(this.mc.player).inventoryIsEmpty())
                NewPlayerTamedCreaturesHelper.dropSelectedInventory(this.mc.player, this.selectedCreatureInfo);
            if (!this.selectedDropInvTest.getCreatureNBT(this.mc.player).inventoryIsEmpty())
                NewPlayerTamedCreaturesHelper.dropSelectedInventory(this.mc.player, this.selectedDropInvTest);


            NewPlayerTamedCreaturesHelper.swapCreatures(this.mc.player, this.selectedCreatureInfo, this.selectedDropInvTest);
            if (this.selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                RiftTileEntityCreatureBoxHelper.forceUpdateCreatureBoxDeployed(this.mc.player, this.selectedCreatureInfo.getCreatureBoxOpenedFrom());
            }
            this.selectNewCreature(null);
            this.selectedDropInvTest = null;
            this.clearPopup();
        }
        if (riftLibButton.buttonId.equals("exitPopup")) this.clearPopup();
    }

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {
        if (riftLibClickableSection.getStringID().startsWith("partyMember:")) {
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    riftLibClickableSection.getStringID().indexOf(":") + 1
            ));
            SelectedCreatureInfo selectionToTest = new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.PARTY, new int[]{clickedPosition});

            //swap something with a creature from the party
            if (this.shufflePartyMemsMode) {
                if (this.selectedCreatureInfo != null) {
                    if (this.posCanSwapBasedOnInventory(selectionToTest)) {
                        NewPlayerTamedCreaturesHelper.swapCreatures(this.mc.player, this.selectedCreatureInfo, selectionToTest);
                        if (this.selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                            RiftTileEntityCreatureBoxHelper.forceUpdateCreatureBoxDeployed(this.mc.player, this.selectedCreatureInfo.getCreatureBoxOpenedFrom());
                        }
                        this.selectNewCreature(null);
                    }
                    else {
                        this.selectedDropInvTest = selectionToTest;
                        this.createPopup(this.dropInventoryPopup());
                    }
                }
                else this.selectNewCreature(selectionToTest);
            }
            //when not shuffling creatures, just select creature from party
            else {
                if (this.selectedCreatureInfo != null) {
                    if (selectionToTest.getCreatureNBT(this.mc.player).nbtIsEmpty()) {
                        this.selectNewCreature(null);
                    }
                    else if (this.selectedCreatureInfo.equals(selectionToTest)) {
                        this.selectNewCreature(null);
                    }
                    else this.selectNewCreature(selectionToTest);
                }
                else if (!selectionToTest.getCreatureNBT(this.mc.player).nbtIsEmpty()) {
                    this.selectNewCreature(selectionToTest);
                }
            }
        }
        else if (riftLibClickableSection.getStringID().startsWith("boxMember:")) {
            int firstColonPos = riftLibClickableSection.getStringID().indexOf(":");
            int secondColonPos = riftLibClickableSection.getStringID().indexOf(":", firstColonPos + 1);
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    secondColonPos + 1
            ));
            SelectedCreatureInfo selectionToTest = new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.BOX, new int[]{this.currentBox, clickedPosition});

            //swap something with a creature from the box
            if (this.shufflePartyMemsMode) {
                if (this.selectedCreatureInfo != null) {
                    if (this.posCanSwapBasedOnInventory(selectionToTest)) {
                        NewPlayerTamedCreaturesHelper.swapCreatures(this.mc.player, this.selectedCreatureInfo, selectionToTest);
                        if (this.selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                            RiftTileEntityCreatureBoxHelper.forceUpdateCreatureBoxDeployed(this.mc.player, this.selectedCreatureInfo.getCreatureBoxOpenedFrom());
                        }
                        this.selectNewCreature(null);
                    }
                    else {
                        this.selectedDropInvTest = selectionToTest;
                        this.createPopup(this.dropInventoryPopup());
                    }
                }
                else this.selectNewCreature(selectionToTest);
            }
            //when not shuffling creatures, just select creature from box
            else {
                if (this.selectedCreatureInfo != null) {
                    if (selectionToTest.getCreatureNBT(this.mc.player).nbtIsEmpty()) {
                        this.selectNewCreature(null);
                    }
                    else if (this.selectedCreatureInfo.equals(selectionToTest)) {
                        this.selectNewCreature(null);
                    }
                    else this.selectNewCreature(selectionToTest);
                }
                else if (!selectionToTest.getCreatureNBT(this.mc.player).nbtIsEmpty()) {
                    this.selectNewCreature(selectionToTest);
                }
            }
        }
        else if (riftLibClickableSection.getStringID().startsWith("boxDeployedMember:")) {
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    riftLibClickableSection.getStringID().indexOf(":") + 1
            ));
            SelectedCreatureInfo selectionToTest = new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED, new int[]{clickedPosition});
            selectionToTest.setCreatureBoxOpenedFrom(new BlockPos(this.x, this.y, this.z));

            //swap something with selected creature
            if (this.shufflePartyMemsMode) {
                if (this.selectedCreatureInfo != null) {
                    if (this.posCanSwapBasedOnInventory(selectionToTest)) {
                        NewPlayerTamedCreaturesHelper.swapCreatures(this.mc.player, this.selectedCreatureInfo, selectionToTest);
                        if (this.selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                            RiftTileEntityCreatureBoxHelper.forceUpdateCreatureBoxDeployed(this.mc.player, this.selectedCreatureInfo.getCreatureBoxOpenedFrom());
                        }
                        this.selectNewCreature(null);
                    }
                    else {
                        this.selectedDropInvTest = selectionToTest;
                        this.createPopup(this.dropInventoryPopup());
                    }
                }
                else this.selectNewCreature(selectionToTest);
            }
            //when not shuffling creatures, just select the creature
            else {
                if (this.selectedCreatureInfo != null) {
                    if (selectionToTest.getCreatureNBT(this.mc.player).nbtIsEmpty()) {
                        this.selectNewCreature(null);
                    }
                    else if (this.selectedCreatureInfo.equals(selectionToTest)) {
                        this.selectNewCreature(null);
                    }
                    else this.selectNewCreature(selectionToTest);
                }
                else if (!selectionToTest.getCreatureNBT(this.mc.player).nbtIsEmpty()) {
                    this.selectNewCreature(selectionToTest);
                }
            }
        }

        switch (riftLibClickableSection.getStringID()) {
            case "shuffleCreaturesButton": {
                this.shufflePartyMemsMode = !this.shufflePartyMemsMode;
                this.setSelectClickableSectionByID("shuffleCreaturesButton", this.shufflePartyMemsMode);
                break;
            }
            case "leftButton": {
                this.currentBox = (this.currentBox <= 0) ? CreatureBoxStorage.maxBoxAmnt - 1 : this.currentBox - 1;
                break;
            }
            case "rightButton": {
                this.currentBox = (this.currentBox >= CreatureBoxStorage.maxBoxAmnt - 1) ? 0 : this.currentBox + 1;
                break;
            }
            case "boxHeader": {
                this.createPopup(this.changeBoxNamePopup());
                break;
            }
            case "creatureBoxInfoButton": {
                this.createPopup(this.creatureBoxInfoPopup());
                break;
            }
        }
    }

    private void selectNewCreature(SelectedCreatureInfo newSelectedCreatureInfo) {
        //unselect all old creatures first
        for (int i = 0; i < NewPlayerTamedCreaturesHelper.maxPartySize; i++) {
            this.setSelectClickableSectionByID(false,"partyMember:"+i, false);
        }
        for (int i = 0; i < CreatureBoxStorage.maxBoxAmnt; i++) {
            for (int j = 0; j < CreatureBoxStorage.maxBoxStorableCreatures; j++) {
                this.setSelectClickableSectionByID(false, "boxMember:"+i+":"+j, false);
            }
        }
        for (int i = 0; i < CreatureBoxStorage.maxBoxStorableCreatures; i++) {
            this.setSelectClickableSectionByID(false, "boxDeployedMember:"+i, false);
        }

        //select new creature
        if (newSelectedCreatureInfo != null) {
            if (newSelectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                this.setSelectClickableSectionByID(false, "partyMember:"+newSelectedCreatureInfo.pos[0], true);
            }
            else if (newSelectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                this.setSelectClickableSectionByID(false, "boxMember:"+newSelectedCreatureInfo.pos[0]+":"+newSelectedCreatureInfo.pos[1], true);
            }
            else if (newSelectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED) {
                this.setSelectClickableSectionByID(false, "boxDeployedMember:"+newSelectedCreatureInfo.pos[0], true);
            }
            this.selectedCreatureInfo = newSelectedCreatureInfo;
            this.creatureToDraw = newSelectedCreatureInfo.getCreatureNBT(this.mc.player).getCreatureAsNBT(this.mc.world);
        }
        else {
            this.selectedCreatureInfo = null;
            this.creatureToDraw = null;
        }
    }

    private boolean posCanSwapBasedOnInventory(SelectedCreatureInfo selectedToTest) {
        CreatureNBT selectedCreatureNBT = this.selectedCreatureInfo.getCreatureNBT(this.mc.player);
        CreatureNBT selectedToTestNBT = selectedToTest.getCreatureNBT(this.mc.player);

        boolean selectedCanHaveInventory = this.selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY
                || this.selectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED;
        boolean toTestCanHaveInventory = selectedToTest.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY
                || selectedToTest.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX_DEPLOYED;

        if (selectedCanHaveInventory && toTestCanHaveInventory) return true;
        else if (selectedCanHaveInventory && !toTestCanHaveInventory) return selectedCreatureNBT.inventoryIsEmpty();
        else if (!selectedCanHaveInventory && toTestCanHaveInventory) return selectedToTestNBT.inventoryIsEmpty();
        else return true;
    }

    @Override
    public void onElementHovered(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {}

    //creature box interfacing
    public RiftNewTileEntityCreatureBox getCreatureBox() {
        TileEntity tileEntity = this.mc.world.getTileEntity(new BlockPos(this.x, this.y, this.z));
        if (!(tileEntity instanceof RiftNewTileEntityCreatureBox)) return null;
        return (RiftNewTileEntityCreatureBox) tileEntity;
    }

    //popups start here
    private List<RiftLibUIElement.Element> changeBoxNamePopup() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //header text
        RiftLibUIElement.TextElement textBoxHeader = new RiftLibUIElement.TextElement();
        textBoxHeader.setText(I18n.format("creature_box.change_box_name"));
        textBoxHeader.setAlignment(RiftLibUIElement.ALIGN_CENTER);
        toReturn.add(textBoxHeader);

        RiftLibUIElement.TextBoxElement textBox = new RiftLibUIElement.TextBoxElement();
        textBox.setID("newBoxName");
        textBox.setWidth(100);
        textBox.setAlignment(RiftLibUIElement.ALIGN_CENTER);
        textBox.setDefaultText(NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player).getBoxName(this.currentBox));
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
        confirmButton.setID("setNewBoxName");
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

    private List<RiftLibUIElement.Element> creatureBoxInfoPopup() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //header
        RiftLibUIElement.TextElement headerElement = new RiftLibUIElement.TextElement();
        headerElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
        headerElement.setText(I18n.format("creature_box.info_header"));
        toReturn.add(headerElement);

        //owner
        RiftLibUIElement.TextElement ownerTextElement = new RiftLibUIElement.TextElement();
        ownerTextElement.setText(I18n.format("creature_box.owner", this.getCreatureBox().getOwnerName()));
        toReturn.add(ownerTextElement);

        //range
        RiftLibUIElement.TextElement rangeElement = new RiftLibUIElement.TextElement();
        rangeElement.setText(I18n.format("creature_box.range", this.getCreatureBox().getDeploymentRangeWidth(), this.getCreatureBox().getDeploymentRangeWidth(), this.getCreatureBox().getDeploymentRangeWidth()));
        toReturn.add(rangeElement);

        //ok button
        RiftLibUIElement.ButtonElement okButton = new RiftLibUIElement.ButtonElement();
        okButton.setAlignment(RiftLibUIElement.ALIGN_CENTER);
        okButton.setSize(60, 20);
        okButton.setText(I18n.format("radial.popup_button.ok"));
        okButton.setID("exitPopup");
        toReturn.add(okButton);

        return toReturn;
    }

    private List<RiftLibUIElement.Element> dropInventoryPopup() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //text
        RiftLibUIElement.TextElement textElement = new RiftLibUIElement.TextElement();
        textElement.setText(I18n.format("creature_box.popup_choice.remove_inventory"));
        toReturn.add(textElement);

        //table for buttons
        RiftLibUIElement.TableContainerElement buttonContainer = new RiftLibUIElement.TableContainerElement();
        buttonContainer.setCellSize(70, 20);
        buttonContainer.setRowCount(2);
        buttonContainer.setAlignment(RiftLibUIElement.ALIGN_CENTER);

        //confirm button
        RiftLibUIElement.ButtonElement confirmButton = new RiftLibUIElement.ButtonElement();
        confirmButton.setSize(60, 20);
        confirmButton.setText(I18n.format("radial.popup_button.confirm"));
        confirmButton.setID("confirmInventoryDrop");
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
