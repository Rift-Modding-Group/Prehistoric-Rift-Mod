package anightdazingzoroark.prift.client.ui.newCreatureBoxScreen.elements;

import anightdazingzoroark.prift.client.ui.partyScreen.elements.RiftPartyMemButtonForParty;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.riftlib.ui.RiftLibUISection;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibUIElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

public class RiftPartyMembersSection extends RiftLibUISection {
    private FixedSizeList<CreatureNBT> partyMembersNBT = new FixedSizeList<>(6, new CreatureNBT());

    public RiftPartyMembersSection(int guiWidth, int guiHeight, int width, int height, int xPos, int yPos, FontRenderer fontRenderer, Minecraft minecraft) {
        super("partyMembersSection", guiWidth, guiHeight, width, height, xPos, yPos, fontRenderer, minecraft);
    }

    @Override
    public List<RiftLibUIElement.Element> defineSectionContents() {
        List<RiftLibUIElement.Element> toReturn = new ArrayList<>();

        //draw table containing elements
        RiftLibUIElement.TableContainerElement table = new RiftLibUIElement.TableContainerElement();
        table.setRowCount(2);
        table.setCellSize(22, 22);

        //draw party members
        for (int i = 0; i < this.partyMembersNBT.size(); i++) {
            CreatureNBT partyMember = this.partyMembersNBT.get(i);
            PartyMemberElement partyMemberElement = new PartyMemberElement();
            partyMemberElement.setPartyMemNBT(partyMember);
            partyMemberElement.setID("partyMember:"+i);
            table.addElement(partyMemberElement);
        }
        toReturn.add(table);

        return toReturn;
    }

    @Override
    protected int drawElement(RiftLibUIElement.Element element, boolean draw, int sectionWidth, int x, int y, int mouseX, int mouseY, float partialTicks) {
        int sectionTop = (this.guiHeight - this.height) / 2 + this.yPos;
        int sectionBottom = sectionTop + this.height;

        if (element instanceof PartyMemberElement) {
            PartyMemberElement partyMemberElement = (PartyMemberElement) element;

            int partyMemButtonWidth = partyMemberElement.getSize()[0];
            int partyMemButtonHeight = partyMemberElement.getSize()[1];

            if (draw) {
                int partyMemberPosX = partyMemberElement.xOffsetFromAlignment(sectionWidth, partyMemButtonWidth, x);

                RiftPartyMemButtonForBox drawnPartyMemButton = new RiftPartyMemButtonForBox(
                        partyMemberElement.getPartyMemNBT(),
                        partyMemButtonWidth,
                        partyMemButtonHeight,
                        partyMemberPosX,
                        y,
                        this.fontRenderer,
                        this.minecraft
                );
                drawnPartyMemButton.setID(partyMemberElement.getID());
                drawnPartyMemButton.doHoverEffects = this.doHoverEffects;
                drawnPartyMemButton.scrollTop = sectionTop;
                drawnPartyMemButton.scrollBottom = sectionBottom;
                if (this.selectedClickableSections.contains(partyMemberElement.getID())) drawnPartyMemButton.setSelected(true);
                drawnPartyMemButton.drawSection(mouseX, mouseY);

                this.clickableSections.add(drawnPartyMemButton);
            }

            return partyMemButtonHeight;
        }
        return super.drawElement(element, draw, sectionWidth, x, y, mouseX, mouseY, partialTicks);
    }

    public void setPartyMembersNBT(FixedSizeList<CreatureNBT> tagCompounds) {
        this.partyMembersNBT = tagCompounds;
    }

    public FixedSizeList<CreatureNBT> getPartyMembersNBT() {
        return this.partyMembersNBT;
    }

    //a new element exclusive to this section only
    public static class PartyMemberElement extends RiftLibUIElement.Element {
        private CreatureNBT partyMemNBT;
        private final int[] size = {20, 20};

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
