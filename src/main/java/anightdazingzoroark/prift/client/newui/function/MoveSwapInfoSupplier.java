package anightdazingzoroark.prift.client.newui.function;

import anightdazingzoroark.prift.client.newui.holder.SelectedMoveInfo;

@FunctionalInterface
public interface MoveSwapInfoSupplier {
    SelectedMoveInfo.SwapInfo getSwapInfo();
}
