package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class RiftPartyMemMovesSection extends RiftGuiScrollableSection {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");
    private NBTTagCompound creatureNBT;
    private String selectedMoveID = "";

    public RiftPartyMemMovesSection(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(115, 124, guiWidth, guiHeight, 124, -13, fontRenderer, minecraft);
    }

    public void setCreatureNBT(NBTTagCompound tagCompound) {
        this.creatureNBT = tagCompound;
    }

    public NBTTagCompound getCreatureNBT() {
        return this.creatureNBT;
    }

    public void setSelectedMove(String moveID) {
        this.selectedMoveID = moveID;
    }

    public String getSelectedMoveID() {
        return this.selectedMoveID;
    }

    @Override
    public RiftGuiScrollableSectionContents defineSectionContents() {
        RiftGuiScrollableSectionContents toReturn = new RiftGuiScrollableSectionContents();

        //header
        toReturn.addTextElement(new RiftGuiScrollableSectionContents.TextElement()
                .setContents(I18n.format("journal.party_label.moves"))
        );

        if (this.creatureNBT != null && !this.creatureNBT.isEmpty()) {
            //get moves
            List<NBTBase> creatureMovesNBT = this.creatureNBT.getTagList("LearnedMoves", 10).tagList;

            //add clickable sections for each move
            for (int i = 0; i < 3; i++) {
                //get move from nbt
                NBTTagCompound moveNBT = (NBTTagCompound) creatureMovesNBT.get(i);
                CreatureMove move = CreatureMove.values()[moveNBT.getInteger("Move")];

                //make clickable section
                toReturn.addClickableSectionElement(new RiftGuiScrollableSectionContents.ClickableSectionElement()
                        .setSize(105, 13)
                        .setImage(background, 400, 360, 105, 13, 287, 237, 287, 250)
                        .setImageSelectedUV(287, 263)
                        .setTextOffsets(0, 1)
                        .setTextContent(I18n.format("creature_move."+move.name().toLowerCase()+".name"))
                        .setTextHoveredColor(0xffffff)
                        .setTextSelectedColor(0xffff00)
                        .setID(move.name())
                        .setImageScale(0.75f)
                        .setTextScale(0.75f)
                        .setBottomSpace(3)
                        .setCentered()
                );
            }
        }

        //add move description
        if (this.selectedMoveID != null && !this.selectedMoveID.isEmpty()) {
            CreatureMove selectedMove = CreatureMove.valueOf(this.selectedMoveID);

            toReturn.addTextElement(new RiftGuiScrollableSectionContents.TextElement()
                    .setContents(I18n.format("creature_move."+selectedMove.name().toLowerCase()+".description"))
                    .setScale(0.75f)
                    .setTextColor(0xffffff)
                    .setBackground(background, 400, 360, 113, 55, 287, 182)
                    .setWidthOffset(4)
                    .setHeightOffset(4)
                    .setBGCentered()
            );
        }

        return toReturn;
    }
}
