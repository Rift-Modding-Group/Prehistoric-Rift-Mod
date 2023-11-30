package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.inventory.CreatureContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
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

    public RiftCreatureInvMenu(IInventory playerInventory, RiftCreature creature) {
        super(new CreatureContainer(creature, Minecraft.getMinecraft().player));
        this.playerInventory = playerInventory;
        this.creatureInventory = creature.creatureInventory;
        this.creature = creature;
        this.allowUserInput = false;
        this.ySize = 254;
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        //saddle slot
        if (this.creature.canBeSaddled() && !this.creature.isBaby()) {
            this.drawTexturedModalRect(k + 7, l + 17, 212, 0, 18, 18);
        }

        //normal inventory slots
        int slots = this.creatureInventory.getSizeInventory() - (this.creature.canBeSaddled() ? 1 : 0);
        for (int i = 0; i < slots / 9; i++) {
            for (int j = 0; j < 9; j++) {
                this.drawTexturedModalRect(k + 7 + (j * 18), l + 49 + (i * 18), 176, 0, 18, 18);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String playerName = this.creature.getOwner().getName();
        this.fontRenderer.drawString(playerName, 8, 160, 4210752);
        String creatureName = this.creature.getName(); //I18n.format("prift.inventory.gear", creatureTypeName)
        String gearName = I18n.format("inventory.gear", creatureName);
        this.fontRenderer.drawString(gearName, 8, 7, 4210752);
        String invName = I18n.format("inventory.inventory", creatureName);
        this.fontRenderer.drawString(invName, 8, 39, 4210752);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mousePosx = mouseX;
        this.mousePosY = mouseY;
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
