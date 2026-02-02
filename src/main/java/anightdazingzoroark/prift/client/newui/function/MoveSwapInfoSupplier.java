package anightdazingzoroark.prift.client.newui.function;

import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;

@FunctionalInterface
public interface MoveSwapInfoSupplier {
    SelectedMoveInfo.SwapInfo getSwapInfo();
}
