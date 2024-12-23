package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RiftGuiCreatureBoxDeployedButton extends GuiButton {
    private final RiftCreature creature;
    public boolean toMove;
    public boolean isSelected;

    public RiftGuiCreatureBoxDeployedButton(RiftCreature creature, int buttonId, int x, int y) {
        super(buttonId, x, y, 30, 30, "");
        this.creature = creature;
        this.isSelected = false;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //the button itself
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            drawModalRectWithCustomSizedTexture(this.x, this.y, this.getButtonUVs()[0], this.getButtonUVs()[1], this.width, this.height, 408, 300);

            if (this.creature != null) {
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
            else {
                //creature icon
                mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/icons/empty_icon.png"));
                drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 24, 24, 24, 24);
            }
        }
    }

    private int[] getButtonUVs() {
        if (this.toMove) return new int[]{222, 246};
        else if (this.hovered || this.isSelected) return new int[]{222, 216};
        return new int[]{192, 216};
    }
}
