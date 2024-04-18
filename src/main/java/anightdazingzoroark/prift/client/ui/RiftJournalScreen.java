package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.enums.CreatureCategory;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
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
    private boolean isScrolling = false;
    private int scrollBarWidth = 6;
    private int scrollBarHeight;
    private int scrollBarX;
    private int scrollBarY;
    private int scrollSidebarOffset = 0;
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/journal_background.png");
    private int sidebarHeight = 203;
    private CreatureCategory sidebarType;

    @Override
    public void initGui() {
        //start with the creature types
        this.sidebarType = null;

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
        //draw sidebar scrollbar
//        this.scrollBarHeight = (this.height * this.height) / this.sidebarHeight;
//        this.scrollBarX = this.width - this.scrollBarWidth;
//        this.scrollBarY = this.scrollSidebarOffset * (this.height - this.scrollBarHeight) / (this.sidebarHeight - this.height);
//        drawRect(this.scrollBarX, this.scrollBarY, this.scrollBarX + this.scrollBarWidth, this.scrollBarY + this.scrollBarHeight, 0xFFFFFFFF);

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
    }

    protected void drawGuiContainerBackgroundLayer() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        drawModalRectWithCustomSizedTexture(k, l, 0, 0, this.xSize, this.ySize, (float)this.xGui, (float)this.yGui);
    }

    protected void sidebarButtonListInit() {
        this.buttonList.clear();
        for (int x = 0; x < CreatureCategory.values().length; x++) {
            this.buttonList.add(new RiftGuiJournalButton(CreatureCategory.values()[x].toString(), x, (-60 + this.xGui)/2 - 135, 26 + 25 * x, 96, 20, CreatureCategory.values()[x].getTranslatedName()));
        }
    }

    protected void setSidebarButtonList(CreatureCategory category) {
        this.buttonList.clear();
        List<RiftCreatureType> creatureTypeList = Arrays.asList(RiftCreatureType.values());
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
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        RiftGuiJournalButton jButton = (RiftGuiJournalButton)button;
        if (this.sidebarType == null) {
            this.sidebarType = CreatureCategory.safeValOf(jButton.getTriggerString());
        }
        else {
            if (jButton.getTriggerString().equals("NULL")) this.sidebarType = null;
        }
    }

    @Override
    public void updateScreen() {
        //update sidebar
        if (this.sidebarType != null) this.setSidebarButtonList(this.sidebarType);
        else this.sidebarButtonListInit();
    }

    //managing scrollable sidebar content starts here
    protected void placeButtons(int mouseX, int mouseY, float partialTicks) {
        int contentY = -this.scrollSidebarOffset;
        for (int i = 0; i < this.buttonList.size(); ++i) {
            (this.buttonList.get(i)).drawButton(this.mc, mouseX, mouseY + contentY, partialTicks);
            this.buttonList.get(i).y += contentY;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseX >= scrollBarX && mouseY >= scrollBarY && mouseX < scrollBarX + scrollBarWidth && mouseY < scrollBarY + scrollBarHeight) {
            this.isScrolling = true;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.isScrolling = false;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();
        if (i != 0) {
            this.scrollSidebarOffset += (i > 0) ? -10 : 10;
            this.scrollSidebarOffset = Math.max(0, Math.min(this.scrollSidebarOffset, this.sidebarHeight - this.height));
        }
    }
}
