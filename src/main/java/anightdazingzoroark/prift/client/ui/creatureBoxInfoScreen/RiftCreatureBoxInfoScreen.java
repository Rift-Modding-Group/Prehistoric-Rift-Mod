package anightdazingzoroark.prift.client.ui.creatureBoxInfoScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.CommonUISections;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.RiftCreatureBoxScreen;
import anightdazingzoroark.prift.client.ui.elements.RiftUISectionCreatureNBTUser;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibButton;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RiftCreatureBoxInfoScreen extends RiftLibUI {
    private final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/info_from_creature_box_background.png");
    private final SelectedCreatureInfo selectedCreatureInfo;

    public RiftCreatureBoxInfoScreen(int x, int y, int z) {
        super(x, y, z);
        this.selectedCreatureInfo = null;
    }

    public RiftCreatureBoxInfoScreen(BlockPos pos, SelectedCreatureInfo selectedCreatureInfo) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.selectedCreatureInfo = selectedCreatureInfo;
    }

    @Override
    public List<RiftLibUISection> uiSections() {
        return Arrays.asList(
                this.createSelectedCreatureOptionsSection(),
                CommonUISections.partyMemberInfoSection(this.width, this.height, 64, -17, this.fontRenderer, this.mc),
                this.createCreatureToDrawSection()
        );
    }

    private RiftLibUISection createCreatureToDrawSection() {
        return new RiftLibUISection("creatureToDrawSection", this.width, this.height, 99, 60, -62, -38, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                RiftLibUIElement.RenderedEntityElement creatureElement = new RiftLibUIElement.RenderedEntityElement();
                creatureElement.setID("creatureToDraw");
                creatureElement.setScale(20f);
                creatureElement.setNotLimitedByBounds();
                creatureElement.setAdditionalSize(0, 40);
                creatureElement.setAlignment(RiftLibUIElement.ALIGN_CENTER);
                creatureElement.setRotationAngle(150);
                toReturn.add(creatureElement);

                return toReturn;
            }
        };
    }

    private RiftUISectionCreatureNBTUser createSelectedCreatureOptionsSection() {
        return new RiftUISectionCreatureNBTUser("selectedCreatureOptionsSection", new CreatureNBT(), this.width, this.height, 100, 85, -62, 44, this.fontRenderer, this.mc) {
            @Override
            public List<RiftLibUIElement.Element> defineSectionContents() {
                List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

                if (this.nbtTagCompound != null && !this.nbtTagCompound.nbtIsEmpty()) {
                    //creature name
                    RiftLibUIElement.TextElement creatureName = new RiftLibUIElement.TextElement();
                    creatureName.setText(this.nbtTagCompound.getCreatureName(true));
                    creatureName.setScale(0.5f);
                    creatureName.setBottomSpace(6);
                    toReturn.add(creatureName);

                    //back to box
                    RiftLibUIElement.ButtonElement backToBoxButton = new RiftLibUIElement.ButtonElement();
                    backToBoxButton.setSize(100, 20);
                    backToBoxButton.setText(I18n.format("creature_box.back_to_box"));
                    backToBoxButton.setID("backToBox");
                    backToBoxButton.setBottomSpace(6);
                    toReturn.add(backToBoxButton);

                    //change name button
                    RiftLibUIElement.ButtonElement changeNameButton = new RiftLibUIElement.ButtonElement();
                    changeNameButton.setSize(100, 20);
                    changeNameButton.setText(I18n.format("creature_box.change_name"));
                    changeNameButton.setID("changeName");
                    changeNameButton.setBottomSpace(6);
                    toReturn.add(changeNameButton);

                    //release button
                    RiftLibUIElement.ButtonElement releaseButton = new RiftLibUIElement.ButtonElement();
                    releaseButton.setSize(100, 20);
                    releaseButton.setText(I18n.format("creature_box.release"));
                    releaseButton.setID("release");
                    releaseButton.setBottomSpace(6);
                    toReturn.add(releaseButton);
                }

                return toReturn;
            }
        };
    }

    private RiftUISectionCreatureNBTUser getSelectedCreatureOptionsSection() {
        return (RiftUISectionCreatureNBTUser) this.getSectionByID("selectedCreatureOptionsSection");
    }

    //get party member info section once its created
    private RiftUISectionCreatureNBTUser getPartyMemberInfoSection() {
        return (RiftUISectionCreatureNBTUser) this.getSectionByID("partyMemberInfoSection");
    }

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
        return new int[]{252, 184};
    }

    @Override
    public RiftLibUIElement.Element modifyUISectionElement(RiftLibUISection riftLibUISection, RiftLibUIElement.Element element) {
        if (riftLibUISection.id.equals("creatureToDrawSection") && element.getID().equals("creatureToDraw")) {
            RiftCreature creatureToDraw = this.selectedCreatureInfo.getCreatureNBT(this.mc.player).getCreatureAsNBT(this.mc.world);
            RiftLibUIElement.RenderedEntityElement creatureElement = (RiftLibUIElement.RenderedEntityElement) element;
            creatureElement.setEntity(creatureToDraw);
        }
        return element;
    }

    @Override
    public RiftLibUISection modifyUISection(RiftLibUISection riftLibUISection) {
        if (riftLibUISection.id.equals("selectedCreatureOptionsSection")) {
            this.getSelectedCreatureOptionsSection().setNBTTagCompound(this.selectedCreatureInfo.getCreatureNBT(this.mc.player));
        }
        if (riftLibUISection.id.equals("partyMemberInfoSection")) {
            this.getPartyMemberInfoSection().setNBTTagCompound(this.selectedCreatureInfo.getCreatureNBT(this.mc.player));
        }
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

    @Override
    protected void onPressEscape() {
        RiftLibUIHelper.showUI(this.mc.player, new RiftCreatureBoxScreen(new BlockPos(this.x, this.y, this.z), this.selectedCreatureInfo));
    }
}
