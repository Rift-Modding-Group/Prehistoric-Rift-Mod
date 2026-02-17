package anightdazingzoroark.prift.client.newui.function;

import anightdazingzoroark.prift.client.newui.holder.SelectedMoveInfo;

public interface MoveSwapInfoConsumer {
    void accept(SelectedMoveInfo.SwapInfo swapInfo);
}
