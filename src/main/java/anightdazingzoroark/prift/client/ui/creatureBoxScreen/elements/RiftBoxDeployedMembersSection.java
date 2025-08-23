package anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiftBoxDeployedMembersSection extends RiftLibUISection {
    private FixedSizeList<CreatureNBT> boxDeployedMembersNBT = new FixedSizeList<>(RiftCreatureBox.maxDeployableCreatures, new CreatureNBT());

    public RiftBoxDeployedMembersSection(int guiWidth, int guiHeight, int width, int height, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        super("creatureBoxDeployedSection", guiWidth, guiHeight, width, height, xPos, yPos, fontRenderer, minecraft);
    }

    @Override
    public List<RiftLibUIElement.Element> defineSectionContents() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //table to put box members in
        RiftLibUIElement.TableContainerElement table = new RiftLibUIElement.TableContainerElement();
        table.setRowCount(5);
        table.setCellSize(35, 35);

        for (int i = 0; i < this.boxDeployedMembersNBT.size(); i++) {
            CreatureNBT partyMember = this.boxDeployedMembersNBT.get(i);
            RiftBoxMembersSection.BoxMemberElement boxMemberElement = new RiftBoxMembersSection.BoxMemberElement();
            boxMemberElement.setPartyMemNBT(partyMember);
            boxMemberElement.setID("boxDeployedMember:"+i);
            table.addElement(boxMemberElement);
        }

        toReturn.add(table);

        return toReturn;
    }

    @Override
    protected int drawElement(RiftLibUIElement.Element element, boolean draw, int sectionWidth, int x, int y, int mouseX, int mouseY, float partialTicks) {
        int sectionTop = (this.guiHeight - this.height) / 2 + this.yPos;
        int sectionBottom = sectionTop + this.height;

        if (element instanceof RiftBoxMembersSection.BoxMemberElement) {
            RiftBoxMembersSection.BoxMemberElement boxMemberElement = (RiftBoxMembersSection.BoxMemberElement) element;

            int partyMemButtonWidth = boxMemberElement.getSize()[0];
            int partyMemButtonHeight = boxMemberElement.getSize()[1];

            if (draw) {
                int partyMemberPosX = boxMemberElement.xOffsetFromAlignment(sectionWidth, partyMemButtonWidth, x);

                RiftBoxMemButtonForBox drawnPartyMemButton = new RiftBoxMemButtonForBox(
                        boxMemberElement.getPartyMemNBT(),
                        partyMemButtonWidth,
                        partyMemButtonHeight,
                        partyMemberPosX,
                        y,
                        this.fontRenderer,
                        this.minecraft
                );
                drawnPartyMemButton.setID(boxMemberElement.getID());
                drawnPartyMemButton.doHoverEffects = this.doHoverEffects;
                drawnPartyMemButton.scrollTop = sectionTop;
                drawnPartyMemButton.scrollBottom = sectionBottom;
                if (this.selectedClickableSections.contains(boxMemberElement.getID())) drawnPartyMemButton.setSelected(true);
                drawnPartyMemButton.drawSection(mouseX, mouseY);

                this.clickableSections.add(drawnPartyMemButton);
            }

            return partyMemButtonHeight;
        }
        return super.drawElement(element, draw, sectionWidth, x, y, mouseX, mouseY, partialTicks);
    }

    public void setBoxDeployedMembersNBT(FixedSizeList<CreatureNBT> tagCompounds) {
        this.boxDeployedMembersNBT = tagCompounds;
    }

    public FixedSizeList<CreatureNBT> getBoxDeployedMembersNBT() {
        return this.boxDeployedMembersNBT;
    }

    //a new element exclusive to this section only
    public static class BoxMemberElement extends RiftLibUIElement.Element {
        private CreatureNBT partyMemNBT;
        private final int[] size = {32, 32};

        public void setPartyMemNBT(CreatureNBT tagCompound) {
            this.partyMemNBT = tagCompound;
        }

        public CreatureNBT getPartyMemNBT() {
            return this.partyMemNBT;
        }

        public int[] getSize() {
            return this.size;
        }
    }
}
