package anightdazingzoroark.prift.client.overlay;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.RiftControls;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

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
        //render left
        int leftPos = this.selectedPos - 1 < 0 ? NewPlayerTamedCreaturesHelper.maxPartySize - 1 : this.selectedPos - 1;
        NBTTagCompound leftPartyMemNBT = partyNBT.get(leftPos);
        this.renderBG(leftPartyMemNBT, xSize, ySize, 0.5f, 0.5f, -35, 0);
        this.renderControlTextureBG(RiftControls.switchLeftwards, xSize, ySize, 0.75f, -35, 25);

        //render middle
        NBTTagCompound middlePartyMemNBT = partyNBT.get(this.selectedPos);
        this.renderBG(middlePartyMemNBT, xSize, ySize, 1f, 1f, 0, 0);

        //render right
        int rightPos = this.selectedPos + 1 >= NewPlayerTamedCreaturesHelper.maxPartySize ? 0 : this.selectedPos + 1;
        NBTTagCompound rightPartyMemNBT = partyNBT.get(rightPos);
        this.renderBG(rightPartyMemNBT, xSize, ySize, 0.5f, 0.5f, 35, 0);
        this.renderControlTextureBG(RiftControls.switchRightwards, xSize, ySize, 0.75f, 35, 25);
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
