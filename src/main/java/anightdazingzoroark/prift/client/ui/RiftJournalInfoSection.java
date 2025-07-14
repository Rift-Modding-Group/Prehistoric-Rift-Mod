package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSectionContents;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RiftJournalInfoSection extends RiftGuiScrollableSection {
    private RiftCreatureType entryType;
    private boolean searchMode;

    public RiftJournalInfoSection(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(264, 190, guiWidth, guiHeight, 60, 7, fontRenderer, minecraft);
        this.scrollbarXOffset = 5;
        this.scrollbarYOffset = 0;
    }

    public void setEntryType(RiftCreatureType entryType) {
        this.entryType = entryType;
    }

    public void setSearchMode(boolean value) {
        this.searchMode = value;
    }

    @Override
    public RiftGuiScrollableSectionContents defineSectionContents() {
        RiftGuiScrollableSectionContents toReturn = new RiftGuiScrollableSectionContents();

        //for when searching
        if (this.searchMode) {
            if (this.entryType != null) toReturn = this.defaultContents();
            else {
                toReturn.addTextBoxElement(new RiftGuiScrollableSectionContents.TextBoxElement()
                        //.setDefaultText("Search...")
                        .setId("searchBox")
                        .setWidth(180)
                );
            }
        }
        //for when not searching
        else {
            if (this.entryType != null) toReturn = this.defaultContents();
            else {
                toReturn.addTextElement(new RiftGuiScrollableSectionContents.TextElement()
                        .setContents(this.getJournalEntry())
                );
            }
        }

        return toReturn;
    }

    private RiftGuiScrollableSectionContents defaultContents() {
        RiftGuiScrollableSectionContents toReturn = new RiftGuiScrollableSectionContents();

        //add image
        if (this.entryType != null) {
            toReturn.addImageElement(new RiftGuiScrollableSectionContents.ImageElement()
                    .setImageLocation(new ResourceLocation(RiftInitialize.MODID, "textures/journal/"+this.entryType.name().toLowerCase()+"_journal.png"))
                    .setImageScale(0.75f)
                    .setImageSize(240, 180)
            );
        }

        //add contents
        if (this.entryType != null) {
            //get diet
            RiftGuiScrollableSectionContents.TextElement dietElement = this.entryType.getCreatureDiet() == null ? null : new RiftGuiScrollableSectionContents.TextElement()
                    .setContents(I18n.format("journal.diet", this.entryType.getCreatureDiet().getTranslatedName()));

            //get levelup rate
            RiftGuiScrollableSectionContents.TextElement levelupRateElement = this.entryType.getLevelupRate() == null ? null : new RiftGuiScrollableSectionContents.TextElement()
                    .setContents(I18n.format("journal.levelup_rate", this.entryType.getLevelupRate().getTranslatedName()));

            //get favorite meals as item list
            List<RiftCreatureConfig.Meal> mealsList = RiftConfigHandler.getConfig(this.entryType).general.favoriteMeals;
            List<String> mealsAsStrings = new ArrayList<>();
            if (mealsList != null) {
                for (RiftCreatureConfig.Meal meal : mealsList) {
                    mealsAsStrings.add(meal.itemId);
                }
            }
            RiftGuiScrollableSectionContents.ItemListElement mealsElement = mealsAsStrings.isEmpty() ? null : new RiftGuiScrollableSectionContents.ItemListElement()
                    .setHeaderText(I18n.format("journal.breeding_foods"))
                    .addItems(mealsAsStrings);

            //get favorite foods as item list
            List<RiftCreatureConfig.Food> foodList = RiftConfigHandler.getConfig(this.entryType).general.favoriteFood;
            List<String> foodAsStrings = new ArrayList<>();
            if (foodList != null) {
                for (RiftCreatureConfig.Food food : foodList) {
                    foodAsStrings.add(food.itemId);
                }
            }
            RiftGuiScrollableSectionContents.ItemListElement foodElement = foodAsStrings.isEmpty() ? null : new RiftGuiScrollableSectionContents.ItemListElement()
                    .setHeaderText(I18n.format("journal.favorite_foods"))
                    .addItems(foodAsStrings);

            //get mining levels
            boolean hasMiningLevels = RiftConfigHandler.getConfig(this.entryType).general.blockBreakLevels != null && !RiftConfigHandler.getConfig(this.entryType).general.blockBreakLevels.isEmpty();
            RiftGuiScrollableSectionContents.MiningLevelListElement miningLevelListElement = !hasMiningLevels ? null : new RiftGuiScrollableSectionContents.MiningLevelListElement()
                    .setHeaderText(I18n.format("journal.mining_levels"))
                    .addMiningLevels(RiftConfigHandler.getConfig(this.entryType).general.blockBreakLevels);

            //now make the tab elements
            toReturn.addTabElement(new RiftGuiScrollableSectionContents.TabElement()
                    .setId("creatureInfo")
                    .setWidth(240)
                    //entry, just the journal entry
                    .addTab("entry", Arrays.asList(new RiftGuiScrollableSectionContents.TextElement()
                            .setContents(this.getJournalEntry()))
                    )
                    //info, such as diet, foods they will consume, etc
                    .addTab("info", Arrays.asList(
                            dietElement,
                            levelupRateElement,
                            mealsElement,
                            foodElement,
                            miningLevelListElement
                    ))
            );
        }

        return toReturn;
    }

    private String getJournalEntry() {
        String languageCode = this.minecraft.gameSettings.language;
        String entryName = this.entryType != null ? this.entryType.name().toLowerCase() : "intro";

        ResourceLocation entryLoc = new ResourceLocation(RiftInitialize.MODID, "journal/" + languageCode + "/" + entryName + ".txt");
        IResourceManager manager = this.minecraft.getResourceManager();

        try (InputStream inputStream = manager.getResource(entryLoc).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            if (!languageCode.equals("en_us")) {
                return getJournalEntry("en_us", entryName);
            }
            return "Error reading journal entry: " + entryLoc;
        }
    }

    private String getJournalEntry(String fallbackLanguageCode, String entryName) {
        ResourceLocation entryLoc = new ResourceLocation(RiftInitialize.MODID, "journal/" + fallbackLanguageCode + "/" + entryName + ".txt");
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();

        try (InputStream inputStream = manager.getResource(entryLoc).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            return "Error reading journal entry: " + entryLoc;
        }
    }
}
