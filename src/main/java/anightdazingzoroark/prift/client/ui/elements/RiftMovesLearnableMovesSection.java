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

public class RiftMovesLearnableMovesSection extends RiftGuiScrollableSection {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/moves_background.png");
    private NBTTagCompound creatureNBT = new NBTTagCompound();
    private String selectedMoveID = "";

    public RiftMovesLearnableMovesSection(int guiWidth, int guiHeight, FontRenderer fontRenderer, Minecraft minecraft) {
        super(105, 95, guiWidth, guiHeight, 62, 9, fontRenderer, minecraft);
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

        //get learnable moves
        if (!this.creatureNBT.isEmpty()) {
            //get moves
            List<NBTBase> learnableMovesNBT = this.creatureNBT.getTagList("LearnableMoves", 10).tagList;
            List<NBTBase> learnedMovesNBT = this.creatureNBT.getTagList("LearnedMoves", 10).tagList;
            learnableMovesNBT.removeIf(learnedMovesNBT::contains);

            //add clickable sections for each move
            for (NBTBase nbtBase : learnableMovesNBT) {
                //get move from nbt
                NBTTagCompound moveNBT = (NBTTagCompound) nbtBase;
                CreatureMove move = CreatureMove.values()[moveNBT.getInteger("Move")];

                //make clickable section
                toReturn.addClickableSectionElement(new RiftGuiScrollableSectionContents.ClickableSectionElement()
                        .setSize(105, 13)
                        .setImage(background, 250, 202, 105, 13, 0, 129, 0, 142)
                        .setImageSelectedUV(0, 155)
                        .setTextOffsets(0, 1)
                        .setTextContent(I18n.format("creature_move." + move.name().toLowerCase() + ".name"))
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

        return toReturn;
    }

    public boolean noHoveredClickableSection() {
        for (RiftClickableSection clickableSection : this.getClickableSections()) {
            if (clickableSection.isHovered) return false;
        }
        return true;
    }
}
