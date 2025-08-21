package anightdazingzoroark.prift.client.ui.movesScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUser;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftMovesScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/moves_background.png");
    private final SelectedCreatureInfo selectedCreature;
    private SelectedMoveInfo hoveredMoveInfo;
    private SelectedMoveInfo selectedMoveInfo;

    public RiftMovesScreen(SelectedCreatureInfo selectedCreature) {
        super(0, 0, 0);
        this.selectedCreature = selectedCreature;
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Arrays.asList(
                this.createLearntMovesSection(),
                this.createBackButtonSection(),
                this.createMoveDescriptionBGSection(),
                this.createMoveDescriptionSection(),
                this.createLearnableMovesHeaderSection(),
                this.createLearnableMovesSection()
        );
    }

    //for creating section for learnt moves
    private RiftUISectionCreatureNBTUser createLearntMovesSection() {
        return new RiftUISectionCreatureNBTUser("learntMovesSection", this.selectedCreature.getCreatureNBT(this.mc.player), this.width, this.height, 115, 124, -62, 4, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //label
                RiftLibUIElement.TextElement learntMovesLabel = new RiftLibUIElement.TextElement();
                learntMovesLabel.setText(I18n.format("journal.party_label.moves"));
                learntMovesLabel.setBottomSpace(6);
                toReturn.add(learntMovesLabel);

                //moves
                if (!this.nbtTagCompound.nbtIsEmpty() && !this.nbtTagCompound.getMovesList().isEmpty()) {
                    for (int i = 0; i < this.nbtTagCompound.getMovesList().size(); i++) {
                        RiftLibUIElement.ClickableSectionElement moveButton = new RiftLibUIElement.ClickableSectionElement();
                        moveButton.setID("learntMove:"+i);
                        moveButton.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                        moveButton.setSize(105, 13);
                        moveButton.setTextContent(this.nbtTagCompound.getMovesList().get(i).getTranslatedName());
                        moveButton.setTextOffsets(0, 1);
                        moveButton.setTextScale(0.75f);
                        moveButton.setImage(background, 250, 202, 105, 13, 0, 129, 0, 142);
                        moveButton.setImageSelectedUV(0, 155);
                        moveButton.setBottomSpace(3);
                        moveButton.setHasOverlayEffects();
                        toReturn.add(moveButton);
                    }
                }

                return toReturn;
            }
        };
    }

    //get learnt moves section after its created
    private RiftUISectionCreatureNBTUser getLearntMovesSection() {
        return (RiftUISectionCreatureNBTUser) this.getSectionByID("learntMovesSection");
    }

    private RiftLibUISection createBackButtonSection() {
        return new RiftLibUISection("backButtonSection", this.width, this.height, 20, 18, -12, -53, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ClickableSectionElement backButton = new RiftLibUIElement.ClickableSectionElement();
                backButton.setSize(20, 18);
                backButton.setID("backButton");
                backButton.setImage(background, 250, 202, 20, 18, 105, 129, 125, 129);
                backButton.setImageScale(0.75f);
                toReturn.add(backButton);

                return toReturn;
            }
        };
    }

    private RiftLibUISection createMoveDescriptionBGSection() {
        return new RiftLibUISection("moveDescriptionBGSection", this.width, this.height, 113, 55, -62, 34, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.ImageElement backgroundElement = new RiftLibUIElement.ImageElement();
                backgroundElement.setImage(background, 250, 202, 113, 55, 105, 147);
                toReturn.add(backgroundElement);

                return toReturn;
            }
        };
    }

    private RiftLibUISection createMoveDescriptionSection() {
        return new RiftUISectionCreatureNBTUser("moveDescriptionSection", NewPlayerTamedCreaturesHelper.getCreatureNBTFromSelected(this.mc.player, this.selectedCreature), this.width, this.height, 107, 49, -62, 34, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                if (this.nbtTagCompound != null && !this.nbtTagCompound.nbtIsEmpty()) {
                    if (hoveredMoveInfo != null) {
                        RiftLibUIElement.TextElement description = new RiftLibUIElement.TextElement();
                        description.setText(hoveredMoveInfo.getMoveUsingNBT(this.nbtTagCompound).getTranslatedDescription());
                        description.setScale(0.75f);
                        description.setTextColor(0xFFFFFF);
                        toReturn.add(description);
                    }
                    else if (selectedMoveInfo != null) {
                        RiftLibUIElement.TextElement description = new RiftLibUIElement.TextElement();
                        description.setText(selectedMoveInfo.getMoveUsingNBT(this.nbtTagCompound).getTranslatedDescription());
                        description.setScale(0.75f);
                        description.setTextColor(0xFFFFFF);
                        toReturn.add(description);
                    }
                }

                return toReturn;
            }
        };
    }

    private RiftLibUISection createLearnableMovesHeaderSection() {
        return new RiftLibUISection("learnableMovesHeaderSection", this.width, this.height, 113, 9, 62, -54, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.TextElement headerText = new RiftLibUIElement.TextElement();
                headerText.setText(I18n.format("journal.moves_label.available_moves"));
                toReturn.add(headerText);

                return toReturn;
            }
        };
    }

    //for creating section for learnable moves
    private RiftUISectionCreatureNBTUser createLearnableMovesSection() {
        return new RiftUISectionCreatureNBTUser("learnableMovesSection", NewPlayerTamedCreaturesHelper.getCreatureNBTFromSelected(this.mc.player, this.selectedCreature), this.width, this.height, 107,97, 62, 11, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                for (int i = 0; i < this.nbtTagCompound.getLearnableMovesList().size(); i++) {
                    RiftLibUIElement.ClickableSectionElement moveButton = new RiftLibUIElement.ClickableSectionElement();
                    moveButton.setID("learnableMove:"+i);
                    moveButton.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                    moveButton.setSize(105, 13);
                    moveButton.setTextContent(this.nbtTagCompound.getLearnableMovesList().get(i).getTranslatedName());
                    moveButton.setTextOffsets(0, 1);
                    moveButton.setTextScale(0.75f);
                    moveButton.setImage(background, 250, 202, 105, 13, 0, 129, 0, 142);
                    moveButton.setImageSelectedUV(0, 155);
                    moveButton.setBottomSpace(3);
                    moveButton.setHasOverlayEffects();
                    toReturn.add(moveButton);
                }

                return toReturn;
            }
        };
    }

    //get learnable moves section after its created
    private RiftUISectionCreatureNBTUser getLearnableMovesSection() {
        return (RiftUISectionCreatureNBTUser) this.getSectionByID("learnableMovesSection");
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {
        NewPlayerTamedCreaturesHelper.forceSyncPartyNBT(this.mc.player);
        return element;
    }

    @Override
    public RiftLibUISection modifyUISection(RiftLibUISection riftLibUISection) {
        this.setUISectionVisibility("moveDescriptionBGSection", (this.hoveredMoveInfo != null || this.selectedMoveInfo != null));
        this.setUISectionVisibility("moveDescriptionSection", (this.hoveredMoveInfo != null || this.selectedMoveInfo != null));

        if (riftLibUISection.id.equals("learntMovesSection")) {
            this.getLearntMovesSection().setNBTTagCompound(this.selectedCreature.getCreatureNBT(this.mc.player));
        }
        if (riftLibUISection.id.equals("learnableMovesSection")) {
            this.getLearnableMovesSection().setNBTTagCompound(this.selectedCreature.getCreatureNBT(this.mc.player));
        }

        return riftLibUISection;
    }

    @Override
    public ResourceLocation drawBackground() {
        return background;
    }

    @Override
    public int[] backgroundTextureSize() {
        return new int[]{250, 202};
    }

    @Override
    public int[] backgroundUV() {
        return new int[]{0, 0};
    }

    @Override
    public int[] backgroundSize() {
        return new int[]{249, 129};
    }

    @Override
    public void onButtonClicked(RiftLibButton riftLibButton) {}

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {
        if (this.clickableSectionInSection("learntMovesSection", riftLibClickableSection)) {
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    riftLibClickableSection.getStringID().indexOf(":") + 1
            ));

            if (this.selectedMoveInfo != null) {
                if (this.selectedMoveInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT) {
                    if (clickedPosition != this.selectedMoveInfo.movePos) {
                        SelectedMoveInfo newSelectedMoveInfo = new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNT, clickedPosition);

                        NewPlayerTamedCreaturesHelper.swapCreatureMoves(
                                this.mc.player,
                                this.selectedCreature,
                                this.selectedMoveInfo,
                                newSelectedMoveInfo
                        );

                    }
                    this.setSelectClickableSectionByID("learntMove:"+this.selectedMoveInfo.movePos, false);
                    this.selectedMoveInfo = null;
                }
                else if (this.selectedMoveInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE) {
                    SelectedMoveInfo moveToSwap = new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNT, clickedPosition);
                    NewPlayerTamedCreaturesHelper.swapCreatureMoves(
                            this.mc.player,
                            this.selectedCreature,
                            this.selectedMoveInfo,
                            moveToSwap
                    );
                    this.setSelectClickableSectionByID("learnableMove:"+this.selectedMoveInfo.movePos, false);
                    this.selectedMoveInfo = null;
                }
            }
            else {
                this.selectedMoveInfo = new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNT, clickedPosition);
                this.setSelectClickableSectionByID("learntMove:"+clickedPosition, true);
            }
        }
        else if (this.clickableSectionInSection("learnableMovesSection", riftLibClickableSection)) {
            int clickedPosition = Integer.parseInt(riftLibClickableSection.getStringID().substring(
                    riftLibClickableSection.getStringID().indexOf(":") + 1
            ));

            if (this.selectedMoveInfo != null) {
                if (this.selectedMoveInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNT) {
                    SelectedMoveInfo moveToSwap = new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNABLE, clickedPosition);
                    NewPlayerTamedCreaturesHelper.swapCreatureMoves(
                            this.mc.player,
                            this.selectedCreature,
                            this.selectedMoveInfo,
                            moveToSwap
                    );
                    this.setSelectClickableSectionByID("learntMove:"+this.selectedMoveInfo.movePos, false);
                    this.selectedMoveInfo = null;
                }
                else if (this.selectedMoveInfo.moveType == SelectedMoveInfo.SelectedMoveType.LEARNABLE) {
                    if (clickedPosition != this.selectedMoveInfo.movePos) {
                        SelectedMoveInfo newSelectedMoveInfo = new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNABLE, clickedPosition);

                        NewPlayerTamedCreaturesHelper.swapCreatureMoves(
                                this.mc.player,
                                this.selectedCreature,
                                this.selectedMoveInfo,
                                newSelectedMoveInfo
                        );

                    }
                    this.setSelectClickableSectionByID("learnableMove:"+this.selectedMoveInfo.movePos, false);
                    this.selectedMoveInfo = null;
                }
            }
            else {
                this.selectedMoveInfo = new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNABLE, clickedPosition);
                this.setSelectClickableSectionByID("learnableMove:"+clickedPosition, true);
            }
        }
        else if (riftLibClickableSection.getStringID().equals("backButton")) {
            this.selectedCreature.exitToLastMenu(this.mc);
        }
    }

    @Override
    public void onElementHovered(RiftLibUISection section, RiftLibUIElement.Element element) {
        if (section.id.equals("learntMovesSection")
                && element instanceof RiftLibUIElement.ClickableSectionElement
                && element.getID().startsWith("learntMove:")) {
            int hoveredPosition = Integer.parseInt(element.getID().substring(
                    element.getID().indexOf(":") + 1
            ));
            this.hoveredMoveInfo = new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNT, hoveredPosition);
        }
        else if (section.id.equals("learnableMovesSection")
                && element instanceof RiftLibUIElement.ClickableSectionElement
                && element.getID().startsWith("learnableMove:")) {
            int hoveredPosition = Integer.parseInt(element.getID().substring(
                    element.getID().indexOf(":") + 1
            ));
            this.hoveredMoveInfo = new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNABLE, hoveredPosition);
        }
    }

    @Override
    protected void onPressEscape() {
        this.selectedCreature.exitToLastMenu(this.mc);
    }
}
