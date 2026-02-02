package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.newui.custom.MoveSwapInfoSyncValue;
import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Unit;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class RiftCreatureMovesPanel {
    public static ParentWidget<?> build(RiftCreature creature, PanelSyncManager syncManager, UISettings settings) {
        //selected move
        IntSyncValue selectedMoveValue = new IntSyncValue(() -> creature.currentSelectedMoveUI, value -> creature.currentSelectedMoveUI = value);
        syncManager.syncValue("selectedMove", selectedMoveValue);

        //side in which selected move was from
        BooleanSyncValue selectedMoveFromRight = new BooleanSyncValue(() -> creature.selectedMoveFromRightUI, value -> creature.selectedMoveFromRightUI = value);
        syncManager.syncValue("selectedMoveFromRight", selectedMoveFromRight);

        //move switching
        BooleanSyncValue isMoveSwitching = new BooleanSyncValue(() -> creature.isMoveSwitchingUI, value -> creature.isMoveSwitchingUI = value);
        syncManager.syncValue("moveSwitching", isMoveSwitching);

        //helper sync value for helper class for swapping moves
        MoveSwapInfoSyncValue moveSwapInfo = new MoveSwapInfoSyncValue(
                () -> creature.moveSwapInfoUI,
                value -> creature.moveSwapInfoUI = value
        );
        syncManager.syncValue("moveSwapInfo", moveSwapInfo);

        return new ParentWidget<>().padding(7, 7).coverChildrenHeight().width(220)
                .child(new Row().coverChildren().childPadding(5)
                        //left side is the creature's current moves
                        .child(new ParentWidget<>().coverChildrenWidth().height(147)
                                .child(currentMovesSide(creature, selectedMoveValue, selectedMoveFromRight, isMoveSwitching, moveSwapInfo))
                        )
                        //separator line
                        .child(new Rectangle().setColor(0xFF000000).asWidget().size(1, 108))
                        //right side is info
                        .child(new ParentWidget<>().coverChildrenWidth().height(147)
                                .child(learnableMovesSide(creature, selectedMoveValue, selectedMoveFromRight, isMoveSwitching, moveSwapInfo))
                        )
                );
    }

    private static Flow currentMovesSide(RiftCreature creature, IntSyncValue selectedMoveValue,
                                         BooleanSyncValue selectedMoveFromRight, BooleanSyncValue isMoveSwitching,
                                         MoveSwapInfoSyncValue moveSwapInfo) {
        List<CreatureMove> creatureMoveList = creature.getLearnedMoves();

        return new Column().debugName("leftSide")
                .childPadding(5).coverChildren()
                .child(IKey.lang("tamepanel.current_moves", creature.getName(false)).asWidget().scale(0.75f))
                .child(new ParentWidget<>().size(80, 70)
                        .child(new Column().coverChildren().childPadding(5)
                                .onUpdateListener(widget -> {
                                    widget.getChildren().clear();

                                    //add current creature moves
                                    for (int i = 0; i < 3; i++) {
                                        if (i < creatureMoveList.size()) {
                                            CreatureMove creatureMove = creatureMoveList.get(i);
                                            int finalI = i;
                                            widget.child(new ButtonWidget<>().size(80, 20)
                                                    .onMousePressed(button -> {
                                                        selectedMoveValue.setIntValue(finalI);
                                                        selectedMoveFromRight.setBoolValue(false);

                                                        if (moveSwapInfo.getValue() == null) return true;
                                                        moveSwapInfo.getValue().setMove(
                                                                new SelectedMoveInfo(SelectedMoveInfo.SelectedMoveType.LEARNT, finalI)
                                                        );
                                                        moveSwapInfo.getValue().applySwap(creature);
                                                        return true;
                                                    })
                                                    .overlay(IKey.str(creatureMove.getTranslatedName()))
                                            );
                                        }
                                        else widget.child(new ButtonWidget<>().size(80, 20)
                                                .overlay(IKey.lang("creature_move.no_move"))
                                        );
                                    }
                                })
                        )
                )
                //add box where info about selected move will be placed
                .child(new ParentWidget<>().size(96, 60)
                .child(new Rectangle().setColor(0xFF000000).setCornerRadius(5).asWidget().size(96, 60))
                .child(new Rectangle().setColor(0xFF808080).setCornerRadius(5).asWidget().size(94, 58).align(Alignment.Center))
                .child(new ParentWidget<>().size(90, 54).align(Alignment.Center)
                        .child(IKey.dynamic(() -> {
                            if (selectedMoveValue.getIntValue() < 0) {
                                if (isMoveSwitching.getBoolValue()) {
                                    return I18n.format("creature_move.none_selected_switch.description");
                                }
                                return I18n.format("creature_move.none_selected.description");
                            }
                            else {
                                if (selectedMoveFromRight.getBoolValue()) {
                                    return creature.getLearnableMoves().get(selectedMoveValue.getIntValue()).getTranslatedDescription();
                                }
                                return creatureMoveList.get(selectedMoveValue.getIntValue()).getTranslatedDescription();
                            }
                        }).asWidget().scale(0.75f).align(Alignment.TopLeft))
                )
        );
    }

    private static Flow learnableMovesSide(RiftCreature creature, IntSyncValue selectedMoveValue,
                                           BooleanSyncValue selectedMoveFromRight, BooleanSyncValue isMoveSwitching,
                                           MoveSwapInfoSyncValue moveSwapInfo) {
        Flow movesColumn = new Column().coverChildrenWidth()
                .childPadding(3).align(Alignment.Center)
                .onUpdateListener(widget -> {
                    widget.getChildren().clear();

                    //add buttons for moves
                    List<CreatureMove> creatureMoveList = creature.getLearnableMoves();
                    for (int i = 0; i < creatureMoveList.size(); i++) {
                        CreatureMove creatureMove = creatureMoveList.get(i);
                        int finalI = i;

                        widget.child(finalI, new ButtonWidget<>().size(80, 20)
                                .onMousePressed(button -> {
                                    selectedMoveValue.setIntValue(finalI);
                                    selectedMoveFromRight.setBoolValue(true);
                                    return true;
                                })
                                .overlay(IKey.str(creatureMove.getTranslatedName()))
                        );
                    }

                    //add additional button that represents no move when in move swap mode
                    if (isMoveSwitching.getBoolValue()) {
                        widget.child(creatureMoveList.size(), new ButtonWidget<>().size(80, 20)
                                .overlay(IKey.lang("creature_move.no_move"))
                        );
                    }

                    //change height based on number of moves
                    if (creatureMoveList.size() <= 5) widget.height(120);
                    else widget.coverChildrenHeight();
                });

        return new Column().debugName("rightSide")
                .childPadding(5).coverChildren()
                .child(new ParentWidget<>().width(96).coverChildrenHeight()
                        //header
                        .child(IKey.lang("tamepanel.available_moves").asWidget().scale(0.75f).align(Alignment.CenterLeft))
                        //swap moves buttom
                        .child(new ToggleButton().overlay(GuiTextures.REVERSE.asIcon().size(12)).size(12)
                                .value(new BoolValue.Dynamic(
                                        isMoveSwitching::getBoolValue,
                                        value -> {
                                            isMoveSwitching.setBoolValue(value);
                                            selectedMoveValue.setIntValue(-1);
                                        })
                                ).align(Alignment.CenterRight)
                        )
                )
                .child(new ParentWidget<>().coverChildrenWidth().height(130)
                        //background
                        .child(new Rectangle().setColor(0xFF000000).setCornerRadius(5).asWidget().size(96, 130))
                        .child(new Rectangle().setColor(0xFF808080).setCornerRadius(5).asWidget().size(94, 128).align(Alignment.Center))
                        //moves
                        .child(new ParentWidget<>().size(92, 120).align(Alignment.Center)
                                .childIf(creature.getLearnableMoves().size() <= 5, movesColumn)
                                .childIf(creature.getLearnableMoves().size() > 5, new ListWidget<>()
                                        .size(92,120).child(movesColumn)
                                )
                        )
                );
    }
}
