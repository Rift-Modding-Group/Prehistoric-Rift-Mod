package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUser;
import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUserWithIntSelector;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class CommonUISections {
    public static RiftLibUISection partyMemberInfoSection(int guiWidth, int guiHeight, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        return new RiftUISectionCreatureNBTUser("partyMemberInfoSection", new CreatureNBT(), guiWidth, guiHeight, 115, 119, xPos, yPos, fontRenderer, minecraft) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                if (this.nbtTagCompound != null && !this.nbtTagCompound.nbtIsEmpty()) {
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

    public static RiftLibUISection partyMemberMovesSection(ResourceLocation background,
                                                           int textureWidth, int textureHeight,
                                                           int shuffleButtonXUV, int shuffleButtonYUV, int shuffleButtonHoveredXUV, int shuffleButtonHoveredYUV, int shuffleButtonSelectedXUV, int shuffleButtonSelectedYUV,
                                                           int moveButtonXUV, int moveButtonYUV, int moveButtonHoveredXUV, int moveButtonHoveredYUV, int moveButtonSelectedXUV, int moveButtonSelectedYUV,
                                                           int guiWidth, int guiHeight, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        return new RiftUISectionCreatureNBTUser("partyMemberMovesSection", new CreatureNBT(), guiWidth, guiHeight, 115, 65, xPos, yPos, fontRenderer, minecraft) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //shuffle moves button
                RiftLibUIElement.ClickableSectionElement shuffleMoves = new RiftLibUIElement.ClickableSectionElement();
                shuffleMoves.setID("shuffleMoves");
                shuffleMoves.setAlignment(RiftLibUIElement.ALIGN_RIGHT);
                shuffleMoves.setImage(background, textureWidth, textureHeight, 20, 18, shuffleButtonXUV, shuffleButtonYUV, shuffleButtonHoveredXUV, shuffleButtonHoveredYUV);
                shuffleMoves.setImageSelectedUV(shuffleButtonSelectedXUV, shuffleButtonSelectedYUV);
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
                    moveClickableSection.setImage(background, textureWidth, textureHeight, 105, 13, moveButtonXUV, moveButtonYUV, moveButtonHoveredXUV, moveButtonHoveredYUV);
                    moveClickableSection.setImageSelectedUV(moveButtonSelectedXUV, moveButtonSelectedYUV);
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

    public static RiftLibUISection moveDescriptionBackgroundSection(ResourceLocation background,
                                                                    int textureWidth, int textureHeight,
                                                                    int uvX, int uvY,
                                                                    int guiWidth, int guiHeight, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        return new RiftLibUISection("moveDescriptionBGSection", guiWidth, guiHeight, 113, 55, xPos, yPos, fontRenderer, minecraft) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //the background image
                RiftLibUIElement.ImageElement backgroundElement = new RiftLibUIElement.ImageElement();
                backgroundElement.setImage(background, textureWidth, textureHeight, 113, 55, uvX, uvY);
                toReturn.add(backgroundElement);

                return toReturn;
            }
        };
    }

    public static RiftUISectionCreatureNBTUserWithIntSelector moveDescriptionSection(int guiWidth, int guiHeight, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        return new RiftUISectionCreatureNBTUserWithIntSelector("moveDescriptionSection", new CreatureNBT(), guiWidth, guiHeight, 107, 49, xPos, yPos, fontRenderer, minecraft) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                //move description of selected move
                if (this.selector >= 0 && this.nbtTagCompound != null && !this.nbtTagCompound.getMovesList().isEmpty()) {
                    RiftLibUIElement.TextElement moveDescription = new RiftLibUIElement.TextElement();
                    moveDescription.setText(this.nbtTagCompound.getMovesList().get(this.selector).getTranslatedDescription());
                    moveDescription.setScale(0.75f);
                    moveDescription.setTextColor(0xFFFFFF);
                    toReturn.add(moveDescription);
                }

                return toReturn;
            }
        };
    }

    public static RiftLibUISection informationClickableSection(int guiWidth, int guiHeight, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        return new RiftLibUISection("informationSection", guiWidth, guiHeight, 54, 11, xPos, yPos, fontRenderer, minecraft) {
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

    public static RiftLibUISection movesClickableSection(int guiWidth, int guiHeight, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        return new RiftLibUISection("movesSection", guiWidth, guiHeight, 54, 11, xPos, yPos, fontRenderer, minecraft) {
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

    public static List<RiftLibUIElement.Element> changeNamePopup(CreatureNBT memberNBT) {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //header
        RiftLibUIElement.TextElement headerTextElement = new RiftLibUIElement.TextElement();
        headerTextElement.setText(I18n.format("journal.change_creature_name"));
        headerTextElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
        toReturn.add(headerTextElement);

        //text box for items
        RiftLibUIElement.TextBoxElement textBox = new RiftLibUIElement.TextBoxElement();
        textBox.setID("newName");
        textBox.setWidth(100);
        textBox.setAlignment(RiftLibUIElement.ALIGN_CENTER);
        if (!memberNBT.getCustomName().isEmpty()) {
            textBox.setDefaultText(memberNBT.getCustomName());
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
