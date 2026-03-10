package anightdazingzoroark.prift.client.overlay;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftControls;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyHelper;
import anightdazingzoroark.prift.server.properties.playerParty.PlayerPartyProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.HashMap;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;
import static net.minecraft.client.gui.Gui.drawRect;

public class RiftPartyMembersOverlay {
    private static final ResourceLocation hud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private PlayerPartyProperties playerParty;
    //this is needed to initialize the party when the player enters the world
    private boolean partyDeployedInitialized;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayer player = Minecraft.getMinecraft().player;

        //define player party
        this.playerParty = PlayerPartyHelper.getPlayerParty(player);

        //block if the player party isn't defined yet
        if (this.playerParty == null) return;

        //initialize PlayerPartyHelper.deployedCreatures here
        if (!this.partyDeployedInitialized) {
            for (int index = 0; index < PlayerPartyHelper.maxSize; index++) {
                CreatureNBT creatureNBT = this.playerParty.getPartyMember(index);
                if (creatureNBT.nbtIsEmpty()) continue;
                if (creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                    RiftCreature correspondingCreature = creatureNBT.findCorrespondingCreature(Minecraft.getMinecraft().world);
                    if (correspondingCreature == null) continue;
                    PlayerPartyHelper.deployedCreatures.put(index, correspondingCreature);
                }
            }

            this.partyDeployedInitialized = true;
        }

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            ScaledResolution resolution = event.getResolution();
            FixedSizeList<CreatureNBT> partyNBT = this.playerParty.getPlayerParty();
            this.renderHUD(partyNBT, resolution.getScaledWidth(), resolution.getScaledHeight());
        }
    }

    private void renderHUD(FixedSizeList<CreatureNBT> partyNBT, int xSize, int ySize) {
        //define middle pos and middle creature nbt
        int middlePos = this.playerParty.getQuickSelectPos();
        CreatureNBT middlePartyMemNBT = PlayerPartyHelper.deployedCreatures.containsKey(middlePos) ?
                new CreatureNBT(PlayerPartyHelper.deployedCreatures.get(middlePos)) : partyNBT.get(middlePos);

        //black transparent background and middle creature info
        this.drawSelectedInfo(middlePartyMemNBT, xSize, ySize);

        //up arrow
        this.drawArrow(xSize, ySize, true);

        //render up
        int leftPos = this.playerParty.getPrevQuickSelectPos();
        CreatureNBT leftPartyMemNBT = PlayerPartyHelper.deployedCreatures.containsKey(leftPos) ?
                new CreatureNBT(PlayerPartyHelper.deployedCreatures.get(leftPos)) : partyNBT.get(leftPos);
        this.renderPartySlot(leftPartyMemNBT, xSize, ySize, 0.5f, 0.5f, -30);

        //render middle
        this.renderPartySlot(middlePartyMemNBT, xSize, ySize, 0.75f, 0.75f, 0);

        //render down
        int rightPos = this.playerParty.getNextQuickSelectPos();
        CreatureNBT rightPartyMemNBT = PlayerPartyHelper.deployedCreatures.containsKey(rightPos) ?
                new CreatureNBT(PlayerPartyHelper.deployedCreatures.get(rightPos)) : partyNBT.get(rightPos);
        this.renderPartySlot(rightPartyMemNBT, xSize, ySize, 0.5f, 0.5f, 30);

        //down arrow
        this.drawArrow(xSize, ySize, false);
    }

    private void drawSelectedInfo(CreatureNBT partyMemNBT, int xSize, int ySize) {
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

        if (!partyMemNBT.nbtIsEmpty()) {
            //some common values
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

            //name and level
            float textScale = 0.5f;
            String partyMemName = partyMemNBT.getCreatureName(true);
            float partyMemNameHeight = fontRenderer.FONT_HEIGHT * textScale;
            float unscaledPartyMemNameX = xSize / 2f + xPosOnScreen + 5;
            float unscaledPartyMemNameY = (ySize - partyMemNameHeight) / 2f + yPosOnScreen - 5;
            int partyMemNameX = (int) (Math.max(1 - textScale, unscaledPartyMemNameX) / textScale);
            int partyMemNameY = (int) (Math.max(1 - textScale, unscaledPartyMemNameY) / textScale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(textScale, textScale, textScale);
            fontRenderer.drawString(partyMemName, partyMemNameX, partyMemNameY, 0xFFFFFF);
            GlStateManager.popMatrix();

            //common stuff for bars
            int barWidth = 50;
            int barHeight = 1;

            //health bar
            float healthPercentage = Math.round(partyMemNBT.getCreatureHealth()[0] / partyMemNBT.getCreatureHealth()[1] * 10) / 10f;
            int healthBarX = (int) (xSize / 2 + xPosOnScreen + 5);
            int healthBarY = (int) ((ySize - barHeight) / 2 + yPosOnScreen);

            //health bar bg
            this.drawRectOutline(healthBarX, healthBarY, barWidth, barHeight, 0xFF868686);
            //health bar
            if (healthPercentage > 0)
                this.drawRectOutline(healthBarX, healthBarY, (int) (barWidth * healthPercentage), barHeight, 0xFFFF0000);

            //render energy bar
            float energyPercentage = Math.round((float) partyMemNBT.getCreatureEnergy()[0] / partyMemNBT.getCreatureEnergy()[1] * 10) / 10f;
            int energyBarX = (int) (xSize / 2 + xPosOnScreen + 5);
            int energyBarY = (int) ((ySize - barHeight) / 2 + yPosOnScreen + 4);

            //energy bar bg
            this.drawRectOutline(energyBarX, energyBarY, barWidth, barHeight, 0xFF868686);
            //energy bar
            if (energyPercentage > 0)
                this.drawRectOutline(energyBarX, energyBarY, (int) (barWidth * energyPercentage), barHeight, 0xFFFFFF00);

            //xp bar
            float experiencePercentage = Math.round((float) partyMemNBT.getCreatureXP()[0] / partyMemNBT.getCreatureXP()[1] * 10) / 10f;
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

    private void renderPartySlot(CreatureNBT partyMemNBT, int xSize, int ySize, float bgScale, float creatureIconScale, int yOffset) {
        //common stuff
        int maxOffsetLeft = (int)((xSize - (bgScale * 38)) / 2f - ((1 - bgScale) * 19));
        int xPosOnScreen = Math.max(-maxOffsetLeft, -210);
        int yPosOnScreen = -20;
        float health = partyMemNBT.getCreatureHealth()[0];
        PlayerTamedCreatures.DeploymentType deploymentType = partyMemNBT.getDeploymentType();

        //render background
        Minecraft.getMinecraft().getTextureManager().bindTexture(hud);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        float unscaledBGX = (xSize - (38 * bgScale)) / 2f + xPosOnScreen;
        float unscaledBGY = (ySize - (38 * bgScale)) / 2f + yPosOnScreen + yOffset;
        int k = (int) (Math.max((1 - bgScale) * 19, unscaledBGX) / bgScale);
        int l = (int) (Math.max(0, unscaledBGY) / bgScale);
        int xUv = (!partyMemNBT.nbtIsEmpty() && health == 0) ? 76 : (
                    !partyMemNBT.nbtIsEmpty() && deploymentType == PlayerTamedCreatures.DeploymentType.PARTY ? 38 : 0
                );
        GlStateManager.pushMatrix();
        GlStateManager.scale(bgScale, bgScale, bgScale);
        Gui.drawModalRectWithCustomSizedTexture(k, l, xUv, 29, 38, 38, 256, 256);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();

        if (!partyMemNBT.nbtIsEmpty()) {
            //render creature icon
            RiftCreatureType creatureType = partyMemNBT.getCreatureType();
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

            //render the pacifier for babies
            if (partyMemNBT.isBaby()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Minecraft.getMinecraft().getTextureManager().bindTexture(hud);
                float pacifierScale = (1 / 3f) * creatureIconScale;
                int pacifierX = (int) ((xSize - 22) / (2 * pacifierScale) + (xPosOnScreen + 15) / pacifierScale);
                int pacifierY = (int) ((ySize - 22) / (2 * pacifierScale) + (yPosOnScreen + yOffset + 15) / pacifierScale);
                GlStateManager.pushMatrix();
                GlStateManager.scale(pacifierScale, pacifierScale, pacifierScale);
                drawModalRectWithCustomSizedTexture(pacifierX, pacifierY, 114, 29, 22, 22, 256, 256);
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawRectOutline(int x, int y, int w, int h, int color) {
        drawRect(x, y, x + w, y + 1, color);             // top
        drawRect(x, y + h - 1, x + w, y + h, color);     // bottom
        drawRect(x, y, x + 1, y + h, color);             // left
        drawRect(x + w - 1, y, x + w, y + h, color);     // right
    }

    //deal with changing overlay position from pressing left or right
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void changeSelectedPartyPos(InputEvent.KeyInputEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (RiftControls.switchUpwards.isKeyDown()) {
            PlayerPartyHelper.changeQuickSelectPosClient(player, true);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        if (RiftControls.switchDownwards.isKeyDown()) {
            PlayerPartyHelper.changeQuickSelectPosClient(player, false);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        if (RiftControls.quickSummonAndDismiss.isKeyDown()) {
            CreatureNBT partyMemNBT = this.playerParty.getPlayerParty().get(this.playerParty.getQuickSelectPos());
            PlayerTamedCreatures.DeploymentType deploymentType = partyMemNBT.getDeploymentType();

            //for dismissing, when creature is deployed
            if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY) {
                PlayerPartyHelper.deployCreatureClient(player, this.playerParty.getQuickSelectPos(), false);
                player.sendStatusMessage(new TextComponentTranslation("party.warning.dismiss_success"), false);
            }
            //for summoning, when creature is dismissed
            else if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                //dont summon when creature is dead
                float health = partyMemNBT.getCreatureHealth()[0];
                if (health <= 0) {
                    player.sendStatusMessage(new TextComponentTranslation("party.warning.cannot_summon_dead"), false);
                }
                //dont summon when player not in apt position
                else if (!this.playerParty.canDeployPartyMember(this.playerParty.getQuickSelectPos())) {
                    player.sendStatusMessage(new TextComponentTranslation("party.warning.cannot_summon"), false);
                }
                else {
                    PlayerPartyHelper.deployCreatureClient(player, this.playerParty.getQuickSelectPos(), true);
                    player.sendStatusMessage(new TextComponentTranslation("party.warning.summon_success"), false);
                }
            }

            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
