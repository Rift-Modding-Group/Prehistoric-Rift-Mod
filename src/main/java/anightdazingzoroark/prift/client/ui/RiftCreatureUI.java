package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.api.creature.RiftCreatureEnums;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.client.ClientEnums;
import anightdazingzoroark.prift.server.dataSerializers.RiftDataSerializers;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureGuiData;
import anightdazingzoroark.prift.server.entity.creature.info.CreatureAcquisitionInfo;
import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

import java.util.Locale;
import java.util.stream.IntStream;

public class RiftCreatureUI {
    public static ModularPanel show(RiftCreatureGuiData data, PanelSyncManager syncManager, UISettings settings) {
        IRiftCreature creature = data.getCreature();

        //inventory
        RiftLibInventoryHandler creatureInventory = creature.getCreatureInventory();
        int inventorySize = creatureInventory.getSlots();
        int inventoryColumns = Math.min(9, inventorySize);
        int inventoryRows = (inventorySize + inventoryColumns - 1) / inventoryColumns;
        int playerInventoryTop = 39 + inventoryRows * 18;
        syncManager.registerSlotGroup("creature_inventory", inventoryColumns);
        IntStream.range(0, inventorySize).forEach(index ->
                syncManager.itemSlot(
                        "creature_inventory", index,
                        new ModularSlot(creatureInventory, index).slotGroup("creature_inventory")
                )
        );
        syncManager.bindPlayerInventory(data.getPlayer());

        //settings options
        syncManager.syncValue("tame_targeting", new EnumSyncValue<>(
                RiftCreatureEnums.TameTargeting.class,
                creature::getTameTargeting,
                creature::setTameTargeting
        ));
        syncManager.syncValue("custom_name", new StringSyncValue(
                creature::getCustomNameTag,
                creature::setCustomNameTag
        ));
        syncManager.syncValue("eat_from_inventory", new BooleanSyncValue(
                creature::getEatFromInventory,
                creature::setEatFromInventory
        ));

        //acquisition info
        GenericSyncValue<CreatureAcquisitionInfo> acquisitionInfoValue = GenericSyncValue.builder(CreatureAcquisitionInfo.class)
                .getter(creature::getAcquisitionInfo)
                .serializer(RiftDataSerializers.ACQUISITION_INFO::write)
                .deserializer(RiftDataSerializers.ACQUISITION_INFO::read)
                .equals((first, second) -> {
                    return first.acquisitionMethod() == second.acquisitionMethod() && first.acquisitionTime() == second.acquisitionTime();
                })
                .copyImmutable().build();
        syncManager.syncValue("acquisition_info", acquisitionInfoValue);

        //for stat table
        int statsNameColumnWidth = 72;
        int statsValueColumnWidth = 63;
        int statsIvColumnWidth = 27;

        //final return value
        PagedWidget.Controller tabController = new PagedWidget.Controller();
        return ModularPanel.defaultPanel("rift_creature", 176, inventoryRows * 18 + 122)
                .child(Flow.row().coverChildrenHeight().topRel(0f, 4, 1f).widthRel(1f)
                        .child(new PageButton(0, tabController)
                                .tab(GuiTextures.TAB_TOP, -1)
                                .overlay(new ItemDrawable(Blocks.CHEST).asIcon())
                                .addTooltipLine(IKey.lang("gui.prift.creature_inventory")))
                        .child(new PageButton(1, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(GuiTextures.GEAR.asIcon())
                                .addTooltipLine(IKey.lang("gui.prift.creature_settings")))
                        .child(new PageButton(2, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(GuiTextures.GRAPH.asIcon())
                                .addTooltipLine(IKey.lang("gui.prift.creature_summary")))
                        .child(new PageButton(3, tabController)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(Items.IRON_SWORD).asIcon())
                                .addTooltipLine(IKey.lang("gui.prift.creature_moves")))
                )
                .child(new PagedWidget<>()
                        .sizeRel(1f)
                        .controller(tabController)
                        //page 1: inventory
                        .addPage(Flow.column()
                                .sizeRel(1f)
                                .child(IKey.lang("gui.prift.creature_inventory").asWidget().pos(7, 8))
                                .child(SlotGroupWidget.builder()
                                        .matrix(IntStream.range(0, inventoryRows)
                                                .mapToObj(row -> "I".repeat(Math.min(inventoryColumns, inventorySize - row * inventoryColumns)))
                                                .toArray(String[]::new))
                                        .key('I', index -> new ItemSlot().syncHandler("creature_inventory", index))
                                        .build().top(20).horizontalCenter()
                                )
                                .child(IKey.lang("gui.prift.player_inventory").asWidget().pos(7, playerInventoryTop - 12))
                                .child(SlotGroupWidget.playerInventory(false).pos(7, playerInventoryTop))
                        )
                        //page 2: settings
                        .addPage(Flow.column()
                                .widthRel(1f)
                                .padding(7, 8)
                                .crossAxisAlignment(Alignment.CrossAxis.START)
                                .childPadding(2)
                                .child(IKey.lang("gui.prift.creature_settings").asWidget())
                                .child(new Grid()
                                        .coverChildren()
                                        .minRowHeight(20)
                                        .alignment(Alignment.CenterLeft)
                                        .row(
                                                IKey.lang("gui.prift.tame_targeting").asWidget()
                                                        .size(82, 20)
                                                        .padding(1),
                                                new CycleButtonWidget().syncHandler("tame_targeting").size(80, 20)
                                                        .stateOverlay(
                                                                RiftCreatureEnums.TameTargeting.ASSIST,
                                                                IKey.str(RiftCreatureEnums.TameTargeting.ASSIST.getTranslatedName())
                                                        )
                                                        .stateOverlay(
                                                                RiftCreatureEnums.TameTargeting.DEFENSIVE,
                                                                IKey.str(RiftCreatureEnums.TameTargeting.DEFENSIVE.getTranslatedName())
                                                        )
                                                        .stateOverlay(
                                                                RiftCreatureEnums.TameTargeting.AGGRESSIVE,
                                                                IKey.str(RiftCreatureEnums.TameTargeting.AGGRESSIVE.getTranslatedName())
                                                        )
                                                        .stateOverlay(
                                                                RiftCreatureEnums.TameTargeting.PASSIVE,
                                                                IKey.str(RiftCreatureEnums.TameTargeting.PASSIVE.getTranslatedName())
                                                        )
                                                        .addTooltip(
                                                                RiftCreatureEnums.TameTargeting.ASSIST.ordinal(),
                                                                IKey.str(RiftCreatureEnums.TameTargeting.ASSIST.getDescription())
                                                        )
                                                        .addTooltip(
                                                                RiftCreatureEnums.TameTargeting.DEFENSIVE.ordinal(),
                                                                IKey.str(RiftCreatureEnums.TameTargeting.DEFENSIVE.getDescription())
                                                        )
                                                        .addTooltip(
                                                                RiftCreatureEnums.TameTargeting.AGGRESSIVE.ordinal(),
                                                                IKey.str(RiftCreatureEnums.TameTargeting.AGGRESSIVE.getDescription())
                                                        )
                                                        .addTooltip(
                                                                RiftCreatureEnums.TameTargeting.PASSIVE.ordinal(),
                                                                IKey.str(RiftCreatureEnums.TameTargeting.PASSIVE.getDescription())
                                                        )
                                        )
                                        .row(
                                                IKey.lang("gui.prift.nickname").asWidget()
                                                        .size(82, 20)
                                                        .padding(1),
                                                new TextFieldWidget()
                                                        .syncHandler("custom_name")
                                                        .setMaxLength(20)
                                                        .hintText(creature.getCreatureType().getLocalizedName())
                                                        .size(80, 20)
                                        )
                                        .row(
                                                IKey.lang("gui.prift.eat_from_inventory").asWidget()
                                                        .size(82, 20)
                                                        .padding(1),
                                                new CycleButtonWidget().syncHandler("eat_from_inventory").size(80, 20)
                                                        .stateOverlay(false, IKey.lang("gui.prift.no"))
                                                        .stateOverlay(true, IKey.lang("gui.prift.yes"))
                                                        .addTooltip(0, IKey.lang("gui.prift.eat_from_inventory_false"))
                                                        .addTooltip(1, IKey.lang("gui.prift.eat_from_inventory_true"))
                                        )
                                )
                        )
                        //page 3: summary
                        .addPage(Flow.column()
                                .widthRel(1f)
                                .padding(7, 8)
                                .crossAxisAlignment(Alignment.CrossAxis.START)
                                .childPadding(1)
                                .child(IKey.lang("gui.prift.creature_summary").asWidget())
                                .child(new Grid().coverChildren().minRowHeight(12).alignment(Alignment.CenterLeft)
                                        .background((context, x, y, width, height, widgetTheme) -> {
                                            GuiDraw.drawRect(x, y, width, 1, 0xFF555555);
                                            IntStream.rangeClosed(1, RiftCreatureEnums.Stats.values().length).forEach(row ->
                                                    GuiDraw.drawRect(x, y + row * 12, width, 1, 0xFF555555)
                                            );
                                            GuiDraw.drawRect(x, y + height - 1, width, 1, 0xFF555555);
                                            GuiDraw.drawRect(x, y, 1, height, 0xFF555555);
                                            GuiDraw.drawRect(x + statsNameColumnWidth, y, 1, height, 0xFF555555);
                                            GuiDraw.drawRect(x + statsNameColumnWidth + statsValueColumnWidth, y, 1, height, 0xFF555555);
                                            GuiDraw.drawRect(x + width - 1, y, 1, height, 0xFF555555);
                                        })
                                        .mapTo(3, (RiftCreatureEnums.Stats.values().length + 1) * 3, index -> {
                                            int row = index / 3;
                                            int column = index % 3;
                                            int columnWidth = switch (column) {
                                                case 0 -> statsNameColumnWidth;
                                                case 1 -> statsValueColumnWidth;
                                                default -> statsIvColumnWidth;
                                            };

                                            TextWidget<?> cell;
                                            //header
                                            if (row == 0) {
                                                cell = switch (column) {
                                                    case 0 -> IKey.lang("gui.prift.creature_summary.stats").asWidget();
                                                    case 1 -> IKey.lang("gui.prift.creature_summary.value").asWidget();
                                                    default -> IKey.lang("gui.prift.creature_summary.iv").asWidget();
                                                };

                                                //explain what IVs are
                                                if (column == 2) cell.addTooltipLine(IKey.lang("gui.prift.creature_summary.iv.description"));
                                            }
                                            //stats
                                            else {
                                                RiftCreatureEnums.Stats stat = RiftCreatureEnums.Stats.values()[row - 1];
                                                cell = switch (column) {
                                                    //stat name
                                                    case 0 -> IKey.lang(stat.getTranslatedName()).asWidget();
                                                    //stat value
                                                    case 1 -> IKey.dynamic(() -> creature.getDisplayString(stat)).asWidget();
                                                    //ivs
                                                    default -> IKey.str(Long.toString(Math.round(creature.getCreatureStats().getIndividualValueForStat(stat)))).asWidget();
                                                };
                                                RiftCreatureEnums.Nature nature = creature.getNature();
                                                if (column == 1 && nature != null && !nature.isNeutral()) {
                                                    if (stat == nature.getStatToBoost()) cell.style(IKey.BLUE);
                                                    else if (stat == nature.getStatToWeaken()) cell.style(IKey.RED);
                                                }

                                                //stat description when hovering over stat row
                                                if (column == 0) cell.addTooltipLine(stat.getDescription());

                                                cell.scale(0.75f);
                                            }

                                            cell.size(columnWidth, 12).padding(3);
                                            if (column > 0) cell.textAlign(Alignment.CenterRight);
                                            return cell;
                                        })
                                )
                                .child(new Rectangle().color(0xFF555555).asWidget()
                                        .width(statsNameColumnWidth + statsValueColumnWidth + statsIvColumnWidth)
                                        .height(1).marginTop(3).marginBottom(4)
                                )
                                .child(IKey.lang("gui.prift.creature_summary.species", creature.getCreatureType().getLocalizedName()).scale(0.8f)
                                        .asWidget().textAlign(Alignment.CenterLeft).widthRel(1f)
                                )
                                .child(IKey.lang("gui.prift.creature_summary.level", creature.getLevel()).scale(0.8f).asWidget().textAlign(Alignment.CenterLeft).widthRel(1f))
                                .child(IKey.lang("gui.prift.creature_summary.nature",
                                                creature.getNature() == null ? I18n.format("gui.prift.unknown") : creature.getNature().getTranslatedName()
                                        )
                                        .scale(0.8f).asWidget().textAlign(Alignment.CenterLeft)
                                        .tooltipDynamic(tooltip -> {
                                            tooltip.addLine(IKey.lang("gui.prift.creature_summary.nature.tooltip"));

                                            RiftCreatureEnums.Nature nature = creature.getNature();
                                            if (nature != null && !nature.isNeutral()) {
                                                tooltip.addLine(IKey.str("+ "+nature.getStatToBoost().getTranslatedName()).style(IKey.BLUE));
                                                tooltip.addLine(IKey.str("- "+nature.getStatToWeaken().getTranslatedName()).style(IKey.RED));
                                            }
                                        })
                                )
                                .child(IKey.lang("gui.prift.creature_summary.uuid", creature.getUniqueID()).scale(0.8f).asWidget()
                                        .textAlign(Alignment.CenterLeft).widthRel(1f)
                                )
                                .child(IKey.dynamic(() -> acquisitionInfoValue.getValue().acquisitionInfoString())
                                        .scale(0.8f).asWidget().textAlign(Alignment.CenterLeft).widthRel(1f)
                                )
                        )
                        //page 4: move information
                        .addPage(Flow.column()
                                .sizeRel(1f)
                                .padding(7, 8)
                                .crossAxisAlignment(Alignment.CrossAxis.START)
                                .childPadding(2)
                                .child(IKey.lang("gui.prift.creature_moves").asWidget())
                                .child(new ListWidget<>()
                                        .widthRel(1f)
                                        .expanded()
                                        .crossAxisAlignment(Alignment.CrossAxis.START)
                                        .children(creature.getCreatureMoves().getUsableMoves(), moveEntry -> {
                                            float staminaConsumption = Math.max(
                                                    moveEntry.getValue().getStaminaCost(),
                                                    moveEntry.getValue().getStaminaDrainPerSecond()
                                            );
                                            return Flow.column()
                                                    .widthRel(1f)
                                                    .coverChildrenHeight()
                                                    .padding(2, 1)
                                                    .crossAxisAlignment(Alignment.CrossAxis.START)
                                                    .child(IKey.lang("move.creature." + moveEntry.getKey()).scale(0.8f).asWidget())
                                                    .child(Flow.row()
                                                            .widthRel(1f)
                                                            .coverChildrenHeight()
                                                            .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                                            .child(IKey.lang(
                                                                    "gui.prift.creature_moves.type",
                                                                    I18n.format("move.creature.type." + moveEntry.getValue().getMoveType().name().toLowerCase(Locale.ROOT))
                                                            ).scale(0.65f).asWidget())
                                                            .child(IKey.lang(
                                                                    "gui.prift.creature_moves.base_power",
                                                                    moveEntry.getValue().getMoveType() == CreatureMoveBuilder.MoveType.STATUS
                                                                            ? I18n.format("gui.prift.not_applicable")
                                                                            : Integer.toString(moveEntry.getValue().getBasePower())
                                                            ).scale(0.65f).asWidget())
                                                    )
                                                    .child(IKey.lang(
                                                            "gui.prift.creature_moves.stamina",
                                                            ClientEnums.StaminaUse.getUse(staminaConsumption).getTranslatedName()
                                                    ).scale(0.65f).asWidget())
                                                    .child(IKey.lang("move.creature." + moveEntry.getKey() + ".description")
                                                            .scale(0.65f).asWidget().widthRel(1f).textAlign(Alignment.CenterLeft))
                                                    .child(new Rectangle().color(0xFF555555).asWidget()
                                                            .widthRel(1f).height(1).marginTop(1));
                                        })
                                )
                        )
                );
    }
}
