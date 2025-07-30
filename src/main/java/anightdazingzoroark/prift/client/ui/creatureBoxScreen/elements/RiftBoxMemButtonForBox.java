package anightdazingzoroark.prift.client.ui.creatureBoxScreen.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftClickableSection;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;

public class RiftBoxMemButtonForBox extends RiftClickableSection {
    private NBTTagCompound creatureNBT;

    public RiftBoxMemButtonForBox(NBTTagCompound creatureNBT, int guiWidth, int guiHeight, int xOffset, int yOffset, FontRenderer fontRenderer, Minecraft minecraft) {
        super(32, 32, guiWidth, guiHeight, xOffset, yOffset, fontRenderer, minecraft);
        this.creatureNBT = creatureNBT;

        //deal with uv textures
        this.textureLocation = new ResourceLocation(RiftInitialize.MODID, "textures/ui/new_creature_box_background.png");
        this.uvWidth = 32;
        this.uvHeight = 32;
        this.textureWidth = 400;
        this.textureHeight = 300;
    }

    @Override
    public void drawSection(int mouseX, int mouseY) {
        //normal contents, means slot has a creature
        if (this.creatureNBT != null && !this.creatureNBT.isEmpty()) {
            //deal with hovering
            this.isHovered = this.isHovered(mouseX, mouseY);

            //some important variables
            RiftCreatureType creatureType = RiftCreatureType.values()[this.creatureNBT.getByte("CreatureType")];
            float health = this.creatureNBT.getFloat("Health");

            //draw background
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int bgX = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
            int bgY = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
            drawModalRectWithCustomSizedTexture(bgX, bgY, 0, 268, this.uvWidth, this.uvHeight, this.textureWidth, this.textureHeight);

            //put on yellow hoverlay when selected
            if (this.isSelected) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
                int l = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
                drawModalRectWithCustomSizedTexture(k, l, 128, 268, this.uvWidth, this.uvHeight, this.textureWidth, this.textureHeight);
            }

            //put on white overlay when hovered
            if (this.isHovered) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
                int l = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
                drawModalRectWithCustomSizedTexture(k, l, 96, 268, this.uvWidth, this.uvHeight, this.textureWidth, this.textureHeight);
            }

            //create creature icon overlay
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ResourceLocation iconLocation = new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+creatureType.name().toLowerCase()+"_icon.png");
            this.minecraft.getTextureManager().bindTexture(iconLocation);
            float iconScale = 0.75f;
            int k = (int) ((this.guiWidth - 24) / (2 * iconScale) + (this.xOffset + this.xAddOffset + 8) / iconScale);
            int l = (int) ((this.guiHeight - 24) / (2 * iconScale) + (this.yOffset + this.yAddOffset + 8) / iconScale);
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
                drawModalRectWithCustomSizedTexture(k, l, 96, 268, this.uvWidth, this.uvHeight, this.textureWidth, this.textureHeight);
            }
        }
    }

    private boolean isHoveredNoNBT(int mouseX, int mouseY) {
        int x = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
        int y = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
        return mouseX >= x && mouseX <= x + 32 * this.scale && mouseY >= y && mouseY <= y + 32 * this.scale;
    }

    public void setCreatureNBT(NBTTagCompound nbtTagCompound) {
        this.creatureNBT = nbtTagCompound;
    }

    public NBTTagCompound getCreatureNBT() {
        return this.creatureNBT;
    }

    //for best performance, DO NOT USE THIS IN METHODS MEANT TO BE LOOPED
    //USE getCreatureNBT() FOR THAT INSTEAD
    public RiftCreature getCreatureFromNBT() {
        return PlayerTamedCreaturesHelper.createCreatureFromNBT(this.minecraft.world, this.creatureNBT);
    }
}
