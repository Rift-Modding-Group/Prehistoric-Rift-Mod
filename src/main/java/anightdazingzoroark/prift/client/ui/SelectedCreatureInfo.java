package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.partyScreen.RiftPartyScreen;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

//this helper class is for sending information to UIs and no less
public class SelectedCreatureInfo {
    public final SelectedPosType selectedPosType;
    public final int[] pos;
    private MenuOpenedFrom menuOpenedFrom;

    public SelectedCreatureInfo(SelectedPosType selectedPosType, int[] pos) {
        this.selectedPosType = selectedPosType;
        this.pos = pos;
    }

    public void setMenuOpenedFrom(MenuOpenedFrom value) {
        this.menuOpenedFrom = value;
    }

    public MenuOpenedFrom getMenuOpenedFrom() {
        return this.menuOpenedFrom;
    }

    public void exitToLastMenu(Minecraft minecraft) {
        if (this.menuOpenedFrom == MenuOpenedFrom.PARTY) {
            RiftLibUIHelper.showUI(minecraft.player, new RiftPartyScreen(this));
        }
    }

    public CreatureNBT getCreatureNBT(EntityPlayer player) {
        if (this.selectedPosType == SelectedPosType.PARTY) return NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(player).get(this.pos[0]);
        return new CreatureNBT();
    }

    public enum SelectedPosType {
        PARTY,
        BOX,
        BOX_DEPLOYED
    }

    public enum MenuOpenedFrom {
        PARTY,
        BOX
    }
}
