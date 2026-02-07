package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.newui.custom.PaddedGrid;
import anightdazingzoroark.prift.client.newui.custom.PartyMemberButtonWidget;
import anightdazingzoroark.prift.client.newui.data.CreatureGuiData;
import anightdazingzoroark.prift.client.newui.data.CreatureGuiFactory;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftOpenCreatureScreen;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.*;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Grid;
import net.minecraft.entity.player.EntityPlayer;

public class NewRiftPartyScreen {
    public static ModularPanel build(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        settings.getRecipeViewerSettings().disableRecipeViewer();

        EntityPlayer player = data.getPlayer();
        FixedSizeList<CreatureNBT> playerPartyNBT = PlayerTamedCreaturesHelper.getPlayerPartyNBT(player);

        return new ModularPanel(UIPanelNames.PARTY_SCREEN)
                .coverChildren().padding(7, 7)
                .child(new Column().coverChildren()
                        .childPadding(5)
                        .child(new ParentWidget<>().coverChildrenHeight().widthRel(1f)
                                .child(IKey.lang("journal.party_label.party").asWidget().align(Alignment.CenterLeft))
                                .child(new ToggleButton().overlay().overlay(GuiTextures.REVERSE.asIcon().size(12))
                                        .size(12).align(Alignment.TopRight)
                                )
                        )
                        .child(new PaddedGrid().coverChildren()
                                .matrix(Grid.mapToMatrix(2, playerPartyNBT.getList(), (index, value) -> {
                                    return new PartyMemberButtonWidget(value)
                                            .onMousePressed(button -> {
                                                RiftCreature creature = value.findCorrespondingCreature(player.world);
                                                if (creature != null) {
                                                    RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, creature));
                                                }
                                                else {
                                                    SelectedCreatureInfo selectionInfo = new SelectedCreatureInfo(
                                                            SelectedCreatureInfo.SelectedPosType.PARTY,
                                                            new int[]{index}
                                                    );
                                                    RiftMessages.WRAPPER.sendToServer(new RiftOpenCreatureScreen(player, selectionInfo));
                                                }
                                                return true;
                                            });
                                })).padding(4)
                        )
                );
    }
}
