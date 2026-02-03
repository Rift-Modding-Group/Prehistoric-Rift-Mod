package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.client.newui.custom.DynamicPageButton;
import anightdazingzoroark.prift.client.newui.custom.DynamicPagedWidget;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.EntityGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.function.Consumer;

public class RiftCreatureScreen {
    public static ModularPanel buildCreatureUI(EntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disableRecipeViewer();
        RiftCreature creature = (RiftCreature) data.getGuiHolder();

        //tab related stuff
        DynamicPagedWidget.Controller tabController = new DynamicPagedWidget.Controller();

        return new ModularPanel(UIPanelNames.INTERACTED_CREATURE_SCREEN)
                .onUpdateListener(new Consumer<ModularPanel>() {
                    @Override
                    public void accept(ModularPanel modularPanel) {
                        DynamicPagedWidget<?> dynamicPagedWidget = this.getDynamicPagedWidget(modularPanel);
                        if (dynamicPagedWidget == null) {
                            modularPanel.width(180);
                            return;
                        }

                        //get index and change panel width depending on current page index
                        switch (dynamicPagedWidget.getCurrentPageIndex()) {
                            case 0:
                            case 1:
                                modularPanel.width(180);
                                break;
                            case 2:
                                modularPanel.width(212);
                                break;
                            case 3:
                                modularPanel.width(220);
                                break;
                        }
                    }

                    private DynamicPagedWidget<?> getDynamicPagedWidget(ModularPanel panel) {
                        List<IWidget> widgetChildren = panel.getChildren();
                        for (IWidget widget : widgetChildren) {
                            if (widget instanceof DynamicPagedWidget) return (DynamicPagedWidget<?>) widget;
                        }
                        return null;
                    }
                })
                .child(new Column()
                        .debugName("creatureScreenTabColumn")
                        .coverChildren()
                        .leftRel(0f, 4, 1f)
                        .child(new DynamicPageButton(0, tabController)
                                .overlay(new ItemDrawable(Blocks.CHEST).asIcon())
                                .addTooltipElement(IKey.lang("tametab.inventory"))
                                .tab(GuiTextures.TAB_LEFT, -1)
                        )
                        .child(new DynamicPageButton(1, tabController)
                                .overlay(GuiTextures.GEAR.asIcon().size(24))
                                .addTooltipElement(IKey.lang("tametab.manage"))
                                .tab(GuiTextures.TAB_LEFT, 0)
                        )
                        .child(new DynamicPageButton(2, tabController)
                                .overlay(GuiTextures.EXCLAMATION.asIcon().size(24))
                                .addTooltipElement(IKey.lang("tametab.info"))
                                .tab(GuiTextures.TAB_LEFT, 0)
                        )
                        .child(new DynamicPageButton(3, tabController)
                                .overlay(new ItemDrawable(Items.IRON_SWORD).asIcon())
                                .addTooltipElement(IKey.lang("tametab.moves"))
                                .tab(GuiTextures.TAB_LEFT, 0)
                        )
                )
                .child(new DynamicPagedWidget<>()
                        .debugName("pagedWidget")
                        .controller(tabController)
                        //page for creature inventory
                        .addPage(creatureInventoryPage(creature, syncManager))
                        //page for creature settings
                        .addPage(creatureSettingsPage(creature, syncManager))
                        //page for creature info
                        .addPage(creatureInfoPage(creature, syncManager, settings))
                        //page for creature moves
                        .addPage(creatureMovesPage(creature, syncManager, settings))
                        .widthRel(1f).coverChildrenHeight()
                )
                .coverChildrenHeight();
    }

    private static ParentWidget<?> creatureInventoryPage(RiftCreature creature, PanelSyncManager syncManager) {
        //set up strings
        String playerName = Minecraft.getMinecraft().player.getName();
        String creatureGearName = I18n.format("inventory.gear", creature.getName(false));
        String creatureInvName = I18n.format("inventory.inventory", creature.getName(false));

        //creature inventory syncing stuff
        syncManager.registerSlotGroup("creatureGear", 9);
        syncManager.registerSlotGroup("creatureInventory", 9);
        ItemStackHandler creatureGear = creature.creatureGear;
        ItemStackHandler creatureInventory = creature.newCreatureInventory;

        //set if the creature has gear
        boolean creatureHasGear = creature.creatureType.gearSlotCount() > 0;

        //build creature gear slots
        SlotGroupWidget.Builder creatureGearBuilder = SlotGroupWidget.builder();
        if (creatureHasGear) {
            creatureGearBuilder.key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(creatureGear, index).slotGroup("creatureGear")));

