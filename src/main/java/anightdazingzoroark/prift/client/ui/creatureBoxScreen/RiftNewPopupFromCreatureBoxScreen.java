package anightdazingzoroark.prift.client.ui.creatureBoxScreen;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class RiftNewPopupFromCreatureBoxScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/popup_from_radial.png");

    //for creature selection
    private final RiftNewCreatureBoxScreen.SelectedPosType selectedPosType;
    private final int selectedPosOne;
    private final int selectedPosTwo;

    public RiftNewPopupFromCreatureBoxScreen(int selectedPosType, int selectedPosOne, int selectedPosTwo) {
        super();
        this.selectedPosType = RiftNewCreatureBoxScreen.SelectedPosType.values()[selectedPosType];
        this.selectedPosOne = selectedPosOne;
        this.selectedPosTwo = selectedPosTwo;
    }

    @Override
    public void initGui() {

    }

    public static enum PopupFromCreatureBox {
        REMOVE_INVENTORY,
        CHANGE_NAME,
        RELEASE,
        NO_CREATURES,
        OWNED_BY_OTHER,
        CREATURE_REVIVING
    }
}
