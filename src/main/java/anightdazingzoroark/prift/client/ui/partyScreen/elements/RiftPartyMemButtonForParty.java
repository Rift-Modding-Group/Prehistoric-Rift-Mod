package anightdazingzoroark.prift.client.ui.partyScreen.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;

public class RiftPartyMemButtonForParty extends RiftLibClickableSection {
    private CreatureNBT creatureNBT;

    public RiftPartyMemButtonForParty(CreatureNBT creatureNBT, int guiWidth, int guiHeight, int xOffset, int yOffset, FontRenderer fontRenderer, Minecraft minecraft) {
        super(57, 38, guiWidth, guiHeight, xOffset, yOffset, fontRenderer, minecraft);
        this.creatureNBT = creatureNBT;

        //deal with uv textures
        this.textureLocation = new ResourceLocation(RiftInitialize.MODID, "textures/ui/party_background.png");
        this.uvWidth = 57;
        this.uvHeight = 38;
        this.textureWidth = 400;
        this.textureHeight = 360;
        this.xUV = 0;
        this.yUV = 203;
    }

    @Override
    public void drawSection(int mouseX, int mouseY) {
        //normal contents, means slot has a creature
        if (this.creatureNBT != null && !this.creatureNBT.nbtIsEmpty()) {
            //deal with hovering
            this.isHovered = this.isHovered(mouseX, mouseY);

            //draw background
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int bgX = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
            int bgY = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
            int xUVTexture = this.xUV;
            int yUVTexture = this.yUV;
            drawModalRectWithCustomSizedTexture(bgX, bgY, xUVTexture, yUVTexture, this.uvWidth, this.uvHeight, this.textureWidth, this.textureHeight);

            //put on red bg when creature is ded
            if (this.creatureNBT.getCreatureHealth()[0] <= 0) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset + 1;
                int l = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset + 1;
                drawModalRectWithCustomSizedTexture(k, l, 57, 221, 55, 28, this.textureWidth, this.textureHeight);
            }

            //put on yellow hoverlay when selected
            if (this.isSelected) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
                int l = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
                drawModalRectWithCustomSizedTexture(k, l, 0, 271, 57, 30, this.textureWidth, this.textureHeight);
            }

            //put on white overlay when hovered
            if (this.isHovered) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
                int l = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
                drawModalRectWithCustomSizedTexture(k, l, 0, 241, 57, 30, this.textureWidth, this.textureHeight);
            }

            //create green background for icon overlay if creature has been deployed
            if (this.creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - 18) / 2 + this.xOffset + this.xAddOffset - 18;
                int l = (this.guiHeight - 18) / 2 + this.yOffset + this.yAddOffset - 8;
                drawModalRectWithCustomSizedTexture(k, l, 57, 203, 18, 18, this.textureWidth, this.textureHeight);
            }

            //create creature icon overlay
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ResourceLocation iconLocation = new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+this.creatureNBT.getCreatureType().name().toLowerCase()+"_icon.png");
            this.minecraft.getTextureManager().bindTexture(iconLocation);
            float iconScale = 0.75f;
            int k = (int) ((this.guiWidth - 24) / (2 * iconScale) + (this.xOffset + this.xAddOffset - 14) / iconScale);
            int l = (int) ((this.guiHeight - 24) / (2 * iconScale) + (this.yOffset + this.yAddOffset - 5) / iconScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(iconScale, iconScale, iconScale);
            drawModalRectWithCustomSizedTexture(k, l, 0, 0, 24, 24, 24, 24);
            GlStateManager.popMatrix();

            //render creature level
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            String levelString = this.creatureNBT.getCreatureLevelWithText();
            float levelStringScale = 0.5f;
            int levelStringX = (int) (((this.guiWidth - this.fontRenderer.getStringWidth(levelString)) / 2f + this.xOffset + this.xAddOffset + 20) / levelStringScale);
            int levelStringY = (int) (((this.guiHeight - this.fontRenderer.FONT_HEIGHT) / 2f + this.yOffset + this.yAddOffset - 5) / levelStringScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(levelStringScale, levelStringScale, levelStringScale);
            this.fontRenderer.drawString(levelString, levelStringX, levelStringY, 0x000000);
            GlStateManager.popMatrix();

            //render creature nickname or name
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            String nameString = this.creatureNBT.getCreatureName(false);
            float nameStringScale = 0.5f;
            int nameStringX = (int) ((this.guiWidth / 2f + this.xOffset + this.xAddOffset - 24) / nameStringScale);
            int nameStringY = (int) (((this.guiHeight - this.fontRenderer.FONT_HEIGHT) / 2f + this.yOffset + this.yAddOffset + 8) / nameStringScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(nameStringScale, nameStringScale, nameStringScale);
            this.fontRenderer.drawString(nameString, nameStringX, nameStringY, 0x000000);
            GlStateManager.popMatrix();

            //render creature healthbar
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int hpBarLength = MathHelper.clamp((int) ((this.creatureNBT.getCreatureHealth()[0] / this.creatureNBT.getCreatureHealth()[1]) * 51),0,51);
            int hpBarX = this.guiWidth / 2 + this.xOffset + this.xAddOffset - 25;
            int hpBarY = (this.guiHeight - 1) / 2 + this.yOffset + this.yAddOffset + 13;
            drawModalRectWithCustomSizedTexture(hpBarX, hpBarY, 0, 301, hpBarLength, 1, this.textureWidth, this.textureHeight);

            //render creature energy
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int energyBarLength = MathHelper.clamp((int) ((this.creatureNBT.getCreatureEnergy()[0] / (float) this.creatureNBT.getCreatureEnergy()[1]) * 51),0,51);
            int energyBarX = this.guiWidth / 2 + this.xOffset + this.xAddOffset - 25;
            int energyBarY = (this.guiHeight - 1) / 2 + this.yOffset + this.yAddOffset + 15;
            drawModalRectWithCustomSizedTexture(energyBarX, energyBarY, 0, 302, energyBarLength, 1, this.textureWidth, this.textureHeight);

            //render creature xp
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int xpBarLength = MathHelper.clamp((int) ((this.creatureNBT.getCreatureXP()[0] / (float) this.creatureNBT.getCreatureXP()[1]) * 51),0,51);
            int xpBarX = this.guiWidth / 2 + this.xOffset + this.xAddOffset - 25;
            int xpBarY = (this.guiHeight - 1) / 2 + this.yOffset + this.yAddOffset + 17;
            drawModalRectWithCustomSizedTexture(xpBarX, xpBarY, 0, 303, xpBarLength, 1, this.textureWidth, this.textureHeight);
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
                drawModalRectWithCustomSizedTexture(k, l, 0, 315, 57, 38, this.textureWidth, this.textureHeight);
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
