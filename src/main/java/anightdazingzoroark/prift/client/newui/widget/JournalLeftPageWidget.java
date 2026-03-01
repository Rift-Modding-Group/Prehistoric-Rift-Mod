package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.RiftUIIcons;
import anightdazingzoroark.prift.client.newui.value.NullableEnumValue;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressProperties;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JournalLeftPageWidget extends ParentWidget<JournalLeftPageWidget> {
    private final JournalProgressProperties journalProgress;
    private final NullableEnumValue.Dynamic<RiftCreatureType> currentCreature;

    private RiftCreatureType.CreatureCategory currentCategory;
    private boolean markForWidgetUpdate = true;

    public JournalLeftPageWidget(JournalProgressProperties journalProgress, NullableEnumValue.Dynamic<RiftCreatureType> currentCreature) {
        this.journalProgress = journalProgress;
        this.currentCreature = currentCreature;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.markForWidgetUpdate) return;

        //main index
        if (this.currentCreature.getValue() == null && this.currentCategory == null) {
            this.removeAll();
            this.child(this.indexPage());
        }
        else if (this.currentCreature.getValue() == null && this.currentCategory != null) {
            this.removeAll();
            this.child(this.categoryPage());
        }
        this.markForWidgetUpdate = false;
    }

    private ParentWidget<?> headerSection() {
        return new ParentWidget<>().widthRel(1f)
                .child(IKey.dynamic(() -> {
                    if (currentCategory == null) return "Index";
                    return currentCategory.getTranslatedName(true);
                }).asWidget().left(0))
                .child(new Row().coverChildren().childPadding(2)
                        .childIf(this.currentCategory != null,
                                () -> new ButtonWidget<>().overlay(RiftUIIcons.BACK).size(12)
                                        .onMousePressed(button -> {
                                            this.currentCategory = null;
                                            this.currentCreature.setValue(null);
                                            this.updatePages();
                                            return true;
                                        })
                        )
                        .child(new ButtonWidget<>().overlay(GuiTextures.SEARCH).size(12))
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
                        this.currentCategory = creatureCategory;
                        this.updatePages();
                        return true;
                    })
            );
        }

        return toReturn;
    }

    private Flow categoryPage() {
        Flow toReturn = new Column().sizeRel(1f).top(0).childPadding(5)
                .child(this.headerSection());

        //children
        Map<RiftCreatureType, Boolean> creatureTypeMap = this.journalProgress.getEncounteredCreatures();
        List<RiftCreatureType> creatureTypeList = this.orderedCreatureTypes();

        for (RiftCreatureType creatureType : creatureTypeList) {
            if (this.currentCategory != RiftCreatureType.CreatureCategory.ALL && creatureType.getCreatureCategory() != this.currentCategory) continue;

            String buttonName = creatureType.getTranslatedName();
            buttonName = creatureTypeMap.get(creatureType) ? "("+buttonName+")" : buttonName;

            toReturn.child(new ButtonWidget<>().height(20).widthRel(0.75f)
                    .overlay(IKey.str(buttonName))
                    .onMousePressed(button -> {
                        this.currentCreature.setValue(creatureType);
                        this.updatePages();
                        return true;
                    })
            );
        }

        return toReturn;
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

    private void updatePages() {
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
}
