package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RiftGuiCreatureBoxBoxButton extends GuiButton {
    private final RiftCreature creature;
    public boolean toMove;

    public RiftGuiCreatureBoxBoxButton(RiftCreature creature, int buttonId, int x, int y) {
        super(buttonId, x, y, 30, 30, "");
        this.creature = creature;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //the button itself
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            drawModalRectWithCustomSizedTexture(this.x, this.y, (this.hovered || this.toMove) ? 222 : 192, this.toMove ? 246 : 216, this.width, this.height, 408, 300);

            //creature icon
            mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+this.creature.creatureType.name().toLowerCase()+"_icon.png"));
            drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 24, 24, 24, 24);

            //x mark over creature icon
            double healthPercentage = this.creature.getHealth() / this.creature.getMaxHealth();
            if (healthPercentage == 0) {
                mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
                drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 60, 248, 24, 24, 408, 300);
            }
        }
    }
}
