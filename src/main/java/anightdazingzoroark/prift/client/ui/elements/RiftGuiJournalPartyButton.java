package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RiftGuiJournalPartyButton extends GuiButton {
    private final RiftCreature creature;

    public RiftGuiJournalPartyButton(RiftCreature creature, int buttonId, int x, int y) {
        super(buttonId, x, y, 96, 32, "");
        this.creature = creature;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //the button itself
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/sidebar_party_button.png"));
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            drawModalRectWithCustomSizedTexture(this.x, this.y, this.hovered ? 96 : 0, 0, this.width, this.height, 192, 44);

            //health bar
            double healthPercentage = this.creature.getHealth() / this.creature.getMaxHealth();
            drawModalRectWithCustomSizedTexture(this.x + 32, this.y + 22, 0, 32, (int)(60 * healthPercentage), 3, 192, 44);

            //energy bar
            double energyPercentage = this.creature.getEnergy() / 20D;
            drawModalRectWithCustomSizedTexture(this.x + 32, this.y + 26, 0, 35, (int)(60 * energyPercentage), 3, 192, 44);

            //creature icon
            mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+this.creature.creatureType.name().toLowerCase()+"_icon.png"));
            drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 24, 24, 24, 24);

            //creature name and level
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            mc.fontRenderer.drawSplitString(this.creature.getName(), (int)((this.x + 32)/0.5), (int)((this.y + 12)/0.5), 140, 0);
            GlStateManager.popMatrix();
        }
    }
}
