package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiCreatureBoxBoxButton;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiCreatureBoxDeployedButton;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiCreatureBoxPartyButton;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import anightdazingzoroark.prift.server.message.*;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBoxHelper;
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
    private RiftCreature selectedCreature = null;
    private boolean changeCreaturesMode;
    private int partyPosSelected = -1;
    private int boxPosSelected = -1;
    private int boxDeployedPosSelected = -1;

    private int partyPosToMove = -1;
    private int boxPosToMove = -1;
    private int boxDeployedPosToMove = -1;

    private int guiTimePassed = 0;

    public RiftCreatureBoxMenu(int selectType, int posToSelect) {
        this.creatureBoxPos = ClientProxy.creatureBoxBlockPos;
        this.changeCreaturesMode = false;

        switch (selectType) {
            case 0: //for party
                this.partyPosSelected = posToSelect;
                break;
            case 1: //for box
                this.boxPosSelected = posToSelect;
                break;
            case 2: //for box deployed
                this.boxDeployedPosSelected = posToSelect;
                break;
        }
    }

    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);

        //force sync from server to client
        PlayerTamedCreaturesHelper.forceSyncParty(this.mc.player);
        PlayerTamedCreaturesHelper.forceSyncBox(this.mc.player);
        PlayerTamedCreaturesHelper.forceSyncPartySizeLevel(this.mc.player);
        PlayerTamedCreaturesHelper.forceSyncBoxSizeLevel(this.mc.player);
        PlayerTamedCreaturesHelper.forceSyncLastSelected(this.mc.player);
        PlayerTamedCreaturesHelper.forceSyncPartyLastOpenedTime(this.mc.player);
        PlayerTamedCreaturesHelper.forceSyncBoxLastOpenedTime(this.mc.player);

        //set selected creature
        if (this.partyPosSelected != -1 && this.boxPosSelected == -1 && this.boxDeployedPosSelected == -1) {
            if (this.getPlayerParty().size() > this.partyPosSelected) {
                this.selectedCreature = this.getPlayerParty().get(this.partyPosSelected);
            }
        }
        else if (this.partyPosSelected == -1 && this.boxPosSelected != -1 && this.boxDeployedPosSelected == -1) {
            if (this.getPlayerBoxedCreatures().size() > this.boxPosSelected) {
                this.selectedCreature = this.getPlayerBoxedCreatures().get(this.boxPosSelected);
            }
        }
        else if (this.partyPosSelected == -1 && this.boxPosSelected == -1 && this.boxDeployedPosSelected != -1) {
            if (this.getCreatureBoxDeployedCreatures().size() > this.boxDeployedPosSelected) {
                this.selectedCreature = this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosSelected);
            }
        }
    }

    @Override
    public void initGui() {
        PlayerTamedCreaturesHelper.setPartyLastOpenedTime(this.mc.player, (int) this.mc.world.getTotalWorldTime());
        PlayerTamedCreaturesHelper.setCreatureBoxLastOpenedTime(this.mc.player, (int) this.mc.world.getTotalWorldTime());
        PlayerTamedCreaturesHelper.openToRegenPlayerBoxCreatures(this.mc.player);

        //reset scrollbars
        this.scrollSidebarOffset = 0;

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc != null && mc.world != null) this.drawDefaultBackground();
        else return;

        //update creatures as long as this page is open
        PlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);
        RiftTileEntityCreatureBoxHelper.updateAllDeployedCreatures(this.mc.world, this.creatureBoxPos);

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //draw sidebar
        this.placePartyMemberButtons(mouseX, mouseY, partialTicks);

        //draw creature box creatures
        this.placeBoxedCreatureButtons(mouseX, mouseY, partialTicks);

        //draw stored creatures
        this.placeBoxedDeployedCreaturesButtons(mouseX, mouseY, partialTicks);

        //draw selected creature info
        this.createSelectedCreatureInfo(mouseX, mouseY, partialTicks);

        //manage creatures stored in creature box
        if (this.guiTimePassed++ % 2 == 0) {
            PlayerTamedCreaturesHelper.reenergizePartyUndeployedCreatures(this.mc.player, this.guiTimePassed / 2);
            PlayerTamedCreaturesHelper.regeneratePlayerBoxCreatures(this.mc.player);
        }

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
                if (this.partyPosToMove == -1 && this.boxPosToMove == -1 && this.boxDeployedPosToMove == -1) {
                    if (partyButton.id < this.getPlayerParty().size()) {
                        this.selectedCreature = this.getPlayerParty().get(partyButton.id);
                        this.partyPosToMove = partyButton.id;
                    }
                }
                else {
                    if (this.boxPosToMove != -1 && this.boxDeployedPosToMove == -1) {
                        //swap box creature w party creature
                        if (partyButton.id < this.getPlayerParty().size()) {
                            //if inventory except saddle not empty, just swap box and party creatures
                            if (this.getPlayerParty().get(partyButton.id).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(partyButton.id), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(partyButton.id), true));

                                PlayerTamedCreaturesHelper.boxPartySwap(this.mc.player, this.boxPosToMove, partyButton.id);

                                this.partyPosSelected = partyButton.id;
                                this.boxPosSelected = -1;
                                this.boxDeployedPosSelected = -1;
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.BOX_PARTY_SWAP;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.boxPosToMove, partyButton.id);
                            }
                        }
                        //move box creature to party
                        else {
                            PlayerTamedCreaturesHelper.boxToParty(this.mc.player, this.boxPosToMove);

                            this.partyPosSelected = partyButton.id;
                            this.boxPosSelected = -1;
                            this.boxDeployedPosSelected = -1;
                        }
                    }
                    else if (this.boxPosToMove == -1 && this.boxDeployedPosToMove != -1) {
                        //swap box deployed creature w party creature
                        if (partyButton.id < this.getPlayerParty().size()) {
                            //if creature is deployed, remove it from world
                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove), true));

                            PlayerTamedCreaturesHelper.boxDeployedPartySwap(this.mc.player, this.creatureBoxPos, this.boxDeployedPosToMove, partyButton.id);

                            this.partyPosSelected = partyButton.id;
                            this.boxPosSelected = -1;
                            this.boxDeployedPosSelected = -1;
                        }
                        //move box deployed creature to party
                        else {
                            //if creature is deployed, remove it from world
                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove), true));

                            PlayerTamedCreaturesHelper.boxDeployedToParty(this.mc.player, this.creatureBoxPos, this.boxDeployedPosToMove);

                            this.partyPosSelected = partyButton.id;
                            this.boxPosSelected = -1;
                            this.boxDeployedPosSelected = -1;
                        }
                    }
                    //party swap
                    else if (partyButton.id < this.getPlayerParty().size()) {
                        PlayerTamedCreaturesHelper.rearrangePartyCreatures(this.mc.player, this.partyPosToMove, partyButton.id);

                        this.partyPosSelected = partyButton.id;
                        this.boxPosSelected = -1;
                        this.boxDeployedPosSelected = -1;
                    }

                    this.partyPosToMove = -1;
                    this.boxPosToMove = -1;
                    this.boxDeployedPosToMove = -1;
                }
            }
            else {
                if (partyButton.id < this.getPlayerParty().size()) {
                    this.selectedCreature = this.getPlayerParty().get(partyButton.id);
                }
                else this.selectedCreature = null;

                this.partyPosSelected = partyButton.id;
                this.boxPosSelected = -1;
                this.boxDeployedPosSelected = -1;
            }
        }
        else if (button instanceof RiftGuiCreatureBoxBoxButton) {
            RiftGuiCreatureBoxBoxButton boxButton = (RiftGuiCreatureBoxBoxButton)button;
            if (this.changeCreaturesMode) {
                if (this.partyPosToMove == -1 && this.boxPosToMove == -1 && this.boxDeployedPosToMove == -1) {
                    if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                        this.selectedCreature = this.getPlayerBoxedCreatures().get(boxButton.id);

                        if (this.selectedCreature.getHealth() > 0) this.boxPosToMove = boxButton.id;
                        else {
                            ClientProxy.popupFromRadial = PopupFromCreatureBox.CREATURE_REVIVING;
                            this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                        }
                    }
                }
                else {
                    if (this.partyPosToMove != -1 && this.boxDeployedPosToMove == -1) {
                        //if selected creature is still regenerating,
                        //a message will pop out saying so
                        if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                            RiftCreature selectedForTest = this.getPlayerBoxedCreatures().get(boxButton.id);

                            //if selected creature that is deployed isn't owned by the person using the box,
                            //a message will pop out saying that it's not theirs
                            if (selectedForTest.getHealth() <= 0) {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.CREATURE_REVIVING;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                                return;
                            }
                        }

                        //swap party creature w box creature
                        if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                            //if inventory except saddle not empty, just move to box
                            if (this.getPlayerParty().get(this.partyPosToMove).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));

                                PlayerTamedCreaturesHelper.partyBoxSwap(this.mc.player, this.partyPosToMove, boxButton.id);

                                this.partyPosSelected = -1;
                                this.boxPosSelected = boxButton.id;
                                this.boxDeployedPosSelected = -1;
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.PARTY_BOX_SWAP;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.partyPosToMove, boxButton.id);
                            }
                        }
                        //move party creature to box
                        else {
                            //if inventory except saddle not empty, just move to box
                            if (this.getPlayerParty().get(this.partyPosToMove).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));

                                PlayerTamedCreaturesHelper.partyToBox(this.mc.player, this.partyPosToMove);

                                this.partyPosSelected = -1;
                                this.boxPosSelected = boxButton.id;
                                this.boxDeployedPosSelected = -1;
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.PARTY_TO_BOX;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.partyPosToMove, 0);
                            }
                        }
                    }
                    else if (this.partyPosToMove == -1 && this.boxDeployedPosToMove != -1) {
                        //if selected creature is still regenerating,
                        //a message will pop out saying so
                        if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                            RiftCreature selectedForTest = this.getPlayerBoxedCreatures().get(boxButton.id);

                            //if selected creature that is deployed isn't owned by the person using the box,
                            //a message will pop out saying that it's not theirs
                            if (selectedForTest.getHealth() <= 0) {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.CREATURE_REVIVING;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                                return;
                            }
                        }

                        //swap box deployed creature with box creature
                        if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                            //if inventory except saddle not empty, just move to box
                            if (this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove), true));

                                PlayerTamedCreaturesHelper.boxDeployedBoxSwap(this.mc.player, this.creatureBoxPos, this.boxDeployedPosToMove, boxButton.id);

                                this.partyPosSelected = -1;
                                this.boxPosSelected = boxButton.id;
                                this.boxDeployedPosSelected = -1;
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_BOX_SWAP;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.boxDeployedPosToMove, boxButton.id);
                            }
                        }
                        //move box deployed creature to box
                        else {
                            //if inventory except saddle not empty, just move to box
                            if (this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove).creatureInventory.isEmptyExceptSaddle()) {
                                //drop items from box deployed creature's inventory
                                NBTTagList inventoryNBT = this.getCreatureBox().getCreatureList().get(this.boxDeployedPosToMove).getTagList("Items", 10);
                                RiftMessages.WRAPPER.sendToServer(new RiftDropCreatureBoxDeployedMemberInventory(ClientProxy.creatureBoxBlockPos, this.boxDeployedPosToMove, inventoryNBT));
                                this.playerTamedCreatures().removeBoxCreatureDeployedInventory(this.mc.player.world, this.creatureBoxPos, this.boxDeployedPosToMove);

                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(this.boxDeployedPosToMove), true));

                                PlayerTamedCreaturesHelper.boxDeployedToBox(this.mc.player, this.creatureBoxPos, this.boxDeployedPosToMove);

                                this.partyPosSelected = -1;
                                this.boxPosSelected = boxButton.id;
                                this.boxDeployedPosSelected = -1;
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.BOX_DEPLOYED_TO_BOX;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.boxDeployedPosToMove, 0);
                            }
                        }
                    }
                    //box swap
                    else if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                        PlayerTamedCreaturesHelper.rearrangeBoxCreatures(this.mc.player, this.boxPosToMove, boxButton.id);

                        this.partyPosSelected = -1;
                        this.boxPosSelected = boxButton.id;
                        this.boxDeployedPosSelected = -1;
                    }

                    this.partyPosToMove = -1;
                    this.boxPosToMove = -1;
                    this.boxDeployedPosToMove = -1;
                }
            }
            else {
                if (boxButton.id < this.getPlayerBoxedCreatures().size()) {
                    this.selectedCreature = this.getPlayerBoxedCreatures().get(boxButton.id);
                }
                else this.selectedCreature = null;

                this.partyPosSelected = -1;
                this.boxPosSelected = boxButton.id;
                this.boxDeployedPosSelected = -1;
            }
        }
        else if (button instanceof RiftGuiCreatureBoxDeployedButton) {
            RiftGuiCreatureBoxDeployedButton boxDeployedButton = (RiftGuiCreatureBoxDeployedButton) button;
            if (this.changeCreaturesMode) {
                if (this.partyPosToMove == -1 && this.boxPosToMove == -1 && this.boxDeployedPosToMove == -1) {
                    if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                        this.selectedCreature = this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id);

                        //if selected creature that is deployed isn't owned by the person using the box,
                        //a message will pop out saying that it's not theirs
                        if (this.selectedCreature.getOwner().equals(this.mc.player)) this.boxDeployedPosToMove = boxDeployedButton.id;
                        else {
                            ClientProxy.popupFromRadial = PopupFromCreatureBox.OWNED_BY_OTHER;
                            this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                        }
                    }
                }
                else {
                    if (this.partyPosToMove != -1 && this.boxPosToMove == -1) {
                        //swap party creature with box deployed creature
                        if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                            //if selected creature that is deployed isn't owned by the person using the box,
                            //a message will pop out saying that it's not theirs
                            RiftCreature selectedForTest = this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id);

                            //if selected creature that is deployed isn't owned by the person using the box,
                            //a message will pop out saying that it's not theirs
                            if (!selectedForTest.getOwner().equals(this.mc.player)) {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.OWNED_BY_OTHER;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                                return;
                            }

                            //if creature is deployed, remove it from world
                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));

                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id), true));

                            PlayerTamedCreaturesHelper.partyBoxDeployedSwap(this.mc.player, this.creatureBoxPos, this.partyPosToMove, boxDeployedButton.id);

                            this.partyPosSelected = -1;
                            this.boxPosSelected = -1;
                            this.boxDeployedPosSelected = boxDeployedButton.id;
                        }
                        //move party creature to box deployed
                        else {
                            //if creature is deployed, remove it from world
                            RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));
                            RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getPlayerParty().get(this.partyPosToMove), true));

                            PlayerTamedCreaturesHelper.partyToBoxDeployed(this.mc.player, this.creatureBoxPos, this.partyPosToMove);

                            this.partyPosSelected = -1;
                            this.boxPosSelected = -1;
                            this.boxDeployedPosSelected = boxDeployedButton.id;
                        }
                    }
                    else if (this.partyPosToMove == -1 && this.boxPosToMove != -1) {
                        //swap box creature with box deployed creature
                        if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                            //if selected creature that is deployed isn't owned by the person using the box,
                            //a message will pop out saying that it's not theirs
                            RiftCreature selectedForTest = this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id);

                            //if selected creature that is deployed isn't owned by the person using the box,
                            //a message will pop out saying that it's not theirs
                            if (!selectedForTest.getOwner().equals(this.mc.player)) {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.OWNED_BY_OTHER;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                                return;
                            }

                            //if inventory except saddle not empty, just move to box
                            if (this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id).creatureInventory.isEmptyExceptSaddle()) {
                                //if creature is deployed, remove it from world
                                RiftMessages.WRAPPER.sendToServer(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id), true));
                                RiftMessages.WRAPPER.sendToAll(new RiftRemoveAfterSendToBox(this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id), true));

                                PlayerTamedCreaturesHelper.boxBoxDeployedSwap(this.mc.player, this.creatureBoxPos, this.boxPosToMove, boxDeployedButton.id);

                                this.partyPosSelected = -1;
                                this.boxPosSelected = -1;
                                this.boxDeployedPosSelected = boxDeployedButton.id;
                            }
                            //otherwise, open a prompt that asks whether or not to have em dropped
                            else {
                                ClientProxy.popupFromRadial = PopupFromCreatureBox.REMOVE_INVENTORY;
                                ClientProxy.swapTypeForPopup = RiftChangePartyOrBoxOrder.SwapType.BOX_BOX_DEPLOYED_SWAP;
                                this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, this.boxPosToMove, boxDeployedButton.id);
                            }
                        }
                        //move box creature to box deployed
                        else {
                            PlayerTamedCreaturesHelper.boxToBoxDeployed(this.mc.player, this.creatureBoxPos, this.boxPosToMove);

                            this.partyPosSelected = -1;
                            this.boxPosSelected = -1;
                            this.boxDeployedPosSelected = boxDeployedButton.id;
                        }
                    }
                    //box deployed swap
                    else if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                        this.playerTamedCreatures().rearrangeDeployedBoxCreatures(this.mc.player.world, this.creatureBoxPos, this.boxDeployedPosToMove, boxDeployedButton.id);

                        this.partyPosSelected = -1;
                        this.boxPosSelected = -1;
                        this.boxDeployedPosSelected = boxDeployedButton.id;
                    }

                    this.partyPosToMove = -1;
                    this.boxPosToMove = -1;
                    this.boxDeployedPosToMove = -1;
                }
            }
            else {
                if (boxDeployedButton.id < this.getCreatureBoxDeployedCreatures().size()) {
                    this.selectedCreature = this.getCreatureBoxDeployedCreatures().get(boxDeployedButton.id);
                }
                else this.selectedCreature = null;

                this.partyPosSelected = -1;
                this.boxPosSelected = -1;
                this.boxDeployedPosSelected = boxDeployedButton.id;
            }
        }
        else if (this.manageSelectedCreatureButtons.contains(button)) {
            if (this.selectedCreature != null) {
                if (this.selectedCreature.getOwner().equals(this.mc.player)) {
                    if (button.id == 0) {
                        ClientProxy.creatureUUID = this.selectedCreature.getUniqueID();
                        ClientProxy.popupFromRadial = PopupFromCreatureBox.CHANGE_NAME;
                        this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                    }
                    else if (button.id == 1) {
                        ClientProxy.creatureUUID = this.selectedCreature.getUniqueID();
                        ClientProxy.popupFromRadial = PopupFromCreatureBox.RELEASE;
                        this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                    }
                }
                else if (button.id == 0 || button.id == 1) {
                    ClientProxy.popupFromRadial = PopupFromCreatureBox.OWNED_BY_OTHER;
                    this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, 0, 0, 0);
                }
            }
            if (button.id == 2) {
                this.changeCreaturesMode = !this.changeCreaturesMode;
                this.partyPosToMove = -1;
                this.boxPosToMove = -1;
                this.boxDeployedPosToMove = -1;
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
                this.buttonList.add(new RiftGuiCreatureBoxPartyButton(creature, x, (this.width - 96)/2 - 147, (this.height - 32) / 2 - 78 + (40 * x), (this.height - 170) / 2 - 10, (this.height - 170) / 2 + 160));
                this.partyBarHeight += 40;
            }
            else {
                this.buttonList.add(new RiftGuiCreatureBoxPartyButton(null, x, (this.width - 96)/2 - 147, (this.height - 32) / 2 - 78 + (40 * x), (this.height - 170) / 2 - 10, (this.height - 170) / 2 + 160));
                this.partyBarHeight += 40;
            }
        }
    }

    private void boxedCreaturesButtonListInit() {
        this.creaturesInBoxButtons.clear();
        this.boxedCreaturesHeight = 0;

        int size = this.getPlayerBoxedCreatures().size() + (this.getPlayerBoxedCreatures().size() < this.playerTamedCreatures().getMaxBoxSize() ? 1 : 0);
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
        int y = (this.height - 170) / 2 - 10;

        // for scaling
        int scaleFactor = new ScaledResolution(this.mc).getScaleFactor();

        int scissorX = x * scaleFactor;
        int scissorY = (this.height - y - 170) * scaleFactor;
        int scissorW = (this.width - 96) * scaleFactor;
        int scissorH = 170 * scaleFactor;

        //for buttons on the side
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        for (GuiButton guiButton : this.buttonList) {
            RiftGuiCreatureBoxPartyButton partyButton = (RiftGuiCreatureBoxPartyButton) guiButton;

            if (this.partyPosSelected != -1 && this.boxDeployedPosSelected == -1 && this.boxPosSelected == -1) {
                partyButton.isSelected = this.partyPosSelected == partyButton.id && partyButton.id < this.getPlayerParty().size();
            }

            partyButton.toMove = this.changeCreaturesMode && this.partyPosToMove != -1 && this.partyPosToMove == guiButton.id;

            //draw button
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //create scrollbar
        if (this.partyBarHeight > 170) {
            int k = (this.width - 1) / 2 - 97;
            int l = (this.height - 170) / 2 - 12;
            //scrollbar background
            drawRect(k, l, k + 1, l + 170, 0xFFA0A0A0);
            //scrollbar progress
            int thumbHeight = Math.max(10, (int)((float)170 * (170f / this.partyBarHeight)));
            int thumbPosition = (int)((float)this.scrollSidebarOffset / (this.partyBarHeight - 170) * (170 - thumbHeight));
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
            RiftGuiCreatureBoxBoxButton boxButton = (RiftGuiCreatureBoxBoxButton) guiButton;

            if (this.boxPosSelected != -1 && this.partyPosSelected == -1 && this.boxDeployedPosSelected == -1) {
                boxButton.isSelected = this.boxPosSelected == boxButton.id && boxButton.id < this.getPlayerBoxedCreatures().size();
            }

            boxButton.toMove = this.changeCreaturesMode && this.boxPosToMove != -1 && this.boxPosToMove == guiButton.id;

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
        int y = (this.height - 63) / 2 + 69;

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
            RiftGuiCreatureBoxDeployedButton boxButton = (RiftGuiCreatureBoxDeployedButton) guiButton;

            if (this.boxDeployedPosSelected != -1 && this.partyPosSelected == -1 && this.boxPosSelected == -1) {
                boxButton.isSelected = this.boxDeployedPosSelected == boxButton.id && boxButton.id < this.getCreatureBoxDeployedCreatures().size();
            }

            boxButton.toMove = this.changeCreaturesMode && this.boxDeployedPosToMove != -1 && this.boxDeployedPosToMove == guiButton.id;

            //draw button
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //create scrollbar
        if (this.boxedCreaturesDeployedHeight > 63) {
            int k = (this.width - 1) / 2 + 77;
            int l = (this.height - 63) / 2 + 69;
            //scrollbar background
            drawRect(k, l, k + 1, l + 63, 0xFF7A7A7A);
            //scrollbar progress
            int thumbHeight = Math.max(10, (int)((float)63 * (63f / this.boxedCreaturesDeployedHeight)));
            int thumbPosition = (int)((float)this.scrollBoxedDeployedCreaturesOffset / (this.boxedCreaturesDeployedHeight - 96) * (96 - thumbHeight));
            drawRect(k, l + thumbPosition, k + 1, l + thumbHeight + thumbPosition, 0xFF616161);
        }
    }

    private void createSelectedCreatureInfo(int mouseX, int mouseY, float partialTicks) {
        if (this.selectedCreature != null) {
            //for creature size
            float scaleMultiplier = RiftUtil.getCreatureModelScale(this.selectedCreature) * 0.5f;

            //make sure its not rotated and red when dead
            if (this.selectedCreature.getHealth() / this.selectedCreature.getMaxHealth() <= 0) {
                this.selectedCreature.deathTime = 0;
                this.selectedCreature.isDead = false;
                this.selectedCreature.hurtTime = 0;
            }

            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();

            //entity position
            GlStateManager.translate(this.width / 2f + 140f, this.height / 2f - 30f, 180.0F);

            //for rotating
            int mouseXMod = this.selectedCreature.getHealth() / this.selectedCreature.getMaxHealth() <= 0 ? 592 : RiftUtil.clamp(mouseX + (this.width / 2 + 140), 480, 592);
            GlStateManager.rotate(mouseXMod, 0.0F, 1.0F, 0.0F);

            GlStateManager.rotate(180, 1.0F, 0.0F, 0.0F);

            //for scale
            GlStateManager.scale(scaleMultiplier, scaleMultiplier, scaleMultiplier);

            //render creature
            Minecraft.getMinecraft().getRenderManager().renderEntity(this.selectedCreature, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);

            //exit gl stuff
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();

            //add pacifier icon for babies
            if (this.selectedCreature.isBaby()) {
                this.mc.getTextureManager().bindTexture(background);
                drawModalRectWithCustomSizedTexture((this.width - 11) / 2 + 100, (this.height - 11) / 2 - 85, 362, 216, 11, 11, 408, 300);
            }

            //add cross over when creature is dead
            if (this.selectedCreature.getHealth() / this.selectedCreature.getMaxHealth() <= 0) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(background);
                drawModalRectWithCustomSizedTexture((this.width - 70) / 2 + 140, (this.height - 68) / 2 - 55, 252, 232, 70, 68, 408, 300);
            }

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
            String healthString = I18n.format("tametrait.health")+": "+ Math.ceil(this.selectedCreature.getHealth()) +"/"+ this.selectedCreature.getMaxHealth() +" HP";
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

            //draw energy
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            String energyString = I18n.format("tametrait.energy")+": "+ Math.ceil(this.selectedCreature.getEnergy()) +"/20";
            this.fontRenderer.drawString(energyString, (int)((this.width / 2 + 86) / 0.5), (int)(((this.height - this.fontRenderer.FONT_HEIGHT) / 2 + 33) / 0.5), 0);
            GlStateManager.popMatrix();

            //draw energy bar
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 36, 252, 216, 110, 4, 408, 300);

            double energyRatio = this.selectedCreature.getEnergy() / 20D;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 36, 252, 228, (int)(110 * energyRatio), 4, 408, 300);

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
            //change positions based on how far the user scrolled
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
        for (GuiButton button : this.creaturesInBoxDeployedButtons) {
            //change positions based on how far the user scrolled
            button.y -= this.scrollBoxedDeployedCreaturesOffset;

            //disable buttons depending on whether or not they are visible
            int minY = (this.height - 63) / 2 + 52;
            int maxY = (this.height - 63) / 2 + 115;
            int visibleButtonSize = 0;
            for (int y = button.y; y <= button.y + 30; y++) {
                if (minY <= y && maxY >= y) visibleButtonSize++;
            }
            if (visibleButtonSize == 0) button.enabled = false;
        }

        //update selected creature buttons
        this.manageSelectedCreatureButtons.clear();
        GuiButton changeNameButton = new GuiButton(0, (this.width - 100)/2 + 140, (this.height - 20)/2 + 60, 100, 20, I18n.format("creature_box.change_name"));
        changeNameButton.enabled = false;
        GuiButton releaseButton = new GuiButton(1, (this.width - 100)/2 + 140, (this.height - 20)/2 + 85, 100, 20, I18n.format("creature_box.release"));
        releaseButton.enabled = false;
        String rearrangeName = this.changeCreaturesMode ? I18n.format("creature_box.stop_rearrange_creatures") : I18n.format("creature_box.rearrange_creatures");
        GuiButton rearrangeButton = new GuiButton(2, (this.width - 100)/2 - 147, (this.height - 20)/2 + 90, 100, 20, rearrangeName);
        this.manageSelectedCreatureButtons.add(changeNameButton);
        this.manageSelectedCreatureButtons.add(releaseButton);
        this.manageSelectedCreatureButtons.add(rearrangeButton);
    }

    private boolean isMouseOverPartyBar(int mouseX, int mouseY) {
        int minX = (this.width - 96) / 2 - 147;
        int minY = (this.height - 170) / 2 - 10;
        int maxX = (this.width - 96) / 2 - 51;
        int maxY = (this.height - 170) / 2 + 160;
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
                this.scrollSidebarOffset = Math.max(0, Math.min(this.scrollSidebarOffset, Math.max(0, this.partyBarHeight - 170)));
            }
            if (this.isMouseOverBoxedCreatures(mouseX, mouseY)) {
                this.scrollBoxedCreaturesOffset += (scroll > 0) ? -10 : 10;
                this.scrollBoxedCreaturesOffset = Math.max(0, Math.min(this.scrollBoxedCreaturesOffset, Math.max(0, this.boxedCreaturesHeight - 96)));
            }
            if (this.isMouseOverBoxedDeployedCreatures(mouseX, mouseY)) {
                this.scrollBoxedDeployedCreaturesOffset += (scroll > 0) ? -10 : 10;
                this.scrollBoxedDeployedCreaturesOffset = Math.max(0, Math.min(this.scrollBoxedDeployedCreaturesOffset, Math.max(0, this.boxedCreaturesDeployedHeight - 63)));
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
