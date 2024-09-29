package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiCreatureBoxBoxButton;
import anightdazingzoroark.prift.client.ui.elements.RiftGuiCreatureBoxPartyButton;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.PopupFromCreatureBox;
import com.google.common.collect.Lists;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RiftCreatureBoxMenu extends GuiScreen {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png");
    protected final int xSize = 408;
    protected final int ySize = 216;
    private int scrollSidebarOffset = 0;
    private int scrollBoxedCreaturesOffset = 0;
    private int partyBarHeight;
    private int boxedCreaturesHeight;
    private final List<GuiButton> manageSelectedCreatureButtons = Lists.<GuiButton>newArrayList();
    private final List<GuiButton> creaturesInBoxButtons = Lists.<GuiButton>newArrayList();
    private RiftCreature selectedCreature;

    @Override
    public void initGui() {
        this.selectedCreature = null;

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
            this.selectedCreature = this.getPlayerParty().get(partyButton.id);
        }
        else if (button instanceof RiftGuiCreatureBoxBoxButton) {
            RiftGuiCreatureBoxBoxButton boxButton = (RiftGuiCreatureBoxBoxButton)button;
            this.selectedCreature = this.getPlayerBoxedCreatures().get(boxButton.id);
        }
        else if (this.manageSelectedCreatureButtons.contains(button)) {
            if (this.selectedCreature != null) {
                if (button.id == 0) {
                    ClientProxy.creatureUUID = this.selectedCreature.getUniqueID();
                    this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, PopupFromCreatureBox.CHANGE_NAME.ordinal(), 0, 0);
                }
                else if (button.id == 1) {
                    ClientProxy.creatureUUID = this.selectedCreature.getUniqueID();
                    this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_CREATURE_BOX, this.mc.player.world, PopupFromCreatureBox.RELEASE.ordinal(), 0, 0);
                }
            }
        }
    }

    private void partyButtonListInit() {
        this.buttonList.clear();
        this.partyBarHeight = 0;

        for (int x = 0; x < this.getPlayerParty().size(); x++) {
            RiftCreature creature = this.getPlayerParty().get(x);
            this.buttonList.add(new RiftGuiCreatureBoxPartyButton(creature, x, (this.width - 96)/2 - 147, (this.height - 32) / 2 - 83 + (40 * x)));
            this.partyBarHeight += 40;
        }
    }

    private void boxedCreaturesButtonListInit() {
        this.creaturesInBoxButtons.clear();
        this.boxedCreaturesHeight = 0;

        int rows = (int) Math.ceil(this.getPlayerBoxedCreatures().size() / 5D);
        for (int x = 0; x < this.getPlayerBoxedCreatures().size(); x++) {
            RiftCreature creature = this.getPlayerBoxedCreatures().get(x);
            this.creaturesInBoxButtons.add(new RiftGuiCreatureBoxBoxButton(creature, x, (this.width - 30)/2 - 72 + (33 * (x % 5)), (this.height - 30)/2 - 66 + (33 * (int)(x/5))));
        }
        for (int x = 0; x < rows; x++) {
            if (x == 0) this.boxedCreaturesHeight += 30;
            else this.boxedCreaturesHeight += 33;
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
        String string = I18n.format("creature_box.creature_box");
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

            //draw level and xp
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            String levelString = I18n.format("tametrait.level", this.selectedCreature.getLevel())+" ("+ this.selectedCreature.getXP() +"/"+ this.selectedCreature.getMaxXP() +" XP)";
            this.fontRenderer.drawString(levelString, (int)((this.width / 2 + 86) / 0.5), (int)(((this.height - this.fontRenderer.FONT_HEIGHT) / 2 + 5) / 0.5), 0);
            GlStateManager.popMatrix();

            //draw xp bar
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 8, 252, 216, 110, 4, 408, 300);

            double xpRatio = (double) this.selectedCreature.getXP() / this.selectedCreature.getMaxXP();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 8, 252, 220, (int)(110 * xpRatio), 4, 408, 300);

            //draw health
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            String healthString = I18n.format("tametrait.health")+": "+ this.selectedCreature.getHealth() +"/"+ this.selectedCreature.getMaxHealth() +" HP";
            this.fontRenderer.drawString(healthString, (int)((this.width / 2 + 86) / 0.5), (int)(((this.height - this.fontRenderer.FONT_HEIGHT) / 2 + 16) / 0.5), 0);
            GlStateManager.popMatrix();

            //draw health bar
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 19, 252, 216, 110, 4, 408, 300);

            double healthRatio = this.selectedCreature.getHealth() / this.selectedCreature.getMaxHealth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(background);
            drawModalRectWithCustomSizedTexture((this.width - 110) / 2 + 141, (this.height - 4) / 2 + 19, 252, 224, (int)(110 * healthRatio), 4, 408, 300);

            //draw other buttons
            //should have change name, manage inventory, and release buttons
            for (GuiButton button : this.manageSelectedCreatureButtons) {
                button.enabled = true;
                button.drawButton(this.mc, mouseX, mouseY, partialTicks);
            }
        }
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

        //update selected creature buttons
        this.manageSelectedCreatureButtons.clear();
        GuiButton changeNameButton = new GuiButton(0, (this.width - 100)/2 + 140, (this.height - 20)/2 + 60, 100, 20, I18n.format("creature_box.change_name"));
        changeNameButton.enabled = false;
        GuiButton releaseButton = new GuiButton(1, (this.width - 100)/2 + 140, (this.height - 20)/2 + 85, 100, 20, I18n.format("creature_box.release"));
        releaseButton.enabled = false;
        this.manageSelectedCreatureButtons.add(changeNameButton);
        this.manageSelectedCreatureButtons.add(releaseButton);
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
        }
    }

    private PlayerTamedCreatures playerTamedCreatures() {
        return EntityPropertiesHandler.INSTANCE.getProperties(this.mc.player, PlayerTamedCreatures.class);
    }

    private List<RiftCreature> getPlayerParty() {
        return this.playerTamedCreatures().getPartyCreatures(this.mc.player.world);
    }

    private List<RiftCreature> getPlayerBoxedCreatures() {
        return this.playerTamedCreatures().getBoxCreatures(this.mc.player.world);
    }
}
