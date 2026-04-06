package anightdazingzoroark.prift.client.ui.widget;

import anightdazingzoroark.prift.client.ui.RiftUIIcons;
import anightdazingzoroark.prift.client.ui.value.NullableEnumValue;
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
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
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
    private boolean searching;
    private final StringValue searchTerm = new StringValue("");
    private String oldSearchTerm;
    private final List<RiftCreatureType> searchResults = new ArrayList<>();

    public JournalLeftPageWidget(@NotNull JournalProgressProperties journalProgress, NullableEnumValue.Dynamic<RiftCreatureType.CreatureCategory> currentCategory, NullableEnumValue.Dynamic<RiftCreatureType> currentCreature) {
        this.journalProgress = journalProgress;
        this.currentCategory = currentCategory;
        this.currentCreature = currentCreature;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        //updating search related stuff
        if (this.searching && !this.searchTerm.getStringValue().equals(this.oldSearchTerm)) {
            this.searchResults.clear();
            this.searchResults.addAll(this.createSearchResults());
            this.removeAll();
            this.child(this.searchPage());
            this.oldSearchTerm = this.searchTerm.getStringValue();
        }

        //updating widget in general
        if (!this.markForWidgetUpdate) return;

        //search mode
        if (this.searching) {
            this.removeAll();
            this.child(this.searchPage());
        }
        //normal mode
        else {
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
        }
        this.markForWidgetUpdate = false;
    }

    private ParentWidget<?> headerSection() {
        return new ParentWidget<>().widthRel(1f)
                .child(IKey.dynamic(() -> {
                    if (currentCategory.getValue() == null) return I18n.format("journal.index");
                    return currentCategory.getValue().getTranslatedName(true);
                }).asWidget().left(0))
                .child(Flow.row().coverChildren().childPadding(2)
                        .childIf(this.currentCategory.getValue() != null || this.searching,
                                () -> new ButtonWidget<>().overlay(RiftUIIcons.BACK).size(12)
                                        .addTooltipElement(IKey.dynamic(
                                                () -> {
                                                    if (this.searching) return I18n.format("journal.exit_search_tooltip");
                                                    return I18n.format("journal.back_to_index_tooltip");
                                                }
                                        ))
                                        .onMousePressed(button -> {
                                            //reset search related stuff
                                            if (this.searching) {
                                                this.resetSearching();
                                                this.currentCreature.setValue(null);
                                                this.updatePages();
                                                return true;
                                            }

                                            //reset current category and creature stuff
                                            this.currentCategory.setValue(null);
                                            this.currentCreature.setValue(null);
                                            this.updatePages();
                                            return true;
                                        })
                        )
                        .childIf(!this.searching, () -> new ButtonWidget<>().overlay(GuiTextures.SEARCH).size(12)
                                .addTooltipElement(IKey.lang("journal.search_tooltip"))
                                .onMousePressed(button -> {
                                    this.searching = !this.searching;
                                    this.updatePages();
                                    return true;
                                })
                        )
                        .right(0)
                );
    }

    private Flow indexPage() {
        Flow toReturn = Flow.column().sizeRel(1f).top(0).childPadding(5)
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
                }).toList();

        return Flow.column().sizeRel(1f).top(0).childPadding(5)
                .child(this.headerSection())
                .child(new PaddedGrid().coverChildren()
                        .matrix(Grid.mapToMatrix(
                                5, creatureTypeList.size(),
                                index -> new CreatureButton(creatureTypeList.get(index), this)
                        ))
                        .padding(4)
                );
    }

    private Flow searchPage() {
        return Flow.column().sizeRel(1f).top(0).childPadding(5)
                .child(this.headerSection())
                .child(new TextFieldWidget().widthRel(0.8f).hintText(I18n.format("journal.search_hint"))
                        .value(this.searchTerm)
                )
                .child(new PaddedGrid().coverChildren()
                        .matrix(Grid.mapToMatrix(
                                5, this.searchResults.size(),
                                index -> new CreatureButton(this.searchResults.get(index), this)
                        ))
                        .padding(4)
                );
    }

    private List<RiftCreatureType> createSearchResults() {
        return this.orderedCreatureTypes()
                .stream().filter(creatureType -> {
                    String creatureTypeString = creatureType.getTranslatedName().toLowerCase();

                    boolean toReturn = this.searchTerm.getStringValue().isEmpty()
                            || creatureTypeString.contains(this.searchTerm.getStringValue());

                    if (this.currentCategory.getValue() == null
                            || this.currentCategory.getValue() == RiftCreatureType.CreatureCategory.ALL) {
                        return toReturn;
                    }
                    else return creatureType.getCreatureCategory() == this.currentCategory.getValue() && toReturn;
                }).collect(Collectors.toList());
    }

    private List<RiftCreatureType> orderedCreatureTypes() {
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
                }
            }
        }
    }

    public boolean getIsSearching() {
        return this.searching;
    }

    public void resetSearching() {
        this.searching = false;
        this.searchTerm.setStringValue("");
        this.oldSearchTerm = null;
    }

    private static class CreatureButton extends ButtonWidget<CreatureButton> {
        @NotNull
        private final RiftCreatureType creatureType;
        @NotNull
        private final JournalLeftPageWidget pageParent;

        public CreatureButton(@NotNull RiftCreatureType creatureType, JournalLeftPageWidget pageParent) {
            this.creatureType = creatureType;
            this.pageParent = pageParent;
            this.size(32);

            //for the tooltip, put in parenthesis the name of the parent
            //when its not discovered
            String creatureName = this.creatureType.getTranslatedName();
            if (!pageParent.journalProgress.getEncounteredCreatures().get(this.creatureType)) {
                creatureName = "("+creatureName+")";
            }
            this.addTooltipElement(creatureName);
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            WidgetTheme theme = this.getActiveWidgetTheme(widgetTheme, this.isHovering());

            //outer outline
            new Rectangle().color(this.outerOutlineColor()).cornerRadius(5).drawAtZero(context, this.getArea(), theme);

            //inner outline
            new Rectangle().color(0xFF484848).cornerRadius(5).draw(context, 1, 1, this.getArea().w() - 2, this.getArea().h() - 2, theme);

            //set background
            new Rectangle().color(0xFF212121).cornerRadius(5).draw(context, 2, 2, this.getArea().w() - 4, this.getArea().h() - 4, theme);

            //add icon
            RiftUIIcons.creatureIcon(this.creatureType).draw(context, 4, 4, 24, 24, theme);

            //lock icon for if the creature is encountered only and not yet truly unlocked
            if (!this.pageParent.journalProgress.getEncounteredCreatures().get(this.creatureType)) {
                GuiTextures.LOCKED.draw(context, 8, 8, 16, 16, theme);
            }
        }

        private int outerOutlineColor() {
            if (this.isHovering()) return 0xFFFFFFFF;
            else if (this.pageParent.currentCreature.getValue() == this.creatureType) return 0xFFFFFF00;
            return 0xFF000000;
        }

        @Override
        public @NotNull Result onMousePressed(int mouseButton) {
            if (this.pageParent.currentCreature.getValue() == this.creatureType) return Result.ACCEPT;
            this.pageParent.currentCreature.setValue(this.creatureType);
            this.pageParent.updatePages();
            this.pageParent.unselectAllCreatureButtons();
            this.playClickSound();
            return Result.SUCCESS;
        }

        @Override
        public WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
            return theme.getFallback();
        }
    }
}
