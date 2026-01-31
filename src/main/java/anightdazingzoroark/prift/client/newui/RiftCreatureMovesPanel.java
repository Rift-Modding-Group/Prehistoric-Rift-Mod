package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
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

        return new ParentWidget<>().padding(7, 7).coverChildrenHeight().width(220)
                .child(new Row().coverChildren().childPadding(5)
                        //left side is the creature's current moves
                        .child(new ParentWidget<>().coverChildrenWidth().height(147)
                                .child(currentMovesSide(creature, selectedMoveValue, selectedMoveFromRight))
                        )
                        //separator line
                        .child(new Rectangle().setColor(0xFF000000).asWidget().size(1, 108))
                        //right side is info
                        .child(new ParentWidget<>().coverChildrenWidth().height(147)
                                .child(learnableMovesSide(creature, selectedMoveValue, selectedMoveFromRight))
                        )
                );
    }

    private static Flow currentMovesSide(RiftCreature creature, IntSyncValue selectedMoveValue, BooleanSyncValue selectedMoveFromRight) {
        Flow toReturn = new Column().debugName("leftSide")
                .childPadding(5).coverChildren()
                .child(IKey.lang("tamepanel.current_moves", creature.getName(false)).asWidget().scale(0.75f));

        //add current creature moves
        List<CreatureMove> creatureMoveList = creature.getLearnedMoves();
        for (int i = 0; i < 3; i++) {
            if (i < creatureMoveList.size()) {
                CreatureMove creatureMove = creatureMoveList.get(i);
                int finalI = i;
                toReturn.child(new ButtonWidget<>().size(80, 20)
                        .onMousePressed(button -> {
                            selectedMoveValue.setIntValue(finalI);
                            selectedMoveFromRight.setBoolValue(false);
                            return true;
                        })
                        .overlay(IKey.str(creatureMove.getTranslatedName()))
                );
            }
            else toReturn.child(new ButtonWidget<>().size(80, 20)
                    .overlay(IKey.lang("creature_move.no_move"))
            );
        }

        //add box where info about selected move will be placed
        toReturn.child(new ParentWidget<>().size(96, 60)
                .child(new Rectangle().setColor(0xFF000000).setCornerRadius(5).asWidget().size(96, 60))
                .child(new Rectangle().setColor(0xFF808080).setCornerRadius(5).asWidget().size(94, 58).align(Alignment.Center))
                .child(new ParentWidget<>().size(90, 54).align(Alignment.Center)
                        .child(IKey.dynamic(() -> {
                            if (selectedMoveValue.getIntValue() < 0) return I18n.format("creature_move.none_selected.description");
                            else if (selectedMoveFromRight.getBoolValue()) return creature.getLearnableMoves().get(selectedMoveValue.getIntValue()).getTranslatedDescription();
                            return creatureMoveList.get(selectedMoveValue.getIntValue()).getTranslatedDescription();
                        }).asWidget().scale(0.75f).align(Alignment.TopLeft))
                )
        );

        return toReturn;
    }

    private static Flow learnableMovesSide(RiftCreature creature, IntSyncValue selectedMoveValue, BooleanSyncValue selectedMoveFromRight) {
        Flow toReturn = new Column().debugName("rightSide")
                .childPadding(5).coverChildren()
                .child(new ParentWidget<>().width(96).coverChildrenHeight()
                        .child(IKey.lang("tamepanel.available_moves").asWidget().scale(0.75f).align(Alignment.CenterLeft))
                        .child(new ButtonWidget<>().overlay(GuiTextures.REVERSE.asIcon().size(12)).size(12).align(Alignment.CenterRight))
                );

        ParentWidget<?> moveList = new ParentWidget<>().coverChildrenWidth().height(130);

        //background
        moveList.child(new Rectangle().setColor(0xFF000000).setCornerRadius(5).asWidget().size(96, 130))
                .child(new Rectangle().setColor(0xFF808080).setCornerRadius(5).asWidget().size(94, 128).align(Alignment.Center));

        //moves column
        Flow movesColumn = new Column().coverChildrenWidth().height(120).childPadding(3).align(Alignment.Center);
        List<CreatureMove> creatureMoveList = creature.getLearnableMoves();
        for (int i = 0; i < creatureMoveList.size(); i++) {
            CreatureMove creatureMove = creatureMoveList.get(i);
            int finalI = i;
            movesColumn.child(new ButtonWidget<>().size(80, 20)
                    .onMousePressed(button -> {
                        selectedMoveValue.setIntValue(finalI);
                        selectedMoveFromRight.setBoolValue(true);
                        return true;
                    })
                    .overlay(IKey.str(creatureMove.getTranslatedName()))
            );
        }
        moveList.child(new ParentWidget<>().size(92, 120).child(movesColumn).align(Alignment.Center));

        return toReturn.child(moveList);
    }
}
