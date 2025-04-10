package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RiftGuiJournalPartyButton extends GuiButton {
    private final RiftCreature creature;
    public boolean toMove;
    public boolean isSelected;

    public RiftGuiJournalPartyButton(RiftCreature creature, int buttonId, int x, int y) {
        super(buttonId, x, y, 96, 32, "");
        this.creature = creature;
        this.toMove = false;
        this.isSelected = false;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //the button itself
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/sidebar_party_button.png"));
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            drawModalRectWithCustomSizedTexture(this.x, this.y, this.getButtonUVs()[0], this.getButtonUVs()[1], this.width, this.height, 192, 96);

            //health bar
            double healthPercentage = this.creature.getHealth() / this.creature.getMaxHealth();
            drawModalRectWithCustomSizedTexture(this.x + 32, this.y + 22, 0, 32, (int)(60 * healthPercentage), 3, 192, 96);

            //energy bar
            double energyPercentage = this.creature.getEnergy() / (double)this.creature.getMaxEnergy();
            drawModalRectWithCustomSizedTexture(this.x + 32, this.y + 26, 0, 35, (int)(60 * energyPercentage), 3, 192, 96);

            //creature icon
            mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+this.creature.creatureType.name().toLowerCase()+"_icon.png"));
            drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 24, 24, 24, 24);

            //x mark over creature icon
            if (healthPercentage == 0) {
                mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/sidebar_party_button.png"));
                drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 60, 32, 24, 24, 192, 96);
            }

            //paficier icon for babies
            if (this.creature.isBaby()) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/sidebar_party_button.png"));
                drawModalRectWithCustomSizedTexture((int)((this.x + 3) / 0.5), (int)((this.y + 21) / 0.5), 0, 38, 11, 11, 192, 96);
                GlStateManager.popMatrix();
            }

            //creature name and level
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            mc.fontRenderer.drawSplitString(this.creature.getName(), (int)((this.x + 32)/0.5), (int)((this.y + 12)/0.5), 140, 0);
            GlStateManager.popMatrix();
        }
    }

    private int[] getButtonUVs() {
        if (this.toMove) return new int[]{96, 32};
        else if (this.hovered || (this.isSelected && this.creature.getDeploymentType() != PlayerTamedCreatures.DeploymentType.PARTY)) return new int[]{96, 0};
        else if (this.creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) return new int[]{96, 64};
        return new int[]{0, 0};
    }
}
