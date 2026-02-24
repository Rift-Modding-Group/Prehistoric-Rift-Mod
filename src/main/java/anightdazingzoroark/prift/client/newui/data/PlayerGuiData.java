package anightdazingzoroark.prift.client.newui.data;

import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressHelper;
import anightdazingzoroark.prift.server.capabilities.playerParty.IPlayerParty;
import anightdazingzoroark.prift.server.capabilities.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerGuiData extends GuiData {
    //party relevant stuff
    public boolean isMoveSwitchingUI;
    public IPlayerParty playerParty;
    public SelectedCreatureInfo.SwapInfo creatureSwapInfo = new SelectedCreatureInfo.SwapInfo();

    //journal relevant stuff
    public IPlayerJournalProgress playerJournalProgress;
    public RiftCreatureType creatureType;
    public RiftCreatureType.CreatureCategory creatureCategory;

    public PlayerGuiData(@NotNull EntityPlayer player) {
        super(player);
        this.playerParty = PlayerPartyHelper.getPlayerParty(player);
        this.playerJournalProgress = PlayerJournalProgressHelper.getPlayerJournalProgress(player);
    }
}
