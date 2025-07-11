package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiSectionButton;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class RiftNewJournalScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/journal_background.png");
    private RiftJournalInfoSection journalInfoSection;
    private RiftJournalEntriesSection journalEntriesSection;
    private RiftCreatureType lastEntry;
    protected final int xSize = 420;
    protected final int ySize = 240;

    @Override
    public void initGui() {
        super.initGui();

        this.journalInfoSection = new RiftJournalInfoSection(this.width, this.height, this.fontRenderer, this.mc);
        this.journalEntriesSection = new RiftJournalEntriesSection(this.width, this.height, this.fontRenderer, this.mc);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        //update sections
        this.journalInfoSection.updateGuiSize(this.width, this.height);
        this.journalEntriesSection.updateGuiSize(this.width, this.height);

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //draw sections
        this.journalInfoSection.drawSectionContents(mouseX, mouseY, partialTicks);
        this.journalEntriesSection.drawSectionContents(mouseX, mouseY, partialTicks);

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, 560F, 240F);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int delta = Mouse.getEventDWheel();
        if (delta != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            this.journalInfoSection.handleMouseInput(mouseX, mouseY, delta);
            this.journalEntriesSection.handleMouseInput(mouseX, mouseY, delta);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        //manually move scroll bar
        this.journalInfoSection.mouseClicked(mouseX, mouseY, mouseButton);
        this.journalEntriesSection.mouseClicked(mouseX, mouseY, mouseButton);

        //manage buttons
        for (RiftGuiSectionButton b : this.journalEntriesSection.getActiveButtons()) {
            if (b.mousePressed(this.mc, mouseX, mouseY)) {
                b.playPressSound(this.mc.getSoundHandler());
                this.onButtonClicked(b);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.journalInfoSection.mouseReleased(mouseX, mouseY, state);
        this.journalEntriesSection.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.journalInfoSection.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.journalEntriesSection.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    //for button click
    private void onButtonClicked(RiftGuiSectionButton button) {
        if (this.journalEntriesSection.getActiveButtons().contains(button)) {
            //choose a category
            if (this.journalEntriesSection.getCurrentCategory() == null) {
                this.journalEntriesSection.setCurrentCategory(
                        RiftCreatureType.CreatureCategory.safeValOf(button.buttonId)
                );
            }
            //choose a creature whose entry shall be read
            else {
                if (button.buttonId.equals("NULL")) {
                    this.lastEntry = null;
                    this.journalEntriesSection.reenableAllButtons();
                    this.journalEntriesSection.setCurrentCategory(null);
                }
                else {
                    RiftCreatureType typeToChoose = RiftCreatureType.safeValOf(button.buttonId);
                    if (typeToChoose != null && typeToChoose != this.lastEntry) {
                        if (this.lastEntry != null) this.journalEntriesSection.reenableButtonById(this.lastEntry.toString());
                        this.journalEntriesSection.disableButtonById(typeToChoose.toString());
                        this.lastEntry = typeToChoose;
                    }
                }
            }
        }
    }
}
