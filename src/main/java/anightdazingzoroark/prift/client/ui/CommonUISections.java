package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUser;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
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
}
