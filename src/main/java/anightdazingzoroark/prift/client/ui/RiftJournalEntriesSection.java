package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiScrollableSectionContents;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RiftJournalEntriesSection extends RiftGuiScrollableSection {
    private RiftCreatureType.CreatureCategory currentCategory;

    public RiftJournalEntriesSection(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(100, 195, guiWidth, guiHeight, -149, 7, fontRenderer, minecraft);
        this.scrollbarXOffset = 3;
    }

    public void setCurrentCategory(RiftCreatureType.CreatureCategory currentCategory) {
        this.currentCategory = currentCategory;
    }

    public RiftCreatureType.CreatureCategory getCurrentCategory() {
        return this.currentCategory;
    }

    @Override
    public RiftGuiScrollableSectionContents defineSectionContents() {
        RiftGuiScrollableSectionContents toReturn = new RiftGuiScrollableSectionContents();

        //for categories unlocked by the player
        if (this.currentCategory == null) {
            for (int x = 0; x < PlayerJournalProgressHelper.getUnlockedCategories(this.minecraft.player).size(); x++) {
                toReturn.addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                        .setName(PlayerJournalProgressHelper.getUnlockedCategories(this.minecraft.player).get(x).getTranslatedName(true))
                        .setId(PlayerJournalProgressHelper.getUnlockedCategories(this.minecraft.player).get(x).toString())
                        .setSize(96, 20)
                        .setBottomSpaceSize(5)
                );
            }
        }
        //for creatures within category unlocked by the player
        else {
            List<RiftCreatureType> creatureTypeList = PlayerJournalProgressHelper.getUnlockedCreatures(this.minecraft.player)
                    .keySet().stream().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
            Map<RiftCreatureType, Boolean> encounteredMap = PlayerJournalProgressHelper.getUnlockedEntriesFromCategory(this.minecraft.player, this.currentCategory);

            //add a back button
            toReturn.addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                    .setName(I18n.format("type.creature.back"))
                    .setId("NULL")
                    .setSize(96, 20)
                    .setBottomSpaceSize(5)
            );

            //add the other elements
            for (RiftCreatureType creatureType : creatureTypeList) {
                if (encounteredMap.get(creatureType) != null) {
                    String name = encounteredMap.get(creatureType) ? creatureType.getTranslatedName() : "("+creatureType.getTranslatedName()+")";

                    toReturn.addButtonElement(new RiftGuiScrollableSectionContents.ButtonElement()
                            .setName(name)
                            .setId(creatureType.toString())
                            .setSize(96, 20)
                            .setBottomSpaceSize(5)
                    );
                }
            }
        }

        return toReturn;
    }
}
