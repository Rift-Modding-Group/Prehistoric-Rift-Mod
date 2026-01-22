package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.EntityGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.items.ItemStackHandler;

public class NewRiftCreatureScreen {
    public static ModularPanel buildCreatureUI(EntityGuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disableRecipeViewer();
        RiftCreature creature = (RiftCreature) data.getGuiHolder();

        //creature inventory syncing stuff
        syncManager.registerSlotGroup("creatureInventory", 9);

        //tab related stuff
        PagedWidget.Controller tabController = new PagedWidget.Controller();
        Row tabButtonRow = (Row) new Row()
                .debugName("creatureScreenTabRow")
                .coverChildren()
                .topRel(0f, 4, 1f)
                .child(new PageButton(0, tabController)
                        .overlay(IKey.str("Inventory"))
                        .tab(GuiTextures.TAB_TOP, -1))
                .child(new PageButton(1, tabController)
                        .overlay(IKey.str("Settings"))
                        .tab(GuiTextures.TAB_TOP, 0))
                .child(new PageButton(2, tabController)
                        .overlay(IKey.str("Information"))
                        .tab(GuiTextures.TAB_TOP, 0));
        PagedWidget<?> pagedWidget = new PagedWidget<>()
                .sizeRel(1f)
                .controller(tabController)
                .addPage(creatureInventoryPage(creature))
                .addPage(new ParentWidget<>())
                .addPage(new ParentWidget<>());

        return new ModularPanel("creatureScreen").size(180, 166)
                .child(tabButtonRow)
                .child(pagedWidget);
    }

    private static ParentWidget<?> creatureInventoryPage(RiftCreature creature) {
        String playerName = Minecraft.getMinecraft().player.getName();
        String creatureInvName = I18n.format("inventory.inventory", creature.getName(false));
        ItemStackHandler creatureInventory = creature.newCreatureInventory;

        //deal with the slotgroupwidget
        SlotGroupWidget.Builder slotGroupBuilder = SlotGroupWidget.builder();
        slotGroupBuilder.key('I', index -> new ItemSlot().slot(SyncHandlers.itemSlot(creatureInventory, index).slotGroup("creatureInventory")));

        //get creature slot count, and use the info to deal with the matrix
        int matrixHeight = (int) Math.ceil(creatureInventory.getSlots() / 9D);
        String[] matrix = new String[matrixHeight];
        int count = creatureInventory.getSlots();
        for (int i = 0; i < matrixHeight; i++) {
            String toAdd = "";
            int stringLength = Math.min(9, count);
            for (int j = 0; j < stringLength; j++) toAdd = toAdd.concat("I");
            matrix[i] = toAdd;
            count -= 9;
        }
        slotGroupBuilder.matrix(matrix);

        //make the widget for creature inventory based on height of inventory
        IWidget creatureInv = slotGroupBuilder.build();
        if (matrixHeight > 3) creatureInv = new ListWidget<>()
                .size(168, 54)
                .horizontalCenter()
                .child(slotGroupBuilder.build());

        //continue w making the page
        ParentWidget<?> toReturn = new ParentWidget<>();
        toReturn.sizeRel(1f, 1f)
                .padding(0, 7)
                .child(new Column()
                        .coverChildrenHeight()
                        .child(IKey.str(creatureInvName).asWidget())
                        .child(creatureInv)
                        .child(IKey.str(playerName).asWidget())
                        .child(SlotGroupWidget.playerInventory(false))
                        .bottomRel(0.2f)
                );
        return toReturn;
    }
}
