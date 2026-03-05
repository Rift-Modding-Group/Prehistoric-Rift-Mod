package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.RiftUIIcons;
import anightdazingzoroark.prift.client.newui.value.NullableEnumValue;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressProperties;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.layout.Row;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JournalLeftPageWidget extends ParentWidget<JournalLeftPageWidget> {
    @NotNull
    private final JournalProgressProperties journalProgress;
    private final NullableEnumValue.Dynamic<RiftCreatureType.CreatureCategory> currentCategory;
    private final NullableEnumValue.Dynamic<RiftCreatureType> currentCreature;

    private boolean markForWidgetUpdate = true;

    public JournalLeftPageWidget(@NotNull JournalProgressProperties journalProgress, NullableEnumValue.Dynamic<RiftCreatureType.CreatureCategory> currentCategory, NullableEnumValue.Dynamic<RiftCreatureType> currentCreature) {
        this.journalProgress = journalProgress;
        this.currentCategory = currentCategory;
        this.currentCreature = currentCreature;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.markForWidgetUpdate) return;

        //main index
        if (this.currentCreature.getValue() == null && this.currentCategory.getValue() == null) {
            this.removeAll();
            this.child(this.indexPage());
        }
        else if (this.currentCreature.getValue() == null && this.currentCategory.getValue() != null) {
            this.removeAll();
            this.child(this.categoryPage());
        }
        else if (this.currentCreature.getValue() != null && this.currentCategory.getValue() == null) {
            this.removeAll();
            this.child(this.indexPage());
        }
        this.markForWidgetUpdate = false;
    }

    private ParentWidget<?> headerSection() {
        return new ParentWidget<>().widthRel(1f)
                .child(IKey.dynamic(() -> {
                    if (currentCategory.getValue() == null) return I18n.format("journal.index");
                    return currentCategory.getValue().getTranslatedName(true);
                }).asWidget().left(0))
                .child(new Row().coverChildren().childPadding(2)
                        .childIf(this.currentCategory.getValue() != null,
                                () -> new ButtonWidget<>().overlay(RiftUIIcons.BACK).size(12)
                                        .addTooltipElement(IKey.lang("journal.back_to_index_tooltip"))
                                        .onMousePressed(button -> {
                                            this.currentCategory.setValue(null);;
                                            this.currentCreature.setValue(null);
                                            this.updatePages();
                                            return true;
                                        })
                        )
                        .child(new ButtonWidget<>().overlay(GuiTextures.SEARCH).size(12)
                                .addTooltipElement(IKey.lang("journal.search_tooltip"))
                        )
                        .right(0)
                );
    }

    private Flow indexPage() {
        Flow toReturn = new Column().sizeRel(1f).top(0).childPadding(5)
                .child(this.headerSection());

        //children
        List<RiftCreatureType.CreatureCategory> categoryList = this.journalProgress.getUnlockedCategories();
        for (RiftCreatureType.CreatureCategory creatureCategory : categoryList) {
            toReturn.child(new ButtonWidget<>().height(20).widthRel(0.75f)
                    .overlay(IKey.str(creatureCategory.getTranslatedName(true)))
                    .onMousePressed(button -> {
                        this.currentCategory.setValue(creatureCategory);
                        this.updatePages();
                        return true;
                    })
            );
        }

        return toReturn;
    }

    private Flow categoryPage() {
        List<RiftCreatureType> creatureTypeList = this.orderedCreatureTypes()
                .stream().filter(creatureType -> {
                    if (currentCategory.getValue() == RiftCreatureType.CreatureCategory.ALL) return true;
                    else return creatureType.getCreatureCategory() == currentCategory.getValue();
                }).collect(Collectors.toList());

        return new Column().sizeRel(1f).top(0).childPadding(5)
                .child(this.headerSection())
                .child(new PaddedGrid().coverChildren()
                        .matrix(Grid.mapToMatrix(
                                5, creatureTypeList.size(),
                                index -> new CreatureButton(creatureTypeList.get(index), this)
                        ))
                        .padding(4)
                );
    }

    private List<RiftCreatureType> orderedCreatureTypes() {
        if (this.journalProgress == null) return new ArrayList<>();

        //get map
        Map<RiftCreatureType, Boolean> creatureTypeMap = this.journalProgress.getEncounteredCreatures();

        //get ordered string list
        return new ArrayList<>(creatureTypeMap.keySet())
                .stream().sorted(Comparator.comparing(RiftCreatureType::name))
                .collect(Collectors.toList());
    }

    public void updatePages() {
        //update this widget
        this.markForWidgetUpdate = true;

        //update right page widget
        if (!this.getParent().isValid()) return;
        for (IWidget children : this.getParent().getChildren()) {
            if (children instanceof JournalRightPageWidget rightPageWidget) {
                rightPageWidget.updatePage();
                break;
            }
        }
    }

    public void unselectAllCreatureButtons() {
        for (IWidget child : this.getChildren()) {
            if (!(child instanceof Flow flow)) continue;
            for (IWidget flowChild : flow.getChildren()) {
                if (!(flowChild instanceof PaddedGrid paddedGrid)) continue;

                for (IWidget paddedGridChild : paddedGrid.getChildren()) {
                    if (!(paddedGridChild instanceof CreatureButton creatureButton)) continue;
                    creatureButton.isSelected = false;
                }
            }
        }
    }

    private static class CreatureButton extends ButtonWidget<CreatureButton> {
        @NotNull
        private final RiftCreatureType creatureType;
        @NotNull
        private final JournalLeftPageWidget pageParent;
        private boolean isSelected;

        public CreatureButton(@NotNull RiftCreatureType creatureType, JournalLeftPageWidget pageParent) {
            this.creatureType = creatureType;
            this.pageParent = pageParent;
            this.size(32);
            this.addTooltipElement(this.creatureType.getTranslatedName());
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());

            //outer outline
            int outerOutlineColor = this.isHovering() ? 0xFFFFFFFF : this.isSelected ? 0xFFFFFF00 :  0xFF000000;
            new Rectangle().color(outerOutlineColor).cornerRadius(5).drawAtZero(context, this.getArea(), theme);

            //inner outline
            new Rectangle().color(0xFF484848).cornerRadius(5).draw(context, 1, 1, this.getArea().w() - 2, this.getArea().h() - 2, theme);

            //set background
            new Rectangle().color(0xFF212121).cornerRadius(5).draw(context, 2, 2, this.getArea().w() - 4, this.getArea().h() - 4, theme);

            //add icon
            RiftUIIcons.creatureIcon(this.creatureType).draw(context, 4, 4, 24, 24, theme);
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            if (this.pageParent.currentCreature.getValue() == this.creatureType) return Result.ACCEPT;
            this.pageParent.currentCreature.setValue(this.creatureType);
            this.pageParent.updatePages();
            this.pageParent.unselectAllCreatureButtons();
            this.isSelected = true;
            this.playClickSound();
            return Result.SUCCESS;
        }

        @Override
        public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
            return theme.getFallback();
        }
    }
}
