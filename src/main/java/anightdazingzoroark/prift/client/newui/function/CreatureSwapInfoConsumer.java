package anightdazingzoroark.prift.client.newui.function;

import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;

public interface CreatureSwapInfoConsumer {
    void accept(SelectedCreatureInfo.SwapInfo swapInfo);
}
