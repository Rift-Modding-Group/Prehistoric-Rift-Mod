package anightdazingzoroark.prift.client.ui.journalScreen.elements;

import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSectionContents;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

import java.util.*;
import java.util.stream.Collectors;

public class RiftJournalIndexSection extends RiftLibUISection {
    private RiftCreatureType.CreatureCategory currentCategory;
    private boolean searchMode;
    private String stringForSearch = "";

    public RiftJournalIndexSection(String id, int guiWidth, int guiHeight, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        super(id, guiWidth, guiHeight, 100, 195, xPos, yPos, fontRenderer, minecraft);
    }

    @Override
    public List<RiftLibUIElement.Element> defineSectionContents() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //in search mode, only add what's searched for
        if (this.searchMode) {
            if (!this.stringForSearch.isEmpty()) {
                //get creature types
                List<RiftCreatureType> creatureTypeList = PlayerJournalProgressHelper.getUnlockedCreatures(this.minecraft.player)
                        .keySet().stream().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
                Map<RiftCreatureType, Boolean> encounteredMap = PlayerJournalProgressHelper.getUnlockedCreatures(this.minecraft.player);

                //filter by those that match stringForSearch
                creatureTypeList = creatureTypeList.stream().filter(type -> type.getTranslatedName().toLowerCase().contains(this.stringForSearch.toLowerCase()))
                        .collect(Collectors.toList());

                //add a back button
                RiftLibUIElement.ButtonElement backButton = new RiftLibUIElement.ButtonElement();
                backButton.setText(I18n.format("type.creature.back"));
                backButton.setID("NULL");
                backButton.setSize(96, 20);
                backButton.setBottomSpace(5);
                toReturn.add(backButton);

                //add the other elements
                for (RiftCreatureType creatureType : creatureTypeList) {
                    if (encounteredMap.get(creatureType) != null) {
                        String name = encounteredMap.get(creatureType) ? creatureType.getTranslatedName() : "("+creatureType.getTranslatedName()+")";

                        RiftLibUIElement.ButtonElement creatureButton = new RiftLibUIElement.ButtonElement();
                        creatureButton.setText(name);
                        creatureButton.setID(creatureType.toString());
                        creatureButton.setSize(96, 20);
                        creatureButton.setBottomSpace(5);
                        toReturn.add(creatureButton);
                    }
                }
            }
        }
        //else, show regular index
        else {
            //for categories unlocked by the player
            if (this.currentCategory == null) {
                for (int x = 0; x < PlayerJournalProgressHelper.getUnlockedCategories(this.minecraft.player).size(); x++) {
                    RiftLibUIElement.ButtonElement categoryButton = new RiftLibUIElement.ButtonElement();
                    categoryButton.setText(PlayerJournalProgressHelper.getUnlockedCategories(this.minecraft.player).get(x).getTranslatedName(true));
                    categoryButton.setID(PlayerJournalProgressHelper.getUnlockedCategories(this.minecraft.player).get(x).toString());
                    categoryButton.setSize(96, 20);
                    categoryButton.setBottomSpace(5);
                    toReturn.add(categoryButton);
                }
            }
            //for creatures within category unlocked by the player
            else {
                List<RiftCreatureType> creatureTypeList = PlayerJournalProgressHelper.getUnlockedCreatures(this.minecraft.player)
                        .keySet().stream().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
                Map<RiftCreatureType, Boolean> encounteredMap = PlayerJournalProgressHelper.getUnlockedEntriesFromCategory(this.minecraft.player, this.currentCategory);

                //add a back button
                RiftLibUIElement.ButtonElement backButton = new RiftLibUIElement.ButtonElement();
                backButton.setText(I18n.format("type.creature.back"));
                backButton.setID("NULL");
                backButton.setSize(96, 20);
                backButton.setBottomSpace(5);
                toReturn.add(backButton);

                //add the other elements
                for (RiftCreatureType creatureType : creatureTypeList) {
                    if (encounteredMap.get(creatureType) != null) {
                        String name = encounteredMap.get(creatureType) ? creatureType.getTranslatedName() : "("+creatureType.getTranslatedName()+")";

                        RiftLibUIElement.ButtonElement creatureButton = new RiftLibUIElement.ButtonElement();
                        creatureButton.setText(name);
                        creatureButton.setID(creatureType.toString());
                        creatureButton.setSize(96, 20);
                        creatureButton.setBottomSpace(5);
                        toReturn.add(creatureButton);
                    }
                }
            }
        }

        return toReturn;
    }

    public void setCurrentCategory(RiftCreatureType.CreatureCategory currentCategory) {
        this.currentCategory = currentCategory;
    }

    public RiftCreatureType.CreatureCategory getCurrentCategory() {
        return this.currentCategory;
    }

    public void setSearchMode(boolean value) {
        this.searchMode = value;
    }

    public String getStringForSearch() {
        return this.stringForSearch;
    }

    public void setStringForSearch(String value) {
        this.stringForSearch = value;
    }
}
