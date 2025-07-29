package anightdazingzoroark.prift.client.overlay;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.NewPlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import static net.minecraft.client.gui.Gui.drawRect;

public class RiftPartyMembersOverlay {
    private static final ResourceLocation hud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
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
        NBTTagCompound middlePartyMemNBT = partyNBT.get(this.selectedPos);

        //black transparent background and middle creature info
        this.drawSelectedInfo(middlePartyMemNBT, xSize, ySize);

        //up arrow
        this.drawArrow(xSize, ySize, true);

        //render up
        int upPos = this.selectedPos - 1 < 0 ? NewPlayerTamedCreaturesHelper.maxPartySize - 1 : this.selectedPos - 1;
        NBTTagCompound leftPartyMemNBT = partyNBT.get(upPos);
        this.renderPartySlot(leftPartyMemNBT, xSize, ySize, 0.5f, 0.5f, -30);

        //render middle
        this.renderPartySlot(middlePartyMemNBT, xSize, ySize, 0.75f, 0.75f, 0);

        //render down
        int downPos = this.selectedPos + 1 >= NewPlayerTamedCreaturesHelper.maxPartySize ? 0 : this.selectedPos + 1;
        NBTTagCompound rightPartyMemNBT = partyNBT.get(downPos);
        this.renderPartySlot(rightPartyMemNBT, xSize, ySize, 0.5f, 0.5f, 30);

