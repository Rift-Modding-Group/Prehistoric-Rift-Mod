package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiJournalButton;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiJournalPartyButton;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.config.RiftCreatureConfig;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressProvider;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.ILeadWorkstationUser;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.CreatureCategory;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftTeleportPartyMemToPlayer;
import com.google.common.collect.Lists;
import mezz.jei.api.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class RiftJournalScreen extends GuiScreen {
    protected final int xSize = 420;
    protected final int ySize = 240;
    protected int guiLeft;
    protected int guiTop;
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/journal_background.png");
    private Map<String, List<Integer>> shownTamingFoodsList = new HashMap<>();
    private Map<String, List<Integer>> shownFavoriteFoodsList = new HashMap<>();
    private Map<String, List<Integer>> shownWorkstationsList = new HashMap<>();
    private CreatureCategory sidebarType;
    private RiftCreatureType entryType;
    private int selectedPartyPos = -1;
    private int partyPosToMove = -1;
    private int entryScrollOffset = 0;
    private int scrollSidebarOffset = 0;
    private int journalEntryHeight;
    private int sidebarHeight;
    private boolean isPartyMode;
    private List<GuiButton> partyMemButtonList = Lists.<GuiButton>newArrayList();
    private boolean changePartyOrderMode;
    private int guiTimePassed = 0;

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
    }

    @Override
    public void initGui() {
        super.initGui();

        //for set last open time
        PlayerTamedCreaturesHelper.setPartyLastOpenedTime(this.mc.player, (int) this.mc.world.getTotalWorldTime());

        //start with the creature types
        this.sidebarType = null;
        this.entryType = null;
        this.selectedPartyPos = PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).isEmpty() ? -1 : PlayerTamedCreaturesHelper.getLastSelected(this.mc.player) < PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).size() ? PlayerTamedCreaturesHelper.getLastSelected(this.mc.player) : PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).size() - 1;

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        //reset scrollbars
        this.entryScrollOffset = 0;
        this.scrollSidebarOffset = 0;

        this.isPartyMode = true;
        this.changePartyOrderMode = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc != null && mc.world != null) this.drawDefaultBackground();
        else return;

        //update creatures as long as this page is open
        PlayerTamedCreaturesHelper.updateAllPartyMems(this.mc.player);

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
            this.placeOtherItemData(mouseX, mouseY);
            this.partyMemButtonList.clear();
        }

        //top buttons
        this.placeTopButtons(mouseX, mouseY);

        if (this.guiTimePassed++ % 2 == 0) {
            PlayerTamedCreaturesHelper.reenergizePartyUndeployedCreatures(this.mc.player, this.guiTimePassed / 2);
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
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, 560F, 240F);
    }

    protected void sidebarButtonListInit() {
        this.buttonList.clear();
        this.sidebarHeight = 0;

        if (this.isPartyMode) {
            //for party entries
            for (int x = 0; x < PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).size(); x++) {
                RiftCreature creature = PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).get(x);
                this.buttonList.add(new RiftGuiJournalPartyButton(creature, x, (this.width - 96)/2 - 147, (this.height - 32) / 2 - 70 + (40 * x)));
                this.sidebarHeight += 40;
            }
        }
        else {
            //for journal entries
            for (int x = 0; x < this.playerJournalProgress().getUnlockedCategories().size(); x++) {
                this.buttonList.add(new RiftGuiJournalButton(this.playerJournalProgress().getUnlockedCategories().get(x).toString(), x, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 79 + (25 * x), 96, 20, this.playerJournalProgress().getUnlockedCategories().get(x).getTranslatedName(true)));
                this.sidebarHeight += 25;
            }
        }
    }

    protected void setSidebarButtonList(CreatureCategory category) {
        this.buttonList.clear();
        this.sidebarHeight = 25;

        List<RiftCreatureType> creatureTypeList = this.playerJournalProgress().getEncounteredCreatures().keySet().stream().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
        //additional filter by category
        if (!category.equals(CreatureCategory.ALL)) {
            creatureTypeList = creatureTypeList.stream()
                    .filter(cat -> cat.getCreatureCategory() == category)
                    .collect(Collectors.toList());
        }
        //add a back button
        this.buttonList.add(new RiftGuiJournalButton("NULL", 0, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 79, 96, 20, I18n.format("type.creature.back")));
        for (int x = 0; x < creatureTypeList.size(); x++) {
            String name = this.playerJournalProgress().getEncounteredCreatures().get(creatureTypeList.get(x)) ? creatureTypeList.get(x).getTranslatedName() : ("("+creatureTypeList.get(x).getTranslatedName()+")");
            this.buttonList.add(new RiftGuiJournalButton(creatureTypeList.get(x).toString(), x + 1, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 79 + (25 * (x + 1)), 96, 20, name));
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
                    PlayerTamedCreaturesHelper.rearrangePartyCreatures(this.mc.player, jButton.id, this.partyPosToMove);
                    this.partyPosToMove = -1;
                }
            }
            else {
                this.selectedPartyPos = jButton.id;
                PlayerTamedCreaturesHelper.setLastSelected(this.mc.player, jButton.id);
            }
        }
        else {
            if (button.id == 0) {
                if (this.getSelectedCreature().getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                    PlayerTamedCreaturesHelper.deployCreatureFromParty(this.mc.player, this.selectedPartyPos, false);
                    this.getSelectedCreature().setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
                }
                else if (this.getSelectedCreature().getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                    if (PlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.selectedPartyPos)) {
                        PlayerTamedCreaturesHelper.deployCreatureFromParty(this.mc.player, this.selectedPartyPos, true);
                        this.getSelectedCreature().setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                    }
                }
            }
            else if (button.id == 1) {
                if (this.getSelectedCreature().getDeploymentType() != PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE
                    && PlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.selectedPartyPos)) {
                    RiftMessages.WRAPPER.sendToServer(new RiftTeleportPartyMemToPlayer(this.getSelectedCreature()));
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

        int imgOffset = 0;
        String journalString = "";
        if (this.entryType != null) {
            //image
            this.mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/journal/"+this.entryType.name().toLowerCase()+"_journal.png"));
            final int imgWidth = 240;
            final int imgHeight = 180;
            int k = (int)((this.width - imgWidth) / 2 + 130 * 0.75);
            int l = (int)((this.height - imgHeight) / 2 + 10 * 0.75);

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75f, 0.75f, 0.75f);
            drawModalRectWithCustomSizedTexture((int) (k / 0.75), (int) (l / 0.75) - (int)(this.entryScrollOffset / 0.75D), 0, 0, imgWidth, imgHeight, (float)imgWidth, (float)imgHeight);
            GlStateManager.popMatrix();

            int textOffset = 0;
            int tamingFoodsHeight = 0;
            int favoriteFoodsHeight = 0;

            //for creatures that can only be tamed by killing and hoping they drop their egg
            //add a warning here about it
            if (this.entryType.isTameable() && !this.entryType.isTameableByFeeding()) {
                imgOffset += 24;
                String labelString = I18n.format("journal.must_kill_for_egg");
                this.fontRenderer.drawSplitString(labelString, (this.width - 248) / 2 + 60, (this.height - this.fontRenderer.FONT_HEIGHT) / 2 + imgOffset - this.entryScrollOffset + 40, 248,0xff0000);
            }

            //taming foods
            if (RiftConfigHandler.getConfig(this.entryType).general.favoriteMeals != null && !RiftConfigHandler.getConfig(this.entryType).general.favoriteMeals.isEmpty()) {
                imgOffset += 24;
                textOffset += 16 * (RiftConfigHandler.getConfig(this.entryType).general.favoriteMeals.size() / 6);
                tamingFoodsHeight = 16 * (RiftConfigHandler.getConfig(this.entryType).general.favoriteMeals.size() / 6);
                String tamingFoodsLabel = this.entryType.isTameableByFeeding() ?  I18n.format("journal.taming_or_breeding_foods") : I18n.format("journal.breeding_foods");
                int tamingFoodsLabelOffset = this.entryType.isTameableByFeeding() ? -5 : -24;
                Map<String, List<Integer>> tempItemList = new HashMap<>();
                this.fontRenderer.drawString(tamingFoodsLabel, (this.width - this.fontRenderer.getStringWidth(tamingFoodsLabel)) / 2 + tamingFoodsLabelOffset, (this.height - this.fontRenderer.FONT_HEIGHT) / 2 + imgOffset - this.entryScrollOffset + 41, 0x000000);
                for (int i = 0; i < RiftConfigHandler.getConfig(this.entryType).general.favoriteMeals.size(); i++) {
                    RiftCreatureConfig.Meal meal = RiftConfigHandler.getConfig(this.entryType).general.favoriteMeals.get(i);
                    List<Integer> pos = new ArrayList<>();
                    pos.add((this.width - 16) / 2 + ((i % 6) * 20) - 56 + this.fontRenderer.getStringWidth(tamingFoodsLabel) + 4);
                    pos.add((this.height - 16) / 2 + ((i / 6) * 20) + 40 + imgOffset - this.entryScrollOffset);
                    tempItemList.put(meal.itemId, pos);
                    this.renderItem(meal.itemId, pos.get(0), pos.get(1));
                }
                this.shownTamingFoodsList = tempItemList;
            }

            //healing foods
            if (this.playerJournalProgress().getEncounteredCreatures().get(this.entryType) && RiftConfigHandler.getConfig(this.entryType).general.favoriteFood != null && !RiftConfigHandler.getConfig(this.entryType).general.favoriteFood.isEmpty()) {
                imgOffset += 16;
                textOffset += 16 * (RiftConfigHandler.getConfig(this.entryType).general.favoriteFood.size() / 8);
                favoriteFoodsHeight = 16 * (RiftConfigHandler.getConfig(this.entryType).general.favoriteFood.size() / 8);
                String favoriteFoodsLabel = I18n.format("journal.favorite_foods");
                Map<String, List<Integer>> tempItemList = new HashMap<>();
                this.fontRenderer.drawString(favoriteFoodsLabel, (this.width - this.fontRenderer.getStringWidth(favoriteFoodsLabel)) / 2 - 25, (this.height - this.fontRenderer.FONT_HEIGHT) / 2 + imgOffset - this.entryScrollOffset + 41, 0x000000);
                for (int i = 0; i < RiftConfigHandler.getConfig(this.entryType).general.favoriteFood.size(); i++) {
                    RiftCreatureConfig.Food food = RiftConfigHandler.getConfig(this.entryType).general.favoriteFood.get(i);
                    List<Integer> pos = new ArrayList<>();
                    pos.add((this.width - 16) / 2 + ((i % 8) * 20) - 56 + this.fontRenderer.getStringWidth(favoriteFoodsLabel) + 4);
                    pos.add((this.height - 16) / 2 + ((i / 8) * 20) + 40 + imgOffset - this.entryScrollOffset);
                    tempItemList.put(food.itemId, pos);
                    this.renderItem(food.itemId, pos.get(0), pos.get(1));
                }
                this.shownFavoriteFoodsList = tempItemList;
            }

            //usable workstations
            if (this.playerJournalProgress().getEncounteredCreatures().get(this.entryType) && (this.entryType.invokeClass(this.mc.player.world) instanceof IWorkstationUser || this.entryType.invokeClass(this.mc.player.world) instanceof ILeadWorkstationUser)) {
                IWorkstationUser workstationUser = this.entryType.invokeClass(this.mc.player.world) instanceof IWorkstationUser ? (IWorkstationUser) this.entryType.invokeClass(this.mc.player.world) : null;
                ILeadWorkstationUser leadWorkstationUser = this.entryType.invokeClass(this.mc.player.world) instanceof ILeadWorkstationUser ? (ILeadWorkstationUser) this.entryType.invokeClass(this.mc.player.world) : null;
                imgOffset += 16;
                int numWorkstationsSize = (workstationUser != null ? RiftUtil.getKeysByValue(workstationUser.getWorkstations(), true).size() : 0)
                        + (leadWorkstationUser != null ? 1 : 0);
                textOffset += 16 * (numWorkstationsSize / 8);
                String workstationsLabel = I18n.format("journal.workstations");
                Map<String, List<Integer>> tempWorkstationsList = new HashMap<>();
                this.fontRenderer.drawString(workstationsLabel, this.width / 2 - 64, (this.height - this.fontRenderer.FONT_HEIGHT) / 2 + imgOffset - this.entryScrollOffset + favoriteFoodsHeight + 41, 0x000000);
                if (workstationUser != null) {
                    for (int i = 0; i < RiftUtil.getKeysByValue(workstationUser.getWorkstations(), true).size(); i++) {
                        String workstationString = RiftUtil.getKeysByValue(workstationUser.getWorkstations(), true).get(i);
                        List<Integer> pos = new ArrayList<>();
                        pos.add((this.width - 16) / 2 + ((i % 8) * 20) - 56 + this.fontRenderer.getStringWidth(workstationsLabel) + 4);
                        pos.add((this.height - 16) / 2 + ((i / 8) * 20) + 40 + imgOffset - this.entryScrollOffset + favoriteFoodsHeight);
                        tempWorkstationsList.put(workstationString, pos);
                        this.renderItem(workstationString, pos.get(0), pos.get(1));
                    }
                }
                if (leadWorkstationUser != null) {
                    //since the only item that can be utilized by lead workstation users is the lead powered crank
                    //it will be used here lol
                    String workstationString = "prift:lead_powered_crank";
                    List<Integer> pos = new ArrayList<>();
                    int i = workstationUser != null ? RiftUtil.getKeysByValue(workstationUser.getWorkstations(), true).size() : 0;
                    pos.add((this.width - 16) / 2 + ((i % 8) * 20) - 56 + this.fontRenderer.getStringWidth(workstationsLabel) + 4);
                    pos.add((this.height - 16) / 2 + ((i / 8) * 20) + 40 + imgOffset - this.entryScrollOffset + favoriteFoodsHeight);
                    tempWorkstationsList.put(workstationString, pos);
                    this.renderItem(workstationString, pos.get(0), pos.get(1));
                }
                this.shownWorkstationsList = tempWorkstationsList;
            }

            //text
            if (this.playerJournalProgress().getEncounteredCreatures().get(this.entryType)) {
                imgOffset += this.entryType != null ? 160 : 18;
                journalString = this.getJournalEntry();
                this.fontRenderer.drawSplitString(journalString, (this.width - 248)/2 + 60, (this.height - 200)/2 + imgOffset - this.entryScrollOffset + textOffset, 248, 0x000000);
            }
        }
        else {
            this.shownTamingFoodsList = new HashMap<>();
            this.shownFavoriteFoodsList = new HashMap<>();
            this.shownWorkstationsList = new HashMap<>();
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //get height of all content
        List<String> wrappedTextLines = this.fontRenderer.listFormattedStringToWidth(journalString, 248);
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

    private void renderItem(String itemId, int x, int y) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        ItemStack itemStack = RiftUtil.getItemStackFromString(itemId);
        renderItem.renderItemAndEffectIntoGUI(itemStack, x, y);
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
        int minY = (this.height - this.sidebarHeight()) / 2;
        int maxX = (this.width - 96) / 2 - 51;
        int maxY = (this.height - this.sidebarHeight()) / 2 + this.sidebarHeight();
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
    private void placeOtherItemData(int mouseX, int mouseY) {
        for (Map.Entry<String, List<Integer>> entry : this.shownTamingFoodsList.entrySet()) {
            if (mouseX >= entry.getValue().get(0) && mouseX <= entry.getValue().get(0) + 16
            && mouseY >= entry.getValue().get(1) && mouseY <= entry.getValue().get(1) + 16) {
                ItemStack itemStack = RiftUtil.getItemStackFromString(entry.getKey());
                List<String> strings = new ArrayList<>();
                strings.add(itemStack.getDisplayName());
                if (Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) strings.add(I18n.format("journal.open_in_jei"));
                this.drawHoveringText(strings, mouseX, mouseY);
            }
        }
        for (Map.Entry<String, List<Integer>> entry : this.shownFavoriteFoodsList.entrySet()) {
            if (mouseX >= entry.getValue().get(0) && mouseX <= entry.getValue().get(0) + 16
                    && mouseY >= entry.getValue().get(1) && mouseY <= entry.getValue().get(1) + 16) {
                ItemStack itemStack = RiftUtil.getItemStackFromString(entry.getKey());
                List<String> strings = new ArrayList<>();
                strings.add(itemStack.getDisplayName());
                if (Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) strings.add(I18n.format("journal.open_in_jei"));
                this.drawHoveringText(strings, mouseX, mouseY);
            }
        }
        for (Map.Entry<String, List<Integer>> entry : this.shownWorkstationsList.entrySet()) {
            if (mouseX >= entry.getValue().get(0) && mouseX <= entry.getValue().get(0) + 16
                    && mouseY >= entry.getValue().get(1) && mouseY <= entry.getValue().get(1) + 16) {
                ItemStack itemStack = RiftUtil.getItemStackFromString(entry.getKey());
                List<String> strings = new ArrayList<>();
                strings.add(itemStack.getDisplayName());
                if (Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) strings.add(I18n.format("journal.open_in_jei"));
                this.drawHoveringText(strings, mouseX, mouseY);
            }
        }
    }

    private void placePartyMemberMenu(int mouseX, int mouseY, float partialTicks) {
        if (this.getSelectedCreature() != null) {
            //for party member size
            RiftCreature creatureToRender = this.getSelectedCreature();
            float scaleMultiplier = RiftUtil.getCreatureModelScale(creatureToRender);

            //make sure its not rotated and red when dead
            if (creatureToRender.getHealth() / creatureToRender.getMaxHealth() <= 0) {
                creatureToRender.deathTime = 0;
                creatureToRender.isDead = false;
                creatureToRender.hurtTime = 0;
            }

            //render entity
            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.translate(this.width / 2f + 50f, this.height / 2f + 50, 180.0F);

            //for rotating
            int mouseXMod = creatureToRender.getHealth() / creatureToRender.getMaxHealth() <= 0 ? 592 : RiftUtil.clamp(mouseX + (this.width / 2 + 50), 480, 592);
            GlStateManager.rotate(mouseXMod, 0.0F, 1.0F, 0.0F);

            GlStateManager.rotate(180, 1.0F, 0.0F, 0.0F);

            GlStateManager.scale(scaleMultiplier, scaleMultiplier, scaleMultiplier);
            Minecraft.getMinecraft().getRenderManager().renderEntity(creatureToRender, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();

            //add pacifier icon for babies
            if (this.getSelectedCreature().isBaby()) {
                this.mc.getTextureManager().bindTexture(background);
                drawModalRectWithCustomSizedTexture((this.width - 22) / 2 - 60, (this.height - 22) / 2 + 85, 420, 218, 22, 22, 560, 240);
            }


            //add cross over when creature is dead
            if (this.getSelectedCreature().getHealth() / this.getSelectedCreature().getMaxHealth() <= 0) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(background);
                drawModalRectWithCustomSizedTexture((this.width - 140) / 2 + 50, (this.height - 136) / 2, 420, 0, 140, 136, 560, 240);
            }

            //for party member button management
            this.partyMemButtonList.clear();

            //for summon/dismiss button
            String summonName = this.getSelectedCreature().getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY ? I18n.format("journal.party_button.dismiss") : I18n.format("journal.party_button.summon");
            GuiButton summonButton = new GuiButton(0, (this.width - 60) / 2 + 20, (this.height - 20)/2 + 90, 60, 20, summonName);
            if (!this.getSelectedCreature().isEntityAlive()) summonButton.enabled = false;

            //for teleport to owner button
            GuiButton tpToOwnerButton = new GuiButton( 1, (this.width - 60) / 2 + 90, (this.height - 20)/2 + 90, 60, 20, I18n.format("journal.party_button.teleport"));
            if (!this.getSelectedCreature().isEntityAlive() || this.getSelectedCreature().getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) tpToOwnerButton.enabled = false;

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
        else if (PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).isEmpty()) {
            String noPartyMembers = I18n.format("journal.warning.no_party_members");
            this.fontRenderer.drawSplitString(noPartyMembers, (this.width - this.fontRenderer.getStringWidth(noPartyMembers))/2 + 60, (this.height - this.fontRenderer.FONT_HEIGHT)/2, 248, 0x000000);
        }
    }

    private void placeJournalButtons(int mouseX, int mouseY, float partialTicks) {
        int x = (this.width - 96) / 2 - 147;
        int y = (this.height - this.sidebarHeight()) / 2 + (this.isPartyMode ? -4 : 8); //if party, -4, else, 8

        // for scaling
        int scaleFactor = new ScaledResolution(this.mc).getScaleFactor();

        int scissorX = x * scaleFactor;
        int scissorY = (this.height - y - this.sidebarHeight()) * scaleFactor;
        int scissorW = (this.width - 96) * scaleFactor;
        int scissorH = this.sidebarHeight() * scaleFactor;

        //for buttons on the side
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        for (GuiButton guiButton : this.buttonList) {
            //change to select highlight when changin party positions
            if (guiButton instanceof RiftGuiJournalPartyButton) {
                RiftGuiJournalPartyButton jButton = (RiftGuiJournalPartyButton) guiButton;
                if (this.partyPosToMove != -1 && jButton.id == this.partyPosToMove) jButton.toMove = true;
                jButton.isSelected = jButton.id == this.selectedPartyPos;
            }

            //draw button
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //for buttons related to party members
        for (GuiButton guiButton : this.partyMemButtonList) {
            //can draw
            boolean canDraw = true;
            if (guiButton.id == 2 && PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).size() < 2) canDraw = false;
            else if (guiButton.id != 2 && this.changePartyOrderMode) canDraw = false;

            //draw the button
            if (canDraw) guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);

            //for drawing cannot summon alert when summon button is hovered
            if (guiButton.id == 0
                    && guiButton.isMouseOver()
                    && guiButton.enabled
                    && !PlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.selectedPartyPos)
                    && this.getSelectedCreature().getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                String cannotSummonMessage = I18n.format("journal.warning.cannot_summon");
                RiftUtil.drawCenteredString(this.fontRenderer, cannotSummonMessage, this.width, this.height, 60, -70, this.mouseOnTopButtonEntries(mouseX, mouseY) ? 16776960 : 0);
            }
            //for drawing cannot teleport alert when telport button is hovered
            else if (guiButton.id == 1
                    && guiButton.isMouseOver()
                    && guiButton.enabled
                    && !PlayerTamedCreaturesHelper.canBeDeployed(this.mc.player, this.selectedPartyPos)
                    && this.getSelectedCreature().getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                String cannotTeleportMessage = I18n.format("journal.warning.cannot_teleport");
                RiftUtil.drawCenteredString(this.fontRenderer, cannotTeleportMessage, this.width, this.height, 60, -70, this.mouseOnTopButtonEntries(mouseX, mouseY) ? 16776960 : 0);
            }
        }

        //create scrollbar
        if (this.sidebarHeight > this.sidebarHeight()) {
            int k = (this.width - 1) / 2 - 97;
            int l = (this.height - this.sidebarHeight()) / 2 + (this.isPartyMode ? -4 : 8);
            //scrollbar background
            drawRect(k, l, k + 1, l + this.sidebarHeight(), 0xFFA0A0A0);
            //scrollbar progress
            int thumbHeight = Math.max(10, (int)((float)this.sidebarHeight() * ((float) this.sidebarHeight() / this.sidebarHeight)));
            int thumbPosition = (int)((float)this.scrollSidebarOffset / (this.sidebarHeight - this.sidebarHeight()) * (this.sidebarHeight() - thumbHeight));
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
                this.scrollSidebarOffset = Math.max(0, Math.min(this.scrollSidebarOffset, Math.max(0, this.sidebarHeight - this.sidebarHeight())));
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

        //on clicking on shown items, open JEI recipes if JEI is available
        if (Loader.isModLoaded(RiftInitialize.JEI_MOD_ID)) {
            if (!this.shownTamingFoodsList.isEmpty()) {
                for (Map.Entry<String, List<Integer>> entry : this.shownTamingFoodsList.entrySet()) {
                    if (mouseX >= entry.getValue().get(0) && mouseX <= entry.getValue().get(0) + 16
                            && mouseY >= entry.getValue().get(1) && mouseY <= entry.getValue().get(1) + 16
                            && Mouse.isButtonDown(0)) {
                        ItemStack itemStack = RiftUtil.getItemStackFromString(entry.getKey());
                        RiftJEI.showRecipesForItemStack(itemStack, false);
                        break;
                    }
                }
            }
            if (!this.shownFavoriteFoodsList.isEmpty()) {
                for (Map.Entry<String, List<Integer>> entry : this.shownFavoriteFoodsList.entrySet()) {
                    if (mouseX >= entry.getValue().get(0) && mouseX <= entry.getValue().get(0) + 16
                            && mouseY >= entry.getValue().get(1) && mouseY <= entry.getValue().get(1) + 16
                            && Mouse.isButtonDown(0)) {
                        ItemStack itemStack = RiftUtil.getItemStackFromString(entry.getKey());
                        RiftJEI.showRecipesForItemStack(itemStack, false);
                        break;
                    }
                }
            }
            if (!this.shownWorkstationsList.isEmpty()) {
                for (Map.Entry<String, List<Integer>> entry : this.shownWorkstationsList.entrySet()) {
                    if (mouseX >= entry.getValue().get(0) && mouseX <= entry.getValue().get(0) + 16
                            && mouseY >= entry.getValue().get(1) && mouseY <= entry.getValue().get(1) + 16
                            && Mouse.isButtonDown(0)) {
                        ItemStack itemStack = RiftUtil.getItemStackFromString(entry.getKey());
                        RiftJEI.showRecipesForItemStack(itemStack, false);
                        break;
                    }
                }
            }
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

    private int sidebarHeight() {
        return this.isPartyMode ? 170 : 200;
    }

    private RiftCreature getSelectedCreature() {
        if (this.selectedPartyPos < 0 || this.selectedPartyPos >= PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).size()) return null;
        return PlayerTamedCreaturesHelper.getPlayerParty(this.mc.player).get(this.selectedPartyPos);
    }

    private IPlayerJournalProgress playerJournalProgress() {
        return this.mc.player.getCapability(PlayerJournalProgressProvider.PLAYER_JOURNAL_PROGRESS_CAPABILITY, null);
    }
}
