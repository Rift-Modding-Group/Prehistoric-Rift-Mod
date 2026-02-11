package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.client.newui.widget.CreatureGearModularSlot;
import anightdazingzoroark.prift.client.newui.widget.CreatureInventoryModularSlot;
import anightdazingzoroark.prift.client.newui.data.CreatureGuiData;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.CreatureGearHandler;
import anightdazingzoroark.prift.server.entity.CreatureInventoryHandler;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import anightdazingzoroark.prift.server.enums.TurretModeTargeting;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
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

import java.util.List;
import java.util.function.Consumer;

public class RiftCreatureScreen {
    public static ModularPanel buildCreatureUI(CreatureGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disable();

        //tab related stuff
        PagedWidget.Controller tabController = new PagedWidget.Controller();

        return new ModularPanel(UIPanelNames.INTERACTED_CREATURE_SCREEN)
                .onUpdateListener(new Consumer<ModularPanel>() {
                    @Override
                    public void accept(ModularPanel modularPanel) {
                        PagedWidget<?> pagedWidget = this.pagedWidget(modularPanel);
                        if (pagedWidget == null) {
                            modularPanel.width(180);
                            return;
                        }

                        //set size of panel based on size of current page
                        modularPanel.size(this.getPageContentSize(pagedWidget)[0], this.getPageContentSize(pagedWidget)[1]);
                    }

                    private PagedWidget<?> pagedWidget(ModularPanel panel) {
                        List<IWidget> widgetChildren = panel.getChildren();
                        for (IWidget widget : widgetChildren) {
                            if (widget instanceof PagedWidget) return (PagedWidget<?>) widget;
                        }
                        return null;
                    }

                    private int[] getPageContentSize(PagedWidget<?> pagedWidget) {
                        if (pagedWidget == null) return new int[]{0, 0};
                        return new int[]{
                                pagedWidget.getCurrentPage().getArea().width,
                                pagedWidget.getCurrentPage().getArea().height
                        };
                    }
                })
                .child(new Column()
                        .name("creatureScreenTabColumn")
                        .coverChildren()
                        .leftRel(0f, 4, 1f)
                        .child(new PageButton(0, tabController)
                                .overlay(new ItemDrawable(Blocks.CHEST).asIcon())
                                .addTooltipElement(IKey.lang("tametab.inventory"))
                                .tab(GuiTextures.TAB_LEFT, -1)
                        )
                        .child(new PageButton(1, tabController)
                                .overlay(GuiTextures.GEAR.asIcon().size(24))
                                .addTooltipElement(IKey.lang("tametab.manage"))
                                .tab(GuiTextures.TAB_LEFT, 0)
                        )
                        .child(new PageButton(2, tabController)
                                .overlay(GuiTextures.EXCLAMATION.asIcon().size(24))
                                .addTooltipElement(IKey.lang("tametab.info"))
                                .tab(GuiTextures.TAB_LEFT, 0)
                        )
                        .child(new PageButton(3, tabController)
                                .overlay(new ItemDrawable(Items.IRON_SWORD).asIcon())
                                .addTooltipElement(IKey.lang("tametab.moves"))
                                .tab(GuiTextures.TAB_LEFT, 0)
                        )
                )
                .child(new PagedWidget<>().name("pagedWidget")
                        .controller(tabController).widthRel(1f).coverChildrenHeight()
                        .addPage(creatureInventoryPage(data, syncManager))
                        .addPage(creatureSettingsPage(data, syncManager))
                        .addPage(creatureInfoPage(data, syncManager, settings))
                        .addPage(creatureMovesPage(data, syncManager, settings))
                );
    }

