package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiJournalButton;
import anightdazingzoroark.prift.server.entity.PlayerJournalProgress;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.enums.CreatureCategory;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
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
    protected final int ySize = 225;
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

        //reset scrollbars
        this.entryScrollOffset = 0;
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
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, (float)this.xSize, (float)this.ySize);
    }

    protected void sidebarButtonListInit() {
        this.buttonList.clear();
        this.sidebarHeight = 0;

        PlayerJournalProgress progress = EntityPropertiesHandler.INSTANCE.getProperties(this.mc.player, PlayerJournalProgress.class);
        for (int x = 0; x < progress.getUnlockedCategories().size(); x++) {
            this.buttonList.add(new RiftGuiJournalButton(progress.getUnlockedCategories().get(x).toString(), x, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 87 + (25 * x), 96, 20, progress.getUnlockedCategories().get(x).getTranslatedName(true)));
            this.sidebarHeight += 25;
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
        this.buttonList.add(new RiftGuiJournalButton("NULL", 0, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 87, 96, 20, I18n.format("type.creature.back")));
        for (int x = 0; x < creatureTypeList.size(); x++) {
            this.buttonList.add(new RiftGuiJournalButton(creatureTypeList.get(x).toString(), x + 1, (this.width - 96)/2 - 147, (this.height - 20) / 2 - 87 + (25 * (x + 1)), 96, 20, creatureTypeList.get(x).getTranslatedName()));
            this.sidebarHeight += 25;
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
        int x = (this.width - 248)/2 + 60;
        int y = (this.height - 200)/2;

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
            this.mc.getTextureManager().bindTexture(this.getJournalPicLocation());
            final int imgWidth = 240;
            final int imgHeight = 180;
            System.out.println("scale factor: "+scaleFactor);
            System.out.println((int)(scaleFactor/0.75));
            int k = (int)((this.width - imgWidth) / 2 + 130 * 0.75);
            int l = (int)((this.height - imgHeight) / 2 + 10 * 0.75);

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75f, 0.75f, 0.75f);
            drawModalRectWithCustomSizedTexture((int) (k / 0.75), (int) (l / 0.75) - (int)(this.entryScrollOffset / 0.75D), 0, 0, imgWidth, imgHeight, (float)imgWidth, (float)imgHeight);
            GlStateManager.popMatrix();
        }
        //text
        int imgOffset = this.entryType != null ? 160 : 10;
        this.fontRenderer.drawSplitString(this.getJournalEntry(), (this.width - 248)/2 + 60, (this.height - 200)/2 + imgOffset - this.entryScrollOffset, 248, 0x000000);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        //get height of all content
        List<String> wrappedTextLines = this.fontRenderer.listFormattedStringToWidth(this.getJournalEntry(), 248);
        int lineHeight = this.fontRenderer.FONT_HEIGHT;
        int displayedTextHeight = wrappedTextLines.size() * lineHeight;
        this.journalEntryHeight = 45 + displayedTextHeight + imgOffset;
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

    protected void placeButtons(int mouseX, int mouseY, float partialTicks) {
        int x = (this.width - 96) / 2 - 147;
        int y = (this.height - 200) / 2;

        // for scaling
        int scaleFactor = new ScaledResolution(this.mc).getScaleFactor();

        int scissorX = x * scaleFactor;
        int scissorY = (this.height - y - 200) * scaleFactor;
        int scissorW = (this.width - 96) * scaleFactor;
        int scissorH = 200 * scaleFactor;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
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
                this.entryScrollOffset = Math.max(0, Math.min(this.entryScrollOffset, Math.max(0, this.journalEntryHeight - 200)));
            }
            if (this.isMouseOverSidebar(mouseX, mouseY)) {
                this.scrollSidebarOffset += (scroll > 0) ? -10 : 10;
                this.scrollSidebarOffset = Math.max(0, Math.min(this.scrollSidebarOffset, Math.max(0, this.sidebarHeight - 200)));
                System.out.println(Math.max(0, this.sidebarHeight - 200));
            }
        }
    }
}
