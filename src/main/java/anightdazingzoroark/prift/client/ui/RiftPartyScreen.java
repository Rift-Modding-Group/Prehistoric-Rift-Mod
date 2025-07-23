package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.*;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftTeleportPartyMemToPlayer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
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
    private final List<GuiButton> partyMemManageButtons = new ArrayList<>();
    private RiftClickableSection swapPartyMemsButton;
    private RiftClickableSection journalButton;
    private RiftClickableSection creatureInfoButton;
    private RiftClickableSection creatureMovesButton;
    private RiftClickableSection swapMovesButton;
    private RiftPartyMemScrollableSection infoScrollableSection;
    private RiftPartyMemMovesSection movesScrollableSection;

    private RiftCreature creatureToDraw;
    private boolean moveManagement;
    private int partyMemPos;
    protected final int xSize = 373;
    protected final int ySize = 182;

    //managing swapping party members
    private boolean swapPartyMemsMode;

    //only relevant in very specific circumstances
    private boolean openedFromManageMoves;

    public RiftPartyScreen(int pos, int openedFromManageMoves) {
        super();
        this.partyMemPos = pos;
        this.openedFromManageMoves = openedFromManageMoves >= 0;
    }

    @Override
    public void initGui() {
        super.initGui();
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

        //create swap party members button
        this.swapPartyMemsButton = new RiftClickableSection(19, 17, this.width, this.height, -71, -67, this.fontRenderer, this.mc);
        this.swapPartyMemsButton.addImage(background, 20, 18, 400, 360, 75, 203, 95, 203);
        this.swapPartyMemsButton.setSelectedUV(115, 203);
        this.swapPartyMemsButton.setScale(0.75f);

        //create party buttons
        this.partyMemButtons.clear();
        FixedSizeList<NBTTagCompound> playerPartyNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player);
        for (int x = 0; x < playerPartyNBT.size(); x++) {
            int partyMemXOffset = -154 + (x % 2) * 60;
            int partyMemYOffset = -41 + (x / 2) * 40;
            RiftPartyMemButton partyMemButton = new RiftPartyMemButton(playerPartyNBT.get(x), this.width, this.height, partyMemXOffset, partyMemYOffset, this.fontRenderer, this.mc);
            this.partyMemButtons.add(partyMemButton);
        }

        //create journal button
        String journalString = I18n.format("journal.party_button.journal");
        this.journalButton = new RiftClickableSection(54, 11, this.width, this.height, -123, 67, this.fontRenderer, this.mc);
        this.journalButton.addString(journalString, false, 0x000000, 0, 0, 0.75f);

        //init party member management buttons
        this.partyMemManageButtons.clear();
        int xButtonOffset = (this.width - 80) / 2;
        int yButtonOffset = (this.height - 20) / 2 + 25;

        //create summon/dismiss button
        String summonString = I18n.format("journal.party_button.summon");
        GuiButton summonButton = new GuiButton(0, xButtonOffset, yButtonOffset, 80, 20, summonString);
        this.partyMemManageButtons.add(summonButton);

        //create teleport button
        String teleportString = I18n.format("journal.party_button.teleport");
        GuiButton teleportButton = new GuiButton(1, xButtonOffset, yButtonOffset + 25, 80, 20, teleportString);
        this.partyMemManageButtons.add(teleportButton);

        //create change name button
        String changeNameString = I18n.format("journal.party_button.change_name");
        GuiButton changeNameButton = new GuiButton(2, xButtonOffset, yButtonOffset + 50, 80, 20, changeNameString);
        this.partyMemManageButtons.add(changeNameButton);

        //creature info button (when theres a selected creature)
        String creatureInfoButtonString = I18n.format("journal.party_member.info");
        this.creatureInfoButton = new RiftClickableSection(54, 11, this.width, this.height, 94, -85, this.fontRenderer, this.mc);
        this.creatureInfoButton.addString(creatureInfoButtonString, false, 0x000000, 0, 1, 0.75f);
        this.creatureInfoButton.setSelected(!this.moveManagement);

        //creature moves button (when theres a selected creature)
        String creatureMovesButtonString = I18n.format("journal.party_member.moves");
        this.creatureMovesButton = new RiftClickableSection(54, 11, this.width, this.height, 152, -85, this.fontRenderer, this.mc);
        this.creatureMovesButton.addString(creatureMovesButtonString, false, 0x000000, 0, 1, 0.75f);
        this.creatureMovesButton.setSelected(this.moveManagement);

        //scrollable section for creature info
        this.infoScrollableSection = new RiftPartyMemScrollableSection(this.width, this.height, this.fontRenderer, this.mc);
        if (this.partyMemPos >= 0) this.infoScrollableSection.setCreatureNBT(playerPartyNBT.get(this.partyMemPos));

        //scrollable section for moves info
        this.movesScrollableSection = new RiftPartyMemMovesSection(this.width, this.height, this.fontRenderer, this.mc);
        if (this.partyMemPos >= 0) this.movesScrollableSection.setCreatureNBT(playerPartyNBT.get(this.partyMemPos));

        //swap moves button
        this.swapMovesButton = new RiftClickableSection(19, 17, this.width, this.height, 177, -67, this.fontRenderer, this.mc);
        this.swapMovesButton.addImage(background, 20, 18, 400, 360, 75, 203, 95, 203);
        this.swapMovesButton.setSelectedUV(115, 203);
        this.swapMovesButton.setScale(0.75f);

        //by default selected button is the first one
        //PlayerTamedCreaturesHelper.setLastSelected(this.mc.player, 0);

        //set creature to draw
        if (this.partyMemPos >= 0) this.creatureToDraw = NewPlayerTamedCreaturesHelper.createCreatureFromNBT(this.mc.world, playerPartyNBT.get(this.partyMemPos));

        //if opened from manage moves, enable move management related stuff
        if (this.partyMemPos >= 0 && this.openedFromManageMoves) {
            this.moveManagement = true;
            this.creatureInfoButton.setSelected(false);
            this.creatureMovesButton.setSelected(true);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc != null && this.mc.world != null) this.drawDefaultBackground();
        else return;

        int selectedXOffset = this.hasSelectedCreature() ? 0 : 124;
        int selectedYOffset = this.hasSelectedCreature() ? -13 : 3;

        //constantly update party members as long as this screen is opened
        this.updateAllPartyMems();

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //draw party label
        String partyLabel = I18n.format("journal.party_label.party");
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

        //draw journal button
        int jButtonXOffset = this.hasSelectedCreature() ? 0 : -2;
        this.journalButton.setAdditionalOffset(selectedXOffset + jButtonXOffset, selectedYOffset);
        this.journalButton.drawSection(mouseX, mouseY);

        //everything else from here on out requires a selected creature
        if (this.hasSelectedCreature()) {
            NBTTagCompound tagCompound = this.partyMemButtons.get(this.partyMemPos).getCreatureNBT();
            RiftCreatureType creatureType = RiftCreatureType.values()[tagCompound.getByte("CreatureType")];
            String partyMemName = (tagCompound.hasKey("CustomName") && !tagCompound.getString("CustomName").isEmpty()) ? tagCompound.getString("CustomName") : creatureType.getTranslatedName();

            //draw red background for ded creature
            if (tagCompound.hasKey("Health") && tagCompound.getFloat("Health") <= 0) {
                GlStateManager.color(1f, 1f, 1f, 1f);
                this.mc.getTextureManager().bindTexture(background);
                int deadBGX = (this.width - 105) / 2;
                int deadBGY = (this.height - 66) / 2 - 31;
                drawModalRectWithCustomSizedTexture(deadBGX, deadBGY, 57, 249, 105, 66, 400f, 360f);
            }

            //draw selected creature
            GlStateManager.color(1f, 1f, 1f, 1f);
            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.translate(this.width / 2f, this.height / 2f, 210f);
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

            //draw creature name and level here
            float partyMemNameScale = 0.75f;
            String partyMemNameString = I18n.format("journal.party_member.name", partyMemName, tagCompound.getInteger("Level"));
            int partyMemNameX = (int) ((this.width - this.fontRenderer.getStringWidth(partyMemNameString) * partyMemNameScale) / (2 * partyMemNameScale));
            int partyMemNameY = (int) ((this.height - this.fontRenderer.FONT_HEIGHT * partyMemNameScale) / (2 * partyMemNameScale) + (15 * partyMemNameScale));
            GlStateManager.pushMatrix();
            GlStateManager.scale(partyMemNameScale, partyMemNameScale, partyMemNameScale);
            this.fontRenderer.renderString(partyMemNameString, partyMemNameX, partyMemNameY, 0x000000, false);
            GlStateManager.popMatrix();

            //draw creature info button
            this.creatureInfoButton.drawSection(mouseX, mouseY);

            //draw creature moves button
            this.creatureMovesButton.drawSection(mouseX, mouseY);

            //draw moves and swap moves button when move management is true
            if (this.moveManagement) {
                this.movesScrollableSection.drawSectionContents(mouseX, mouseY, partialTicks);
                this.swapMovesButton.drawSection(mouseX, mouseY);
            }
            //draw info when move management is false
            else this.infoScrollableSection.drawSectionContents(mouseX, mouseY, partialTicks);

            //draw member management buttons
            for (GuiButton partyMemButton: this.partyMemManageButtons) {
                //extra stuff for the buttons
                //for summon/dismiss
                if (partyMemButton.id == 0) {
                    partyMemButton.displayString = this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY ? I18n.format("journal.party_button.dismiss") : I18n.format("journal.party_button.summon");

                    //disable if creature is ded
                    partyMemButton.enabled = NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos)
                            && (!this.getMemberNBT().hasKey("Health") || this.getMemberNBT().getFloat("Health") > 0);
                }
                else if (partyMemButton.id == 1) {
                    //disable if creature is not summoned
                    partyMemButton.enabled = this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY && NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos);
                }
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.color(1f, 1f, 1f, 1f);
                partyMemButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
            }

            //draw text overlays for member management buttons
            for (GuiButton partyMemButton: this.partyMemManageButtons) {
                if (partyMemButton.id == 0) {
                    //draw overlay text for when the summon/dismiss button is disabled
                    if ((!NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos)
                            || (this.getMemberNBT().hasKey("Health") && this.getMemberNBT().getFloat("Health") <= 0))
                            && partyMemButton.isMouseOver()) {
                        String cannotSummonLocation = I18n.format("journal.warning.cannot_summon");
                        String cannotSummonDead = I18n.format("journal.warning.cannot_summon_dead");
                        String finalOverlayString = (this.getMemberNBT().hasKey("Health") && this.getMemberNBT().getFloat("Health") <= 0)
                                ? cannotSummonDead
                                : cannotSummonLocation;
                        this.drawHoveringText(finalOverlayString, mouseX, mouseY);
                    }
                }
                else if (partyMemButton.id == 1) {
                    if ((this.getPartyMemDeployment() != PlayerTamedCreatures.DeploymentType.PARTY
                            || !NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos))
                            && partyMemButton.isMouseOver()) {
                        String finalOverlayString = I18n.format("journal.warning.cannot_teleport");
                        this.drawHoveringText(finalOverlayString, mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        //manage party button clicking
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButton partyMemButton = this.partyMemButtons.get(x);
            //for swapping around party members
            if (this.swapPartyMemsMode) {
                if (partyMemButton.isHovered(mouseX, mouseY)) {
                    if (this.partyMemPos == -1 && !partyMemButton.getCreatureNBT().isEmpty()) {
                        this.partyMemPos = x;

                        //play sound
                        partyMemButton.playPressSound(this.mc.getSoundHandler());
                    }
                    else if (this.partyMemPos > -1) {
                        NewPlayerTamedCreaturesHelper.rearrangePartyCreatures(this.mc.player, x, this.partyMemPos);
                        this.partyMemPos = -1;

                        //play sound
                        partyMemButton.playPressSound(this.mc.getSoundHandler());
                    }
                }
            }
            //for just managing individual party members
            else {
                if (partyMemButton.isHovered(mouseX, mouseY) && !partyMemButton.getCreatureNBT().isEmpty()) {
                    //PlayerTamedCreaturesHelper.setLastSelected(this.mc.player, x);
                    if (this.partyMemPos != x) {
                        this.creatureToDraw = partyMemButton.getCreatureFromNBT();
                        this.partyMemPos = x;
                        this.infoScrollableSection.setCreatureNBT(partyMemButton.getCreatureNBT());
                        this.movesScrollableSection.setCreatureNBT(partyMemButton.getCreatureNBT());
                        this.movesScrollableSection.setSelectedMove("");
                        this.movesScrollableSection.unselectAllClickableSections();
                    }
                    else {
                        this.creatureToDraw = null;
                        this.partyMemPos = -1;
                        this.infoScrollableSection.setCreatureNBT(new NBTTagCompound());
                        this.movesScrollableSection.setCreatureNBT(new NBTTagCompound());
                    }

                    //reset back to creature info
                    this.moveManagement = false;
                    this.creatureInfoButton.setSelected(true);
                    this.creatureMovesButton.setSelected(false);

                    //play sound
                    partyMemButton.playPressSound(this.mc.getSoundHandler());
                }
            }
        }

        //manage swapping party mode
        if (this.swapPartyMemsButton.isHovered(mouseX, mouseY)) {
            //swap out of switch party mems mode
            if (this.swapPartyMemsMode) {
                this.swapPartyMemsMode = false;
                this.swapPartyMemsButton.setSelected(false);
            }
            //switch to switch party mems mode
            else {
                this.swapPartyMemsMode = true;
                this.swapPartyMemsButton.setSelected(true);

                //reset everything else
                this.creatureToDraw = null;
                this.partyMemPos = -1;
                this.infoScrollableSection.setCreatureNBT(new NBTTagCompound());
                this.movesScrollableSection.setCreatureNBT(new NBTTagCompound());
                this.moveManagement = false;
                this.creatureInfoButton.setSelected(true);
                this.creatureMovesButton.setSelected(false);
            }
            this.swapPartyMemsButton.playPressSound(this.mc.getSoundHandler());
        }

        //open the journal upon opening journal button
        if (this.journalButton.isHovered(mouseX, mouseY)) {
            //play sound
            this.journalButton.playPressSound(this.mc.getSoundHandler());
            //open ui
            this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_JOURNAL, this.mc.world, 0, 0, 0);
        }

        //open creature info
        if (this.creatureInfoButton.isHovered(mouseX, mouseY)) {
            if (this.moveManagement) this.creatureInfoButton.playPressSound(this.mc.getSoundHandler());
            this.moveManagement = false;
            this.creatureInfoButton.setSelected(true);
            this.creatureMovesButton.setSelected(false);
            this.movesScrollableSection.setSelectedMove("");
            this.movesScrollableSection.unselectAllClickableSections();
        }
        else if (this.creatureMovesButton.isHovered(mouseX, mouseY)) {
            if (!this.moveManagement) this.creatureMovesButton.playPressSound(this.mc.getSoundHandler());
            this.moveManagement = true;
            this.creatureInfoButton.setSelected(false);
            this.creatureMovesButton.setSelected(true);
        }

        //deal with manage party member buttons
        for (GuiButton button : this.partyMemManageButtons) {
            if (!button.enabled) continue;

            if (button.isMouseOver()) {
                //summon/dismiss
                if (button.id == 0) {
                    if (this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY) {
                        if (NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos)) {
                            NewPlayerTamedCreaturesHelper.deployCreatureFromParty(this.mc.player, this.partyMemPos, false);
                            button.playPressSound(this.mc.getSoundHandler());
                        }
                    }
                    else if (this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                        NewPlayerTamedCreaturesHelper.deployCreatureFromParty(this.mc.player, this.partyMemPos, true);
                        button.playPressSound(this.mc.getSoundHandler());
                    }
                }
                //teleport
                else if (button.id == 1) {
                    if (this.getPartyMemDeployment() == PlayerTamedCreatures.DeploymentType.PARTY) {
                        if (NewPlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.partyMemPos)) {
                            RiftMessages.WRAPPER.sendToServer(new RiftTeleportPartyMemToPlayer(this.mc.player, this.partyMemPos));
                            button.playPressSound(this.mc.getSoundHandler());
                        }
                    }
                }
                //change name
                else if (button.id == 2) {
                    this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MENU_FROM_PARTY, this.mc.world, this.partyMemPos, 0, 0);
                    button.playPressSound(this.mc.getSoundHandler());
                }
            }
        }

        //deal with setting selected move
        if (this.moveManagement) {
            for (RiftClickableSection moveSection : this.movesScrollableSection.getClickableSections()) {
                if (moveSection.isHovered(mouseX, mouseY) && !this.movesScrollableSection.getSelectedMoveID().equals(moveSection.getStringID())) {
                    this.movesScrollableSection.setSelectedMove(moveSection.getStringID());
                    this.movesScrollableSection.unselectAllClickableSections();
                    this.movesScrollableSection.selectClickableSectionById(moveSection.getStringID());
                    moveSection.playPressSound(this.mc.getSoundHandler());
                }
                else if (moveSection.isHovered(mouseX, mouseY) && this.movesScrollableSection.getSelectedMoveID().equals(moveSection.getStringID())) {
                    this.movesScrollableSection.setSelectedMove("");
                    this.movesScrollableSection.unselectAllClickableSections();
                    moveSection.playPressSound(this.mc.getSoundHandler());
                }
            }
        }

        //open the moves screen when clicking on swap moves button
        if (this.swapMovesButton.isHovered(mouseX, mouseY)) {
            //play sound
            this.journalButton.playPressSound(this.mc.getSoundHandler());
            //open ui
            this.mc.player.openGui(RiftInitialize.instance, RiftGui.GUI_MOVES, this.mc.world, this.partyMemPos, 0, 0);
        }
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int xScreenSize = this.hasSelectedCreature() ? 373 : 125;
        int yScreenSize = this.hasSelectedCreature() ? 182 : 151;
        int k = (this.width - xScreenSize) / 2;
        int l = (this.height - yScreenSize) / 2;
        int uvX = this.hasSelectedCreature() ? 0 : 162;
        int uvY = this.hasSelectedCreature() ? 0 : 182;
        drawModalRectWithCustomSizedTexture(k, l, uvX, uvY, xScreenSize, yScreenSize, 400f, 360f);
    }

    private boolean hasSelectedCreature() {
        return this.partyMemPos >= 0 && this.creatureToDraw != null;
    }

    private void updateAllPartyMems() {
        NewPlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);
        FixedSizeList<NBTTagCompound> newPartyNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(this.mc.player);
        for (int x = 0; x < this.partyMemButtons.size(); x++) {
            RiftPartyMemButton button = this.partyMemButtons.get(x);
            button.setCreatureNBT(newPartyNBT.get(x));
        }
    }

    private NBTTagCompound getMemberNBT() {
        if (this.partyMemPos >= 0) return this.partyMemButtons.get(this.partyMemPos).getCreatureNBT();
        return new NBTTagCompound();
    }

    private PlayerTamedCreatures.DeploymentType getPartyMemDeployment() {
        if (this.getMemberNBT().hasKey("DeploymentType")) return PlayerTamedCreatures.DeploymentType.values()[this.getMemberNBT().getByte("DeploymentType")];
        return PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE;
    }
}