            //get gear slot count
            String gearMatrixRow = "";
            for (int i = 0; i < creatureGear.getSlots(); i++) gearMatrixRow = gearMatrixRow.concat("I");
            creatureGearBuilder.matrix(gearMatrixRow);
        }

        //build creature inventory slots
        SlotGroupWidget.Builder creatureInvBuilder = SlotGroupWidget.builder();
        creatureInvBuilder.key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(creatureInventory, index).slotGroup("creatureInventory")));

        //get creature slot count, and use the info to deal with the matrix
        int matrixHeight = (int) Math.ceil(creatureInventory.getSlots() / 9D);
        String[] invMatrix = new String[matrixHeight];
        int count = creatureInventory.getSlots();
        for (int i = 0; i < matrixHeight; i++) {
            String toAdd = "";
            int stringLength = Math.min(9, count);
            for (int j = 0; j < stringLength; j++) toAdd = toAdd.concat("I");
            invMatrix[i] = toAdd;
            count -= 9;
        }
        creatureInvBuilder.matrix(invMatrix);

        //continue w making the page
        return new ParentWidget<>().debugName("inventoryPage")
                .padding(7, 7)
                .child(new Column().coverChildrenHeight().childPadding(5)
                        //creature gear
                        .childIf(creatureHasGear, new Column()
                                .coverChildren()
                                //header
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(IKey.str(creatureGearName).asWidget().align(Alignment.CenterLeft))
                                )
                                //gear contents
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(creatureGearBuilder.build().align(Alignment.CenterLeft))
                                )
                        )
                        //creature inventory
                        .child(new Column()
                                .coverChildren()
                                //header
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(IKey.str(creatureInvName).asWidget().align(Alignment.CenterLeft))
                                )
                                //if inventory is bigger than 27 slots, the inventory widget is to be a scrollable list
                                .childIf(matrixHeight > 3, new ListWidget<>()
                                        .size(168, 54)
                                        .horizontalCenter()
                                        .child(creatureInvBuilder.build())
                                )
                                //otherwise, its just the inventory itself
                                .childIf(matrixHeight <= 3, creatureInvBuilder.build())
                        )
                        //player inventory
                        .child(new Column()
                                .coverChildren()
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(IKey.str(playerName).asWidget().align(Alignment.CenterLeft))
                                )
                                .child(SlotGroupWidget.playerInventory(false))
                        )
                ).width(180).coverChildrenHeight();
    }

    private static ParentWidget<?> creatureSettingsPage(RiftCreature creature, PanelSyncManager syncManager) {
        //creature sitting syncing
        BooleanSyncValue sittingValue = new BooleanSyncValue(creature::isSitting, creature::setSitting);
        syncManager.syncValue("creatureSitting", sittingValue);

        //turret mode syncing
        BooleanSyncValue turretModeValue = new BooleanSyncValue(creature::isTurretMode, creature::setTurretMode);
        syncManager.syncValue("turretMode", turretModeValue);

        //define bottom parentwidget
        boolean hasOptionsButtons = canHaveOptionsButtons(creature);

        //additional widgets that depend on value of turretModeValue
        IWidget turretTargetingOptions = turretTargetingOptionGroup(creature, syncManager, hasOptionsButtons);
        IWidget creatureBehavior = creatureBehaviorGroup(creature, syncManager, hasOptionsButtons);

        //final return value
        return new ParentWidget<>()
                .debugName("settingsPage")
                .padding(7, 7)
                .child(new Column()
                        .childPadding(5)
                        .child(new ParentWidget<>()
                                .debugName("top")
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .child(new Column().coverChildren()
                                        .childPadding(2)
                                        .child(new Row()
                                                .debugName("sittingRow")
                                                .coverChildrenHeight()
                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                .childPadding(2)
                                                .child(new CycleButtonWidget()
                                                        .value(new BoolValue.Dynamic(
                                                                () -> {
                                                                    return sittingValue.getValue() && !turretModeValue.getValue();
                                                                },
                                                                value -> {
                                                                    sittingValue.setBoolValue(value && !turretModeValue.getValue());
                                                                }
                                                        ))
                                                        .stateOverlay(GuiTextures.CHECK_BOX)
                                                        .size(14, 14)
                                                )
                                                .child(IKey.dynamic(() -> {
                                                            if (turretModeValue.getBoolValue()) return IKey.STRIKETHROUGH.toString()+I18n.format("creature_menu.sitting");
                                                            else return I18n.format("creature_menu.sitting");
                                                        }).asWidget().verticalCenter()
                                                )
                                        )
                                        //if creature has turret mode and is at base, add option
                                        .childIf(creature.canEnterTurretMode()
                                                && creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE, new Row()
                                                .coverChildrenHeight()
                                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                                .childPadding(2)
                                                .child(new CycleButtonWidget()
                                                        .value(new BoolValue.Dynamic(
                                                                turretModeValue::getValue,
                                                                value -> {
                                                                    turretModeValue.setBoolValue(value);
                                                                    sittingValue.setBoolValue(false);
                                                                }
                                                        ))
                                                        .stateOverlay(GuiTextures.CHECK_BOX)
                                                        .size(14, 14)
                                                )
                                                .child(IKey.lang("creature_menu.turret_mode").asWidget().verticalCenter())
                                        )
                                )
                        )
                        .child(new ParentWidget<>()
                                .debugName("bottom")
                                .widthRel(1f)
                                .coverChildrenHeight()
                                //if there's an options button section, there's the widget with the options
                                .childIf(hasOptionsButtons, new ParentWidget<>()
                                        .onUpdateListener(widget -> {
                                            if (turretModeValue.getBoolValue()) {
                                                tryAddChild(widget, turretTargetingOptions);
                                                tryRemoveChild(widget, creatureBehavior);
                                            }
                                            else {
                                                tryAddChild(widget, creatureBehavior);
                                                tryRemoveChild(widget, turretTargetingOptions);
                                            }
                                        })
                                        .debugName("creatureBehaviorOrTurretTargeting")
                                        .coverChildren()
                                        .align(Alignment.TopLeft)
                                )
                                .childIf(hasOptionsButtons, creatureOptionsGroup(creature, syncManager).align(Alignment.TopRight))
                                //if theres no options button section, its just the widget
                                .childIf(!hasOptionsButtons, new ParentWidget<>()
                                        .onUpdateListener(widget -> {
                                            if (turretModeValue.getBoolValue()) {
                                                tryAddChild(widget, turretTargetingOptions);
                                                tryRemoveChild(widget, creatureBehavior);
                                            }
                                            else {
                                                tryAddChild(widget, creatureBehavior);
                                                tryRemoveChild(widget, turretTargetingOptions);
                                            }
                                        })
                                        .debugName("creatureBehaviorOrTurretTargeting")
                                        .coverChildren()
                                        .align(Alignment.TopCenter)
                                )
                        )
                        .coverChildrenHeight()
                )
                .width(180).coverChildrenHeight();
    }

    private static ParentWidget<?> creatureBehaviorGroup(RiftCreature creature, PanelSyncManager syncManager, boolean hasOptionsButtons) {
        int buttonWidth = hasOptionsButtons ? 80 : 160;

        //creature tame behavior syncing
        IntSyncValue creatureBehaviorValue = new IntSyncValue(
                () -> creature.getTameBehavior().ordinal(),
                val -> creature.setTameBehavior(TameBehaviorType.values()[val])
        );
        syncManager.syncValue("creatureBehavior", creatureBehaviorValue);

        Flow behaviorOptions = new Column()
                .coverChildrenHeight().width(buttonWidth)
                .childPadding(3)
                .child(IKey.lang("creature_menu.header.creature_behavior").asWidget());

        //loop over all tame behavior types, to then make buttons associated with them
        for (TameBehaviorType behavior : TameBehaviorType.values()) {
            behaviorOptions.child(
                    new ToggleButton().size(buttonWidth, 20)
                            .overlay(IKey.lang(behavior.getTranslatedName()))
                            .value(new BoolValue.Dynamic(
                                    () -> creatureBehaviorValue.getIntValue() == behavior.ordinal() && creature.getTameBehavior() == behavior,
                                    value -> creatureBehaviorValue.setIntValue(behavior.ordinal())
                            ))
            );
        }

        return behaviorOptions;
    }

    private static ParentWidget<?> turretTargetingOptionGroup(RiftCreature creature, PanelSyncManager syncManager, boolean hasOptionsButtons) {
        int buttonWidth = hasOptionsButtons ? 80 : 160;

        //turret mode targeting syncing
        IntSyncValue turretTargetingValue = new IntSyncValue(
                () -> creature.getTurretTargeting().ordinal(),
                value -> creature.setTurretModeTargeting(TurretModeTargeting.values()[value])
        );
        syncManager.syncValue("turretTargeting", turretTargetingValue);

        Flow turretModeOptions = new Column()
                .coverChildrenHeight().width(buttonWidth)
                .childPadding(3)
                .child(IKey.lang("creature_menu.header.turret_mode_targeting").asWidget());

        for (TurretModeTargeting turretModeTargeting : TurretModeTargeting.values()) {
            turretModeOptions.child(
                    new ToggleButton().size(buttonWidth, 20)
                            .overlay(IKey.lang(turretModeTargeting.getTranslatedName()))
                            .value(new BoolValue.Dynamic(
                                    () -> turretTargetingValue.getIntValue() == turretModeTargeting.ordinal() && creature.getTurretTargeting() == turretModeTargeting,
                                    value -> turretTargetingValue.setIntValue(turretModeTargeting.ordinal())
                            ))
            );
        }

        return turretModeOptions;
    }

    private static boolean canHaveOptionsButtons(RiftCreature creature) {
        return creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE &&
                (creature instanceof IWorkstationUser || creature instanceof IHarvestWhenWandering);
    }

    private static ParentWidget<?> creatureOptionsGroup(RiftCreature creature, PanelSyncManager syncManager) {
        Flow creatureOptions = new Column()
                .coverChildrenHeight().width(80)
                .childPadding(3)
                .child(IKey.lang("creature_menu.header.base_options").asWidget());

        //workstation button
        if (creature instanceof IWorkstationUser) {
            IWorkstationUser workstationUser = (IWorkstationUser) creature;

            creatureOptions.child(
                    new ButtonWidget<>().size(80, 20)
                            .overlay(IKey.dynamic(() -> {
                                if (!workstationUser.hasWorkstation()) return I18n.format("radial.choice.set_workstation");
                                else return I18n.format("radial.choice.clear_workstation");
                            }))
                            .onMousePressed(button -> {
                                if (!workstationUser.hasWorkstation()) {
                                    creature.setSitting(false);
                                    ClientProxy.settingCreatureWorkstation = true;
                                    ClientProxy.creatureIdForWorkstation = creature.getEntityId();

                                    EntityPlayer player = Minecraft.getMinecraft().player;
                                    player.sendStatusMessage(new TextComponentTranslation("action.set_creature_workstation_start"), false);

                                    creatureOptions.getPanel().closeIfOpen();
                                }
                                else workstationUser.clearWorkstation(false);
                                return true;
                            })
            );
        }

        //harvest on wander button
        if (creature instanceof IHarvestWhenWandering) {
            IHarvestWhenWandering harvestWhenWanderingUser = (IHarvestWhenWandering) creature;

            BooleanSyncValue harvestWhenWanderingVal = new BooleanSyncValue(
                    harvestWhenWanderingUser::canHarvest,
                    harvestWhenWanderingUser::setCanHarvest
            );
            syncManager.syncValue("harvestWhenWandering", harvestWhenWanderingVal);

            creatureOptions.child(
                    new ToggleButton().size(80, 20)
                            .overlay(IKey.dynamic(() -> {
                                if (harvestWhenWanderingUser.canHarvest()) return I18n.format("radial.choice.clear_wander_harvest");
                                else return I18n.format("radial.choice.set_wander_harvest");
                            }))
                            .value(new BoolValue.Dynamic(
                                    harvestWhenWanderingVal::getValue,
                                    harvestWhenWanderingVal::setBoolValue
                            ))
            );
        }

        return creatureOptions;
    }

    private static ParentWidget<?> creatureInfoPage(RiftCreature creature, PanelSyncManager syncManager, UISettings settings) {
        return RiftCreatureInfoPanel.build(creature, syncManager, settings).debugName("infoPage");
    }

    private static ParentWidget<?> creatureMovesPage(RiftCreature creature, PanelSyncManager syncManager, UISettings settings) {
        return RiftCreatureMovesPanel.build(creature, syncManager, settings).debugName("movesPage");
    }

    private static void tryAddChild(ParentWidget<?> parentWidget, IWidget childToAdd) {
        if (!parentWidget.getChildren().contains(childToAdd)) {
            parentWidget.child(childToAdd);
            parentWidget.scheduleResize();
        }
    }

    private static void tryRemoveChild(ParentWidget<?> parentWidget, IWidget childToRemove) {
        if (parentWidget.getChildren().contains(childToRemove)) {
            parentWidget.remove(childToRemove);
            parentWidget.scheduleResize();
        }
    }
}
