package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftClickableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftPartyMemButton;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RiftPartyScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");
    private final List<RiftPartyMemButton> partyMemButtons = new ArrayList<>();
    private RiftClickableSection swapPartyMemsButton;
    private RiftClickableSection journalButton;
    private RiftCreature creatureToDraw;
    private int partyMemPos = -1;
    protected final int xSize = 373;
    protected final int ySize = 157;

    @Override
    public void initGui() {
        super.initGui();

        //create swap party members button
        this.swapPartyMemsButton = new RiftClickableSection(19, 17, this.width, this.height, -71, -67, this.fontRenderer, this.mc);
        this.swapPartyMemsButton.addImage(background, 20, 18, 400, 312, 75, 178, 95, 178);
        this.swapPartyMemsButton.setScale(0.75f);

        //create party buttons
        this.partyMemButtons.clear();
        List<NBTTagCompound> playerPartyNBT = new ArrayList(PlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player));
        for (int x = 0; x < playerPartyNBT.size(); x++) {
            int partyMemXOffset = -154 + (x % 2) * 60;
            int partyMemYOffset = -41 + (x / 2) * 40;
            RiftPartyMemButton partyMemButton = new RiftPartyMemButton(playerPartyNBT.get(x), this.width, this.height, partyMemXOffset, partyMemYOffset, this.fontRenderer, this.mc);
            this.partyMemButtons.add(partyMemButton);
        }

        //create journal button
        String journalString = "Journal";
        this.journalButton = new RiftClickableSection(54, 10, this.width, this.height, -123, 67, this.fontRenderer, this.mc);
        this.journalButton.addString(journalString, false, 0x000000, 5, 1, 0.75f);

        //by default selected button is the first one
        //PlayerTamedCreaturesHelper.setLastSelected(this.mc.player, 0);

        //set creature to draw
        //this.creatureToDraw = PlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, playerPartyNBT.get(0));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        int selectedXOffset = this.hasSelectedCreature() ? 0 : 124;
        int selectedYOffset = this.hasSelectedCreature() ? 0 : 3;

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //draw party label
        String partyLabel = "Party";
        int partyLabelX = (this.width - this.fontRenderer.getStringWidth(partyLabel)) / 2 - 165 + selectedXOffset;
        int partyLabelY = (this.height - this.fontRenderer.FONT_HEIGHT) / 2 - 68 + selectedYOffset;
        this.fontRenderer.drawString(partyLabel, partyLabelX, partyLabelY, 0x000000);

        //draw party buttons
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButton partyMemButton = this.partyMemButtons.get(x);
            partyMemButton.setAdditionalOffset(selectedXOffset, selectedYOffset);
            partyMemButton.drawSection(mouseX, mouseY);
            partyMemButton.setSelected(x == this.partyMemPos);
        }

        //draw swap party members button
        this.swapPartyMemsButton.setAdditionalOffset(selectedXOffset, selectedYOffset);
        this.swapPartyMemsButton.drawSection(mouseX, mouseY);

        //draw selected creature
        if (this.hasSelectedCreature()) {
            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.translate(this.width / 2f, this.height / 2f, 180f);
            GlStateManager.rotate(180, 1f, 0f, 0f);
            GlStateManager.rotate(150, 0f, 1f, 0f);
            GlStateManager.scale(20f, 20f, 20f);
            this.mc.getRenderManager().renderEntity(this.creatureToDraw, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        //draw journal button
        int jButtonXOffset = this.hasSelectedCreature() ? 0 : -2;
        this.journalButton.setAdditionalOffset(selectedXOffset + jButtonXOffset, selectedYOffset);
        this.journalButton.drawSection(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //manage party button clicking
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButton partyMemButton = this.partyMemButtons.get(x);
            if (partyMemButton.isHovered(mouseX, mouseY)) {
                //PlayerTamedCreaturesHelper.setLastSelected(this.mc.player, x);
                if (this.partyMemPos != x) {
                    this.creatureToDraw = partyMemButton.getCreatureFromNBT();
                    this.partyMemPos = x;
                }
                else {
                    this.creatureToDraw = null;
                    this.partyMemPos = -1;
                }
            }
        }

        //open the journal upon opening journal button
        if (this.journalButton.isHovered(mouseX, mouseY)) {
            this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_JOURNAL, this.mc.world, 0, 0, 0);
        }
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int xScreenSize = this.hasSelectedCreature() ? 373 : 125;
        int yScreenSize = this.hasSelectedCreature() ? 157 : 151;
        int k = (this.width - xScreenSize) / 2;
        int l = (this.height - yScreenSize) / 2;
        int uvX = this.hasSelectedCreature() ? 0 : 162;
        int uvY = this.hasSelectedCreature() ? 0 : 157;
        drawModalRectWithCustomSizedTexture(k, l, uvX, uvY, xScreenSize, yScreenSize, 400f, 312f);
    }

    private boolean hasSelectedCreature() {
        return this.partyMemPos >= 0 && this.creatureToDraw != null;
    }
}
