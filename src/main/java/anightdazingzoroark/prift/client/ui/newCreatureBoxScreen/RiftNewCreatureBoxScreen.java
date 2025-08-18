package anightdazingzoroark.prift.client.ui.newCreatureBoxScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUser;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RiftNewCreatureBoxScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/new_creature_box_background.png");

    public RiftNewCreatureBoxScreen(int x, int y, int z) {
        super(x, y, z);
    }

    public RiftNewCreatureBoxScreen(BlockPos pos) {
        super(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Arrays.asList(
                this.createPartyHeaderSection()
        );
    }

    private RiftLibUISection createPartyHeaderSection() {
        return new RiftLibUISection("partyHeaderSection", this.width, this.height, 40, 9, -88, -96, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.TextElement headerElement = new RiftLibUIElement.TextElement();
                headerElement.setText(I18n.format("journal.party_label.party"));
                toReturn.add(headerElement);

                return toReturn;
            }
        };
    }

    /*
    private RiftUISectionCreatureNBTUser createPartyMembersSection() {
        return new RiftUISectionCreatureNBTUser("partyMembersSection", th) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                return Collections.emptyList();
            }
        };
    }
     */

    @Override
    public ResourceLocation drawBackground() {
        return this.background;
    }

    @Override
    public int[] backgroundTextureSize() {
        return new int[]{400, 300};
    }

    @Override
    public int[] backgroundUV() {
        return new int[]{0, 0};
    }

    @Override
    public int[] backgroundSize() {
        return new int[]{227, 246};
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
    public void onElementHovered(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {

    }
}
