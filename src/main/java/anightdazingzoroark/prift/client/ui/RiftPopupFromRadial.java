package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.PopupFromRadial;
import anightdazingzoroark.prift.server.inventory.DummyContainer;
import anightdazingzoroark.prift.server.message.RiftChangeCreatureName;
import anightdazingzoroark.prift.server.message.RiftClearHomePosFromPopup;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftManageClaimCreature;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraftforge.fml.common.StartupQuery.reset;

@SideOnly(Side.CLIENT)
public class RiftPopupFromRadial extends GuiScreen {
    private final RiftCreature creature;
    private GuiTextField textField;
    protected int xSize = 176;
    protected int ySize = 96;
    protected int guiLeft;
    protected int guiTop;
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/popup_from_radial.png");
    private PopupFromRadial popupFromRadial;

    public RiftPopupFromRadial(RiftCreature creature) {
        this.creature = creature;
        this.popupFromRadial = (PopupFromRadial)ClientProxy.popupFromRadial;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        //buttons and other things
        if (this.popupFromRadial == PopupFromRadial.SET_HOME || this.popupFromRadial == PopupFromRadial.UNCLAIM || this.popupFromRadial == PopupFromRadial.CLAIM) {
            this.buttonList.add(new GuiButton(0, (this.width - 60)/2 - 40, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.yes")));
            this.buttonList.add(new GuiButton(1, (this.width - 60)/2 + 40, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.no")));
        }
        else if (this.popupFromRadial == PopupFromRadial.CHANGE_NAME) {
            this.textField = new GuiTextField(0, fontRenderer, (this.width - 120)/2, (this.height - 20)/2 - 5, 120, 20);
            this.textField.setMaxStringLength(20);
            this.textField.setFocused(true);
            this.buttonList.add(new GuiButton(0, (this.width - 60)/2 - 40, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.confirm")));
            this.buttonList.add(new GuiButton(1, (this.width - 60)/2 + 40, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.cancel")));
        }
        super.initGui();
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        if (this.mc != null && mc.world != null) this.drawDefaultBackground();
        else return;
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawGuiContainerBackgroundLayer(p_73863_3_, p_73863_1_, p_73863_2_);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
        if (this.textField != null) this.textField.drawTextBox();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) k, (float) l, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();

        //text
        String s = I18n.format(I18n.format("radial.popup_choice."+this.popupFromRadial.name().toLowerCase()));
        RiftUtil.drawCenteredString(this.fontRenderer, s, (this.xSize - 148)/ 2, (this.ySize) / 2 - 35, 148, 0x000000);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (this.popupFromRadial == PopupFromRadial.SET_HOME) {
            if (button.id == 0) {
                RiftMessages.WRAPPER.sendToServer(new RiftClearHomePosFromPopup(this.creature));
            }
        }
        else if (this.popupFromRadial == PopupFromRadial.UNCLAIM) {
            if (button.id == 0) {
                RiftMessages.WRAPPER.sendToServer(new RiftManageClaimCreature(this.creature, false));
            }
        }
        else if (this.popupFromRadial == PopupFromRadial.CLAIM) {
            if (button.id == 0) {
                RiftMessages.WRAPPER.sendToServer(new RiftManageClaimCreature(this.creature, true));
            }
        }
        else if (this.popupFromRadial == PopupFromRadial.CHANGE_NAME) {
            if (button.id == 0) {
                RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureName(this.creature, this.textField.getText()));
            }
        }
        this.mc.player.closeScreen();
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, (176F), (96F));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) this.mc.player.closeScreen();
        if (this.textField != null) this.textField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        if (this.textField != null) this.textField.updateCursorCounter();
    }
}
