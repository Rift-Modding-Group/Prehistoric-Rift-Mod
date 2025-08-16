package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ui.creatureBoxScreen.RiftNewCreatureBoxScreen;
import anightdazingzoroark.prift.client.ui.partyScreen.RiftNewPartyScreen;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import net.minecraft.client.Minecraft;

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
            RiftLibUIHelper.showUI(minecraft.player, new RiftNewPartyScreen(this));
        }
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
