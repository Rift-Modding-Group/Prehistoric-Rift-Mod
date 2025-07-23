package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiSectionButton;
import anightdazingzoroark.prift.client.ui.elements.RiftPopupFromPlayerPartyChangeName;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.message.RiftChangePartyMemName;
import anightdazingzoroark.prift.server.message.RiftMessages;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Map;

public class RiftPopupFromPlayerParty extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/popup_from_radial.png");
    private RiftPopupFromPlayerPartyChangeName changeName;
    private final int pos;

    public RiftPopupFromPlayerParty(int pos) {
        super();
        this.pos = pos;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.changeName = new RiftPopupFromPlayerPartyChangeName(this.width, this.height, this.fontRenderer, this.mc);
        //get the name of the creature
        this.changeName.setTextFieldString("NewName", this.getNBTFromPos().getString("CustomName"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && mc.world != null) this.drawDefaultBackground();
        else return;

        this.drawGuiContainerBackgroundLayer();

        //draw change name related stuff
        this.changeName.drawSectionContents(mouseX, mouseY, partialTicks);
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - 176) / 2;
        int l = (this.height - 96) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, 176, 96, 176f, 96f);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //deal with confirm or cancel buttons when changing name
        int sectionTop = (this.changeName.guiHeight - this.changeName.height) / 2 + this.changeName.yOffset;
        int sectionBottom = sectionTop + this.changeName.height;
        for (RiftGuiSectionButton button : this.changeName.getActiveButtons()) {
            int buttonTop = button.y;
            int buttonBottom = button.y + button.height;
            boolean clickWithinVisiblePart = mouseY >= Math.max(buttonTop, sectionTop) && mouseY <= Math.min(buttonBottom, sectionBottom);
            if (clickWithinVisiblePart && button.mousePressed(this.mc, mouseX, mouseY)) {
                if (button.buttonId.equals("SetNewName")) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangePartyMemName(this.mc.player, this.pos, this.changeName.getTextFieldString("NewName")));
                    this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_PARTY, this.mc.world, this.pos, -1, 0);
                }
                else if (button.buttonId.equals("Exit")) {
                    this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_PARTY, this.mc.world, this.pos, -1, 0);
                }
                button.playPressSound(this.mc.getSoundHandler());
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_PARTY, this.mc.world, this.pos, -1, 0);

        //deal with text box's input
        for (Map.Entry<String, GuiTextField> textBoxEntry : this.changeName.getTextFields().entrySet()) {
            if (textBoxEntry.getValue() != null) {
                textBoxEntry.getValue().textboxKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void updateScreen() {
        //deal with text boxes
        for (Map.Entry<String, GuiTextField> textBoxEntry : this.changeName.getTextFields().entrySet()) {
            if (textBoxEntry.getValue() != null) {
                textBoxEntry.getValue().updateCursorCounter();
            }
        }
    }

    private NBTTagCompound getNBTFromPos() {
        return NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player).get(this.pos);
    }
}