    private static ParentWidget<?> creatureInventoryPage(CreatureGuiData data, PanelSyncManager syncManager) {
        EntityPlayer player = data.getPlayer();

        //set up strings
        String playerName = player.getName();
        String creatureGearName = I18n.format("inventory.gear", data.getName(false));
        String creatureInvName = I18n.format("inventory.inventory", data.getName(false));

        //creature inventory syncing stuff
        syncManager.registerSlotGroup("creatureGear", Math.min(data.getCreatureType().gearSlotCount(), 9));
        syncManager.registerSlotGroup("creatureInventory", 9);
        CreatureGearHandler creatureGear = data.getCreatureGear();
        CreatureInventoryHandler creatureInventory = data.getCreatureInventory();
        syncManager.bindPlayerInventory(player);

        //set if the creature has gear
        boolean creatureHasGear = data.getCreatureType().gearSlotCount() > 0;
        //build creature gear slots
        SlotGroupWidget.Builder creatureGearBuilder = SlotGroupWidget.builder();
        if (creatureHasGear) {
            //continue building widget for creature gear
            creatureGearBuilder.key('I', index -> {
                CreatureGearModularSlot modularSlot = new CreatureGearModularSlot(creatureGear, index);
                if (data.dataType == CreatureGuiData.DataType.SELECTION) {
                    modularSlot = new CreatureGearModularSlot((SelectedCreatureInfo) data.getGuiHolder(), creatureGear, index);
                }

                return new ItemSlot().slot(modularSlot
                        .slotGroup("creatureGear").filter(creatureGear::itemStackUsableAsGear)
                        .accessibility(data.gearSlotChangeable(), data.gearSlotChangeable())
                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                            if (!client) {
                                data.setSaddled(creatureGear.hasSaddle());
                                data.setLargeWeapon(creatureGear.getLargeWeapon());
                            }
                        })
                );
            });

