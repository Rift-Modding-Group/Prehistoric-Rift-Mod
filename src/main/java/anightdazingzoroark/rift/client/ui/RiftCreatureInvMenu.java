package anightdazingzoroark.rift.client.ui;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.inventory.CreatureContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftCreatureInvMenu extends GuiContainer {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_inventory.png");
    private final IInventory playerInventory;
    private final IInventory creatureInventory;
    private final RiftCreature creature;
    private float mousePosx;
    private float mousePosY;
    private GuiButton prevButton;
    private GuiButton nextButton;

    public RiftCreatureInvMenu(IInventory playerInventory, RiftCreature creature) {
        super(new CreatureContainer(creature, Minecraft.getMinecraft().player));
        this.buttonList.clear();
        this.playerInventory = playerInventory;
        this.creatureInventory = creature.creatureInventory;
        this.creature = creature;
        this.allowUserInput = false;
        this.ySize = 238;
        if (creatureInventory.getSizeInventory() - (creature.canBeSaddled() ? 1 : 0) > 27) {
            this.addPageButtons();
        }
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        //saddle slot
        if (this.creature.canBeSaddled() && !this.creature.isChild()) {
            this.drawTexturedModalRect(k + 7, l + 17, 180, 238, 18, 18);
        }

        //normal inventory slots
        int slots = this.creatureInventory.getSizeInventory() - (this.creature.canBeSaddled() ? 1 : 0);
        for (int i = 0; i < Math.min(3, slots / 9); i++) {
            this.drawTexturedModalRect(k + 7, l + 73 + (i * 18), 0, 238, 162, 18);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String playerName = this.creature.getOwner().getName();
        this.fontRenderer.drawString(playerName, 8, 145, 4210752);
        String creatureName = this.creature.getName();
        this.fontRenderer.drawString(creatureName, 8, 7, 4210752);
        if (creatureInventory.getSizeInventory() - (creature.canBeSaddled() ? 1 : 0) > 27) {
            String page = (this.creature.invPage + 1)+"/"+this.creature.invPageCount();
            this.fontRenderer.drawString(page, this.xSize / 2 - this.fontRenderer.getStringWidth(page) / 2, 130, 4210752);
        }
    }

    private void addPageButtons() {
        this.buttonList.add(this.prevButton = new GuiButton(0, 8, 130, 12, 12, "<"));
        this.buttonList.add(this.nextButton = new GuiButton(1, 170, 130, 12, 12, ">"));
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mousePosx = mouseX;
        this.mousePosY = mouseY;
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                this.creature.prevPage();
                break;
            case 1:
                this.creature.nextPage();
                break;
        }
        this.initGui();
        this.updateScreen();
    }
}
