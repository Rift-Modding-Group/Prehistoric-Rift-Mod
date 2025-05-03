package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RiftGuiCreatureBoxBoxButton extends GuiButton {
    private final RiftCreature creature;
    public boolean toMove;
    public boolean isSelected;

    public RiftGuiCreatureBoxBoxButton(RiftCreature creature, int buttonId, int x, int y) {
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
                double healthPercentage = this.creature.getHealth() / this.creature.getMaxHealth();
                boolean isIncapped = healthPercentage == 0 && this.creature.getBoxReviveTime() > 0;

                if (isIncapped) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);
                }
                //creature icon
                mc.getTextureManager().bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+this.creature.creatureType.name().toLowerCase()+"_icon.png"));
                drawModalRectWithCustomSizedTexture(this.x + 3, this.y + 3, 0, 0, 24, 24, 24, 24);
                if (isIncapped) {
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }

                //paficier icon for babies
                if (this.creature.isBaby()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                    mc.renderEngine.bindTexture(new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png"));
                    drawModalRectWithCustomSizedTexture((int)((this.x + 3) / 0.5), (int)((this.y + 21) / 0.5), 362, 216, 11, 11, 408, 300);
                    GlStateManager.popMatrix();
                }

                if (isIncapped) {
                    //translucent bar over countdown
                    drawRect(this.x + 3, this.y + 10, this.x + 27, this.y + 17, 0x80262626);

                    //countdown for recovery
                    String minutes = this.getReviveTimeMinutes(this.creature.getBoxReviveTime())[0];
                    String seconds = this.getReviveTimeMinutes(this.creature.getBoxReviveTime())[1];
                    String time = minutes+":"+seconds;

                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                    mc.fontRenderer.drawString(time, (int)((this.x + 8)/0.5), (int)((this.y + 12) /0.5), 0);
                    GlStateManager.popMatrix();
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

    private String[] getReviveTimeMinutes(int tick) {
        int minutesInt = (int)((float)tick / 1200F);
        int secondsInt = (int)((float)tick / 20F);
        secondsInt = secondsInt - (minutesInt * 60);

        String minutesString = minutesInt < 10 ? "0"+minutesInt : String.valueOf(minutesInt);
        String secondsString = secondsInt < 10 ? "0"+secondsInt : String.valueOf(secondsInt);
        return new String[]{minutesString, secondsString};
    }
}
