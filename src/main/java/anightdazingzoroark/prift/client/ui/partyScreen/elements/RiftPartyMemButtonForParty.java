package anightdazingzoroark.prift.client.ui.partyScreen.elements;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.elements.RiftClickableSection;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.ui.uiElement.RiftLibClickableSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;

public class RiftPartyMemButtonForParty extends RiftLibClickableSection {
    private NBTTagCompound creatureNBT;

    public RiftPartyMemButtonForParty(NBTTagCompound creatureNBT, int guiWidth, int guiHeight, int xOffset, int yOffset, FontRenderer fontRenderer, Minecraft minecraft) {
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
        if (this.creatureNBT != null && !this.creatureNBT.isEmpty()) {
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

            //some important variables
            RiftCreatureType creatureType = RiftCreatureType.values()[this.creatureNBT.getByte("CreatureType")];
            PlayerTamedCreatures.DeploymentType deploymentType = PlayerTamedCreatures.DeploymentType.values()[this.creatureNBT.getByte("DeploymentType")];
            float health = this.creatureNBT.getFloat("Health");

            //put on red bg when creature is ded
            if (health <= 0) {
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
            if (deploymentType.equals(PlayerTamedCreatures.DeploymentType.PARTY)) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.minecraft.getTextureManager().bindTexture(this.textureLocation);
                int k = (this.guiWidth - 18) / 2 + this.xOffset + this.xAddOffset - 18;
                int l = (this.guiHeight - 18) / 2 + this.yOffset + this.yAddOffset - 8;
                drawModalRectWithCustomSizedTexture(k, l, 57, 203, 18, 18, this.textureWidth, this.textureHeight);
            }

            //create creature icon overlay
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ResourceLocation iconLocation = new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+creatureType.name().toLowerCase()+"_icon.png");
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
            String levelString = I18n.format("tametrait.level", this.creatureNBT.getInteger("Level"));
            float levelStringScale = 0.5f;
            int levelStringX = (int) (((this.guiWidth - this.fontRenderer.getStringWidth(levelString)) / 2f + this.xOffset + this.xAddOffset + 20) / levelStringScale);
            int levelStringY = (int) (((this.guiHeight - this.fontRenderer.FONT_HEIGHT) / 2f + this.yOffset + this.yAddOffset - 5) / levelStringScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(levelStringScale, levelStringScale, levelStringScale);
            this.fontRenderer.drawString(levelString, levelStringX, levelStringY, 0x000000);
            GlStateManager.popMatrix();

            //render creature nickname or name
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            String nameString = (this.creatureNBT.hasKey("CustomName") && !this.creatureNBT.getString("CustomName").isEmpty()) ? this.creatureNBT.getString("CustomName") : creatureType.getTranslatedName();
            float nameStringScale = 0.5f;
            int nameStringX = (int) ((this.guiWidth / 2f + this.xOffset + this.xAddOffset - 24) / nameStringScale);
            int nameStringY = (int) (((this.guiHeight - this.fontRenderer.FONT_HEIGHT) / 2f + this.yOffset + this.yAddOffset + 8) / nameStringScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(nameStringScale, nameStringScale, nameStringScale);
            this.fontRenderer.drawString(nameString, nameStringX, nameStringY, 0x000000);
            GlStateManager.popMatrix();

            //render creature healthbar
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            //get health first
            float maxHealth = health;
            for (NBTBase nbtBase: this.creatureNBT.getTagList("Attributes", 10).tagList) {
                if (nbtBase instanceof NBTTagCompound) {
                    NBTTagCompound tagCompound = (NBTTagCompound) nbtBase;

                    if (!tagCompound.hasKey("Name") || !tagCompound.getString("Name").equals("generic.maxHealth")) continue;

                    maxHealth = (float) tagCompound.getDouble("Base");
                }
            }
            //now draw the hp bar
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int hpBarLength = MathHelper.clamp((int) ((health / maxHealth) * 51),0,51);
            int hpBarX = this.guiWidth / 2 + this.xOffset + this.xAddOffset - 26;
            int hpBarY = (this.guiHeight - 1) / 2 + this.yOffset + this.yAddOffset + 12;
            drawModalRectWithCustomSizedTexture(hpBarX, hpBarY, 0, 301, hpBarLength, 1, this.textureWidth, this.textureHeight);

            //render creature energy
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            float energy = this.creatureNBT.getInteger("Energy");
            float maxEnergy = RiftConfigHandler.getConfig(creatureType).stats.maxEnergy;
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int energyBarLength = MathHelper.clamp((int) ((energy / maxEnergy) * 51),0,51);
            int energyBarX = this.guiWidth / 2 + this.xOffset + this.xAddOffset - 26;
            int energyBarY = (this.guiHeight - 1) / 2 + this.yOffset + this.yAddOffset + 14;
            drawModalRectWithCustomSizedTexture(energyBarX, energyBarY, 0, 302, energyBarLength, 1, this.textureWidth, this.textureHeight);

            //render creature xp
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            float xp = this.creatureNBT.getInteger("XP");
            float maxXP = creatureType.getMaxXP(this.creatureNBT.getInteger("Level"));
            this.minecraft.getTextureManager().bindTexture(this.textureLocation);
            int xpBarLength = MathHelper.clamp((int) ((xp / maxXP) * 51),0,51);
            int xpBarX = this.guiWidth / 2 + this.xOffset + this.xAddOffset - 26;
            int xpBarY = (this.guiHeight - 1) / 2 + this.yOffset + this.yAddOffset + 16;
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
        int x = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
        int y = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
        return mouseX >= x && mouseX <= x + this.width * this.scale && mouseY >= y && mouseY <= y + this.height * this.scale;
    }

    private boolean isHoveredNoNBT(int mouseX, int mouseY) {
        int x = (this.guiWidth - this.width) / 2 + this.xOffset + this.xAddOffset;
        int y = (this.guiHeight - this.height) / 2 + this.yOffset + this.yAddOffset;
        return mouseX >= x && mouseX <= x + 57 * this.scale && mouseY >= y && mouseY <= y + 38 * this.scale;
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
