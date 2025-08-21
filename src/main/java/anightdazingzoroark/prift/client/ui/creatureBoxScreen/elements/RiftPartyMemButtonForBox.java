package anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;

public class RiftPartyMemButtonForBox extends RiftLibClickableSection {
    private CreatureNBT creatureNBT;

    public RiftPartyMemButtonForBox(CreatureNBT creatureNBT, int guiWidth, int guiHeight, int xOffset, int yOffset, FontRenderer fontRenderer, Minecraft minecraft) {
        super(20, 20, guiWidth, guiHeight, xOffset, yOffset, fontRenderer, minecraft);
        this.creatureNBT = creatureNBT;

        //deal with uv textures
        this.textureLocation = new ResourceLocation(RiftInitialize.MODID, "textures/ui/creature_box_background.png");
        this.uvWidth = 20;
        this.uvHeight = 20;
        this.textureWidth = 400;
        this.textureHeight = 300;
        this.xUV = 0;
        this.yUV = 248;
    }

    @Override
    public void drawSection(int mouseX, int mouseY) {
        //normal contents, means slot has a creature
        if (this.creatureNBT != null && !this.creatureNBT.nbtIsEmpty()) {
            //deal with hovering
            this.isHovered = this.isHovered(mouseX, mouseY);

            //draw background
            //background changes based on whether or not creature is deployed or not
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int bgX = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
            int bgY = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
            int xUVTexture = this.creatureNBT.getCreatureHealth()[0] <= 0 ? 40 : (
                    this.creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY ? 20 : 0
            );
            int yUVTexture = 248;
            Gui.drawModalRectWithCustomSizedTexture(bgX, bgY, xUVTexture, yUVTexture, this.uvWidth, this.uvHeight, this.textureWidth, this.textureHeight);

            //put on yellow hoverlay when selected
            if (this.isSelected) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
                int l = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
                drawModalRectWithCustomSizedTexture(k, l, 80, 248, 20, 20, this.textureWidth, this.textureHeight);
            }

            //put on white overlay when hovered
            if (this.isHovered) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
                int l = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
                drawModalRectWithCustomSizedTexture(k, l, 60, 248, 20, 20, this.textureWidth, this.textureHeight);
            }

            //create creature icon overlay
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ResourceLocation iconLocation = new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+this.creatureNBT.getCreatureType().name().toLowerCase()+"_icon.png");
            this.minecraft.getTextureManager().bindTexture(iconLocation);
            float iconScale = 0.5f;
            int k = (int) ((this.guiWidth - 24) / (2 * iconScale) + (this.xOffset + this.xAddOffset + 5) / iconScale);
            int l = (int) ((this.guiHeight - 24) / (2 * iconScale) + (this.yOffset + this.yAddOffset + 5) / iconScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(iconScale, iconScale, iconScale);
            drawModalRectWithCustomSizedTexture(k, l, 0, 0, 24, 24, 24, 24);
            GlStateManager.popMatrix();
        }
        //blank contents, means slot is empty
        else {
            this.isHovered = this.isHoveredNoNBT(mouseX, mouseY);

            //put on white overlay when hovered
            if (this.isHovered) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
                int l = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
                Gui.drawModalRectWithCustomSizedTexture(k, l, 60, 248, 20, 20, this.textureWidth, this.textureHeight);
            }
        }
    }

    public boolean isHovered(int mouseX, int mouseY) {
        if (!this.doHoverEffects) return false;
        int x = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
        int y = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
        return mouseX >= x && mouseX <= x + this.width * this.scale && mouseY >= y && mouseY <= y + this.height * this.scale;
    }

    private boolean isHoveredNoNBT(int mouseX, int mouseY) {
        if (!this.doHoverEffects) return false;
        int x = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
        int y = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
        return mouseX >= x && mouseX <= x + 57 * this.scale && mouseY >= y && mouseY <= y + 38 * this.scale;
    }

    public void setCreatureNBT(CreatureNBT nbtTagCompound) {
        this.creatureNBT = nbtTagCompound;
    }

    public CreatureNBT getCreatureNBT() {
        return this.creatureNBT;
    }

    //for best performance, DO NOT USE THIS IN METHODS MEANT TO BE LOOPED
    //USE getCreatureNBT() FOR THAT INSTEAD
    public RiftCreature getCreatureFromNBT() {
        return this.creatureNBT.getCreatureAsNBT(this.minecraft.world);
    }
}