            //get gear slot count
            String gearMatrixRow = "";
            for (int i = 0; i < creatureGear.getSlots(); i++) gearMatrixRow = gearMatrixRow.concat("I");
            creatureGearBuilder.matrix(gearMatrixRow);
        }

        //build creature inventory slots
        SlotGroupWidget.Builder creatureInvBuilder = SlotGroupWidget.builder();
        creatureInvBuilder.key('I', index -> {
            CreatureInventoryModularSlot modularSlot = new CreatureInventoryModularSlot(creatureInventory, index);
            if (data.dataType == CreatureGuiData.DataType.SELECTION) {
                modularSlot = new CreatureInventoryModularSlot((SelectedCreatureInfo) data.getGuiHolder(), creatureInventory, index);
            }
            return new ItemSlot().slot(modularSlot.slotGroup("creatureInventory"));
        });

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
        return new ParentWidget<>().name("inventoryPage")
                .padding(7, 7)
                .child(new Column().coverChildrenHeight().childPadding(5)
                        //creature gear
                        .childIf(creatureHasGear, () -> new Column()
                                .coverChildren()
                                //header
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(IKey.str(creatureGearName).asWidget().left(0))
                                )
                                //gear contents
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(creatureGearBuilder.build().left(0).name("creature_gear"))
                                )
                        )
                        //creature inventory
                        .child(new Column()
                                .coverChildren()
                                //header
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(IKey.str(creatureInvName).asWidget().left(0))
                                )
                                //if inventory is bigger than 27 slots, the inventory widget is to be a scrollable list
                                .childIf(matrixHeight > 3, () -> new ListWidget<>()
                                        .size(168, 54)
                                        .horizontalCenter()
                                        .child(creatureInvBuilder.build().name("creature_inventory"))
                                )
                                //otherwise, its just the inventory itself
                                .childIf(matrixHeight <= 3, () -> creatureInvBuilder.build().name("creature_inventory"))
                        )
                        //player inventory
                        .child(new Column()
                                .coverChildren()
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(IKey.str(playerName).asWidget().left(0))
                                )
                                .child(SlotGroupWidget.playerInventory(false))
                        )
                ).width(180).coverChildrenHeight();
    }

    private static ParentWidget<?> creatureSettingsPage(CreatureGuiData data, PanelSyncManager syncManager) {
        //creature sitting syncing
        BooleanSyncValue sittingValue = new BooleanSyncValue(data::isSitting, data::setSitting);
        syncManager.syncValue("creatureSitting", sittingValue);

        //turret mode syncing
        BooleanSyncValue turretModeValue = new BooleanSyncValue(data::isTurretMode, data::setTurretMode);
        syncManager.syncValue("turretMode", turretModeValue);

        //define bottom parentwidget
        boolean hasOptionsButtons = canHaveOptionsButtons(data);

        //additional widgets that depend on value of turretModeValue
        IWidget turretTargetingOptions = turretTargetingOptionGroup(data, syncManager, hasOptionsButtons);
        IWidget creatureBehavior = creatureBehaviorGroup(data, syncManager, hasOptionsButtons);

        //final return value
        return new ParentWidget<>()
                .name("settingsPage")
                .padding(7, 7)
                .child(new Column()
                        .childPadding(5)
                        .child(new ParentWidget<>()
                                .name("top")
                                .widthRel(1f)
                                .coverChildrenHeight()
                                .child(new Column().coverChildren()
                                        .childPadding(2)
                                        .child(new Row()
                                                .name("sittingRow")
                                                .coverChildrenHeight()
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
                                        .childIf(data.canEnterTurretMode()
                                                && data.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE,
                                                () -> new Row()
                                                .coverChildrenHeight()
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
                                .name("bottom")
                                .widthRel(1f)
                                .coverChildrenHeight()
                                //if there's an options button section, there's the widget with the options
                                .childIf(hasOptionsButtons, () -> new ParentWidget<>()
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
                                        .name("creatureBehaviorOrTurretTargeting")
                                        .coverChildren()
                                        .left(0)
                                )
                                .childIf(hasOptionsButtons, () -> creatureOptionsGroup(data, syncManager).align(Alignment.TopRight))
                                //if theres no options button section, its just the widget
                                .childIf(!hasOptionsButtons, () -> new ParentWidget<>()
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
                                        .name("creatureBehaviorOrTurretTargeting")
                                        .coverChildren()
                                )
                        )
                        .coverChildrenHeight()
                )
                .width(180).coverChildrenHeight();
    }

    private static ParentWidget<?> creatureBehaviorGroup(CreatureGuiData data, PanelSyncManager syncManager, boolean hasOptionsButtons) {
        int buttonWidth = hasOptionsButtons ? 80 : 160;

        //creature tame behavior syncing
        IntSyncValue creatureBehaviorValue = new IntSyncValue(
                () -> data.getTameBehavior().ordinal(),
                val -> data.setTameBehavior(TameBehaviorType.values()[val])
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
                                    () -> creatureBehaviorValue.getIntValue() == behavior.ordinal() && data.getTameBehavior() == behavior,
                                    value -> creatureBehaviorValue.setIntValue(behavior.ordinal())
                            ))
            );
        }

        return behaviorOptions;
    }

    private static ParentWidget<?> turretTargetingOptionGroup(CreatureGuiData data, PanelSyncManager syncManager, boolean hasOptionsButtons) {
        int buttonWidth = hasOptionsButtons ? 80 : 160;

        //turret mode targeting syncing
        IntSyncValue turretTargetingValue = new IntSyncValue(
                () -> data.getTurretTargeting().ordinal(),
                value -> data.setTurretModeTargeting(TurretModeTargeting.values()[value])
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
                                    () -> turretTargetingValue.getIntValue() == turretModeTargeting.ordinal() && data.getTurretTargeting() == turretModeTargeting,
                                    value -> turretTargetingValue.setIntValue(turretModeTargeting.ordinal())
                            ))
            );
        }

        return turretModeOptions;
    }

    private static boolean canHaveOptionsButtons(CreatureGuiData data) {
        if (data.dataType == CreatureGuiData.DataType.SELECTION) return false;
        RiftCreature creature = (RiftCreature) data.getGuiHolder();
        return creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE &&
                (creature instanceof IWorkstationUser || creature instanceof IHarvestWhenWandering);
    }

    private static ParentWidget<?> creatureOptionsGroup(CreatureGuiData data, PanelSyncManager syncManager) {
        if (data.dataType == CreatureGuiData.DataType.SELECTION) return new ParentWidget<>();

        RiftCreature creature = (RiftCreature) data.getGuiHolder();
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

    private static ParentWidget<?> creatureInfoPage(CreatureGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return RiftCreatureInfoPanel.build(data, syncManager, settings).name("infoPage");
    }

    private static ParentWidget<?> creatureMovesPage(CreatureGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return RiftCreatureMovesPanel.build(data, syncManager, settings).name("movesPage");
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
