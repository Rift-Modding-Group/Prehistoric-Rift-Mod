package anightdazingzoroark.prift.client.ui.function;

import anightdazingzoroark.prift.client.ui.holder.SelectedMoveInfo;

@FunctionalInterface
public interface MoveSwapInfoSupplier {
    SelectedMoveInfo.SwapInfo getSwapInfo();
}
