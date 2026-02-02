package anightdazingzoroark.prift.client.newui.function;

import anightdazingzoroark.prift.client.ui.SelectedMoveInfo;

public interface MoveSwapInfoConsumer {
    void accept(SelectedMoveInfo.SwapInfo swapInfo);
}
