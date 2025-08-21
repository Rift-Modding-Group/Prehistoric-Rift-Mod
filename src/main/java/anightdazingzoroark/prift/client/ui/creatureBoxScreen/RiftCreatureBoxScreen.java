package anightdazingzoroark.prift.client.ui.creatureBoxScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.CommonUISections;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.creatureBoxInfoScreen.RiftCreatureBoxInfoScreen;
import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUser;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements.RiftBoxMembersSection;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements.RiftPartyMembersSection;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureBoxStorage;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftCreatureBoxScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png");
    private SelectedCreatureInfo selectedCreatureInfo;
    private int currentBox;
    private RiftCreature creatureToDraw;

    public RiftCreatureBoxScreen(int x, int y, int z) {
        super(x, y, z);
    }

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
                this.createCreatureToDrawSection(),
                this.createSelectedCreatureInfoSection()
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
                boxHeaderElement.setImage(background, 400, 300, 96, 13, 100, 255, 196, 255);
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
                leftButtonElement.setImage(background, 400, 300, 13, 13, 173, 268, 199, 268);
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
                rightButtonElement.setImage(background, 400, 300, 13, 13, 160, 268, 186, 268);
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
                shuffleButtonElement.setImage(background, 400, 300, 20, 18, 160, 282, 180, 282);
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

    @Override
    public ResourceLocation drawBackground() {
        return this.background;
    }

    @Override
    public int[] backgroundTextureSize() {
        return new int[]{400, 300};
    }

    @Override
    public int[] backgroundUV() {
        return new int[]{0, 0};
    }

    @Override
    public int[] backgroundSize() {
        int xOffset = this.selectedCreatureInfo != null ? 124 : 0;
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
        switch (riftLibUISection.id) {
            case "partyMembersSection": {
                //update party members
                NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);
                this.getPartyMembersSection().setPartyMembersNBT(NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player));

                int xOffset = this.selectedCreatureInfo != null ? -152 : -90;
                this.getPartyMembersSection().repositionSection(xOffset, -55);
                break;
            }
            case "boxMembersSection": {
                this.getBoxMembersSection().setBoxMembersNBT(
                        NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player).getBoxContents(this.currentBox),
                        this.currentBox
                );

                int xOffset = this.selectedCreatureInfo != null ? -39 : 23;
                this.getBoxMembersSection().repositionSection(xOffset, -35);
                break;
            }
            case "selectedCreatureInfoSection": {
                if (this.selectedCreatureInfo != null) this.getSelectedCreatureInfoSection().setNBTTagCompound(this.selectedCreatureInfo.getCreatureNBT(this.mc.player));
                else this.getSelectedCreatureInfoSection().setNBTTagCompound(new CreatureNBT());
                break;
            }
            case "partyHeaderSection": {
                int xOffset = this.selectedCreatureInfo != null ? -150 : -88;
                this.getSectionByID("partyHeaderSection").repositionSection(xOffset, -96);
                break;
            }
            case "boxHeaderSection": {
                int xOffset = this.selectedCreatureInfo != null ? -39 : 23;
                this.getSectionByID("boxHeaderSection").repositionSection(xOffset, -114);
                break;
            }
            case "leftButtonHeaderSection": {
                int xOffset = this.selectedCreatureInfo != null ? -99 : -37;
                this.getSectionByID("leftButtonHeaderSection").repositionSection(xOffset, -114);
                break;
            }
            case "rightButtonHeaderSection": {
                int xOffset = this.selectedCreatureInfo != null ? 21 : 83;
                this.getSectionByID("rightButtonHeaderSection").repositionSection(xOffset, -114);
                break;
            }
            case "shuffleCreaturesButtonSection": {
                int xOffset = this.selectedCreatureInfo != null ? 38 : 100;
                this.getSectionByID("shuffleCreaturesButtonSection").repositionSection(xOffset, -113);
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
        if (riftLibButton.buttonId.equals("exitPopup")) this.clearPopup();
    }

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {
        if (riftLibClickableSection.getStringID().startsWith("partyMember:")) {
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    riftLibClickableSection.getStringID().indexOf(":") + 1
            ));
            SelectedCreatureInfo selectionToTest = new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.PARTY, new int[]{clickedPosition});

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
        else if (riftLibClickableSection.getStringID().startsWith("boxMember:")) {
            int firstColonPos = riftLibClickableSection.getStringID().indexOf(":");
            int secondColonPos = riftLibClickableSection.getStringID().indexOf(":", firstColonPos + 1);
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    secondColonPos + 1
            ));
            SelectedCreatureInfo selectionToTest = new SelectedCreatureInfo(SelectedCreatureInfo.SelectedPosType.BOX, new int[]{this.currentBox, clickedPosition});

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

        switch (riftLibClickableSection.getStringID()) {
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

        //select new creature
        if (newSelectedCreatureInfo != null) {
            if (newSelectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.PARTY) {
                this.setSelectClickableSectionByID(false, "partyMember:"+newSelectedCreatureInfo.pos[0], true);
            }
            else if (newSelectedCreatureInfo.selectedPosType == SelectedCreatureInfo.SelectedPosType.BOX) {
                this.setSelectClickableSectionByID(false, "boxMember:"+newSelectedCreatureInfo.pos[0]+":"+newSelectedCreatureInfo.pos[1], true);
            }
            this.selectedCreatureInfo = newSelectedCreatureInfo;
            this.creatureToDraw = newSelectedCreatureInfo.getCreatureNBT(this.mc.player).getCreatureAsNBT(this.mc.world);
        }
        else {
            this.selectedCreatureInfo = null;
            this.creatureToDraw = null;
        }
    }

    @Override
    public void onElementHovered(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {}

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
}
