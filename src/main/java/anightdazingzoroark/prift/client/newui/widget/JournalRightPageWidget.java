package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.RiftUIIcons;
import anightdazingzoroark.prift.client.newui.value.NullableEnumValue;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.properties.journalProgress.JournalProgressProperties;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.layout.Row;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JournalRightPageWidget extends ParentWidget<JournalRightPageWidget> {
    @NotNull
    private final JournalProgressProperties journalProgress;
    private final NullableEnumValue.Dynamic<RiftCreatureType> currentCreature;

    private boolean markForWidgetUpdate = true;

    public JournalRightPageWidget(@NotNull JournalProgressProperties journalProgress, NullableEnumValue.Dynamic<RiftCreatureType> currentCreature) {
        this.journalProgress = journalProgress;
        this.currentCreature = currentCreature;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.markForWidgetUpdate) return;

        this.removeAll();
        if (this.currentCreature.getValue() != null) this.child(this.creaturePageContents());
        else this.child(this.noCreaturePageContents());
        this.markForWidgetUpdate = false;
    }

    //todo: maybe add statistics on how many creatures unlocked? idk
    private Flow noCreaturePageContents() {
        return Flow.column().sizeRel(1f).childPadding(5)
                .child(IKey.str("The journal is where you get to read about creatures you have encountered.").asWidget()
                        .scale(0.75f).left(0));
    }

    private Flow creaturePageContents() {
        PagedWidget.Controller tabController = new PagedWidget.Controller();

        PagedWidget<?> pagedWidget = new PagedWidget<>().controller(tabController).widthRel(1f).height(93)
                .background(new Rectangle().hollow().color(0xFF000000))
                //creature description
                .addPage(this.creatureDescriptionTab());
        //creature info, only if the creature can have it
        if (this.canHaveInfoTab()) pagedWidget.addPage(this.creatureInfoTab());

        return Flow.column().sizeRel(1f).childPadding(5)
                .child(IKey.str(this.currentCreature.getValue().getTranslatedName()).asWidget().left(0))
                .child(RiftUIIcons.creatureIllustration(this.currentCreature.getValue()).asWidget().size(120, 90))
                .child(Flow.row().childPadding(3).coverChildren().left(0)
                        .child(new JournalTabButton(0, tabController)
                                .overlay(IKey.lang("journal.tab.description"))
                                .background(false, new Rectangle().hollow().color(0xFF000000))
                                .background(true, new Rectangle().hollow().color(0xFFFFFFFF))
                        )
                        .childIf(this.canHaveInfoTab(), () -> new JournalTabButton(1, tabController)
                                .overlay(IKey.lang("journal.tab.info"))
                                .background(false, new Rectangle().hollow().color(0xFF000000))
                                .background(true, new Rectangle().hollow().color(0xFFFFFFFF))
                        )
                )
                .child(pagedWidget);
    }

    private ListWidget<?, ?> creatureDescriptionTab() {
        if (this.currentCreature.getValue() == null) return new ListWidget<>().size(181, 85).align(Alignment.CENTER);

        return new ListWidget<>().size(181, 85).align(Alignment.CENTER)
                .child(Flow.column().widthRel(1f).coverChildrenHeight().childPadding(3)
                        .childIf(!this.journalProgress.getEncounteredCreatures().get(this.currentCreature.getValue()),
                                () -> IKey.lang("journal.entry.locked").scale(0.75f)
                                        .alignment(Alignment.CenterLeft).asWidget().left(0)
                        )
                        .childIf(this.journalProgress.getEncounteredCreatures().get(this.currentCreature.getValue())
                                && !this.currentCreature.getValue().listBehaviorsAsString().isEmpty(),
                                () -> IKey.lang("journal.description.behaviors", this.currentCreature.getValue().listBehaviorsAsString())
                                .scale(0.75f).alignment(Alignment.CenterLeft).asWidget().left(0)
                        )
                        .childIf(this.journalProgress.getEncounteredCreatures().get(this.currentCreature.getValue()),
                                () -> IKey.lang("journal.description.diet", this.currentCreature.getValue().getCreatureDiet().getTranslatedName())
                                .scale(0.75f).alignment(Alignment.CenterLeft).asWidget().left(0)
                        )
                        .childIf(this.journalProgress.getEncounteredCreatures().get(this.currentCreature.getValue()),
                                () -> IKey.str(this.currentCreature.getValue().getJournalEntry())
                                .scale(0.75f).alignment(Alignment.CenterLeft).asWidget().left(0)
                        )
                );
    }

    private ListWidget<?, ?> creatureInfoTab() {
        if (this.currentCreature.getValue() == null) return new ListWidget<>().size(181, 85).align(Alignment.CENTER);

        //list down block break rates
        List<String> listBlockBreakLevels = RiftConfigHandler.getConfig(this.currentCreature.getValue()).general.blockBreakLevels;
        List<ImmutablePair<String, Integer>> listBlockBreak = listBlockBreakLevels.stream().map(
                blockBreakLevel -> {
                    //separate by colon
                    String[] splitString = blockBreakLevel.split(":");
                    String toolNameString = splitString[0];
                    String toolLevelString = splitString[1];

                    //get tool level
                    int toolLevel = 1;
                    try {
                        toolLevel = Integer.parseInt(toolLevelString);
                    }
                    catch (Exception e) {}

                    return new ImmutablePair<>(toolNameString, toolLevel);
                }
        ).collect(Collectors.toList());

        //list down taming foods
        List<RiftCreatureConfig.Meal> listMeals = RiftConfigHandler.getConfig(this.currentCreature.getValue()).general.favoriteMeals;
        List<ItemStack> listMealsItemStack = listMeals != null ? listMeals.stream().map(
                meal -> RiftUtil.getItemStackFromString(meal.itemId)
        ).collect(Collectors.toList()) : Collections.emptyList();

        //list down favorite foods
        List<RiftCreatureConfig.Food> listFoods = RiftConfigHandler.getConfig(this.currentCreature.getValue()).general.favoriteFood;
        List<ItemStack> listFoodsItemStack = listFoods != null ? listFoods.stream().map(
                food -> RiftUtil.getItemStackFromString(food.itemId)
        ).collect(Collectors.toList()) : Collections.emptyList();

        return new ListWidget<>().size(181, 85).align(Alignment.CENTER)
                .child(Flow.column().widthRel(1f).coverChildrenHeight().childPadding(3)
                        //message for if the creature must be tamed by killing and hoping an egg drops
                        .childIf(!this.journalProgress.getEncounteredCreatures().get(this.currentCreature.getValue())
                                        && this.currentCreature.getValue().isTameable
                                        && !this.currentCreature.getValue().isTameableByFeeding,
                                () -> IKey.lang("journal.must_kill_for_egg").scale(0.75f).color(0xFFFF0000).asWidget()
                                        .alignment(Alignment.CenterLeft).left(0)
                        )
                        //block break rates
                        .childIf(!listBlockBreak.isEmpty(), () -> Flow.column().coverChildrenHeight().widthRel(1f)
                                .child(IKey.lang("journal.mining_levels").scale(0.75f).asWidget().alignment(Alignment.CenterLeft).left(0))
                                .child(new PaddedGrid().coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                8, listBlockBreak.size(),
                                                index -> new ToolDisplay().setForMiningLevel(listBlockBreak.get(index).left, listBlockBreak.get(index).right)
                                        ))
                                        .padding(3).alignment(Alignment.CenterLeft).left(0)
                                )
                        )
                        //list of foods it must consume to be tamed or bred
                        .childIf(!listMealsItemStack.isEmpty(), () -> Flow.column().coverChildrenHeight().widthRel(1f)
                                //for if the creature can be tamed by being fed
                                .childIf(this.currentCreature.getValue().isTameableByFeeding,
                                        () -> IKey.lang("journal.taming_or_breeding_foods").scale(0.75f).asWidget().alignment(Alignment.CenterLeft).left(0)
                                )
                                //for otherwise
                                .childIf(!this.currentCreature.getValue().isTameableByFeeding,
                                        () -> IKey.lang("journal.breeding_foods").scale(0.75f).asWidget().alignment(Alignment.CenterLeft).left(0)
                                )
                                .child(new PaddedGrid().coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                8, listMealsItemStack.size(),
                                                index -> new ItemDisplayOpenToJEIWidget().item(listMealsItemStack.get(index))
                                        ))
                                        .padding(3).alignment(Alignment.CenterLeft).left(0)
                                )
                        )
                        //list of foods that it can be fed to be healed
                        .childIf(!listFoodsItemStack.isEmpty(), () -> Flow.column().coverChildrenHeight().widthRel(1f)
                                .child(IKey.lang("journal.favorite_foods").scale(0.75f).asWidget().alignment(Alignment.CenterLeft).left(0))
                                .child(new PaddedGrid().coverChildren()
                                        .matrix(Grid.mapToMatrix(
                                                8, listFoodsItemStack.size(),
                                                index -> new ItemDisplayOpenToJEIWidget().item(listFoodsItemStack.get(index))
                                        ))
                                        .padding(3).alignment(Alignment.CenterLeft).left(0)
                                )
                        )
                );
    }

    private boolean canHaveInfoTab() {
        if (this.currentCreature.getValue() == null) return false;

        List<String> listBlockBreakLevels = RiftConfigHandler.getConfig(this.currentCreature.getValue()).general.blockBreakLevels;
        List<RiftCreatureConfig.Meal> listMeals = RiftConfigHandler.getConfig(this.currentCreature.getValue()).general.favoriteMeals;
        List<RiftCreatureConfig.Food> listFoods = RiftConfigHandler.getConfig(this.currentCreature.getValue()).general.favoriteFood;

        return (listBlockBreakLevels != null && !listBlockBreakLevels.isEmpty())
                || (listMeals != null && !listMeals.isEmpty())
                || (listFoods != null && !listFoods.isEmpty());
    }

    public void updatePage() {
        this.markForWidgetUpdate = true;
    }

    private static class JournalTabButton extends PageButton {
        public JournalTabButton(int index, PagedWidget.Controller controller) {
            super(index, controller);
        }

        @Override
        public PageButton overlay(IDrawable... drawable) {
            //get longest drawable and use that info to set this button's width
            int longestWidth = 0;
            for (IDrawable iDrawable : drawable) {
                if (!(iDrawable instanceof LangKey langKey)) continue;
                int currentWidth = langKey.getDefaultWidth();
                if (currentWidth > longestWidth) longestWidth = currentWidth;
            }
            this.width(longestWidth + 6);

            return super.overlay(drawable);
        }
    }

    private static class ToolDisplay extends ItemDisplayWidget {
        private int miningLevel = -1;

        @Override
        protected WidgetThemeEntry<?> getWidgetThemeInternal(ITheme theme) {
            return theme.getFallback();
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            if (this.getIngredient() == null || !(this.getIngredient() instanceof ItemStack itemStack)) return;
            if (!Platform.isStackEmpty(itemStack)) {
                GuiDraw.drawItem(itemStack, 1, 1, 16, 16, context.getCurrentDrawingZ());
                IKey.str(String.valueOf(this.miningLevel)).color(0xFFFFFFFF)
                        .drawAligned(
                                context,
                                0, 0,
                                this.getArea().width, this.getArea().height,
                                widgetTheme.getTheme(), Alignment.BottomRight
                        );
            }
        }

        public ToolDisplay setForMiningLevel(String toolName, int miningLevel) {
            //set tool
            ItemStack tool = ItemStack.EMPTY;
            switch (toolName) {
                case "pickaxe": {
                    tool = new ItemStack(Items.STONE_PICKAXE);
                    break;
                }
                case "axe": {
                    tool = new ItemStack(Items.STONE_AXE);
                    break;
                }
                case "shovel": {
                    tool = new ItemStack(Items.STONE_SHOVEL);
                    break;
                }
            }
            this.item(tool);

            //set mining level
            this.miningLevel = miningLevel;

            //set hover text
            this.addTooltipLine(IKey.lang("journal.mining_info", toolName, String.valueOf(miningLevel)));

            return this;
        }
    }
}
