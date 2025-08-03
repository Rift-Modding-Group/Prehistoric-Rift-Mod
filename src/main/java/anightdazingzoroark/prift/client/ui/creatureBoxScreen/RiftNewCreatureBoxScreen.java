package anightdazingzoroark.prift.client.ui.creatureBoxScreen;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements.RiftBoxMemButtonForBox;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements.RiftCreatureBoxSelectedScrollableSection;
import anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements.RiftPartyMemButtonForBox;
import anightdazingzoroark.prift.client.ui.elements.RiftClickableSection;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiSectionButton;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureBoxStorage;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiftNewCreatureBoxScreen extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/new_creature_box_background.png");
    private SelectedPos selectedPos;
    private int currentBox;
    private final List<RiftPartyMemButtonForBox> partyMemButtons = new ArrayList<>();
    private String boxName = "";
    private final List<RiftBoxMemButtonForBox> boxMemButtons = new ArrayList<>();
    private RiftCreature creatureToDraw;
    private RiftCreatureBoxSelectedScrollableSection selectedScrollableSection;
    private RiftClickableSection switchMembersButton;
    private RiftClickableSection leftBoxButton;
    private RiftClickableSection rightBoxButton;
    private boolean switchMembersMode;

    public RiftNewCreatureBoxScreen(int selectedPosType, int selectedPosOne, int selectedPosTwo) {
        super();

        if (selectedPosType >= 0) {
            SelectedPosType type = SelectedPosType.values()[selectedPosType];
            if (type == SelectedPosType.PARTY) this.selectedPos = new SelectedPos(type, new int[]{selectedPosOne});
            else if (type == SelectedPosType.BOX) this.selectedPos = new SelectedPos(type, new int[]{selectedPosOne, selectedPosTwo});
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        //update info upon opening
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

        //create party buttons
        this.partyMemButtons.clear();
        FixedSizeList<NBTTagCompound> playerPartyNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player);
        for (int x = 0; x < playerPartyNBT.size(); x++) {
            int partyMemXOffset = -101 + (x % 2) * 22;
            int partyMemYOffset = -78 + (x / 2) * 22;
            RiftPartyMemButtonForBox partyMemButton = new RiftPartyMemButtonForBox(playerPartyNBT.get(x), this.width, this.height, partyMemXOffset, partyMemYOffset, this.fontRenderer, this.mc);
            this.partyMemButtons.add(partyMemButton);
        }

        //create current box
        this.currentBox = NewPlayerTamedCreaturesHelper.getLastOpenedBox(this.mc.player);
        CreatureBoxStorage creatureBoxStorage = NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player);
        //create box name
        this.boxName = creatureBoxStorage.getBoxName(this.currentBox);
        //create box member buttons
        this.boxMemButtons.clear();
        FixedSizeList<NBTTagCompound> creatureBoxNBT = creatureBoxStorage.getBoxContents(this.currentBox);
        for (int x = 0; x < creatureBoxNBT.size(); x++) {
            int creatureBoxMemX = -47 + (x % 5) * 35;
            int creatureBoxMemY = -89 + (x / 5) * 35;
            RiftBoxMemButtonForBox boxMemButton = new RiftBoxMemButtonForBox(creatureBoxNBT.get(x), this.width, this.height, creatureBoxMemX, creatureBoxMemY, this.fontRenderer, this.mc);
            this.boxMemButtons.add(boxMemButton);
        }

        //create switch button (and scale it)
        this.switchMembersButton = new RiftClickableSection(20, 18, this.width, this.height, 103, -111, this.fontRenderer, this.mc);
        this.switchMembersButton.addImage(background, 20, 18, 400, 300, 160, 282, 180, 282);
        this.switchMembersButton.setSelectedUV(200, 282);
        this.switchMembersButton.setScale(0.75f);

        //create left and right box buttons
        this.leftBoxButton = new RiftClickableSection(13, 13, this.width, this.height, -37, -113, this.fontRenderer, this.mc);
        this.leftBoxButton.addImage(background, 13, 13, 400, 300, 173, 268, 199, 268);

        this.rightBoxButton = new RiftClickableSection(13, 13, this.width, this.height, 83, -113, this.fontRenderer, this.mc);
        this.rightBoxButton.addImage(background, 13, 13, 400, 300, 160, 268, 186, 268);

        //create scrollable section for selected creature
        this.selectedScrollableSection = new RiftCreatureBoxSelectedScrollableSection(this.width, this.height, this.fontRenderer, this.mc);
        //add info to scrollable section if there somehow is data
        if (this.selectedPos != null) {
            if (this.selectedPos.selectedPosType == SelectedPosType.PARTY) {
                NBTTagCompound selectedNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player).get(this.selectedPos.pos[0]);
                this.creatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, selectedNBT);
                this.selectedScrollableSection.setCreatureNBT(selectedNBT);
            }
            else if (this.selectedPos.selectedPosType == SelectedPosType.BOX) {
                NBTTagCompound selectedNBT = NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player).getBoxContents(this.selectedPos.pos[0]).get(this.selectedPos.pos[1]);
                this.creatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, selectedNBT);
                this.selectedScrollableSection.setCreatureNBT(selectedNBT);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        //offset for when theres selected creature
        int selectedXOffset = this.selectedPos != null && !this.switchMembersMode ? -62 : 0;

        //constantly update party members and boxed
        //creatures as long as this screen is opened
        this.updateAllCreatures();

        //draw background
        this.drawGuiContainerBackgroundLayer();

        //draw label for party members
        String partyLabel = I18n.format("journal.party_label.party");
        int partyLabelX = this.width / 2 - 108 + selectedXOffset;
        int partyLabelY = (this.height - this.fontRenderer.FONT_HEIGHT) / 2 - 96;
        this.fontRenderer.drawString(partyLabel, partyLabelX, partyLabelY, 0x000000);

        //draw party buttons
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButtonForBox partyMemButton = this.partyMemButtons.get(x);
            partyMemButton.setAdditionalOffset(selectedXOffset, 0);
            partyMemButton.drawSection(mouseX, mouseY);

            //enable/disable selection
            if (this.selectedPos != null && this.selectedPos.selectedPosType == SelectedPosType.PARTY) {
                partyMemButton.setSelected(x == this.selectedPos.pos[0]);
            }
            else partyMemButton.setSelected(false);
        }

        //draw bg for box name (whose name can be changed by clicking on it)
        //for now its just a background
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - 96) / 2 + 23 + selectedXOffset;
        int l = (this.height - 13) / 2 - 113;
        drawModalRectWithCustomSizedTexture(k, l, 100, 255, 96, 13, 400f, 300f);
        //put the actual name on it now
        int boxNameX = (this.width - this.fontRenderer.getStringWidth(this.boxName)) / 2 + 23 + selectedXOffset;
        int boxNameY = (this.height - this.fontRenderer.FONT_HEIGHT) / 2 - 112;
        this.fontRenderer.drawString(this.boxName, boxNameX, boxNameY, 0xffffff);

        //draw swap creatures button
        this.switchMembersButton.setAdditionalOffset(selectedXOffset, 0);
        this.switchMembersButton.drawSection(mouseX, mouseY);

        //draw arrows
        this.leftBoxButton.setAdditionalOffset(selectedXOffset, 0);
        this.leftBoxButton.drawSection(mouseX, mouseY);
        this.rightBoxButton.setAdditionalOffset(selectedXOffset, 0);
        this.rightBoxButton.drawSection(mouseX, mouseY);

        //draw creature box buttons
        for (int x = 0; x < this.boxMemButtons.size(); x++) {
            RiftBoxMemButtonForBox boxMemButton = this.boxMemButtons.get(x);

            boxMemButton.setAdditionalOffset(selectedXOffset, 0);
            boxMemButton.drawSection(mouseX, mouseY);

            //enable/disable selection
            if (this.selectedPos != null && this.selectedPos.selectedPosType == SelectedPosType.BOX) {
                boxMemButton.setSelected(this.currentBox == this.selectedPos.pos[0] && x == this.selectedPos.pos[1]);
            }
            else boxMemButton.setSelected(false);
        }

        //draw creature info
        if (!this.switchMembersMode && this.selectedPos != null && this.selectedScrollableSection.getCreatureNBT() != null && !this.selectedScrollableSection.getCreatureNBT().isEmpty()) {
            //draw creature
            if (this.creatureToDraw != null) {
                GlStateManager.color(1f, 1f, 1f, 1f);
                GlStateManager.pushMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.enableDepth();
                GlStateManager.translate(this.width / 2f + 110, this.height / 2f - 30, 210f);
                GlStateManager.rotate(180, 1f, 0f, 0f);
                GlStateManager.rotate(150, 0f, 1f, 0f);
                GlStateManager.scale(20f, 20f, 20f);
                this.creatureToDraw.deathTime = 0;
                this.creatureToDraw.isDead = false;
                this.creatureToDraw.hurtTime = 0;
                this.mc.getRenderManager().renderEntity(this.creatureToDraw, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
                GlStateManager.disableDepth();
                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }

            //draw section
            this.selectedScrollableSection.drawSectionContents(mouseX, mouseY, partialTicks);
        }
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int backgroundWidth = this.selectedPos != null && !this.switchMembersMode ? 351 : 227;
        int k = (this.width - backgroundWidth) / 2;
        int l = (this.height - 246) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, backgroundWidth, 246, 400f, 300f);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //manage party button clicking
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButtonForBox partyMemButton = this.partyMemButtons.get(x);

            //switch creatures around
            if (this.switchMembersMode) {
                //switch creature from party with something already selected
                if (this.selectedPos != null) {
                    if (partyMemButton.isHovered(mouseX, mouseY)) {
                        //switch box creature with party creature
                        if (this.selectedPos.selectedPosType == SelectedPosType.BOX) {
                            //safety check for box to party: dismiss the party member first if its deployed
                            NBTTagCompound partyMemNBT = partyMemButton.getCreatureNBT();
                            if (partyMemNBT != null && !partyMemNBT.isEmpty()) {
                                PlayerTamedCreatures.DeploymentType deploymentType = PlayerTamedCreatures.DeploymentType.values()[partyMemNBT.getByte("DeploymentType")];
                                if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY) {
                                    NewPlayerTamedCreaturesHelper.deployCreatureFromParty(this.mc.player, x, false);
                                }
                            }
                            NewPlayerTamedCreaturesHelper.boxPartySwap(this.mc.player, this.selectedPos.pos[0], this.selectedPos.pos[1], x);
                            this.selectedPos = null;

                            partyMemButton.playPressSound(this.mc.getSoundHandler());
                        }
                        //switch within party
                        else if (this.selectedPos.selectedPosType == SelectedPosType.PARTY) {
                            NewPlayerTamedCreaturesHelper.rearrangePartyCreatures(this.mc.player, x, this.selectedPos.pos[0]);
                            this.selectedPos = null;

                            partyMemButton.playPressSound(this.mc.getSoundHandler());
                        }
                    }
                }
                //if theres nothing already initially selected, select the creature
                else {
                    if (partyMemButton.isHovered(mouseX, mouseY)) {
                        this.selectedPos = new SelectedPos(SelectedPosType.PARTY, new int[]{x});
                        partyMemButton.playPressSound(this.mc.getSoundHandler());
                    }
                }
            }
            //select creature
            else {
                if (partyMemButton.isHovered(mouseX, mouseY) && partyMemButton.getCreatureNBT() != null && !partyMemButton.getCreatureNBT().isEmpty()) {
                    //if theres a selected pos and the clicked button is
                    //equal to said selected pos, clear it and the creature to display
                    if (this.selectedPos != null && this.selectedPos.selectedPosType == SelectedPosType.PARTY && this.selectedPos.pos[0] == x) {
                        this.selectedPos = null;
                        this.creatureToDraw = null;
                        this.selectedScrollableSection.setCreatureNBT(new NBTTagCompound());
                    }
                    //otherwise set it and use its nbt to create the creature to display
                    else {
                        this.selectedPos = new SelectedPos(SelectedPosType.PARTY, new int[]{x});
                        this.creatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, partyMemButton.getCreatureNBT());
                        this.selectedScrollableSection.setCreatureNBT(partyMemButton.getCreatureNBT());
                    }

                    partyMemButton.playPressSound(this.mc.getSoundHandler());
                }
            }
        }

        //manage box button clicking
        for (int x = 0; x < this.boxMemButtons.size(); x++) {
            RiftBoxMemButtonForBox boxMemButton = this.boxMemButtons.get(x);

            //switch creatures around
            if (this.switchMembersMode) {
                //switch creature from box with something already selected
                if (this.selectedPos != null) {
                    if (boxMemButton.isHovered(mouseX, mouseY)) {
                        //switch within box
                        if (this.selectedPos.selectedPosType == SelectedPosType.BOX) {
                            NewPlayerTamedCreaturesHelper.rearrangeBoxCreatures(this.mc.player, this.currentBox, x, this.selectedPos.pos[0], this.selectedPos.pos[1]);
                            this.selectedPos = null;

                            boxMemButton.playPressSound(this.mc.getSoundHandler());
                        }
                        //switch party creature with box creature
                        else if (this.selectedPos.selectedPosType == SelectedPosType.PARTY) {
                            //safety check for box to party: dismiss the party member first if its deployed
                            NBTTagCompound partyMemNBT = this.partyMemButtons.get(this.selectedPos.pos[0]).getCreatureNBT();
                            if (partyMemNBT != null && !partyMemNBT.isEmpty()) {
                                PlayerTamedCreatures.DeploymentType deploymentType = PlayerTamedCreatures.DeploymentType.values()[partyMemNBT.getByte("DeploymentType")];
                                if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY) {
                                    NewPlayerTamedCreaturesHelper.deployCreatureFromParty(this.mc.player, this.selectedPos.pos[0], false);
                                }
                            }
                            NewPlayerTamedCreaturesHelper.boxPartySwap(this.mc.player, this.currentBox, x, this.selectedPos.pos[0]);
                            this.selectedPos = null;

                            boxMemButton.playPressSound(this.mc.getSoundHandler());
                        }
                    }
                }
                else {
                    //if theres nothing already initially selected, select the creature
                    if (boxMemButton.isHovered(mouseX, mouseY)) {
                        this.selectedPos = new SelectedPos(SelectedPosType.BOX, new int[]{this.currentBox, x});
                        boxMemButton.playPressSound(this.mc.getSoundHandler());
                    }
                }
            }
            else {
                //select creature
                if (boxMemButton.isHovered(mouseX, mouseY) && boxMemButton.getCreatureNBT() != null && !boxMemButton.getCreatureNBT().isEmpty()) {
                    //if theres a selected pos and the clicked button is
                    //equal to said selected pos, clear it and the creature to display
                    if (this.selectedPos != null && this.selectedPos.selectedPosType == SelectedPosType.BOX
                            && this.selectedPos.pos[0] == this.currentBox && this.selectedPos.pos[1] == x) {
                        this.selectedPos = null;
                        this.creatureToDraw = null;
                        this.selectedScrollableSection.setCreatureNBT(new NBTTagCompound());
                    }
                    //otherwise set it and use its nbt to create the creature to display
                    else {
                        this.selectedPos = new SelectedPos(SelectedPosType.BOX, new int[]{this.currentBox, x});
                        this.creatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, boxMemButton.getCreatureNBT());
                        this.selectedScrollableSection.setCreatureNBT(boxMemButton.getCreatureNBT());
                    }

                    boxMemButton.playPressSound(this.mc.getSoundHandler());
                }
            }
        }

        //manage switch creatures button
        if (this.switchMembersButton.isHovered(mouseX, mouseY)) {
            this.switchMembersMode = !this.switchMembersMode;
            this.switchMembersButton.setSelected(this.switchMembersMode);
            this.selectedScrollableSection.setCreatureNBT(null);
            //when switching back out of switch mode, remove selected creature
            if (!this.switchMembersMode) this.selectedPos = null;
            this.switchMembersButton.playPressSound(this.mc.getSoundHandler());
        }

        //manage left box clicking
        if (this.leftBoxButton.isHovered(mouseX, mouseY)) {
            //change current box
            this.currentBox = (this.currentBox - 1 < 0) ? (CreatureBoxStorage.maxBoxAmnt - 1) : this.currentBox - 1;
            NewPlayerTamedCreaturesHelper.setLastOpenedBox(this.mc.player, this.currentBox);
            NewPlayerTamedCreaturesHelper.forceSyncLastOpenedBox(this.mc.player);

            CreatureBoxStorage creatureBoxStorage = NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player);
            //change box name
            this.boxName = creatureBoxStorage.getBoxName(this.currentBox);
            //change box member buttons
            this.boxMemButtons.clear();
            FixedSizeList<NBTTagCompound> creatureBoxNBT = creatureBoxStorage.getBoxContents(this.currentBox);
            for (int x = 0; x < creatureBoxNBT.size(); x++) {
                int creatureBoxMemX = -47 + (x % 5) * 35;
                int creatureBoxMemY = -89 + (x / 5) * 35;
                RiftBoxMemButtonForBox boxMemButton = new RiftBoxMemButtonForBox(creatureBoxNBT.get(x), this.width, this.height, creatureBoxMemX, creatureBoxMemY, this.fontRenderer, this.mc);
                this.boxMemButtons.add(boxMemButton);
            }
            this.leftBoxButton.playPressSound(this.mc.getSoundHandler());
        }
        //manage right click box clicking
        else if (this.rightBoxButton.isHovered(mouseX, mouseY)) {
            //change current box
            this.currentBox = (this.currentBox + 1 >= CreatureBoxStorage.maxBoxAmnt) ? 0 : this.currentBox + 1;
            NewPlayerTamedCreaturesHelper.setLastOpenedBox(this.mc.player, this.currentBox);
            NewPlayerTamedCreaturesHelper.forceSyncLastOpenedBox(this.mc.player);

            CreatureBoxStorage creatureBoxStorage = NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player);
            //change box name
            this.boxName = creatureBoxStorage.getBoxName(this.currentBox);
            //change box member buttons
            this.boxMemButtons.clear();
            FixedSizeList<NBTTagCompound> creatureBoxNBT = creatureBoxStorage.getBoxContents(this.currentBox);
            for (int x = 0; x < creatureBoxNBT.size(); x++) {
                int creatureBoxMemX = -47 + (x % 5) * 35;
                int creatureBoxMemY = -89 + (x / 5) * 35;
                RiftBoxMemButtonForBox boxMemButton = new RiftBoxMemButtonForBox(creatureBoxNBT.get(x), this.width, this.height, creatureBoxMemX, creatureBoxMemY, this.fontRenderer, this.mc);
                this.boxMemButtons.add(boxMemButton);
            }
            this.rightBoxButton.playPressSound(this.mc.getSoundHandler());
        }

        //manage buttons on selected creature info
        int sectionTop = (this.selectedScrollableSection.guiHeight - this.selectedScrollableSection.height) / 2 + this.selectedScrollableSection.yOffset;
        int sectionBottom = sectionTop + this.selectedScrollableSection.height;
        for (RiftGuiSectionButton button : this.selectedScrollableSection.getActiveButtons()) {
            int buttonTop = button.y;
            int buttonBottom = button.y + button.height;
            boolean clickWithinVisiblePart = mouseY >= Math.max(buttonTop, sectionTop) && mouseY <= Math.min(buttonBottom, sectionBottom);
            if (clickWithinVisiblePart && button.mousePressed(this.mc, mouseX, mouseY)) {
                if (button.buttonId.equals("moreInfo") && this.selectedPos != null) {
                    int posOne = this.selectedPos.pos[0];
                    int posTwo = this.selectedPos.pos.length > 1 ? this.selectedPos.pos[1] : -1;
                    this.mc.player.openGui(
                            RiftInitialize.instance,
                            RiftGui.GUI_INFO_FROM_BOX,
                            this.mc.world,
                            this.selectedPos.selectedPosType.ordinal(),
                            posOne,
                            posTwo
                    );
                }
                else if (button.buttonId.equals("release")) {}

                button.playPressSound(this.mc.getSoundHandler());
            }
        }
    }

    private void updateAllCreatures() {
        //update party creatures while ui is opened
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player); //update nbt from deployed creatures
        FixedSizeList<NBTTagCompound> newPartyNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player);
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButtonForBox button = this.partyMemButtons.get(x);
            button.setCreatureNBT(newPartyNBT.get(x));
        }

        //update box creatures while ui is opened
        NewPlayerTamedCreaturesHelper.forceSyncBoxNBT(this.mc.player);
        if (this.currentBox < 0 || this.currentBox >= CreatureBoxStorage.maxBoxAmnt) return; //skip if current box is out of bounds for safety purposes
        FixedSizeList<NBTTagCompound> newBoxNBT = NewPlayerTamedCreaturesHelper.getCreatureBoxStorage(this.mc.player).getBoxContents(this.currentBox);
        for (int x = 0; x < this.boxMemButtons.size(); x++) {
            RiftBoxMemButtonForBox button = this.boxMemButtons.get(x);
            button.setCreatureNBT(newBoxNBT.get(x));
        }
    }

    private NBTTagCompound getNBTFromSelectedPos() {
        //if theres no selected pos, just return empty nbt
        if (this.selectedPos == null) return new NBTTagCompound();

        //get nbt for party creature
        if (this.selectedPos.selectedPosType == SelectedPosType.PARTY) {
            return this.partyMemButtons.get(this.selectedPos.pos[0]).getCreatureNBT();
        }
        //get nbt for creature box creature
        else if (this.selectedPos.selectedPosType == SelectedPosType.BOX) {
            return this.boxMemButtons.get(this.selectedPos.pos[1]).getCreatureNBT();
        }
        //just return empty nbt if theres no other selectedPosType for some reason
        return new NBTTagCompound();
    }

    public enum SelectedPosType {
        PARTY,
        BOX,
        BOX_DEPLOYED
    }

    private static class SelectedPos {
        public final SelectedPosType selectedPosType;
        public final int[] pos;

        public SelectedPos(SelectedPosType selectedPosType, int[] pos) {
            this.selectedPosType = selectedPosType;
            this.pos = pos;
        }
    }
}