package anightdazingzoroark.prift.client.ui.elements;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;

public class RiftPartyMemButton extends RiftClickableSection {
    public RiftPartyMemButton(int guiWidth, int guiHeight, int xOffset, int yOffset, FontRenderer fontRenderer, Minecraft minecraft) {
        super(57, 38, guiWidth, guiHeight, xOffset, yOffset, fontRenderer, minecraft);
        //deal with uv textures
        this.textureLocation = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");
        this.uvWidth = 57;
        this.uvHeight = 38;
        this.textureWidth = 400;
        this.textureHeight = 300;
        this.xUV = 0;
        this.yUV = 178;
    }

    @Override
    public void drawSection(int mouseX, int mouseY) {
        //deal with hovering
        this.isHovered = this.isHovered(mouseX, mouseY);

        //draw image
        if (this.textureLocation != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int k = (this.guiWidth - this.width) / 2 + this.xOffset;
            int l = (this.guiHeight - this.height) / 2 + this.yOffset;
            int xUVTexture = this.xUV;
            int yUVTexture = this.yUV;
            drawModalRectWithCustomSizedTexture(k, l, xUVTexture, yUVTexture, this.uvWidth, this.uvHeight, this.textureWidth, this.textureHeight);
        }

        //put on white overlay when hovered
        if (this.isHovered) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int k = (this.guiWidth - this.width) / 2 + this.xOffset;
            int l = (this.guiHeight - this.height) / 2 + this.yOffset;
            drawModalRectWithCustomSizedTexture(k, l, 0, 216, 57, 30, this.textureWidth, this.textureHeight);
        }
    }
}
