package anightdazingzoroark.prift.client.newui.data;

import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerGuiData extends GuiData {
    //menu stuff
    public boolean isMoveSwitchingUI;
    public SelectedCreatureInfo.SwapInfo creatureSwapInfo = new SelectedCreatureInfo.SwapInfo();

    public PlayerGuiData(@NotNull EntityPlayer player) {
        super(player);
    }
}
