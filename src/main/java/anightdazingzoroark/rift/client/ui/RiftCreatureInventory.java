package anightdazingzoroark.rift.client.ui;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.inventory.CreatureContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RiftCreatureInventory extends GuiContainer {
    private static final ResourceLocation background = new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_inventory.png");
    private final IInventory playerInventory;
    private final IInventory creatureInventory;
    private final RiftCreature creature;
    private float mousePosx;
    private float mousePosY;

    public RiftCreatureInventory(IInventory playerInventory, RiftCreature creature) {
        super(new CreatureContainer(creature, Minecraft.getMinecraft().player));
        this.playerInventory = playerInventory;
        this.creatureInventory = creature.creatureInventory;
        this.creature = creature;
        this.allowUserInput = false;
        this.ySize = 222;
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        if (this.creature.canBeSaddled() && !this.creature.isChild()) {
            this.drawTexturedModalRect(k + 7, l + 17, 180, 222, 18, 18);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mousePosx = mouseX;
        this.mousePosY = mouseY;
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
