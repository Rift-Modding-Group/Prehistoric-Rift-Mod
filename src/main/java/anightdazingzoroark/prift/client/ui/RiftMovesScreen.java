package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftClickableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftMovesLearnableMovesSection;
import anightdazingzoroark.prift.client.ui.elements.RiftMovesMemMovesSection;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class RiftMovesScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/moves_background.png");
    private final int pos; //for now only party will be affected, there will be more ints that will direct to box and position in box
    private RiftMovesMemMovesSection memberCurrentMoves;
    private RiftClickableSection returnToPartyButton;
    private RiftMovesLearnableMovesSection learnableMovesSection;

    //for switching
    private int learnedMoveToSwitch = -1;
    private String learnableMoveToSwitch = "";

    public RiftMovesScreen(int pos) {
        super();
        this.pos = pos;
    }

    @Override
    public void initGui() {
        super.initGui();

        //create current moves
        this.memberCurrentMoves = new RiftMovesMemMovesSection(this.width, this.height, this.fontRenderer, this.mc);
        this.memberCurrentMoves.setCreatureNBT(this.getNBTFromPos());

        //create button to go back to party
        this.returnToPartyButton = new RiftClickableSection(20, 18, this.width, this.height, -10, -49, this.fontRenderer, this.mc);
        this.returnToPartyButton.addImage(background, 20, 18, 250, 202, 105, 129, 125, 129);
        this.returnToPartyButton.setScale(0.75f);

        //create section for learnable moves
        this.learnableMovesSection = new RiftMovesLearnableMovesSection(this.width, this.height, this.fontRenderer, this.mc);
        this.learnableMovesSection.setCreatureNBT(this.getNBTFromPos());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        //constantly update as long as screen is open
        this.updateMoves();

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //when hovering over a move button, set move description
        for (RiftClickableSection moveSection : this.memberCurrentMoves.getClickableSections()) {
            if (moveSection.isHovered(mouseX, mouseY)) {
                this.memberCurrentMoves.setSelectedMove(moveSection.getStringID());
            }
        }

        //draw current moves
        this.memberCurrentMoves.drawSectionContents(mouseX, mouseY, partialTicks);

        //draw back button
        this.returnToPartyButton.drawSection(mouseX, mouseY);

        //draw header for available moves
        String availableMovesLabel = I18n.format("journal.moves_label.available_moves");
        int partyLabelX = this.width / 2 + 5;
        int partyLabelY = (this.height - this.fontRenderer.FONT_HEIGHT) / 2 - 54;
        this.fontRenderer.drawString(availableMovesLabel, partyLabelX, partyLabelY, 0x000000);

        //draw learnable moves section
        this.learnableMovesSection.drawSectionContents(mouseX, mouseY, partialTicks);
        //when hovering over a learnable move, set move description
        for (RiftClickableSection moveSection : this.learnableMovesSection.getClickableSections()) {
            if (moveSection.isHovered(mouseX, mouseY)) {
                this.memberCurrentMoves.setSelectedMove(moveSection.getStringID());
            }
        }

        //if no move button is hovered over, clear move description
        if (this.memberCurrentMoves.noHoveredClickableSection() && this.learnableMovesSection.noHoveredClickableSection()) {
            this.memberCurrentMoves.setSelectedMove("");
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //for going back to party after clicking the back button
        if (this.returnToPartyButton.isHovered(mouseX, mouseY)) {
            this.returnToPartyButton.playPressSound(this.mc.getSoundHandler());
            this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_PARTY, this.mc.world, this.pos, 0, 0);
        }

        //for selecting move in current moves for swapping
        for (int i = 0; i < this.memberCurrentMoves.getClickableSections().size(); i++) {
            RiftClickableSection currentMove = this.memberCurrentMoves.getClickableSections().get(i);
            if (currentMove.isHovered(mouseX, mouseY)) {
                //select learnt move
                if (this.learnedMoveToSwitch < 0 && this.learnableMoveToSwitch.isEmpty()) {
                    this.learnedMoveToSwitch = i;
                    this.memberCurrentMoves.setSelectedMove(currentMove.getStringID());
                    this.memberCurrentMoves.selectClickableSectionById(currentMove.getStringID());
                    currentMove.playPressSound(this.mc.getSoundHandler());
                }
                //switch this learnt move with an already selected learnable move
                else if (!this.learnableMoveToSwitch.isEmpty()) {
                    NewPlayerTamedCreaturesHelper.partyMemSwapLearntMoveWithLearnableMove(this.mc.player, this.pos, i, this.learnableMoveToSwitch);

                    //reset
                    this.learnedMoveToSwitch = -1;
                    this.learnableMoveToSwitch = "";
                    this.learnableMovesSection.setSelectedMove("");
                    this.learnableMovesSection.unselectAllClickableSections();
                    currentMove.playPressSound(this.mc.getSoundHandler());
                }
                //switch this learnt move with an already selected learnt move
                else {
                    NewPlayerTamedCreaturesHelper.partyMemSwapLearntMoves(this.mc.player, this.pos, this.learnedMoveToSwitch, i);

                    //reset
                    this.learnedMoveToSwitch = -1;
                    this.learnableMoveToSwitch = "";
                    this.memberCurrentMoves.setSelectedMove("");
                    this.memberCurrentMoves.unselectAllClickableSections();
                    this.learnableMovesSection.unselectAllClickableSections();
                    currentMove.playPressSound(this.mc.getSoundHandler());
                }
            }
        }

        //for selecting move in learnable moves for swapping
        for (int i = 0; i < this.learnableMovesSection.getClickableSections().size(); i++) {
            RiftClickableSection currentMove = this.learnableMovesSection.getClickableSections().get(i);
            if (currentMove.isHovered(mouseX, mouseY)) {
                //select learnable move
                if (this.learnedMoveToSwitch < 0 && this.learnableMoveToSwitch.isEmpty()) {
                    this.learnableMoveToSwitch = currentMove.getStringID();
                    this.learnableMovesSection.setSelectedMove(currentMove.getStringID());
                    this.learnableMovesSection.unselectAllClickableSections();
                    this.learnableMovesSection.selectClickableSectionById(currentMove.getStringID());
                    currentMove.playPressSound(this.mc.getSoundHandler());
                }
                //deselect learnable move if it is selected twice
                else if (this.learnedMoveToSwitch < 0 && this.learnableMoveToSwitch.equals(currentMove.getStringID())) {
                    //reset
                    this.learnedMoveToSwitch = -1;
                    this.learnableMoveToSwitch = "";
                    this.memberCurrentMoves.setSelectedMove("");
                    this.memberCurrentMoves.unselectAllClickableSections();
                    this.learnableMovesSection.unselectAllClickableSections();
                    currentMove.playPressSound(this.mc.getSoundHandler());
                }
                //switch this learnable move with an already selected learnt move
                else {
                    NewPlayerTamedCreaturesHelper.partyMemSwapLearntMoveWithLearnableMove(this.mc.player, this.pos, this.learnedMoveToSwitch, currentMove.getStringID());

                    //reset
                    this.learnedMoveToSwitch = -1;
                    this.learnableMoveToSwitch = "";
                    this.memberCurrentMoves.setSelectedMove("");
                    this.memberCurrentMoves.unselectAllClickableSections();
                    this.learnableMovesSection.unselectAllClickableSections();
                    currentMove.playPressSound(this.mc.getSoundHandler());
                }
            }
        }
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - 249) / 2;
        int l = (this.height - 129) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, 249, 129, 250f, 202f);
    }

    private NBTTagCompound getNBTFromPos() {
        return NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player).get(this.pos);
    }

    private void updateMoves() {
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);
        this.memberCurrentMoves.setCreatureNBT(this.getNBTFromPos());
        this.learnableMovesSection.setCreatureNBT(this.getNBTFromPos());
    }
}
