package anightdazingzoroark.prift.client.ui.movesScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.partyScreen.RiftNewPartyScreen;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class RiftNewMovesScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/moves_background.png");
    private final SelectedCreatureInfo selectedCreature;

    public RiftNewMovesScreen(SelectedCreatureInfo selectedCreature) {
        super(0, 0, 0);
        this.selectedCreature = selectedCreature;
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Collections.emptyList();
    }

    @Override
    public ResourceLocation drawBackground() {
        return background;
    }

    @Override
    public int[] backgroundTextureSize() {
        return new int[]{250, 202};
    }

    @Override
    public int[] backgroundUV() {
        return new int[]{0, 0};
    }

    @Override
    public int[] backgroundSize() {
        return new int[]{249, 129};
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {
        return element;
    }

    @Override
    public RiftLibUISection modifyUISection(RiftLibUISection riftLibUISection) {
        return riftLibUISection;
    }

    @Override
    public void onButtonClicked(RiftLibButton riftLibButton) {

    }

    @Override
    public void onClickableSectionClicked(RiftLibClickableSection riftLibClickableSection) {

    }

    @Override
    protected void onPressEscape() {
        RiftLibUIHelper.showUI(this.mc.player, new RiftNewPartyScreen(this.selectedCreature));
    }
}
