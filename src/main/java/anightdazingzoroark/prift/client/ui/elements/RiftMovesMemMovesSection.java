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

public class RiftMovesMemMovesSection extends RiftGuiScrollableSection {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/moves_background.png");
    private NBTTagCompound creatureNBT = new NBTTagCompound();
    private String selectedMoveID = "";

    public RiftMovesMemMovesSection(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(115, 124, guiWidth, guiHeight, -62, 4, fontRenderer, minecraft);
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
                        .setImage(background, 250, 202, 105, 13, 0, 129, 0, 142)
                        .setImageSelectedUV(0, 155)
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
        CreatureMove selectedMove = CreatureMove.safeValueOf(this.selectedMoveID);
        String moveDescription = selectedMove != null ? I18n.format("creature_move."+selectedMove.name().toLowerCase()+".description")
                : I18n.format("creature_move.select_move");

        toReturn.addTextElement(new RiftGuiScrollableSectionContents.TextElement()
                .setContents(moveDescription)
                .setScale(0.75f)
                .setTextColor(0xffffff)
                .setBackground(background, 250, 202, 113, 55, 105, 147)
                .setWidthOffset(4)
                .setHeightOffset(4)
                .setBGCentered()
        );

        return toReturn;
    }

    public boolean noHoveredClickableSection() {
        for (RiftClickableSection clickableSection : this.getClickableSections()) {
            if (clickableSection.isHovered) return false;
        }
        return true;
    }
}
