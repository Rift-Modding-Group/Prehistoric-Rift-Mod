package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiJournalButton;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiJournalPartyButton;
import anightdazingzoroark.prift.server.entity.PlayerJournalProgress;
import anightdazingzoroark.prift.server.entity.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.CreatureCategory;
import anightdazingzoroark.prift.server.message.RiftChangePartyOrder;
import anightdazingzoroark.prift.server.message.RiftManagePartyMem;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftTeleportPartyMemToPlayer;
import com.google.common.collect.Lists;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class RiftJournalScreen extends GuiScreen {
    protected final int xSize = 420;
    protected final int ySize = 240;
    protected int guiLeft;
    protected int guiTop;
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/journal_background.png");
    private CreatureCategory sidebarType;
    private RiftCreatureType entryType;
    private RiftCreature selectedPartyMem;
    private int partyPosToMove = -1;
    private int entryScrollOffset = 0;
    private int scrollSidebarOffset = 0;
    private int journalEntryHeight;
    private int sidebarHeight;
    private boolean isPartyMode;
    private List<GuiButton> partyMemButtonList = Lists.<GuiButton>newArrayList();
    private boolean changePartyOrderMode;

    @Override
    public void initGui() {
        //start with the creature types
        this.sidebarType = null;
        this.entryType = null;
        this.selectedPartyMem = this.getPlayerParty().isEmpty() ? null : this.getPlayerParty().get(this.playerTamedCreatures().getLastSelected());

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        //reset scrollbars
        this.entryScrollOffset = 0;
        this.scrollSidebarOffset = 0;

        this.isPartyMode = true;
        this.changePartyOrderMode = false;

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc != null && mc.world != null) this.drawDefaultBackground();
        else return;

        //draw screen
        this.drawGuiContainerBackgroundLayer();

        //draw sidebar
        this.placeJournalButtons(mouseX, mouseY, partialTicks);

        if (this.isPartyMode) {
            this.placePartyMemberMenu(mouseX, mouseY, partialTicks);
        }
        else {
            //draw entry and image
            this.placeJournalEntry();
            this.partyMemButtonList.clear();
        }

        //top buttons
        this.placeTopButtons(mouseX, mouseY);

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
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, (float)this.xSize, (float)this.ySize);
    }

    protected void sidebarButtonListInit() {
        this.buttonList.clear();
        this.sidebarHeight = 0;

        if (this.isPartyMode) {
            //for party entries
            for (int x = 0; x < this.getPlayerParty().size(); x++) {
                RiftCreature creature = this.getPlayerParty().get(x);
                this.buttonList.add(new RiftGuiJournalPartyButton(creature, x, (this.width - 96)/2 - 147, (this.height - 32) / 2 - 75 + (40 * x)));
                this.sidebarHeight += 40;
            }
        }
        else {
            //for journal entries
            PlayerJournalProgress progress = EntityPropertiesHandler.INSTANCE.getProperties(this.mc.player, PlayerJournalProgress.class);
            for (int x = 0; x < progress.getUnlockedCategories().size(); x++) {
                this.buttonList.add(new RiftGuiJournalButton(progress.getUnlockedCategories().get(x).toString(), x, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 79 + (25 * x), 96, 20, progress.getUnlockedCategories().get(x).getTranslatedName(true)));
                this.sidebarHeight += 25;
            }
        }
    }

    protected void setSidebarButtonList(CreatureCategory category) {
        this.buttonList.clear();
        this.sidebarHeight = 25;

        PlayerJournalProgress progress = EntityPropertiesHandler.INSTANCE.getProperties(this.mc.player, PlayerJournalProgress.class);
        List<RiftCreatureType> creatureTypeList = progress.getUnlockedCreatures();
        //sort by name
        Collections.sort(creatureTypeList, new Comparator<RiftCreatureType>() {
            @Override
            public int compare(RiftCreatureType f1, RiftCreatureType f2) {
                return f1.name().compareTo(f2.name());
            }
        });
        //additional filter by category
        if (!category.equals(CreatureCategory.ALL)) {
            creatureTypeList = creatureTypeList.stream()
                    .filter(cat -> cat.getCreatureCategory() == category)
                    .collect(Collectors.toList());
        }
        //add a back button
        this.buttonList.add(new RiftGuiJournalButton("NULL", 0, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 79, 96, 20, I18n.format("type.creature.back")));
        for (int x = 0; x < creatureTypeList.size(); x++) {
            this.buttonList.add(new RiftGuiJournalButton(creatureTypeList.get(x).toString(), x + 1, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 79 + (25 * (x + 1)), 96, 20, creatureTypeList.get(x).getTranslatedName()));
            this.sidebarHeight += 25;
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button instanceof RiftGuiJournalButton) {
            RiftGuiJournalButton jButton = (RiftGuiJournalButton)button;
            if (this.sidebarType == null) {
                this.sidebarType = CreatureCategory.safeValOf(jButton.getTriggerString());
                this.scrollSidebarOffset = 0;
            }
            else {
                if (jButton.getTriggerString().equals("NULL")) {
                    this.sidebarType = null;
                    this.entryType = null;
                    this.scrollSidebarOffset = 0;
                }
                else this.entryType = RiftCreatureType.safeValOf(jButton.getTriggerString());
                this.entryScrollOffset = 0;
            }
        }
        else if (button instanceof RiftGuiJournalPartyButton) {
            RiftGuiJournalPartyButton jButton = (RiftGuiJournalPartyButton)button;
            if (this.changePartyOrderMode) {
                if (this.partyPosToMove == -1) {
                    this.partyPosToMove = jButton.id;
                }
                else {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangePartyOrder(jButton.id, this.partyPosToMove));
                    this.playerTamedCreatures().rearrangePartyCreatures(jButton.id, this.partyPosToMove);
                    this.partyPosToMove = -1;
                }
            }
            else {
                this.selectedPartyMem = this.getPlayerParty().get(jButton.id);
                this.playerTamedCreatures().setLastSelected(jButton.id);
            }
        }
        else {
            if (button.id == 0) {
                if (this.selectedPartyMem.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                    RiftMessages.WRAPPER.sendToAll(new RiftManagePartyMem(this.selectedPartyMem, false));
                    RiftMessages.WRAPPER.sendToServer(new RiftManagePartyMem(this.selectedPartyMem, false));
                    this.selectedPartyMem.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
                    this.selectedPartyMem.updatePlayerTameList();
                }
                else if (this.selectedPartyMem.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                    if (RiftUtil.testCanBeDeployed(this.selectedPartyMem, this.mc.player)) {
                        RiftMessages.WRAPPER.sendToServer(new RiftManagePartyMem(this.selectedPartyMem, true));
                        this.selectedPartyMem.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                        this.selectedPartyMem.updatePlayerTameList();
                    }
                }
            }
            else if (button.id == 1) {
                if (this.selectedPartyMem.getDeploymentType() != PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE
                    && RiftUtil.testCanBeDeployed(this.selectedPartyMem, this.mc.player)) {
                    RiftMessages.WRAPPER.sendToServer(new RiftTeleportPartyMemToPlayer(this.selectedPartyMem));
                }
            }
            else if (button.id == 2) {
                this.changePartyOrderMode = !this.changePartyOrderMode;
            }
        }
    }

    @Override
    public void updateScreen() {
        //update sidebar
        if (this.sidebarType != null && !this.isPartyMode) this.setSidebarButtonList(this.sidebarType);
        else this.sidebarButtonListInit();
        for (GuiButton button : this.buttonList) {
            button.y -= this.scrollSidebarOffset;
        }
    }

    //managing journal entry and pic starts here
    private void placeJournalEntry() {
        //scrollability
        int x = (this.width - 248)/2 + 60;
        int y = (this.height - 200)/2 + 8;

        // for scaling
        int scaleFactor = new ScaledResolution(this.mc).getScaleFactor();

        int scissorX = x * scaleFactor;
        int scissorY = (this.height - y - 200) * scaleFactor;
        int scissorW = 248 * scaleFactor;
        int scissorH = 200 * scaleFactor;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        //image
        if (this.entryType != null) {
            this.mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/journal/"+this.entryType.name().toLowerCase()+"_journal.png"));
            final int imgWidth = 240;
            final int imgHeight = 180;
            int k = (int)((this.width - imgWidth) / 2 + 130 * 0.75);
            int l = (int)((this.height - imgHeight) / 2 + 10 * 0.75);

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75f, 0.75f, 0.75f);
            drawModalRectWithCustomSizedTexture((int) (k / 0.75), (int) (l / 0.75) - (int)(this.entryScrollOffset / 0.75D), 0, 0, imgWidth, imgHeight, (float)imgWidth, (float)imgHeight);
            GlStateManager.popMatrix();
        }
        //text
        int imgOffset = this.entryType != null ? 168 : 18;
        this.fontRenderer.drawSplitString(this.getJournalEntry(), (this.width - 248)/2 + 60, (this.height - 200)/2 + imgOffset - this.entryScrollOffset, 248, 0x000000);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //get height of all content
        List<String> wrappedTextLines = this.fontRenderer.listFormattedStringToWidth(this.getJournalEntry(), 248);
        int lineHeight = this.fontRenderer.FONT_HEIGHT;
        int displayedTextHeight = wrappedTextLines.size() * lineHeight;
        this.journalEntryHeight = displayedTextHeight + imgOffset;

        //create scrollbar
        if (this.journalEntryHeight > 200) {
            int k = (this.width - 5) / 2 + 195;
            int l = (this.height - 200) / 2 + 8;
            //scrollbar background
            drawRect(k, l, k + 5, l + 200, 0xFFA0A0A0);
            //scrollbar progress
            int thumbHeight = Math.max(10, (int)((float)200 * (200f / this.journalEntryHeight)));
            int thumbPosition = (int)((float)this.entryScrollOffset / (this.journalEntryHeight - 200) * (200 - thumbHeight));
            drawRect(k, l + thumbPosition, k + 5, l + thumbHeight + thumbPosition, 0xFFC0C0C0);
        }
    }

    private String getJournalEntry() {
        Minecraft mc = Minecraft.getMinecraft();
        String languageCode = mc.gameSettings.language;
        String entryName = this.entryType != null ? this.entryType.name().toLowerCase() : "intro";

        ResourceLocation entryLoc = new ResourceLocation(RiftInitialize.MODID, "journal/" + languageCode + "/" + entryName + ".txt");
        IResourceManager manager = mc.getResourceManager();

        try (InputStream inputStream = manager.getResource(entryLoc).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            if (!languageCode.equals("en_us")) {
                return getJournalEntry("en_us", entryName);
            }
            return "Error reading journal entry: " + entryLoc;
        }
    }

    private String getJournalEntry(String fallbackLanguageCode, String entryName) {
        ResourceLocation entryLoc = new ResourceLocation(RiftInitialize.MODID, "journal/" + fallbackLanguageCode + "/" + entryName + ".txt");
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();

        try (InputStream inputStream = manager.getResource(entryLoc).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            return "Error reading journal entry: " + entryLoc;
        }
    }

    private boolean isMouseOverSidebar(int mouseX, int mouseY) {
        int minX = (this.width - 96) / 2 - 147;
        int minY = (this.height - 200) / 2;
        int maxX = (this.width - 96) / 2 - 51;
        int maxY = (this.height - 200) / 2 + 200;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    private boolean isMouseOverEntry(int mouseX, int mouseY) {
        int minX = (this.width - 248)/2 + 60;
        int maxX = (this.width - 248)/2 + 308;
        int minY = (this.height - 200) / 2;
        int maxY = (this.height - 200) / 2 + 200;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    //managing journal entry and pic ends here
    private void placePartyMemberMenu(int mouseX, int mouseY, float partialTicks) {
        if (this.selectedPartyMem != null) {
            //for party member size
            RiftCreature creatureToRender = this.selectedPartyMem;
            float scaleMultiplier = RiftUtil.getCreatureModelScale(this.selectedPartyMem);

            //render entity
            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.translate(this.width / 2f + 50f, this.height / 2f + 50, 180.0F);

            //for rotating
            int mouseXMod = RiftUtil.clamp(mouseX + (this.width / 2 + 50), 480, 592);
            GlStateManager.rotate(mouseXMod, 0.0F, 1.0F, 0.0F);

            GlStateManager.rotate(180, 1.0F, 0.0F, 0.0F);

            GlStateManager.scale(scaleMultiplier, scaleMultiplier, scaleMultiplier);
            Minecraft.getMinecraft().getRenderManager().renderEntity(creatureToRender, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();

            //for party member button management
            this.partyMemButtonList.clear();

            //for summon/dismiss button
            String summonName = this.selectedPartyMem.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY ? I18n.format("journal.party_button.dismiss") : I18n.format("journal.party_button.summon");
            GuiButton summonButton = new GuiButton(0, (this.width - 60) / 2 + 20, (this.height - 20)/2 + 90, 60, 20, summonName);
            if (this.selectedPartyMem.isIncapacitated()) summonButton.enabled = false;

            //for teleport to owner button
            GuiButton tpToOwnerButton = new GuiButton( 1, (this.width - 60) / 2 + 90, (this.height - 20)/2 + 90, 60, 20, I18n.format("journal.party_button.teleport"));
            if (this.selectedPartyMem.isIncapacitated() || this.selectedPartyMem.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) tpToOwnerButton.enabled = false;

            //for rearrange button
            String rearrangeName = this.changePartyOrderMode ? I18n.format("journal.party_button.stop_rearrange_party") : I18n.format("journal.party_button.rearrange_party");
            GuiButton rearrangeButton = new GuiButton(2, (this.width - 96)/2 - 147, (this.height - 20)/2 + 95, 96, 20, rearrangeName);

            this.partyMemButtonList.add(summonButton);
            this.partyMemButtonList.add(tpToOwnerButton);
            this.partyMemButtonList.add(rearrangeButton);

            //for rearrange instructions
            if (this.changePartyOrderMode) {
                String instructions = I18n.format("journal.warning.rearrange_instructions");
                RiftUtil.drawMultiLineString(this.fontRenderer, instructions, (this.width - 120)/2 + 60, (this.height - this.fontRenderer.FONT_HEIGHT)/2 + 70, 120, 0);
            }
        }
        else if (this.getPlayerParty().isEmpty()) {
            String noPartyMembers = I18n.format("journal.warning.no_party_members");
            this.fontRenderer.drawSplitString(noPartyMembers, (this.width - this.fontRenderer.getStringWidth(noPartyMembers))/2 + 60, (this.height - this.fontRenderer.FONT_HEIGHT)/2, 248, 0x000000);
        }
    }

    private void placeJournalButtons(int mouseX, int mouseY, float partialTicks) {
        int x = (this.width - 96) / 2 - 147;
        int y = (this.height - 200) / 2 + 8;

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
            //change to select highlight when changin party positions
            if (guiButton instanceof RiftGuiJournalPartyButton) {
                RiftGuiJournalPartyButton jButton = (RiftGuiJournalPartyButton) guiButton;
                if (this.partyPosToMove != -1 && jButton.id == this.partyPosToMove) {
                    jButton.toMove = true;
                }
            }

            //draw button
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //for buttons related to party members
        for (GuiButton guiButton : this.partyMemButtonList) {
            //can draw
            boolean canDraw = true;
            if (guiButton.id == 2 && this.getPlayerParty().size() < 2) canDraw = false;
            else if (guiButton.id != 2 && this.changePartyOrderMode) canDraw = false;

            //draw the button
            if (canDraw) guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);

            //for drawing cannot summon alert when summon button is hovered
            if (guiButton.id == 0
                    && guiButton.isMouseOver()
                    && guiButton.enabled
                    && !RiftUtil.testCanBeDeployed(this.selectedPartyMem, this.mc.player)
                    && this.selectedPartyMem.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                String cannotSummonMessage = I18n.format("journal.warning.cannot_summon");
                RiftUtil.drawCenteredString(this.fontRenderer, cannotSummonMessage, this.width, this.height, 60, -70, this.mouseOnTopButtonEntries(mouseX, mouseY) ? 16776960 : 0);
            }
            //for drawing cannot teleport alert when telport button is hovered
            else if (guiButton.id == 1
                    && guiButton.isMouseOver()
                    && guiButton.enabled
                    && !RiftUtil.testCanBeDeployed(this.selectedPartyMem, this.mc.player)
                    && this.selectedPartyMem.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                String cannotTeleportMessage = I18n.format("journal.warning.cannot_teleport");
                RiftUtil.drawCenteredString(this.fontRenderer, cannotTeleportMessage, this.width, this.height, 60, -70, this.mouseOnTopButtonEntries(mouseX, mouseY) ? 16776960 : 0);
            }
        }

        //create scrollbar
        if (this.sidebarHeight > 200) {
            int k = (this.width - 1) / 2 - 97;
            int l = (this.height - 200) / 2 + 8;
            //scrollbar background
            drawRect(k, l, k + 1, l + 200, 0xFFA0A0A0);
            //scrollbar progress
            int thumbHeight = Math.max(10, (int)((float)200 * (200f / this.sidebarHeight)));
            int thumbPosition = (int)((float)this.scrollSidebarOffset / (this.sidebarHeight - 200) * (200 - thumbHeight));
            drawRect(k, l + thumbPosition, k + 1, l + thumbHeight + thumbPosition, 0xFFC0C0C0);
        }
    }

    //for top buttons
    //they aren't really buttons they gonna be more like clickable text that does stuff when u click on em
    protected void placeTopButtons(int mouseX, int mouseY) {
        RiftUtil.drawCenteredString(this.fontRenderer, I18n.format("journal.top_button.entries"), this.width, this.height, -32, 111, this.mouseOnTopButtonEntries(mouseX, mouseY) ? 16776960 : 0);
        RiftUtil.drawCenteredString(this.fontRenderer, I18n.format("journal.top_button.party"), this.width, this.height, -148, 111, this.mouseOnTopButtonParty(mouseX, mouseY) ? 16776960 : 0);
    }

    private boolean mouseOnTopButtonEntries(int mouseX, int mouseY) {
        int minX = (this.width - 102)/2 - 32;
        int maxX = (this.width - 102)/2 + 70;
        int minY = (this.height - 13) / 2 - 112;
        int maxY = (this.height - 13) / 2 - 99;

        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY && this.isPartyMode && !this.changePartyOrderMode;
    }

    private boolean mouseOnTopButtonParty(int mouseX, int mouseY) {
        int minX = (this.width - 102)/2 - 148;
        int maxX = (this.width - 102)/2 - 46;
        int minY = (this.height - 13) / 2 - 112;
        int maxY = (this.height - 13) / 2 - 99;
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY && !this.isPartyMode;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int scroll = Mouse.getEventDWheel();
        //edit scroll offsets
        if (scroll != 0) {
            if (this.isMouseOverEntry(mouseX, mouseY)) {
                this.entryScrollOffset += (scroll > 0) ? -10 : 10;
                this.entryScrollOffset = Math.max(0, Math.min(this.entryScrollOffset, Math.max(0, this.journalEntryHeight - 200)));
            }
            if (this.isMouseOverSidebar(mouseX, mouseY)) {
                this.scrollSidebarOffset += (scroll > 0) ? -10 : 10;
                this.scrollSidebarOffset = Math.max(0, Math.min(this.scrollSidebarOffset, Math.max(0, this.sidebarHeight - 200)));
            }
        }

        //on clicking mouse on top stuff
        if (Mouse.isButtonDown(0) && this.mouseOnTopButtonEntries(mouseX, mouseY)) { //switch to journal entries
            this.isPartyMode = false;
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        else if (Mouse.isButtonDown(0) && this.mouseOnTopButtonParty(mouseX, mouseY)) { //switch to party
            this.isPartyMode = true;
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    //on clicking on the buttons in the party part of the journal
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            for (int i = 0; i < this.partyMemButtonList.size(); ++i) {
                GuiButton guibutton = this.partyMemButtonList.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.partyMemButtonList);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                        break;
                    guibutton = event.getButton();
                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.partyMemButtonList));
                }
            }
        }
    }

    private PlayerTamedCreatures playerTamedCreatures() {
        return EntityPropertiesHandler.INSTANCE.getProperties(this.mc.player, PlayerTamedCreatures.class);
    }

    private List<RiftCreature> getPlayerParty() {
        return this.playerTamedCreatures().getPartyCreatures(this.mc.world);
    }
}
