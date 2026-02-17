package anightdazingzoroark.prift.client.newui.data;

import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerParty.IPlayerParty;
import anightdazingzoroark.prift.server.capabilities.playerParty.PlayerPartyHelper;
import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerGuiData extends GuiData {
    //menu stuff
    public boolean isMoveSwitchingUI;
    public IPlayerParty playerParty;
    public SelectedCreatureInfo.SwapInfo creatureSwapInfo = new SelectedCreatureInfo.SwapInfo();

    public PlayerGuiData(@NotNull EntityPlayer player) {
        super(player);
        this.playerParty = PlayerPartyHelper.getPlayerParty(player);
    }
}
