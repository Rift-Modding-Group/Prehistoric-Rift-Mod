package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RiftGuiCreatureBoxPartyButton extends GuiButton {
    private final RiftCreature creature;
    public boolean toMove;

    public RiftGuiCreatureBoxPartyButton(RiftCreature creature, int buttonId, int x, int y) {
        super(buttonId, x, y, 96, 32, "");
        this.creature = creature;
        this.toMove = false;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //the button itself
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            drawModalRectWithCustomSizedTexture(this.x, this.y, (this.hovered || this.toMove) ? 96 : 0, this.toMove ? 248 : 216, this.width, this.height, 408, 300);

            //health bar
            double healthPercentage = this.creature.getHealth() / this.creature.getMaxHealth();
            drawModalRectWithCustomSizedTexture(this.x + 31, this.y + 22, 0, 248, (int)(60 * healthPercentage), 3, 480, 300);

            //energy bar
            double energyPercentage = this.creature.getEnergy() / 20D;
            drawModalRectWithCustomSizedTexture(this.x + 31, this.y + 26, 0, 251, (int)(60 * energyPercentage), 3, 480, 300);

            //creature icon
            mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+this.creature.creatureType.name().toLowerCase()+"_icon.png"));
            drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 24, 24, 24, 24);

            //x mark over creature icon
            if (healthPercentage == 0) {
                mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
                drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 60, 248, 24, 24, 480, 300);
            }

            //creature name and level
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            mc.fontRenderer.drawSplitString(this.creature.getName(), (int)((this.x + 32)/0.5), (int)((this.y + 12)/0.5), 140, 0);
            GlStateManager.popMatrix();
        }
    }
}
