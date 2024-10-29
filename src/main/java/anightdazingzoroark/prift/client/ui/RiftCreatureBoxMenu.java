package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiCreatureBoxBoxButton;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiCreatureBoxDeployedButton;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiCreatureBoxPartyButton;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import anightdazingzoroark.prift.server.message.*;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RiftCreatureBoxMenu extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png");
    private BlockPos creatureBoxPos;
    protected final int xSize = 408;
    protected final int ySize = 216;
    private int scrollSidebarOffset = 0;
    private int scrollBoxedCreaturesOffset = 0;
    private int scrollBoxedDeployedCreaturesOffset = 0;
    private int partyBarHeight;
    private int boxedCreaturesHeight;
    private int boxedCreaturesDeployedHeight;
    private final List<GuiButton> manageSelectedCreatureButtons = Lists.<GuiButton>newArrayList();
    private final List<GuiButton> creaturesInBoxButtons = Lists.<GuiButton>newArrayList();
    private final List<GuiButton> creaturesInBoxDeployedButtons = Lists.<GuiButton>newArrayList();
    private RiftCreature selectedCreature;
    private boolean changeCreaturesMode;
    private int partyPosToMove = -1;
    private int boxPosToMove = -1;
    private int boxDeployedToMove = -1;

    public RiftCreatureBoxMenu() {
        this.creatureBoxPos = ClientProxy.creatureBoxBlockPos;
    }

    @Override
    public void initGui() {
        this.selectedCreature = null;
        this.changeCreaturesMode = false;

        //reset scrollbars
        this.scrollSidebarOffset = 0;

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc != null && mc.world != null) this.drawDefaultBackground();
        else return;

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //draw sidebar
        this.placePartyMemberButtons(mouseX, mouseY, partialTicks);

        //draw creature box creatures
        this.placeBoxedCreatureButtons(mouseX, mouseY, partialTicks);

        //draw stored creatures
        this.placeBoxedDeployedCreaturesButtons(mouseX, mouseY, partialTicks);

        //draw party member info
        this.createSelectedCreatureInfo(mouseX, mouseY, partialTicks);

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
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, 408, 300);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button instanceof RiftGuiCreatureBoxPartyButton) {
            RiftGuiCreatureBoxPartyButton partyButton = (RiftGuiCreatureBoxPartyButton)button;
            if (this.changeCreaturesMode) {
                if (this.partyPosToMove == -1 && this.boxPosToMove == -1 && this.boxDeployedToMove == -1) {
                    if (partyButton.id < this.getPlayerParty().size()) {
                        this.selectedCreature = this.getPlayerParty().get(partyButton.id);
                        this.partyPosToMove = partyButton.id;
                    }
                }
                else {
                    if (this.boxPosToMove != -1 && this.boxDeployedToMove == -1) {
                        //swap box creature w party creature
                        if (partyButton.id < this.getPlayerParty().size()) {
                            //if inventory except saddle not empty, just swap box and party creatures
                            if (this.getPlayerParty().get(partyButton.id).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(partyButton.id), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(partyButton.id), true));

                                RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_PARTY_SWAP, this.boxPosToMove, partyButton.id));
                                this.playerTamedCreatures().boxCreatureToPartyCreature(this.boxPosToMove, partyButton.id);
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.BOX_PARTY_SWAP;
                                this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.boxPosToMove, partyButton.id);
                            }
                        }
                        //move box creature to party
                        else {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_TO_PARTY, this.boxPosToMove));
                            this.playerTamedCreatures().boxCreatureToParty(this.boxPosToMove);
                        }
                    }
                    else if (this.boxPosToMove == -1 && this.boxDeployedToMove != -1) {
                        //swap box deployed creature w party creature
                        if (partyButton.id < this.getPlayerParty().size()) {
                            //if creature is deployed, remove it from world
                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));

                            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_PARTY_SWAP, this.creatureBoxPos, this.boxDeployedToMove, partyButton.id));
                            this.playerTamedCreatures().boxCreatureDeployedToPartyCreature(this.mc.player.world, this.creatureBoxPos, this.boxDeployedToMove, partyButton.id);
                            this.getCreatureBox().createCreatureList();
                        }
                        //move box deployed creature to party
                        else {
                            //if creature is deployed, remove it from world
                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));

                            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_TO_PARTY, this.creatureBoxPos, this.boxDeployedToMove));
                            this.playerTamedCreatures().boxCreatureDeployedToParty(this.mc.player.world, this.creatureBoxPos, this.boxDeployedToMove);
                            this.getCreatureBox().createCreatureList();
                        }
                    }
                    //party swap
                    else if (partyButton.id < this.getPlayerParty().size()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.REARRANGE_PARTY, this.partyPosToMove, partyButton.id));
                        this.playerTamedCreatures().rearrangePartyCreatures(this.partyPosToMove, partyButton.id);
                    }

                    this.partyPosToMove = -1;
                    this.boxPosToMove = -1;
                    this.boxDeployedToMove = -1;
                }
            }
            else {
                if (partyButton.id < this.getPlayerParty().size()) {
                    this.selectedCreature = this.getPlayerParty().get(partyButton.id);
                }
                else this.selectedCreature = null;

                this.partyPosToMove = partyButton.id;
                this.boxPosToMove = -1;
                this.boxDeployedToMove = -1;
            }
        }
        else if (button instanceof RiftGuiCreatureBoxBoxButton) {
            RiftGuiCreatureBoxBoxButton boxButton = (RiftGuiCreatureBoxBoxButton)button;
            if (this.changeCreaturesMode) {
                if (this.partyPosToMove == -1 && this.boxPosToMove == -1 && this.boxDeployedToMove == -1) {
                    if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                        this.selectedCreature = this.getPlayerBoxedCreatures().get(boxButton.id);
                        this.boxPosToMove = boxButton.id;
                    }
                }
                else {
                    if (this.partyPosToMove != -1 && this.boxDeployedToMove == -1) {
                        //swap party creature w box creature
                        if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                            //if inventory except saddle not empty, just move to box
                            if (this.getPlayerParty().get(this.partyPosToMove).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));

                                RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_BOX_SWAP, this.partyPosToMove, boxButton.id));
                                this.playerTamedCreatures().partyCreatureToBoxCreature(this.partyPosToMove, boxButton.id);
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.PARTY_BOX_SWAP;
                                this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.partyPosToMove, boxButton.id);
                            }
                        }
                        //move party creature to box
                        else {
                            //if inventory except saddle not empty, just move to box
                            if (this.getPlayerParty().get(this.partyPosToMove).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));

                                RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_TO_BOX, this.partyPosToMove));
                                this.playerTamedCreatures().partyCreatureToBox(this.partyPosToMove);
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.PARTY_TO_BOX;
                                this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.partyPosToMove, 0);
                            }
                        }
                    }
                    else if (this.partyPosToMove == -1 && this.boxDeployedToMove != -1) {
                        //swap box deployed creature with box creature
                        if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                            //if inventory except saddle not empty, just move to box
                            if (this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));

                                RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_BOX_SWAP, this.creatureBoxPos, this.boxDeployedToMove, boxButton.id));
                                this.playerTamedCreatures().boxCreatureDeployedToBoxCreature(this.mc.player.world, this.creatureBoxPos, this.boxDeployedToMove, boxButton.id);
                                this.getCreatureBox().createCreatureList();
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_BOX_SWAP;
                                this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.boxDeployedToMove, boxButton.id);
                            }
                        }
                        //move box deployed creature to box
                        else {
                            //if inventory except saddle not empty, just move to box
                            if (this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove).creatureInventory.isEmptyExceptSaddle()) {
                                //drop items from box deployed creature's inventory
                                NBTTagList inventoryNBT = this.getCreatureBox().getCreatureList().get(this.boxDeployedToMove).getTagList("Items", 10);
                                RiftMessages.WRAPPER.sendToServer(new RiftDropCreatureBoxDeployedMemberInventory(ClientProxy.creatureBoxBlockPos, this.boxDeployedToMove, inventoryNBT));
                                this.playerTamedCreatures().removeBoxCreatureDeployedInventory(this.mc.player.world, this.creatureBoxPos, this.boxDeployedToMove);

                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));

                                RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_TO_BOX, this.creatureBoxPos, this.boxDeployedToMove));
                                this.playerTamedCreatures().boxCreatureDeployedToBox(this.mc.player.world, this.creatureBoxPos, this.boxDeployedToMove);
                                this.getCreatureBox().createCreatureList();
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_TO_BOX;
                                this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.boxDeployedToMove, 0);
                            }
                        }
                    }
                    //box swap
                    else if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrBoxOrder(RiftChangePartyOrBoxOrder.SwapType.REARRANGE_BOX, this.boxPosToMove, boxButton.id));
                        this.playerTamedCreatures().rearrangeBoxCreatures(this.boxPosToMove, boxButton.id);
                    }

                    this.partyPosToMove = -1;
                    this.boxPosToMove = -1;
                    this.boxDeployedToMove = -1;
                }
            }
            else {
                if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                    this.selectedCreature = this.getPlayerBoxedCreatures().get(boxButton.id);
                }
                else this.selectedCreature = null;

                this.partyPosToMove = -1;
                this.boxPosToMove = boxButton.id;
                this.boxDeployedToMove = -1;
            }
        }
        else if (button instanceof RiftGuiCreatureBoxDeployedButton) {
            RiftGuiCreatureBoxDeployedButton boxDeployedButton = (RiftGuiCreatureBoxDeployedButton) button;
            if (this.changeCreaturesMode) {
                if (this.partyPosToMove == -1 && this.boxPosToMove == -1 && this.boxDeployedToMove == -1) {
                    if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                        this.selectedCreature = this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id);
                        this.boxDeployedToMove = boxDeployedButton.id;
                    }
                }
                else {
                    if (this.partyPosToMove != -1 && this.boxPosToMove == -1) {
                        //swap party creature with box deployed creature
                        if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                            //if creature is deployed, remove it from world
                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));

                            if (this.boxDeployedToMove != -1) {
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedToMove), true));
                            }

                            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_BOX_DEPLOYED_SWAP, this.creatureBoxPos, this.partyPosToMove, boxDeployedButton.id));
                            this.playerTamedCreatures().partyCreatureToBoxCreatureDeployed(this.mc.player.world, this.creatureBoxPos, this.partyPosToMove, boxDeployedButton.id);
                            this.getCreatureBox().createCreatureList();
                        }
                        //move party creature to box deployed
                        else {
                            //if creature is deployed, remove it from world
                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));

                            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.PARTY_TO_BOX_DEPLOYED, this.creatureBoxPos, this.partyPosToMove));
                            this.playerTamedCreatures().partyCreatureToBoxDeployed(this.mc.player.world, this.creatureBoxPos, this.partyPosToMove);
                            this.getCreatureBox().createCreatureList();
                        }
                    }
                    else if (this.partyPosToMove == -1 && this.boxPosToMove != -1) {
                        //swap box creature with box deployed creature
                        if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                            //if inventory except saddle not empty, just move to box
                            if (this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id), true));

                                RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_BOX_DEPLOYED_SWAP, this.creatureBoxPos, this.boxPosToMove, boxDeployedButton.id));
                                this.playerTamedCreatures().boxCreatureToBoxCreatureDeployed(this.mc.player.world, this.creatureBoxPos, this.boxPosToMove, boxDeployedButton.id);
                                this.getCreatureBox().createCreatureList();
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.BOX_BOX_DEPLOYED_SWAP;
                                this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.boxPosToMove, boxDeployedButton.id);
                            }
                        }
                        //move box creature to box deployed
                        else {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.BOX_TO_BOX_DEPLOYED, this.creatureBoxPos, this.boxPosToMove));
                            this.playerTamedCreatures().boxCreatureToBoxDeployed(this.mc.player.world, this.creatureBoxPos, this.boxPosToMove);
                            this.getCreatureBox().createCreatureList();
                        }
                    }
                    //box deployed swap
                    else if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftChangeBoxDeployedOrder(RiftChangePartyOrBoxOrder.SwapType.REARRANGE_BOX_DEPLOYED, this.creatureBoxPos, this.boxDeployedToMove, boxDeployedButton.id));
                        this.playerTamedCreatures().rearrangeDeployedBoxCreatures(this.mc.player.world, this.creatureBoxPos, this.boxDeployedToMove, boxDeployedButton.id);
                        this.getCreatureBox().createCreatureList();
                    }

                    this.partyPosToMove = -1;
                    this.boxPosToMove = -1;
                    this.boxDeployedToMove = -1;
                }
            }
            else {
                if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                    this.selectedCreature = this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id);
                }
                else this.selectedCreature = null;

                this.partyPosToMove = -1;
                this.boxPosToMove = -1;
                this.boxDeployedToMove = boxDeployedButton.id;
            }
        }
        else if (this.manageSelectedCreatureButtons.contains(button)) {
            if (this.selectedCreature != null) {
                if (button.id == 0) {
                    ClientProxy.creatureUUID = this.selectedCreature.getUniqueID();
                    ClientProxy.popupFromRadial = PopupFromCreatureBox.CHANGE_NAME;
                    this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                }
                else if (button.id == 1) {
                    ClientProxy.creatureUUID = this.selectedCreature.getUniqueID();
                    ClientProxy.popupFromRadial = PopupFromCreatureBox.RELEASE;
                    this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                }
            }
            if (button.id == 2) {
                this.changeCreaturesMode = !this.changeCreaturesMode;
                this.partyPosToMove = -1;
                this.boxPosToMove = -1;
                this.boxDeployedToMove = -1;
            }
        }
    }

    private void partyButtonListInit() {
        this.buttonList.clear();
        this.partyBarHeight = 0;

        int size = this.getPlayerParty().size() + (this.getPlayerParty().size() < this.playerTamedCreatures().getMaxPartySize() ? 1 : 0);
        for (int x = 0; x < size; x++) {
            if (x < this.getPlayerParty().size()) {
                RiftCreature creature = this.getPlayerParty().get(x);
                this.buttonList.add(new RiftGuiCreatureBoxPartyButton(creature, x, (this.width - 96)/2 - 147, (this.height - 32) / 2 - 83 + (40 * x)));
                this.partyBarHeight += 40;
            }
            else {
                this.buttonList.add(new RiftGuiCreatureBoxPartyButton(null, x, (this.width - 96)/2 - 147, (this.height - 32) / 2 - 83 + (40 * x)));
                this.partyBarHeight += 40;
            }
        }
    }

    private void boxedCreaturesButtonListInit() {
        this.creaturesInBoxButtons.clear();
        this.boxedCreaturesHeight = 0;

        int size = this.getPlayerBoxedCreatures().size() + (this.getPlayerBoxedCreatures().size() < PlayerTamedCreatures.maxBoxSize ? 1 : 0);
        int rows = (int) Math.ceil(size / 5D);
        for (int x = 0; x < size; x++) {
            if (x < this.getPlayerBoxedCreatures().size()) {
                RiftCreature creature = this.getPlayerBoxedCreatures().get(x);
                this.creaturesInBoxButtons.add(new RiftGuiCreatureBoxBoxButton(creature, x, (this.width - 30)/2 - 72 + (33 * (x % 5)), (this.height - 30)/2 - 66 + (33 * (int)(x/5))));
            }
            else this.creaturesInBoxButtons.add(new RiftGuiCreatureBoxBoxButton(null, x, (this.width - 30)/2 - 72 + (33 * (x % 5)), (this.height - 30)/2 - 66 + (33 * (int)(x/5))));
        }
        for (int x = 0; x < rows; x++) {
            if (x == 0) this.boxedCreaturesHeight += 30;
            else this.boxedCreaturesHeight += 33;
        }
    }

    private void boxedCreaturesDeployedButtonListInit() {
        this.creaturesInBoxDeployedButtons.clear();
        this.boxedCreaturesDeployedHeight = 0;

        if (this.getCreatureBox() != null) {
            int size = this.getCreatureBoxDeployedCreatures().size() + (this.getCreatureBoxDeployedCreatures().size() < this.getCreatureBox().getMaxWanderingCreatures() ? 1 : 0);
            int rows = (int) Math.ceil(size / 5D);

            for (int x = 0; x < size; x++) {
                if (x < this.getCreatureBoxDeployedCreatures().size()) {
                    RiftCreature creature = this.getCreatureBoxDeployedCreatures().get(x);
                    this.creaturesInBoxDeployedButtons.add(new RiftGuiCreatureBoxDeployedButton(creature, x, (this.width - 30)/2 - 72 + (33 * (x % 5)), (this.height - 30)/2 + 52 + (33 * (int)(x/5))));
                }
                else this.creaturesInBoxDeployedButtons.add(new RiftGuiCreatureBoxDeployedButton(null, x, (this.width - 30)/2 - 72 + (33 * (x % 5)), (this.height - 30)/2 + 52 + (33 * (int)(x/5))));
            }
            for (int x = 0; x < rows; x++) {
                if (x == 0) this.boxedCreaturesDeployedHeight += 30;
                else this.boxedCreaturesDeployedHeight += 33;
            }
        }
    }

    private void placePartyMemberButtons(int mouseX, int mouseY, float partialTicks) {
        int x = (this.width - 96) / 2 - 147;
        int y = (this.height - 200) / 2;

        // for scaling
        int scaleFactor = new ScaledResolution(this.mc).getScaleFactor();

        int scissorX = x * scaleFactor;
        int scissorY = (this.height - y - 200) * scaleFactor;
        int scissorW = (this.width - 96) * scaleFactor;
        int scissorH = 200 * scaleFactor;

        //for buttons on the side
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        for (GuiButton guiButton : this.buttonList) {
            if (this.changeCreaturesMode && this.partyPosToMove != -1 && this.partyPosToMove == guiButton.id) {
                RiftGuiCreatureBoxPartyButton partyButton = (RiftGuiCreatureBoxPartyButton) guiButton;
                partyButton.toMove = true;
            }

            //draw button
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //create scrollbar
        if (this.partyBarHeight > 200) {
            int k = (this.width - 1) / 2 - 97;
            int l = (this.height - 200) / 2;
            //scrollbar background
            drawRect(k, l, k + 1, l + 200, 0xFFA0A0A0);
            //scrollbar progress
            int thumbHeight = Math.max(10, (int)((float)200 * (200f / this.partyBarHeight)));
            int thumbPosition = (int)((float)this.scrollSidebarOffset / (this.partyBarHeight - 200) * (200 - thumbHeight));
            drawRect(k, l + thumbPosition, k + 1, l + thumbHeight + thumbPosition, 0xFFC0C0C0);
        }
    }

    private void placeBoxedCreatureButtons(int mouseX, int mouseY, float partialTicks) {
        //place name
        String string = I18n.format("creature_box.creature_box", this.mc.player.getName());
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75f, 0.75f, 0.75f);
        this.fontRenderer.drawString(string, (int) ((this.width/2 - 90)/0.75), (int)(((this.height - this.fontRenderer.FONT_HEIGHT)/2 - 90)/0.75), 0);
        GlStateManager.popMatrix();

        //create area
        int x = (this.width - 162) / 2 - 87;
        int y = (this.height - 96) / 2 - 33;

        // for scaling
        int scaleFactor = new ScaledResolution(this.mc).getScaleFactor();

        int scissorX = x * scaleFactor;
        int scissorY = (this.height - y - 96) * scaleFactor;
        int scissorW = (this.width - 162) * scaleFactor;
        int scissorH = 96 * scaleFactor;

        //for buttons on the side
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        for (GuiButton guiButton : this.creaturesInBoxButtons) {
            if (this.changeCreaturesMode && this.boxPosToMove != -1 && this.boxPosToMove == guiButton.id) {
                RiftGuiCreatureBoxBoxButton boxButton = (RiftGuiCreatureBoxBoxButton) guiButton;
                boxButton.toMove = true;
            }

            //draw button
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //create scrollbar
        if (this.boxedCreaturesHeight > 96) {
            int k = (this.width - 1) / 2 + 77;
            int l = (this.height - 96) / 2 - 33;
            //scrollbar background
            drawRect(k, l, k + 1, l + 96, 0xFF7A7A7A);
            //scrollbar progress
            int thumbHeight = Math.max(10, (int)((float)96 * (96f / this.boxedCreaturesHeight)));
            int thumbPosition = (int)((float)this.scrollBoxedCreaturesOffset / (this.boxedCreaturesHeight - 96) * (96 - thumbHeight));
            drawRect(k, l + thumbPosition, k + 1, l + thumbHeight + thumbPosition, 0xFF616161);
        }
    }

    private void placeBoxedDeployedCreaturesButtons(int mouseX, int mouseY, float partialTicks) {
        //place name
        String string = I18n.format("creature_box.deployed_creatures");
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75f, 0.75f, 0.75f);
        this.fontRenderer.drawString(string, (int) ((this.width/2 - 90)/0.75), (int)(((this.height - this.fontRenderer.FONT_HEIGHT)/2 + 27)/0.75), 0);
        GlStateManager.popMatrix();

        //create area
        int x = (this.width - 162) / 2 - 87;
        int y = (this.height - 63) / 2 + 52;

        // for scaling
        int scaleFactor = new ScaledResolution(this.mc).getScaleFactor();

        int scissorX = x * scaleFactor;
        int scissorY = (this.height - y - 63) * scaleFactor;
        int scissorW = (this.width - 162) * scaleFactor;
        int scissorH = 63 * scaleFactor;

        //for buttons on the side
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        for (GuiButton guiButton : this.creaturesInBoxDeployedButtons) {
            if (this.changeCreaturesMode && this.boxDeployedToMove != -1 && this.boxDeployedToMove == guiButton.id) {
                RiftGuiCreatureBoxDeployedButton boxButton = (RiftGuiCreatureBoxDeployedButton) guiButton;
                boxButton.toMove = true;
            }

            //draw button
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //create scrollbar
        if (this.boxedCreaturesDeployedHeight > 63) {
            int k = (this.width - 1) / 2 + 77;
            int l = (this.height - 96) / 2 + 48;
            //scrollbar background
            drawRect(k, l, k + 1, l + 96, 0xFF7A7A7A);
            //scrollbar progress
            int thumbHeight = Math.max(10, (int)((float)63 * (63f / this.boxedCreaturesDeployedHeight)));
            int thumbPosition = (int)((float)this.scrollBoxedDeployedCreaturesOffset / (this.boxedCreaturesDeployedHeight - 96) * (96 - thumbHeight));
            drawRect(k, l + thumbPosition, k + 1, l + thumbHeight + thumbPosition, 0xFF616161);
        }
    }

    private void createSelectedCreatureInfo(int mouseX, int mouseY, float partialTicks) {
        if (this.selectedCreature != null) {
            //for creature size
            RiftCreature creatureToRender = this.selectedCreature;
            float scaleMultiplier = RiftUtil.getCreatureModelScale(this.selectedCreature) * 0.5f;

            //render entity
            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.translate(this.width / 2f + 140f, this.height / 2f - 30f, 180.0F);

            //for rotating
            int mouseXMod = RiftUtil.clamp(mouseX + (this.width / 2 + 140), 480, 592);
            GlStateManager.rotate(mouseXMod, 0.0F, 1.0F, 0.0F);

            GlStateManager.rotate(180, 1.0F, 0.0F, 0.0F);

            GlStateManager.scale(scaleMultiplier, scaleMultiplier, scaleMultiplier);
            Minecraft.getMinecraft().getRenderManager().renderEntity(creatureToRender, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();

            //draw name
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            String name = this.selectedCreature.hasCustomName() ? this.selectedCreature.getCustomNameTag() + " (" + this.selectedCreature.creatureType.friendlyName + ")" : this.selectedCreature.getName(false);
            this.fontRenderer.drawString(name, (int)((this.width / 2 + 86) / 0.5), (int)(((this.height - this.fontRenderer.FONT_HEIGHT) / 2 - 5) / 0.5), 0);
            GlStateManager.popMatrix();

            //draw owner name
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            String ownerName = I18n.format("tametrait.owner", this.selectedCreature.getOwner().getName());
            this.fontRenderer.drawString(ownerName, (int)((this.width / 2 + 86) / 0.5), (int)(((this.height - this.fontRenderer.FONT_HEIGHT) / 2 + 3) / 0.5), 0);
            GlStateManager.popMatrix();

            //draw level and xp
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            String levelString = I18n.format("tametrait.level", this.selectedCreature.getLevel())+" ("+ this.selectedCreature.getXP() +"/"+ this.selectedCreature.getMaxXP() +" XP)";
            this.fontRenderer.drawString(levelString, (int)((this.width / 2 + 86) / 0.5), (int)(((this.height - this.fontRenderer.FONT_HEIGHT) / 2 + 11) / 0.5), 0);
            GlStateManager.popMatrix();

            //draw xp bar
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 14, 252, 216, 110, 4, 408, 300);

            double xpRatio = (double) this.selectedCreature.getXP() / this.selectedCreature.getMaxXP();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 14, 252, 220, (int)(110 * xpRatio), 4, 408, 300);

            //draw health
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            String healthString = I18n.format("tametrait.health")+": "+ this.selectedCreature.getHealth() +"/"+ this.selectedCreature.getMaxHealth() +" HP";
            this.fontRenderer.drawString(healthString, (int)((this.width / 2 + 86) / 0.5), (int)(((this.height - this.fontRenderer.FONT_HEIGHT) / 2 + 22) / 0.5), 0);
            GlStateManager.popMatrix();

            //draw health bar
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 25, 252, 216, 110, 4, 408, 300);

            double healthRatio = this.selectedCreature.getHealth() / this.selectedCreature.getMaxHealth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 25, 252, 224, (int)(110 * healthRatio), 4, 408, 300);

            //draw other buttons
            for (GuiButton button : this.manageSelectedCreatureButtons) {
                if (button.id != 2 && !this.changeCreaturesMode) {
                    button.enabled = true;
                    button.drawButton(this.mc, mouseX, mouseY, partialTicks);
                }
            }
        }
        if (!this.manageSelectedCreatureButtons.isEmpty() && this.manageSelectedCreatureButtons.get(2).id == 2) this.manageSelectedCreatureButtons.get(2).drawButton(this.mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        //update sidebar
        this.partyButtonListInit();
        for (GuiButton button : this.buttonList) {
            button.y -= this.scrollSidebarOffset;
        }

        //update boxed creatures area
        this.boxedCreaturesButtonListInit();
        for (GuiButton button : this.creaturesInBoxButtons) {
            //change positions based on how far the user scrolled
            button.y -= this.scrollBoxedCreaturesOffset;

            //disable buttons depending on whether or not they are visible
            int minY = (this.height - 96) / 2 - 33;
            int maxY = (this.height - 96) / 2 + 63;
            int visibleButtonSize = 0;
            for (int y = button.y; y <= button.y + 30; y++) {
                if (minY <= y && maxY >= y) visibleButtonSize++;
            }
            if (visibleButtonSize == 0) button.enabled = false;
        }

        //update deployed box creatures
        this.boxedCreaturesDeployedButtonListInit();

        //update selected creature buttons
        this.manageSelectedCreatureButtons.clear();
        GuiButton changeNameButton = new GuiButton(0, (this.width - 100)/2 + 140, (this.height - 20)/2 + 60, 100, 20, I18n.format("creature_box.change_name"));
        changeNameButton.enabled = false;
        GuiButton releaseButton = new GuiButton(1, (this.width - 100)/2 + 140, (this.height - 20)/2 + 85, 100, 20, I18n.format("creature_box.release"));
        releaseButton.enabled = false;
        String rearrangeName = this.changeCreaturesMode ? I18n.format("creature_box.stop_rearrange_creatures") : I18n.format("creature_box.rearrange_creatures");
        GuiButton rearrangeButton = new GuiButton(2, (this.width - 100)/2 - 147, (this.height - 20)/2 + 85, 100, 20, rearrangeName);
        this.manageSelectedCreatureButtons.add(changeNameButton);
        this.manageSelectedCreatureButtons.add(releaseButton);
        this.manageSelectedCreatureButtons.add(rearrangeButton);
    }

    private boolean isMouseOverPartyBar(int mouseX, int mouseY) {
        int minX = (this.width - 96) / 2 - 147;
        int minY = (this.height - 200) / 2 - 8;
        int maxX = (this.width - 96) / 2 - 51;
        int maxY = (this.height - 200) / 2 + 192;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    private boolean isMouseOverBoxedCreatures(int mouseX, int mouseY) {
        int minX = (this.width - 162) / 2 - 6;
        int minY = (this.height - 96) / 2 - 33;
        int maxX = (this.width - 162) / 2 + 156;
        int maxY = (this.height - 96) / 2 + 63;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    private boolean isMouseOverBoxedDeployedCreatures(int mouseX, int mouseY) {
        int minX = (this.width - 162) / 2 - 6;
        int minY = (this.height - 63) / 2 + 52;
        int maxX = (this.width - 162) / 2 + 156;
        int maxY = (this.height - 63) / 2 + 115;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int scroll = Mouse.getEventDWheel();
        //edit scroll offsets
        if (scroll != 0) {
            if (this.isMouseOverPartyBar(mouseX, mouseY)) {
                this.scrollSidebarOffset += (scroll > 0) ? -10 : 10;
                this.scrollSidebarOffset = Math.max(0, Math.min(this.scrollSidebarOffset, Math.max(0, this.partyBarHeight - 200)));
            }
            if (this.isMouseOverBoxedCreatures(mouseX, mouseY)) {
                this.scrollBoxedCreaturesOffset += (scroll > 0) ? -10 : 10;
                this.scrollBoxedCreaturesOffset = Math.max(0, Math.min(this.scrollBoxedCreaturesOffset, Math.max(0, this.boxedCreaturesHeight - 96)));
            }
            if (this.isMouseOverBoxedDeployedCreatures(mouseX, mouseY)) {
                this.scrollBoxedDeployedCreaturesOffset += (scroll > 0) ? -10 : 10;
                this.scrollBoxedDeployedCreaturesOffset = Math.max(0, Math.min(this.scrollBoxedDeployedCreaturesOffset, Math.max(0, this.boxedCreaturesDeployedHeight - 96)));
            }
        }
    }

    //for other button lists
    //on clicking on the buttons in the party part of the journal
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            //for buttons that manage the selected creature
            for (int i = 0; i < this.manageSelectedCreatureButtons.size(); ++i) {
                GuiButton guibutton = this.manageSelectedCreatureButtons.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.manageSelectedCreatureButtons);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                        break;
                    guibutton = event.getButton();
                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.manageSelectedCreatureButtons));
                }
            }

            //for buttons that manage the creatures stored in the box
            for (int i = 0; i < this.creaturesInBoxButtons.size(); ++i) {
                GuiButton guibutton = this.creaturesInBoxButtons.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.creaturesInBoxButtons);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                        break;
                    guibutton = event.getButton();
                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.creaturesInBoxButtons));
                }
            }

            //for buttons that manage creatures deployed from box
            for (int i = 0; i < this.creaturesInBoxDeployedButtons.size(); ++i) {
                GuiButton guibutton = this.creaturesInBoxDeployedButtons.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.creaturesInBoxDeployedButtons);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                        break;
                    guibutton = event.getButton();
                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.creaturesInBoxDeployedButtons));
                }
            }
        }
    }

    private IPlayerTamedCreatures playerTamedCreatures() {
        return this.mc.player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);
    }

    private List<RiftCreature> getPlayerParty() {
        return this.playerTamedCreatures().getPartyCreatures(this.mc.player.world);
    }

    private List<RiftCreature> getPlayerBoxedCreatures() {
        return this.playerTamedCreatures().getBoxCreatures(this.mc.player.world);
    }

    private RiftTileEntityCreatureBox getCreatureBox() {
        if (this.mc.player.world.getTileEntity(this.creatureBoxPos) instanceof RiftTileEntityCreatureBox) {
            return (RiftTileEntityCreatureBox) this.mc.player.world.getTileEntity(this.creatureBoxPos);
        }
        return null;
    }

    private List<RiftCreature> getCreatureBoxDeployedCreatures() {
        if (this.getCreatureBox() != null) return this.getCreatureBox().getCreatures();
        return new ArrayList<>();
    }
}