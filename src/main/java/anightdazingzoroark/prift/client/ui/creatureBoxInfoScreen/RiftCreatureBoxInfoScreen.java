package anightdazingzoroark.prift.client.ui.creatureBoxInfoScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.creatureBoxInfoScreen.elements.RiftCreatureBoxInfoButtons;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.RiftNewCreatureBoxScreen;
import anightdazingzoroark.prift.client.ui.elements.RiftCreatureInfoScrollableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftCreatureMovesScrollableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiSectionButton;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class RiftCreatureBoxInfoScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/info_from_creature_box_background.png");

    //for creature selection
    private final RiftNewCreatureBoxScreen.SelectedPosType selectedPosType;
    private final int selectedPosOne;
    private final int selectedPosTwo;

    //selected creature
    private NBTTagCompound selectedCreatureNBT;
    private RiftCreature selectedCreatureToDraw;

    //sections
    private RiftCreatureBoxInfoButtons infoButtons;
    private RiftCreatureInfoScrollableSection infoScrollableSection;
    private RiftCreatureMovesScrollableSection movesScrollableSection;

    public RiftCreatureBoxInfoScreen(int selectedPosType, int selectedPosOne, int selectedPosTwo) {
        super();
        this.selectedPosType = RiftNewCreatureBoxScreen.SelectedPosType.values()[selectedPosType];
        this.selectedPosOne = selectedPosOne;
        this.selectedPosTwo = selectedPosTwo;
    }

    @Override
    public void initGui() {
        super.initGui();

        //update deployed creature info upon opening
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

        //get current nbt and creature to draw
        if (this.selectedPosType == RiftNewCreatureBoxScreen.SelectedPosType.PARTY) {
            this.selectedCreatureNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player).get(this.selectedPosOne);
            this.selectedCreatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, this.selectedCreatureNBT);
        }
        else if (this.selectedPosType == RiftNewCreatureBoxScreen.SelectedPosType.BOX) {
            this.selectedCreatureNBT = NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player).getBoxContents(this.selectedPosOne).get(this.selectedPosTwo);
            this.selectedCreatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, this.selectedCreatureNBT);
        }

        //init info buttons
        this.infoButtons = new RiftCreatureBoxInfoButtons(this.width, this.height, this.fontRenderer, this.mc);
        this.infoScrollableSection = new RiftCreatureInfoScrollableSection(this.width, this.height, 70, 10, this.fontRenderer, this.mc);
        this.movesScrollableSection = new RiftCreatureMovesScrollableSection(this.width, this.height, 70, 10, this.fontRenderer, this.mc);

        //add nbt to info sections
        if (this.selectedCreatureNBT != null) {
            this.infoButtons.setCreatureNBT(this.selectedCreatureNBT);
            this.infoScrollableSection.setCreatureNBT(this.selectedCreatureNBT);
            this.movesScrollableSection.setCreatureNBT(this.selectedCreatureNBT);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        //draw background
        this.drawGuiContainerBackgroundLayer();

        //draw creature
        if (this.selectedCreatureToDraw != null) {
            GlStateManager.color(1f, 1f, 1f, 1f);
            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.translate(this.width / 2f - 65, this.height / 2f + 15, 210f);
            GlStateManager.rotate(180, 1f, 0f, 0f);
            GlStateManager.rotate(150, 0f, 1f, 0f);
            GlStateManager.scale(20f, 20f, 20f);
            this.selectedCreatureToDraw.deathTime = 0;
            this.selectedCreatureToDraw.isDead = false;
            this.selectedCreatureToDraw.hurtTime = 0;
            this.mc.getRenderManager().renderEntity(this.selectedCreatureToDraw, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        //ensure nbt exists
        if (this.selectedCreatureNBT != null) {
            //draw info buttons section
            this.infoButtons.drawSectionContents(mouseX, mouseY, partialTicks);

            //draw party mem info
            this.infoScrollableSection.drawSectionContents(mouseX, mouseY, partialTicks);
        }
        //if nbt somehow got removed, maybe it got corrupted or whatever, go back to party screen
        else {}
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - 252) / 2;
        int l = (this.height - 144) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, 252, 152, 400f, 300f);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //deal with buttons on creature info buttons
        int sectionTop = (this.infoButtons.guiHeight - this.infoButtons.height) / 2 + this.infoButtons.yOffset;
        int sectionBottom = sectionTop + this.infoButtons.height;
        for (RiftGuiSectionButton button : this.infoButtons.getActiveButtons()) {
            int buttonTop = button.y;
            int buttonBottom = button.y + button.height;
            boolean clickWithinVisiblePart = mouseY >= Math.max(buttonTop, sectionTop) && mouseY <= Math.min(buttonBottom, sectionBottom);
            if (clickWithinVisiblePart && button.mousePressed(this.mc, mouseX, mouseY)) {
                if (button.buttonId.equals("backToBox")) {
                    this.mc.player.openGui(
                            RiftInitialize.instance,
                            RiftGui.GUI_CREATURE_BOX,
                            this.mc.world,
                            this.selectedPosType.ordinal(),
                            this.selectedPosOne,
                            this.selectedPosTwo
                    );
                }
                else if (button.buttonId.equals("release")) {

                }
                button.playPressSound(this.mc.getSoundHandler());
            }
        }
    }
}
