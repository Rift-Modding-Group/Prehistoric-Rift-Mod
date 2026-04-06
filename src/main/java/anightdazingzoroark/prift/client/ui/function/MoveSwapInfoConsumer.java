package anightdazingzoroark.prift.client.ui.function;

import anightdazingzoroark.prift.client.ui.holder.SelectedMoveInfo;

public interface MoveSwapInfoConsumer {
    void accept(SelectedMoveInfo.SwapInfo swapInfo);
}
