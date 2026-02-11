package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.newui.sync.MoveSwapInfoSyncValue;
import anightdazingzoroark.prift.client.newui.data.CreatureGuiData;
import anightdazingzoroark.prift.client.newui.sync.SelectedMoveInfoSyncValue;
import anightdazingzoroark.prift.client.newui.widget.MoveListWidget;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import net.minecraft.client.resources.I18n;

public class RiftCreatureMovesPanel {
    public static final int[] size = {220, 164};

    public static ParentWidget<?> build(CreatureGuiData data, PanelSyncManager syncManager, UISettings settings) {
        //selected move
        SelectedMoveInfoSyncValue selectedMoveValue = new SelectedMoveInfoSyncValue(
                () -> data.selectedMoveInfoUI,
                value -> data.selectedMoveInfoUI = value
        );
        syncManager.syncValue("selectedMove", selectedMoveValue);

        //move switching
        BooleanSyncValue isMoveSwitching = new BooleanSyncValue(() -> data.isMoveSwitchingUI, value -> data.isMoveSwitchingUI = value);
        syncManager.syncValue("moveSwitching", isMoveSwitching);

        //helper sync value for helper class for swapping moves
        MoveSwapInfoSyncValue moveSwapInfo = new MoveSwapInfoSyncValue(
                () -> data.moveSwapInfoUI,
                value -> data.moveSwapInfoUI = value
        );
        syncManager.syncValue("moveSwapInfo", moveSwapInfo);

        return new ParentWidget<>().padding(7, 7).coverChildrenHeight().width(220)
                .child(new Row().coverChildren().childPadding(5)
                        //left side is the creature's current moves
                        .child(new ParentWidget<>().coverChildrenWidth().height(147)
                                .child(currentMovesSide(data, selectedMoveValue, isMoveSwitching, moveSwapInfo))
                        )
                        //separator line
                        .child(new Rectangle().color(0xFF000000).asWidget().size(1, 108))
                        //right side is info
                        .child(new ParentWidget<>().coverChildrenWidth().height(147)
                                .child(learnableMovesSide(data, selectedMoveValue, isMoveSwitching, moveSwapInfo))
                        )
                );
    }

    private static Flow currentMovesSide(CreatureGuiData data, SelectedMoveInfoSyncValue selectedMoveValue, BooleanSyncValue isMoveSwitching,
                                         MoveSwapInfoSyncValue moveSwapInfo) {
        return new Column().name("leftSide")
                .childPadding(5).coverChildren()
                //header
                .child(new ParentWidget<>().width(96).coverChildrenHeight()
                        .child(IKey.lang("tamepanel.current_moves", data.getName(false)).asWidget().scale(0.75f).left(0))
                )
                //current moves
                .child(new MoveListWidget(data, selectedMoveValue, SelectedMoveInfo.SelectedMoveType.LEARNT, moveSwapInfo, isMoveSwitching).size(90, 70))
                //add box where info about selected move will be placed
                .child(new ParentWidget<>().size(96, 60)
                .child(new Rectangle().color(0xFF000000).cornerRadius(5).asWidget().size(96, 60))
                .child(new Rectangle().color(0xFF808080).cornerRadius(5).asWidget().size(94, 58).center())
                .child(new ParentWidget<>().size(90, 54).center()
                        .child(IKey.dynamic(() -> {
                            if (selectedMoveValue.getValue() != null) {
                                CreatureMove selectedMove = selectedMoveValue.getValue().applyMove(data);
                                if (selectedMove != null) return selectedMove.getTranslatedDescription();
                                return I18n.format("creature_move.none_selected_switch.description");
                            }
                            else {
                                if (isMoveSwitching.getBoolValue()) {
                                    return I18n.format("creature_move.none_selected_switch.description");
                                }
                                return I18n.format("creature_move.none_selected.description");
                            }
                        }).asWidget().scale(0.75f).left(0))
                )
        );
    }

    private static Flow learnableMovesSide(CreatureGuiData data, SelectedMoveInfoSyncValue selectedMoveValue, BooleanSyncValue isMoveSwitching,
                                           MoveSwapInfoSyncValue moveSwapInfo) {
        return new Column().name("rightSide")
                .childPadding(5).coverChildren()
                .child(new ParentWidget<>().width(96).coverChildrenHeight()
                        //header
                        .child(IKey.lang("tamepanel.available_moves").asWidget().scale(0.75f).left(0))
                        //swap moves buttom
                        .child(new ToggleButton().overlay(GuiTextures.REVERSE.asIcon().size(12)).size(12)
                                .value(new BoolValue.Dynamic(
                                        isMoveSwitching::getBoolValue,
                                        value -> {
                                            isMoveSwitching.setBoolValue(value);
                                            selectedMoveValue.setValue(null);
                                            moveSwapInfo.getValue().clear();
                                        })
                                ).right(0)
                        )
                )
                .child(new ParentWidget<>().coverChildrenWidth().height(130)
                        //background
                        .child(new Rectangle().color(0xFF000000).cornerRadius(5).asWidget().size(96, 130))
                        .child(new Rectangle().color(0xFF808080).cornerRadius(5).asWidget().size(94, 128).center())
                                .child(new MoveListWidget(data, selectedMoveValue, SelectedMoveInfo.SelectedMoveType.LEARNABLE, moveSwapInfo, isMoveSwitching)
                                        .size(90, 120).center())
                );
    }
}