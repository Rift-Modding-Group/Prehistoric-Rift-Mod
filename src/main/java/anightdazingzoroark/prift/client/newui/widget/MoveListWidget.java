package anightdazingzoroark.prift.client.newui.widget;

import anightdazingzoroark.prift.client.newui.data.CreatureGuiData;
import anightdazingzoroark.prift.client.newui.value.MoveSwapInfoSyncValue;
import anightdazingzoroark.prift.client.newui.value.SelectedMoveInfoSyncValue;
import anightdazingzoroark.prift.client.newui.holder.SelectedMoveInfo;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.widgets.ListValueWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class MoveListWidget extends ListValueWidget<CreatureMove, MoveListWidget.MoveButton, MoveListWidget> {
    private final CreatureGuiData creatureGuiData;
    private final SelectedMoveInfoSyncValue selectedMoveInfo;
    private final MoveSwapInfoSyncValue moveSwapInfo;
    private final BooleanSyncValue isMoveSwitching;
    private final SelectedMoveInfo.SelectedMoveType section;

    public MoveListWidget(CreatureGuiData creatureGuiData, SelectedMoveInfoSyncValue selectedMoveInfo,
                          SelectedMoveInfo.SelectedMoveType section, MoveSwapInfoSyncValue moveSwapInfo,
                          BooleanSyncValue isMoveSwitching) {
        super(MoveButton::getCreatureMove);
        this.childSeparator(IIcon.EMPTY.asIcon().height(5));
        this.creatureGuiData = creatureGuiData;
        this.selectedMoveInfo = selectedMoveInfo;
        this.section = section;
        this.moveSwapInfo = moveSwapInfo;
        this.isMoveSwitching = isMoveSwitching;
        this.createChildren();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.moveSwapInfo.getValue().getCanUpdate(this.section)) {
            this.removeAll();
            this.createChildren();
            this.scheduleResize();
            this.selectedMoveInfo.setValue(null);
        }
    }

    private void createChildren() {
        List<CreatureMove> moveList = this.section == SelectedMoveInfo.SelectedMoveType.LEARNT ?
                this.creatureGuiData.getLearnedMoves().getList() : this.creatureGuiData.getLearnableMoves();

        this.children(moveList, move -> new MoveButton(move, this.section));

        if (this.section == SelectedMoveInfo.SelectedMoveType.LEARNABLE) {
            this.child(new MoveButton(null, SelectedMoveInfo.SelectedMoveType.LEARNABLE));
        }
    }

    @Override
    public boolean layoutWidgets() {
        if (!this.hasChildren()) return true;
        if (!this.resizer().isSizeCalculated(getAxis())) return false;
        GuiAxis axis = getAxis();
        int separatorSize = 5;
        int p = this.getArea().getPadding().getStart(axis);

        List<IWidget> orderedChildren = this.getOrderedChildren();
        for (int widgetIndex = 0; widgetIndex < orderedChildren.size(); widgetIndex++) {
            IWidget widget = orderedChildren.get(widgetIndex);

            if (shouldIgnoreChildSize(widget)) {
                widget.resizer().updateResized();
                continue;
            }
            if (widget.resizer().hasPos(axis)) {
                widget.resizer().updateResized(); // this is required when the widget has a pos on the main axis, but not on the cross axis
                continue;
            }
            if (!widget.resizer().isSizeCalculated(axis)) return false;

            p += widget.getArea().getMargin().getStart(axis);
            widget.getArea().setRelativePoint(axis, p);
            p += widget.getArea().getSize(axis) + widget.getArea().getMargin().getEnd(axis);
            widget.resizer().setPosResized(axis, true);
            widget.resizer().setMarginPaddingApplied(true);

            if (widgetIndex + 1 < orderedChildren.size() && this.getOrderedChildren().get(widgetIndex + 1).isEnabled()) p += separatorSize;
        }

        int size = p + this.getArea().getPadding().getEnd(axis);
        getScrollData().setScrollSize(size);
        int widgetSize = this.getArea().getSize(axis);
        if (size < widgetSize) {
            this.getArea().setSize(getAxis(), size);
            this.resizer().setSizeResized(axis, true);
            if (this.resizer().isPosCalculated(axis)) this.resizer().setPosResized(axis, false);
        }
        return true;
    }

    private boolean hasMove(CreatureMove move) {
        if (this.getChildren().isEmpty()) return false;
        for (IWidget widget : this.getChildren()) {
            if (!(widget instanceof MoveButton moveButton)) continue;
            if (moveButton.creatureMove == move) return true;
        }
        return false;
    }

    private int getMoveIndex(CreatureMove move) {
        if (!this.hasMove(move)) return -1;
        for (int index = 0; index < this.getChildren().size(); index++) {
            IWidget widget = this.getChildren().get(index);
            if (!(widget instanceof MoveButton moveButton)) continue;
            if (moveButton.creatureMove == move) return index;
        }
        return -1;
    }

    @Override
    public int getDefaultWidth() {
        return 90;
    }

    public static class MoveButton extends ToggleButton {
        private final CreatureMove creatureMove;
        private final SelectedMoveInfo.SelectedMoveType section;
        private MoveListWidget parent;

        public MoveButton(CreatureMove creatureMoveToAdd, SelectedMoveInfo.SelectedMoveType sectionToAdd) {
            super();
            this.size(80, 20);
            this.creatureMove = creatureMoveToAdd;
            this.section = sectionToAdd;
            this.value(new BoolValue.Dynamic(
                    () -> {
                        if (parent == null || parent.selectedMoveInfo.getValue() == null || creatureMove == null) return false;
                        return parent.selectedMoveInfo.getValue().movePos == parent.getMoveIndex(creatureMove)
                                && parent.selectedMoveInfo.getValue().moveType == section
                                && section == parent.section;
                    },
                    value -> {
                        if (parent == null) return;
                        boolean canHaveIndex = section == SelectedMoveInfo.SelectedMoveType.LEARNT ||
                                (section == SelectedMoveInfo.SelectedMoveType.LEARNABLE && creatureMove != null);
                        int buttonIndex = canHaveIndex ? parent.getMoveIndex(creatureMove) : -1;

                        //set selected move
                        if (creatureMove != null
                                || (parent.isMoveSwitching.getBoolValue() && parent.moveSwapInfo.getValue().canChooseNextMove())) {
                            parent.selectedMoveInfo.setValue(new SelectedMoveInfo(section, buttonIndex));
                        }

                        //move swapping is managed beyond this point
                        if (!parent.isMoveSwitching.getBoolValue()) return;

                        if (creatureMove != null || parent.moveSwapInfo.getValue().canChooseNextMove()) {
                            parent.moveSwapInfo.getValue().setMove(parent.selectedMoveInfo.getValue());
                            if (!parent.moveSwapInfo.getValue().canSwap()) return;
                            parent.moveSwapInfo.getValue().applySwap(parent.creatureGuiData);
                        }
                    })
            );
            this.overlay(IKey.dynamic(() -> {
                if (creatureMove != null) return creatureMove.getTranslatedName();
                else return I18n.format("creature_move.no_move");
            }));
        }

        @Override
        public void onInit() {
            super.onInit();
            if (this.getParent() instanceof MoveListWidget parent) this.parent = parent;
            if (this.section == SelectedMoveInfo.SelectedMoveType.LEARNABLE
                    && this.creatureMove == null) this.setEnabled(false);
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            if (this.parent == null) return;
            if (this.section == SelectedMoveInfo.SelectedMoveType.LEARNABLE
                    && this.creatureMove == null) this.setEnabled(this.parent.isMoveSwitching.getBoolValue());
        }

        public CreatureMove getCreatureMove() {
            return this.creatureMove;
        }
    }
}