        //down arrow
        this.drawArrow(xSize, ySize, false);
    }

    private void drawSelectedInfo(NBTTagCompound partyMemNBT, int xSize, int ySize) {
        //common values
        int boxWidth = 70;
        int boxHeight = 25;
        int maxOffsetLeft = (int)((xSize - boxWidth) / 2f + 5);
        int xPosOnScreen = Math.max(-maxOffsetLeft, -200);
        int yPosOnScreen = -20;

        //draw
        int x = Math.max(0, xSize) / 2 + xPosOnScreen;
        int y = (Math.max(0, ySize - boxHeight)) / 2 + yPosOnScreen;
        Gui.drawRect(x, y, x + boxWidth, y + boxHeight, 0xC0000000);

        if (!partyMemNBT.isEmpty()) {
            //some common values
            RiftCreatureType creatureType = RiftCreatureType.values()[partyMemNBT.getByte("CreatureType")];
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

            //name and level
            float textScale = 0.5f;
            String partyMemName = (partyMemNBT.hasKey("CustomName") && !partyMemNBT.getString("CustomName").isEmpty()) ? partyMemNBT.getString("CustomName") : creatureType.getTranslatedName();
            int partyMemLevel = partyMemNBT.getInteger("Level");
            float partyMemNameHeight = fontRenderer.FONT_HEIGHT * textScale;
            float unscaledPartyMemNameX = xSize / 2f + xPosOnScreen + 5;
            float unscaledPartyMemNameY = (ySize - partyMemNameHeight) / 2f + yPosOnScreen - 5;
            int partyMemNameX = (int) (Math.max(1 - textScale, unscaledPartyMemNameX) / textScale);
            int partyMemNameY = (int) (Math.max(1 - textScale, unscaledPartyMemNameY) / textScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(textScale, textScale, textScale);
            fontRenderer.drawString(I18n.format("journal.party_member.name", partyMemName, partyMemLevel), partyMemNameX, partyMemNameY, 0xFFFFFF);
            GlStateManager.popMatrix();

            //common stuff for bars
            int barWidth = 50;
            int barHeight = 1;

            //health bar
            float health = this.getCreatureHealthFromNBT(partyMemNBT)[0];
            float maxHealth = this.getCreatureHealthFromNBT(partyMemNBT)[1];
            float healthPercentage = health / maxHealth;
            int healthBarX = (int) (xSize / 2 + xPosOnScreen + 5);
            int healthBarY = (int) ((ySize - barHeight) / 2 + yPosOnScreen);

            //health bar bg
            this.drawRectOutline(healthBarX, healthBarY, barWidth, barHeight, 0xFF868686);
            //health bar
            if (healthPercentage > 0)
                this.drawRectOutline(healthBarX, healthBarY, (int) (barWidth * healthPercentage), barHeight, 0xFFFF0000);

            //render energy bar
            int energy = partyMemNBT.getInteger("Energy");
            int maxEnergy = RiftConfigHandler.getConfig(creatureType).stats.maxEnergy;
            float energyPercentage = (float) energy / maxEnergy;
            int energyBarX = (int) (xSize / 2 + xPosOnScreen + 5);
            int energyBarY = (int) ((ySize - barHeight) / 2 + yPosOnScreen + 4);

            //energy bar bg
            this.drawRectOutline(energyBarX, energyBarY, barWidth, barHeight, 0xFF868686);
            //energy bar
            if (energyPercentage > 0)
                this.drawRectOutline(energyBarX, energyBarY, (int) (barWidth * energyPercentage), barHeight, 0xFFFFFF00);

            //xp bar
            int experience = partyMemNBT.getInteger("XP");
            int maxExperience = creatureType.getMaxXP(partyMemNBT.getInteger("Level"));
            float experiencePercentage = (float) experience / maxExperience;
            int experienceBarX = (int) (xSize / 2 + xPosOnScreen + 5);
            int experienceBarY = (int) ((ySize - barHeight) / 2 + yPosOnScreen + 8);

            //experience bar bg
            this.drawRectOutline(experienceBarX, experienceBarY, barWidth, barHeight, 0xFF868686);
            //experience bar
            if (experiencePercentage > 0)
                this.drawRectOutline(experienceBarX, experienceBarY, (int) (barWidth * experiencePercentage), barHeight, 0xFF98D06B);
        }
    }

    private void drawArrow(int xSize, int ySize, boolean isUp) {
        int maxOffsetLeft = (int)((xSize - 32) / 2f - 42);
        int xPosOnScreen = Math.max(-maxOffsetLeft, -168);
        int yPosOnScreen = -20 + (isUp ? -20 : 20);

        Minecraft.getMinecraft().getTextureManager().bindTexture(hud);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);

        float unscaledBGX = (xSize - 32) / 2f + xPosOnScreen;
        float unscaledBGY = (ySize - 8) / 2f + yPosOnScreen;

        int k = (int) Math.max(0, unscaledBGX);
        int l = (int) Math.max(0, unscaledBGY);
        int xUV = isUp ? 0 : 32;

        Gui.drawModalRectWithCustomSizedTexture(k, l, xUV, 67, 32, 8, 256, 256);
        GlStateManager.disableBlend();
    }

    private void renderPartySlot(NBTTagCompound partyMemNBT, int xSize, int ySize, float bgScale, float creatureIconScale, int yOffset) {
        //common stuff
        int maxOffsetLeft = (int)((xSize - (bgScale * 38)) / 2f - ((1 - bgScale) * 19));
        int xPosOnScreen = Math.max(-maxOffsetLeft, -210);
        int yPosOnScreen = -20;
        float health = this.getCreatureHealthFromNBT(partyMemNBT)[0];
        PlayerTamedCreatures.DeploymentType deploymentType = PlayerTamedCreatures.DeploymentType.values()[partyMemNBT.getByte("DeploymentType")];

        //render background
        Minecraft.getMinecraft().getTextureManager().bindTexture(hud);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        float unscaledBGX = (xSize - (38 * bgScale)) / 2f + xPosOnScreen;
        float unscaledBGY = (ySize - (38 * bgScale)) / 2f + yPosOnScreen + yOffset;
        int k = (int) (Math.max((1 - bgScale) * 19, unscaledBGX) / bgScale);
        int l = (int) (Math.max(0, unscaledBGY) / bgScale);
        int xUv = (!partyMemNBT.isEmpty() && health == 0) ? 76 : (
                    !partyMemNBT.isEmpty() && deploymentType == PlayerTamedCreatures.DeploymentType.PARTY ? 38 : 0
                );
        GlStateManager.pushMatrix();
        GlStateManager.scale(bgScale, bgScale, bgScale);
        Gui.drawModalRectWithCustomSizedTexture(k, l, xUv, 29, 38, 38, 256, 256);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();

        //render creature icon
        if (!partyMemNBT.isEmpty()) {
            RiftCreatureType creatureType = RiftCreatureType.values()[partyMemNBT.getByte("CreatureType")];
            ResourceLocation creatureIcon = new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+creatureType.name().toLowerCase()+"_icon.png");
            Minecraft.getMinecraft().getTextureManager().bindTexture(creatureIcon);
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            float unscaledIconX = (xSize - (24 * creatureIconScale)) / 2f + xPosOnScreen;
            float unscaledIconY = (ySize - (24 * creatureIconScale)) / 2f + yPosOnScreen + yOffset;
            int iconXPos = (int) (Math.max((1 - creatureIconScale) * 12, unscaledIconX) / creatureIconScale);
            int iconYPos = (int) (Math.max(0, unscaledIconY) / creatureIconScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(creatureIconScale, creatureIconScale, creatureIconScale);
            Gui.drawModalRectWithCustomSizedTexture(iconXPos, iconYPos, 0, 0, 24, 24, 24, 24);
            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
        }
    }

    private void drawRectOutline(int x, int y, int w, int h, int color) {
        drawRect(x, y, x + w, y + 1, color);             // top
        drawRect(x, y + h - 1, x + w, y + h, color);     // bottom
        drawRect(x, y, x + 1, y + h, color);             // left
        drawRect(x + w - 1, y, x + w, y + h, color);     // right
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
        if (RiftControls.switchUpwards.isKeyDown()) {
            this.selectedPos = this.selectedPos - 1 < 0 ? NewPlayerTamedCreaturesHelper.maxPartySize - 1 : this.selectedPos - 1;
            NewPlayerTamedCreaturesHelper.setSelectedPartyPosFromOverlay(player, this.selectedPos);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        if (RiftControls.switchDownwards.isKeyDown()) {
            this.selectedPos = this.selectedPos + 1 >= NewPlayerTamedCreaturesHelper.maxPartySize ? 0 : this.selectedPos + 1;
            NewPlayerTamedCreaturesHelper.setSelectedPartyPosFromOverlay(player, this.selectedPos);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        if (RiftControls.quickSummonAndDismiss.isKeyDown() && this.selectedPos >= 0) {
            NBTTagCompound partyMemNBT = NewPlayerTamedCreaturesHelper.getPlayerPartyNBT(player).get(this.selectedPos);
            PlayerTamedCreatures.DeploymentType deploymentType = PlayerTamedCreatures.DeploymentType.values()[partyMemNBT.getByte("DeploymentType")];

            //for dismissing, when creature is deployed
            if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY) {
                NewPlayerTamedCreaturesHelper.deployCreatureFromParty(player, this.selectedPos, false);
                NewPlayerTamedCreaturesHelper.forceSyncPartyNBT(player);
                player.sendStatusMessage(new TextComponentTranslation("journal.warning.dismiss_success"), false);
            }
            //for summoning, when creature is dismissed
            else if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                //dont summon when creature is dead
                float health = this.getCreatureHealthFromNBT(partyMemNBT)[0];
                if (health <= 0) {
                    player.sendStatusMessage(new TextComponentTranslation("journal.warning.cannot_summon_dead"), false);
                }
                //dont summon when player not in apt position
                else if (!NewPlayerTamedCreaturesHelper.canBeDeployed(player, this.selectedPos)) {
                    player.sendStatusMessage(new TextComponentTranslation("journal.warning.cannot_summon"), false);
                }
                else {
                    NewPlayerTamedCreaturesHelper.deployCreatureFromParty(player, this.selectedPos, true);
                    player.sendStatusMessage(new TextComponentTranslation("journal.warning.summon_success"), false);
                }
            }

            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
