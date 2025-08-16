package anightdazingzoroark.prift.client.ui.journalScreen.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
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
import java.util.List;
import java.util.stream.Collectors;

public class RiftJournalInfoSection extends RiftLibUISection {
    private RiftCreatureType entryType;

    public RiftJournalInfoSection(String id, int guiWidth, int guiHeight, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        super(id, guiWidth, guiHeight, 268, 194, xPos, yPos, fontRenderer, minecraft);
    }

    @Override
    public List<RiftLibUIElement.Element> defineSectionContents() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        if (this.entryType != null) {
            //image for creature
            RiftLibUIElement.ImageElement creatureImage = new RiftLibUIElement.ImageElement();
            creatureImage.setImage(
                    new ResourceLocation(RiftInitialize.MODID, "textures/journal/"+this.entryType.name().toLowerCase()+"_journal.png"),
                    240, 180,
                    240, 180,
                    0, 0
            );
            creatureImage.setScale(0.75f);
            creatureImage.setAlignment(RiftLibUIElement.ALIGN_CENTER);
            toReturn.add(creatureImage);

            //define tab
            RiftLibUIElement.TabElement tabs = new RiftLibUIElement.TabElement();
            tabs.setWidth(256);

            //make tab
            tabs.addTab("entryTab", I18n.format("gui.tab_header.entry"), this.defineEntryTab());

            //make info tab
            tabs.addTab("infoTab", I18n.format("gui.tab_header.info"), this.defineInfoTab());

            toReturn.add(tabs);
        }
        else {
            //just show the intro
            RiftLibUIElement.TextElement introTextElement = new RiftLibUIElement.TextElement();
            introTextElement.setText(this.getJournalEntry());
            toReturn.add(introTextElement);
        }

        return toReturn;
    }

    public List<RiftLibUIElement.Element> defineEntryTab() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //modify contents based on whether or not the creatures entry is unlocked
        RiftLibUIElement.TextElement creatureInfo = new RiftLibUIElement.TextElement();
        boolean isUnlocked = PlayerJournalProgressHelper.getUnlockedCreatures(this.minecraft.player).get(this.entryType);
        String finalEntry = isUnlocked
                ? this.getJournalEntry()
                : I18n.format("journal.entry.locked");
        creatureInfo.setText(finalEntry);
        toReturn.add(creatureInfo);

        return toReturn;
    }

    public List<RiftLibUIElement.Element> defineInfoTab() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //get diet
        RiftLibUIElement.TextElement dietElement = new RiftLibUIElement.TextElement();
        String dietText = this.entryType.getCreatureDiet() == null ? null : I18n.format("journal.diet", this.entryType.getCreatureDiet().getTranslatedName());
        if (dietText != null) {
            dietElement.setText(dietText);
            toReturn.add(dietElement);
        }

        //get levelup rate
        RiftLibUIElement.TextElement levelupRateElement = new RiftLibUIElement.TextElement();
        String levelupRateText = this.entryType.getLevelupRate() == null ? null : I18n.format("journal.levelup_rate", this.entryType.getLevelupRate().getTranslatedName());
        if (levelupRateText != null) {
            levelupRateElement.setText(levelupRateText);
            toReturn.add(levelupRateElement);
        }

        //get favorite meals
        //test first for available meals
        List<RiftCreatureConfig.Meal> mealsList = RiftConfigHandler.getConfig(this.entryType).general.favoriteMeals;
        List<String> mealsAsStrings = new ArrayList<>();
        if (mealsList != null) {
            for (RiftCreatureConfig.Meal meal : mealsList) {
                mealsAsStrings.add(meal.itemId);
            }
        }
        //if not empty, make the element
        if (!mealsAsStrings.isEmpty()) {
            //label first
            RiftLibUIElement.TextElement favoriteMealsLabelElement = new RiftLibUIElement.TextElement();
            favoriteMealsLabelElement.setText(I18n.format("journal.breeding_foods"));
            favoriteMealsLabelElement.setBottomSpace(0);
            toReturn.add(favoriteMealsLabelElement);

            //table full of meals next
            RiftLibUIElement.TableContainerElement mealsTableElement = new RiftLibUIElement.TableContainerElement();
            mealsTableElement.setCellSize(16, 16);
            mealsTableElement.setRowCount(8);
            for (String mealAsString : mealsAsStrings) {
                RiftLibUIElement.ItemElement mealAsItem = new RiftLibUIElement.ItemElement();
                mealAsItem.setItemStack(RiftUtil.getItemStackFromString(mealAsString));
                mealsTableElement.addElement(mealAsItem);
            }
            toReturn.add(mealsTableElement);
        }

        //get favorite foods
        //test first for available foods
        List<RiftCreatureConfig.Food> foodList = RiftConfigHandler.getConfig(this.entryType).general.favoriteFood;
        List<String> foodAsStrings = new ArrayList<>();
        if (foodList != null) {
            for (RiftCreatureConfig.Food food : foodList) {
                foodAsStrings.add(food.itemId);
            }
        }
        //if not empty, make the element
        if (!foodAsStrings.isEmpty()) {
            //label first
            RiftLibUIElement.TextElement favoriteFoodLabelElement = new RiftLibUIElement.TextElement();
            favoriteFoodLabelElement.setText(I18n.format("journal.favorite_foods"));
            favoriteFoodLabelElement.setBottomSpace(0);
            toReturn.add(favoriteFoodLabelElement);

            //table full of foods next
            RiftLibUIElement.TableContainerElement foodTableElement = new RiftLibUIElement.TableContainerElement();
            foodTableElement.setCellSize(16, 16);
            foodTableElement.setRowCount(8);
            for (String foodAsString : foodAsStrings) {
                RiftLibUIElement.ItemElement foodAsItem = new RiftLibUIElement.ItemElement();
                foodAsItem.setItemStack(RiftUtil.getItemStackFromString(foodAsString));
                foodTableElement.addElement(foodAsItem);
            }
            toReturn.add(foodTableElement);
        }

        //get mining levels
        boolean hasMiningLevels = RiftConfigHandler.getConfig(this.entryType).general.blockBreakLevels != null && !RiftConfigHandler.getConfig(this.entryType).general.blockBreakLevels.isEmpty();
        if (hasMiningLevels) {
            //label first
            RiftLibUIElement.TextElement miningLevelsLabelElement = new RiftLibUIElement.TextElement();
            miningLevelsLabelElement.setText(I18n.format("journal.mining_levels"));
            miningLevelsLabelElement.setBottomSpace(0);
            toReturn.add(miningLevelsLabelElement);

            //mining levels
            RiftLibUIElement.TableContainerElement miningLevelsTableElement = new RiftLibUIElement.TableContainerElement();
            miningLevelsTableElement.setCellSize(16, 16);
            miningLevelsTableElement.setRowCount(8);
            List<String> miningLevels = RiftConfigHandler.getConfig(this.entryType).general.blockBreakLevels;
            for (String miningLevel : miningLevels) {
                RiftLibUIElement.ToolElement toolElement = new RiftLibUIElement.ToolElement();

                //get tool
                String tool = miningLevel.substring(0, miningLevel.indexOf(":"));
                toolElement.setToolType(tool);

                //get mining level
                int level = Integer.parseInt(miningLevel.substring(miningLevel.indexOf(":")+1));
                toolElement.setMiningLevel(level);

                miningLevelsTableElement.addElement(toolElement);
            }
            toReturn.add(miningLevelsTableElement);
        }

        return toReturn;
    }

    public void setEntryType(RiftCreatureType entryType) {
        this.entryType = entryType;
    }

    public RiftCreatureType getEntryType() {
        return this.entryType;
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
