package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.PlayerJournalProgress;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.RiftEntityProperties;
import anightdazingzoroark.prift.server.enums.CreatureCategory;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class RiftJournalScreen extends GuiScreen {
    protected int xSize = 420;
    protected int ySize = 225;
    public final int xGui = 420;
    public final int yGui = 225;
    protected int guiLeft;
    protected int guiTop;
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/journal_background.png");
    private CreatureCategory sidebarType;
    private RiftCreatureType entryType;
    private int entryScrollOffset = 0;
    private int scrollSidebarOffset = 0;
    private int journalEntryHeight;
    private int sidebarHeight;

    @Override
    public void initGui() {
        //start with the creature types
        this.sidebarType = null;
        this.entryType = null;

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (mc != null && mc.world != null) this.drawDefaultBackground();
        else return;

        //draw screen
        this.drawGuiContainerBackgroundLayer();
        //draw sidebar
        this.placeButtons(mouseX, mouseY, partialTicks);
        //draw entry and image
        this.placeJournalEntry();

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
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, (float)this.xGui, (float)this.yGui);
    }

    protected void sidebarButtonListInit() {
        this.buttonList.clear();
        this.sidebarHeight = 0;


        PlayerJournalProgress progress = EntityPropertiesHandler.INSTANCE.getProperties(this.mc.player, PlayerJournalProgress.class);
        for (int x = 0; x < progress.getUnlockedCategories().size(); x++) {
            this.buttonList.add(new RiftGuiJournalButton(progress.getUnlockedCategories().get(x).toString(), x, (-60 + this.xGui)/2 - 135, 26 + 25 * x, 96, 20, progress.getUnlockedCategories().get(x).getTranslatedName(true)));
            this.sidebarHeight += 25;
        }
    }

    protected void setSidebarButtonList(CreatureCategory category) {
        this.buttonList.clear();
        this.sidebarHeight = 0;

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
        this.buttonList.add(new RiftGuiJournalButton("NULL", 0, (-60 + this.xGui)/2 - 135, 26, 96, 20, I18n.format("type.creature.back")));
        for (int x = 0; x < creatureTypeList.size(); x++) {
            this.buttonList.add(new RiftGuiJournalButton(creatureTypeList.get(x).toString(), x + 1, (-60 + this.xGui)/2 - 135, 26 + 25 * (x + 1), 96, 20, creatureTypeList.get(x).getTranslatedName()));
            this.sidebarHeight += 31;
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
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

    @Override
    public void updateScreen() {
        //update sidebar
        if (this.sidebarType != null) this.setSidebarButtonList(this.sidebarType);
        else this.sidebarButtonListInit();
        for (GuiButton button : this.buttonList) {
            button.y -= this.scrollSidebarOffset;
        }
    }

    //managing journal entry and pic starts here
    private void placeJournalEntry() {
        //scrollability
        int x = (-60 + this.xGui) / 2 - 3;
        int y = (-60 + this.xGui) / 2 - 140;
        int w = 250;
        int h = this.height - y * 2;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        int scaleFactor = scaledResolution.getScaleFactor();
        int scissorX = x * scaleFactor;
        int scissorY = (scaledResolution.getScaledHeight() - (y + h)) * scaleFactor;
        int scissorW = w * scaleFactor;
        int scissorH = h * scaleFactor;
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        //image
        if (this.entryType != null) {
            this.mc.getTextureManager().bindTexture(this.getJournalPicLocation());
            final int imgWidth = 240;
            final int imgHeight = 180;
            int k = (this.width - imgWidth) / 2 + 147;
            int l = (this.height - imgHeight) / 2 + 25;

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75f, 0.75f, 0.75f);
            drawModalRectWithCustomSizedTexture(k, l - (int)(this.entryScrollOffset / 0.75D), 0, 0, imgWidth, imgHeight, (float)imgWidth, (float)imgHeight);
            GlStateManager.popMatrix();
        }
        //text
        int imgOffset = this.entryType != null ? 160 : 0;
        this.fontRenderer.drawSplitString(this.getJournalEntry(), (-60 + this.xGui)/2 - 3, (-60 + this.xGui)/2 - 140 + imgOffset - this.entryScrollOffset, 248, 0x000000);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //get height of all content
        List<String> wrappedTextLines = this.fontRenderer.listFormattedStringToWidth(this.getJournalEntry(), 248);
        int lineHeight = this.fontRenderer.FONT_HEIGHT;
        int displayedTextHeight = wrappedTextLines.size() * lineHeight;
        this.journalEntryHeight = (int)(0.5D * 180) + displayedTextHeight + imgOffset;
    }

    private String getJournalEntry() {
        Minecraft mc = Minecraft.getMinecraft();
        String languageCode = mc.gameSettings.language;
        String entryName = this.entryType != null ? this.entryType.name().toLowerCase() : "intro";

        ResourceLocation entryLoc = this.getJournalResourceLocation(languageCode, entryName);
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
        ResourceLocation entryLoc = this.getJournalResourceLocation(fallbackLanguageCode, entryName);
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();

        try (InputStream inputStream = manager.getResource(entryLoc).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            return "Error reading journal entry: " + entryLoc;
        }
    }

    private ResourceLocation getJournalResourceLocation(String languageCode, String entryName) {
        return new ResourceLocation(RiftInitialize.MODID, "journal/" + languageCode + "/" + entryName + ".txt");
    }

    private ResourceLocation getJournalPicLocation() {
        return new ResourceLocation(RiftInitialize.MODID, "textures/journal/"+this.entryType.name().toLowerCase()+"_journal.png");
    }

    private boolean isMouseOverSidebar(int mouseX, int mouseY) {
        return mouseX >= 40 && mouseX <= 149 && mouseY >= 22 && mouseY <= 229;
    }

    private boolean isMouseOverEntry(int mouseX, int mouseY) {
        return mouseX >= 158 && mouseY >= 17 && mouseX <= 442 && mouseY <= 228;
    }

    //managing journal entry and pic ends here

    //managing sidebar content
    protected void placeButtons(int mouseX, int mouseY, float partialTicks) {
        int x = (-60 + this.xGui) / 2 - 280;
        int y = (-60 + this.xGui) / 2 - 155;
        int w = 250;
        int h = this.height - y * 2;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        int scaleFactor = scaledResolution.getScaleFactor();
        int scissorX = x * scaleFactor;
        int scissorY = (scaledResolution.getScaledHeight() - (y + h)) * scaleFactor;
        int scissorW = w * scaleFactor;
        int scissorH = h * scaleFactor;
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        for (GuiButton guiButton : this.buttonList) {
            guiButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    //manage scrollability
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
                this.entryScrollOffset = Math.max(0, Math.min(this.entryScrollOffset, Math.max(0, this.journalEntryHeight - 250)));
            }
            if (this.isMouseOverSidebar(mouseX, mouseY)) {
                this.scrollSidebarOffset += (scroll > 0) ? -10 : 10;
                this.scrollSidebarOffset = Math.max(0, Math.min(this.scrollSidebarOffset, Math.max(0, this.sidebarHeight - 250)));
            }
        }
    }
}
