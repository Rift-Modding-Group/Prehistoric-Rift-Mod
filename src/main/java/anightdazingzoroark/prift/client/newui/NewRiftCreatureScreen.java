package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.TameBehaviorType;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.EntityGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraftforge.items.ItemStackHandler;

public class NewRiftCreatureScreen {
    public static ModularPanel buildCreatureUI(EntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disableRecipeViewer();
        RiftCreature creature = (RiftCreature) data.getGuiHolder();

        //tab related stuff
        PagedWidget.Controller tabController = new PagedWidget.Controller();
        Row tabButtonRow = (Row) new Row()
                .debugName("creatureScreenTabRow")
                .coverChildren()
                .topRel(0f, 4, 1f)
                .child(new PageButton(0, tabController)
                        .overlay(new ItemDrawable(Blocks.CHEST).asIcon())
                        .tab(GuiTextures.TAB_TOP, -1))
                .child(new PageButton(1, tabController)
                        .overlay(GuiTextures.GEAR.asIcon().size(24))
                        .tab(GuiTextures.TAB_TOP, 0));
        PagedWidget<?> pagedWidget = new PagedWidget<>()
                .debugName("pagedWidget").sizeRel(1f)
                .controller(tabController)
                .addPage(creatureInventoryPage(creature, syncManager))
                .addPage(creatureSettingsPage(creature, syncManager))
                .addPage(new ParentWidget<>());

        return new ModularPanel("creatureScreen").size(180, 192)
                .child(tabButtonRow)
                .child(pagedWidget);
                //.coverChildrenHeight(); this is here until i figure out how to dynamically change height of a ModularPanel
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

        //make the widget for creature inventory based on height of inventory
        IWidget creatureInv = creatureInvBuilder.build();
        if (matrixHeight > 3) creatureInv = new ListWidget<>()
                .size(168, 54)
                .horizontalCenter()
                .child(creatureInvBuilder.build());

        //continue w making the page
        Flow column = new Column().coverChildrenHeight();
        if (creatureHasGear) column
                .child(IKey.str(creatureGearName).asWidget())
                .child(creatureGearBuilder.build());
        column.child(IKey.str(creatureInvName).asWidget())
                .child(creatureInv)
                .child(IKey.str(playerName).asWidget())
                .child(SlotGroupWidget.playerInventory(false))
                .bottomRel(0.2f);
        return new ParentWidget<>().sizeRel(1f, 1f)
                .padding(7, 7)
                .child(column);
    }

    private static ParentWidget<?> creatureSettingsPage(RiftCreature creature, PanelSyncManager syncManager) {
        //creature sitting syncing
        BooleanSyncValue sittingValue = new BooleanSyncValue(
                creature::isSitting,
                creature::setSitting
        );
        syncManager.syncValue("creatureSitting", sittingValue);

        //creature information button
        ButtonWidget<?> creatureInfoButton = new ButtonWidget<>()
                .overlay(GuiTextures.EXCLAMATION)
                .size(12).align(Alignment.TopRight);

        //set sitting
        Flow sittingOptions = new Row()
                .coverChildrenHeight()
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .childPadding(2)
                .child(new CycleButtonWidget()
                        .value(new BoolValue.Dynamic(
                                sittingValue::getValue,
                                sittingValue::setBoolValue
                        ))
                        .stateOverlay(GuiTextures.CHECK_BOX)
                        .size(14, 14)
                )
                .child(IKey.str("Sitting").asWidget().verticalCenter());

        //define top parentwidget
        ParentWidget<?> topParentWidget = new ParentWidget<>()
                .debugName("top")
                .widthRel(1f)
                .coverChildrenHeight()
                .align(Alignment.TopCenter)
                .child(sittingOptions);

        //define bottom parentwidget
        boolean hasOptionsButtons = canHaveOptionsButtons(creature);
        ParentWidget<?> bottomParentWidget = new ParentWidget<>()
                .debugName("bottom")
                .widthRel(1f)
                .coverChildrenHeight()
                .align(Alignment.BottomCenter);
        if (hasOptionsButtons) {
            bottomParentWidget.child(creatureBehaviorGroup(creature, syncManager, true).align(Alignment.TopLeft))
                    .child(creatureOptionsGroup(creature, syncManager).align(Alignment.TopRight));
        }
        else bottomParentWidget.child(creatureBehaviorGroup(creature, syncManager, false).align(Alignment.TopCenter));

        //final return value
        return new ParentWidget<>().sizeRel(1f, 1f)
                .padding(7, 7)
                .child(creatureInfoButton)
                .child(topParentWidget)
                .child(bottomParentWidget);
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
                .child(IKey.lang("radial.choice.behavior").asWidget());

        //loop over all tame behavior types, to then make buttons associated with them
        for (TameBehaviorType behavior : TameBehaviorType.values()) {
            //skip turret mode if the creature cannot use it
            if (!creature.canEnterTurretMode() && behavior == TameBehaviorType.TURRET) continue;

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

    private static boolean canHaveOptionsButtons(RiftCreature creature) {
        return creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE &&
                (creature instanceof IWorkstationUser || creature instanceof IHarvestWhenWandering);
    }

    private static ParentWidget<?> creatureOptionsGroup(RiftCreature creature, PanelSyncManager syncManager) {
        Flow creatureOptions = new Column()
                .coverChildrenHeight().width(80)
                .childPadding(3)
                .child(IKey.lang("radial.choice.options").asWidget());

        //workstation button
        if (creature instanceof IWorkstationUser) {
            creatureOptions.child(
                    new ButtonWidget<>().size(80, 20)
                            .overlay(IKey.lang("radial.choice.set_workstation"))
            );
        }

        //harvest on wander button
        if (creature instanceof IHarvestWhenWandering) {
            creatureOptions.child(
                    new ButtonWidget<>().size(80, 20)
                            .overlay(IKey.lang("radial.choice.set_wander_harvest"))
            );
        }

        return creatureOptions;
    }
}
