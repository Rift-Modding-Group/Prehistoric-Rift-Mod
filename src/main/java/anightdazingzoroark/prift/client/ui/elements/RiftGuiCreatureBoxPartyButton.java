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
    public boolean isSelected;
    public int topLimit;
    public int bottomLimit;

    public RiftGuiCreatureBoxPartyButton(RiftCreature creature, int buttonId, int x, int y, int topLimit, int bottomLimit) {
        super(buttonId, x, y, 96, 32, "");
        this.creature = creature;
        this.toMove = false;
        this.isSelected = false;
        this.topLimit = topLimit;
        this.bottomLimit = bottomLimit;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //the button itself
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
            int upper = Math.max(this.y, this.topLimit);
            int lower = Math.min(this.y + this.height, this.bottomLimit);
            this.hovered = mouseX >= this.x && mouseY >= upper && mouseX < this.x + this.width && mouseY < lower;
            drawModalRectWithCustomSizedTexture(this.x, this.y, this.getButtonUVs()[0], this.getButtonUVs()[1], this.width, this.height, 408, 300);

            if (this.creature != null) {
                //health bar
                double healthPercentage = this.creature.getHealth() / this.creature.getMaxHealth();
                drawModalRectWithCustomSizedTexture(this.x + 32, this.y + 22, 0, 248, (int)(60 * healthPercentage), 3, 408, 300);

                //energy bar
                double energyPercentage = this.creature.getEnergy() / 20D;
                drawModalRectWithCustomSizedTexture(this.x + 32, this.y + 26, 0, 251, (int)(60 * energyPercentage), 3, 408, 300);

                //creature icon
                mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+this.creature.creatureType.name().toLowerCase()+"_icon.png"));
                drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 24, 24, 24, 24);

                //x mark over creature icon
                if (healthPercentage == 0) {
                    mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
                    drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 60, 248, 24, 24, 408, 300);
                }

                //paficier icon for babies
                if (this.creature.isBaby()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                    mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
                    drawModalRectWithCustomSizedTexture((int)((this.x + 3) / 0.5), (int)((this.y + 21) / 0.5), 362, 216, 11, 11, 408, 300);
                    GlStateManager.popMatrix();
                }

                //creature name and level
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                mc.fontRenderer.drawSplitString(this.creature.getName(), (int)((this.x + 32)/0.5), (int)((this.y + 12)/0.5), 140, 0);
                GlStateManager.popMatrix();
            }
            else {
                //creature icon
                mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/icons/empty_icon.png"));
                drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 24, 24, 24, 24);
            }
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        int upper = Math.max(this.y, this.topLimit);
        int lower = Math.min(this.y + this.height, this.bottomLimit);
        return this.enabled && this.visible && mouseX >= this.x && mouseY >= upper && mouseX < this.x + this.width && mouseY < lower;
    }

    private int[] getButtonUVs() {
        if (this.toMove) return new int[]{96, 248};
        else if (this.hovered || this.isSelected) return new int[]{96, 216};
        return new int[]{0, 216};
    }
}
