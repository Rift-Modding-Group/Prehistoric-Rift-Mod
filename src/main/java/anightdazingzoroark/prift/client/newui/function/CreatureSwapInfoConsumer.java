package anightdazingzoroark.prift.client.newui.function;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;

public interface CreatureSwapInfoConsumer {
    void accept(SelectedCreatureInfo.SwapInfo swapInfo);
}
