package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import anightdazingzoroark.prift.server.message.*;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RiftPopupFromCreatureBox extends GuiScreen {
    private GuiTextField textField;
    protected int xSize = 176;
    protected int ySize = 96;
    protected int guiLeft;
    protected int guiTop;
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/popup_from_radial.png");
    private final PopupFromCreatureBox popupType;
    private final int valOne;
    private final int valTwo;

    public RiftPopupFromCreatureBox(int valOne, int valTwo) {
        this.popupType = (PopupFromCreatureBox) ClientProxy.popupFromRadial;
        this.valOne = valOne;
        this.valTwo = valTwo;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        //buttons and other things
        if (this.popupType == PopupFromCreatureBox.REMOVE_INVENTORY || this.popupType == PopupFromCreatureBox.RELEASE) {
            this.buttonList.add(new GuiButton(0, (this.width - 60)/2 - 40, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.yes")));
            this.buttonList.add(new GuiButton(1, (this.width - 60)/2 + 40, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.no")));
        }
        else if (this.popupType == PopupFromCreatureBox.CHANGE_NAME) {
            this.textField = new GuiTextField(0, fontRenderer, (this.width - 120)/2, (this.height - 20)/2 - 5, 120, 20);
            this.textField.setMaxStringLength(20);
            this.textField.setFocused(true);
            this.buttonList.add(new GuiButton(0, (this.width - 60)/2 - 40, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.confirm")));
            this.buttonList.add(new GuiButton(1, (this.width - 60)/2 + 40, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.cancel")));
        }
        else if (this.popupType == PopupFromCreatureBox.NO_CREATURES) {
            this.buttonList.add(new GuiButton(0, (this.width - 60)/2, (this.height - 20)/2 + 30, 60, 20, I18n.format("radial.popup_button.ok")));
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
        String s = I18n.format(I18n.format("creature_box.popup_choice."+this.popupType.name().toLowerCase()));
        RiftUtil.drawMultiLineString(this.fontRenderer, s, (this.xSize - 148)/ 2, (this.ySize) / 2 - 35, 148, 0x000000);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, (176F), (96F));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (this.popupType == PopupFromCreatureBox.REMOVE_INVENTORY) {
            if (button.id == 0) {
                RiftChangePartyOrBoxOrder.SwapType swapType = (RiftChangePartyOrBoxOrder.SwapType) ClientProxy.swapTypeForPopup;
                switch (swapType) {
                    case BOX_PARTY_SWAP:
                        //drop items from party creature's inventory
                        RiftMessages.WRAPPER.sendToServer(new RiftDropPartyMemberInventory(this.valTwo));
                        this.playerTamedCreatures().removePartyCreatureInventory(this.valTwo);

                        //if creature is deployed, remove it from world
                        RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.valTwo), true));
                        RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.valTwo), true));

                        PlayerTamedCreaturesHelper.boxPartySwap(this.mc.player, this.valOne, this.valTwo);
                        break;
                    case PARTY_BOX_SWAP:
                        //drop items from party creature's inventory
                        RiftMessages.WRAPPER.sendToServer(new RiftDropPartyMemberInventory(this.valOne));
                        this.playerTamedCreatures().removePartyCreatureInventory(this.valOne);

                        //if creature is deployed, remove it from world
                        RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.valOne), true));
                        RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.valOne), true));

                        PlayerTamedCreaturesHelper.partyBoxSwap(this.mc.player, this.valOne, this.valTwo);
                        break;
                    case PARTY_TO_BOX:
                        //drop items from party creature's inventory
                        RiftMessages.WRAPPER.sendToServer(new RiftDropPartyMemberInventory(this.valOne));
                        this.playerTamedCreatures().removePartyCreatureInventory(this.valOne);

                        //if creature is deployed, remove it from world
                        RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.valOne), true));
                        RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.valOne), true));

                        PlayerTamedCreaturesHelper.partyToBox(this.mc.player, this.valOne);
                        break;
                    case BOX_DEPLOYED_BOX_SWAP:
                        //drop items from box deployed creature's inventory
                        NBTTagList inventoryNBTOne = this.getCreatureBox().getCreatureList().get(this.valOne).getTagList("Items", 10);
                        RiftMessages.WRAPPER.sendToServer(new RiftDropCreatureBoxDeployedMemberInventory(ClientProxy.creatureBoxBlockPos, this.valOne, inventoryNBTOne));
                        this.playerTamedCreatures().removeBoxCreatureDeployedInventory(this.mc.player.world, ClientProxy.creatureBoxBlockPos,this.valOne);

                        //if creature is deployed, remove it from world
                        RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.valOne), true));
                        RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.valOne), true));

                        PlayerTamedCreaturesHelper.boxDeployedBoxSwap(this.mc.player, ClientProxy.creatureBoxBlockPos, this.valOne, this.valTwo);
                        break;
                    case BOX_DEPLOYED_TO_BOX:
                        //drop items from box deployed creature's inventory
                        NBTTagList inventoryNBTTwo = this.getCreatureBox().getCreatureList().get(this.valOne).getTagList("Items", 10);
                        RiftMessages.WRAPPER.sendToServer(new RiftDropCreatureBoxDeployedMemberInventory(ClientProxy.creatureBoxBlockPos, this.valOne, inventoryNBTTwo));
                        this.playerTamedCreatures().removeBoxCreatureDeployedInventory(this.mc.player.world, ClientProxy.creatureBoxBlockPos, this.valOne);

                        //if creature is deployed, remove it from world
                        RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.valOne), true));
                        RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.valOne), true));

                        PlayerTamedCreaturesHelper.boxDeployedToBox(this.mc.player, ClientProxy.creatureBoxBlockPos, this.valOne);
                        break;
                    case BOX_BOX_DEPLOYED_SWAP:
                        //drop items from box deployed creature's inventory
                        NBTTagList inventoryNBTThree = this.getCreatureBox().getCreatureList().get(this.valTwo).getTagList("Items", 10);
                        RiftMessages.WRAPPER.sendToServer(new RiftDropCreatureBoxDeployedMemberInventory(ClientProxy.creatureBoxBlockPos, this.valTwo, inventoryNBTThree));
                        this.playerTamedCreatures().removeBoxCreatureDeployedInventory(this.mc.player.world, ClientProxy.creatureBoxBlockPos, this.valTwo);

                        //if creature is deployed, remove it from world
                        RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.valTwo), true));
                        RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.valTwo), true));

                        PlayerTamedCreaturesHelper.boxBoxDeployedSwap(this.mc.player, ClientProxy.creatureBoxBlockPos, this.valOne, this.valTwo);
                        break;
                }
                ClientProxy.swapTypeForPopup = null;
                ClientProxy.popupFromRadial = null;
            }
        }
        else if (this.popupType == PopupFromCreatureBox.CHANGE_NAME) {
            if (button.id == 0) {
                //change creature name in the box ui
                NBTTagCompound compound = new NBTTagCompound();
                compound.setString("CustomName", this.textField.getText());
                this.playerTamedCreatures().modifyCreature(ClientProxy.creatureUUID , compound);

                //change creature name in the world
                RiftMessages.WRAPPER.sendToServer(new RiftChangeNameFromBox(ClientProxy.creatureUUID, this.textField.getText()));
                ClientProxy.creatureUUID = null;
                ClientProxy.popupFromRadial = null;
            }
        }
        else if (this.popupType == PopupFromCreatureBox.RELEASE) {
            if (button.id == 0) {
                //remove creature in the box ui
                this.playerTamedCreatures().removeCreature(ClientProxy.creatureUUID);
                this.playerTamedCreatures().removeCreatureFromBoxDeployed(this.mc.player.world, ClientProxy.creatureBoxBlockPos, ClientProxy.creatureUUID);

                //remove creature from the party
                RiftMessages.WRAPPER.sendToAll(new RiftRemoveCreatureFromBox(ClientProxy.creatureUUID));
                RiftMessages.WRAPPER.sendToServer(new RiftRemoveCreatureFromBox(ClientProxy.creatureUUID));
                ClientProxy.creatureUUID = null;
                ClientProxy.popupFromRadial = null;
            }
        }

        this.mc.player.closeScreen();
        if (this.popupType == PopupFromCreatureBox.RELEASE && this.getPlayerParty().isEmpty() && this.getPlayerBox().isEmpty()) {}
        else if (this.popupType != PopupFromCreatureBox.NO_CREATURES) {
            this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
        }
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

    private IPlayerTamedCreatures playerTamedCreatures() {
        return this.mc.player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
    }

    private List<RiftCreature> getPlayerParty() {
        return this.playerTamedCreatures().getPartyCreatures(this.mc.world);
    }

    private List<RiftCreature> getPlayerBox() {
        return this.playerTamedCreatures().getBoxCreatures(this.mc.world);
    }

    private RiftTileEntityCreatureBox getCreatureBox() {
        if (this.mc.player.world.getTileEntity(ClientProxy.creatureBoxBlockPos) instanceof RiftTileEntityCreatureBox) {
            return (RiftTileEntityCreatureBox) this.mc.player.world.getTileEntity(ClientProxy.creatureBoxBlockPos);
        }
        return null;
    }

    private List<RiftCreature> getCreatureBoxDeployedCreatures() {
        if (this.getCreatureBox() != null) return this.getCreatureBox().getCreatures();
        return new ArrayList<>();
    }
}
