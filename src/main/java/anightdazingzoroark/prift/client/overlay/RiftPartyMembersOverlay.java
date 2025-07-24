package anightdazingzoroark.prift.client.overlay;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.RiftGui;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.opengl.GL11;

import static net.minecraft.client.gui.Gui.drawRect;

public class RiftPartyMembersOverlay {
    private static final ResourceLocation hud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final ResourceLocation genericButtonIcon = new ResourceLocation(RiftInitialize.MODID, "textures/ui/generic_button_icon.png");
    private final double barChangeTolerance = 0.05;
    private int selectedPos = -1;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        IPlayerTamedCreatures playerTamedCreatures = NewPlayerTamedCreaturesHelper.getPlayerTamedCreatures(player);

        if (playerTamedCreatures == null) return;

        //selectedPos starts out as -1, to reduce potential lag
        //from repeatedly sending in packets for selected pos for overlay
        //this is here
        if (this.selectedPos < 0) {
            this.selectedPos = NewPlayerTamedCreaturesHelper.getSelectedPartyPosFromOverlay(player);
        }

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            ScaledResolution resolution = event.getResolution();
            FixedSizeList<NBTTagCompound> partyNBT = playerTamedCreatures.getPartyNBT();
            this.renderHUD(partyNBT, resolution.getScaledWidth(), resolution.getScaledHeight());
        }
    }

    private void renderHUD(FixedSizeList<NBTTagCompound> partyNBT, int xSize, int ySize) {
        //render translucent black background
        this.drawTranslucentBackground(xSize, ySize);

        //render left
        int leftPos = this.selectedPos - 1 < 0 ? NewPlayerTamedCreaturesHelper.maxPartySize - 1 : this.selectedPos - 1;
        NBTTagCompound leftPartyMemNBT = partyNBT.get(leftPos);
        this.renderBG(leftPartyMemNBT, xSize, ySize, 0.5f, 0.5f, -35, 0);
        this.renderControlTextureBG(RiftControls.switchLeftwards, xSize, ySize, 0.75f, -35, 20);

        //render middle
        NBTTagCompound middlePartyMemNBT = partyNBT.get(this.selectedPos);
        this.renderBG(middlePartyMemNBT, xSize, ySize, 1f, 1f, 0, 0);

        //render right
        int rightPos = this.selectedPos + 1 >= NewPlayerTamedCreaturesHelper.maxPartySize ? 0 : this.selectedPos + 1;
        NBTTagCompound rightPartyMemNBT = partyNBT.get(rightPos);
        this.renderBG(rightPartyMemNBT, xSize, ySize, 0.5f, 0.5f, 35, 0);
        this.renderControlTextureBG(RiftControls.switchRightwards, xSize, ySize, 0.75f, 35, 20);

        //render name, health bar, and energy bar for middle creature
        if (!middlePartyMemNBT.isEmpty()) {
            RiftCreatureType creatureType = RiftCreatureType.values()[middlePartyMemNBT.getByte("CreatureType")];
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

            //render name
            float textScale = 0.5f;
            String partyMemName = (middlePartyMemNBT.hasKey("CustomName") && !middlePartyMemNBT.getString("CustomName").isEmpty()) ? middlePartyMemNBT.getString("CustomName") : creatureType.getTranslatedName();
            int partyMemNameWidth = fontRenderer.getStringWidth(partyMemName);
            int partyMemNameHeight = fontRenderer.FONT_HEIGHT;
            int partyMemNameX = (int) (((xSize - (partyMemNameWidth * textScale)) / 2 - 170) / textScale);
            int partyMemNameY = (int) (((ySize - (partyMemNameHeight * textScale)) / 2 + 102) / textScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(textScale, textScale, textScale);
            fontRenderer.drawString(partyMemName, partyMemNameX, partyMemNameY, 0xFFFFFF);
            GlStateManager.popMatrix();

            //render level
            String levelName = I18n.format("tametrait.level", middlePartyMemNBT.getInteger("Level"));
            int levelNameWidth = fontRenderer.getStringWidth(levelName);
            int levelNameHeight = fontRenderer.FONT_HEIGHT;
            int levelNameX = (int) (((xSize - (levelNameWidth * textScale)) / 2 - 170) / textScale);
            int levelNameY = (int) (((ySize - (levelNameHeight * textScale)) / 2 + 107) / textScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(textScale, textScale, textScale);
            fontRenderer.drawString(levelName, levelNameX, levelNameY, 0xFFFFFF);
            GlStateManager.popMatrix();

            //common stuff for bars
            int barWidth = 50;
            int barHeight = 1;

            //render health bar
            float health = this.getCreatureHealthFromNBT(middlePartyMemNBT)[0];
            float maxHealth = this.getCreatureHealthFromNBT(middlePartyMemNBT)[1];
            float healthPercentage = health / maxHealth;
            int healthBarX = (int) ((xSize - barWidth) / 2 - 170);
            int healthBarY = (int) ((ySize - barHeight) / 2 + 110);

            //health bar bg
            this.drawRectOutline(healthBarX, healthBarY, barWidth, barHeight, 0xFF868686);
            //health bar
            if (healthPercentage > 0)
                this.drawRectOutline(healthBarX, healthBarY, (int) (barWidth * healthPercentage), barHeight, 0xFFFF0000);

            //render energy bar
            int energy = middlePartyMemNBT.getInteger("Energy");
            int maxEnergy = RiftConfigHandler.getConfig(creatureType).stats.maxEnergy;
            float energyPercentage = (float) energy / maxEnergy;
            int energyBarX = (int) ((xSize - barWidth) / 2 - 170);
            int energyBarY = (int) ((ySize - barHeight) / 2 + 113);

            //energy bar bg
            this.drawRectOutline(energyBarX, energyBarY, barWidth, barHeight, 0xFF868686);
            //energy bar
            if (energyPercentage > 0)
                this.drawRectOutline(energyBarX, energyBarY, (int) (barWidth * energyPercentage), barHeight, 0xFFFFFF00);

            //render experience bar
            int experience = middlePartyMemNBT.getInteger("XP");
            int maxExperience = creatureType.getMaxXP(middlePartyMemNBT.getInteger("Level"));
            float experiencePercentage = (float) experience / maxExperience;
            int experienceBarX = (int) ((xSize - barWidth) / 2 - 170);
            int experienceBarY = (int) ((ySize - barHeight) / 2 + 116);

            //experience bar bg
            this.drawRectOutline(experienceBarX, experienceBarY, barWidth, barHeight, 0xFF868686);
            //experience bar
            if (experiencePercentage > 0)
                this.drawRectOutline(experienceBarX, experienceBarY, (int) (barWidth * experiencePercentage), barHeight, 0xFF98D06B);
        }
    }

    private void drawRectOutline(int x, int y, int w, int h, int color) {
        drawRect(x, y, x + w, y + 1, color);             // top
        drawRect(x, y + h - 1, x + w, y + h, color);     // bottom
        drawRect(x, y, x + 1, y + h, color);             // left
        drawRect(x + w - 1, y, x + w, y + h, color);     // right
    }

    private void drawTranslucentBackground(int xSize, int ySize) {
        int width = 110;
        int height = 40;
        int x = (xSize - width) / 2 - 170;
        int y = (ySize - height) / 2 + 100;
        //drawRoundedRect(x, y, width, height, 6, 0xC0000000, 8);
        Gui.drawRect(x, y, x + width, y + height, 0xC0000000);
    }


    private void renderBG(NBTTagCompound partyMemNBT, int xSize, int ySize, float scale, float creatureIconScale, int xOffset, int yOffset) {
        //render background
        Minecraft.getMinecraft().getTextureManager().bindTexture(hud);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int k = (int) (((xSize - (38 * scale)) / 2f - 170 + xOffset) / scale);
        int l = (int) (((ySize - (38 * scale)) / 2f + 80 + yOffset) / scale);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        RiftUtil.drawTexturedModalRect(k, l, 0, 29, 38, 38);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();

        //render creature icon
        if (!partyMemNBT.isEmpty()) {
            RiftCreatureType creatureType = RiftCreatureType.values()[partyMemNBT.getByte("CreatureType")];
            ResourceLocation creatureIcon = new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+creatureType.name().toLowerCase()+"_icon.png");
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(creatureIcon);
            int iconXPos = (int) (((xSize - (24 * creatureIconScale)) / 2f - 170 + xOffset) / creatureIconScale);
            int iconYPos = (int) (((ySize - (24 * creatureIconScale)) / 2f + 80 + yOffset) / creatureIconScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(creatureIconScale, creatureIconScale, creatureIconScale);
            Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 24, 24, 24, 24);
            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
        }
    }

    private void renderControlTextureBG(KeyBinding keyBinding, int xSize, int ySize, float scale, int xOffset, int yOffset) {
        //show icon
        Minecraft.getMinecraft().getTextureManager().bindTexture(genericButtonIcon);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int k = (int) (((xSize - (16 * scale)) / 2f - 170 + xOffset) / scale);
        int l = (int) (((ySize - (16 * scale)) / 2f + 80 + yOffset) / scale);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        Gui.drawModalRectWithCustomSizedTexture(k, l, 0, 0, 16, 16, 16, 16);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();

        //show control key
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        float controlKeyTextScale = 0.5f;
        String controlKey = RiftControls.getStringFromKeyBinding(keyBinding);
        int controlKeyWidth = fontRenderer.getStringWidth(controlKey);
        int controlPosX = (int) (((xSize - (controlKeyWidth * controlKeyTextScale)) / 2f - 170 + xOffset) / controlKeyTextScale);
        int controlPosY = (int) (((ySize - (fontRenderer.FONT_HEIGHT * controlKeyTextScale)) / 2f + 80 + yOffset) / controlKeyTextScale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(controlKeyTextScale, controlKeyTextScale, controlKeyTextScale);
        fontRenderer.drawString(controlKey, controlPosX, controlPosY, 0x000000);
        GlStateManager.popMatrix();
    }

    //0 is for health, 1 is for max health
    private float[] getCreatureHealthFromNBT(NBTTagCompound creatureNBT) {
        if (creatureNBT == null || creatureNBT.isEmpty()) return new float[]{0f, 0f};
        float health = creatureNBT.getFloat("Health");
        float maxHealth = health;
        for (NBTBase nbtBase: creatureNBT.getTagList("Attributes", 10).tagList) {
            if (nbtBase instanceof NBTTagCompound) {
                NBTTagCompound tagCompound = (NBTTagCompound) nbtBase;

                if (!tagCompound.hasKey("Name") || !tagCompound.getString("Name").equals("generic.maxHealth")) continue;

                maxHealth = (float) tagCompound.getDouble("Base");
            }
        }
        return new float[]{health, maxHealth};
    }

    //deal with changing overlay position from pressing left or right
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void changeSelectedPartyPos(InputEvent.KeyInputEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (RiftControls.switchLeftwards.isKeyDown()) {
            this.selectedPos = this.selectedPos - 1 < 0 ? NewPlayerTamedCreaturesHelper.maxPartySize - 1 : this.selectedPos - 1;
            NewPlayerTamedCreaturesHelper.setSelectedPartyPosFromOverlay(player, this.selectedPos);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        if (RiftControls.switchRightwards.isKeyDown()) {
            this.selectedPos = this.selectedPos + 1 >= NewPlayerTamedCreaturesHelper.maxPartySize ? 0 : this.selectedPos + 1;
            NewPlayerTamedCreaturesHelper.setSelectedPartyPosFromOverlay(player, this.selectedPos);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
