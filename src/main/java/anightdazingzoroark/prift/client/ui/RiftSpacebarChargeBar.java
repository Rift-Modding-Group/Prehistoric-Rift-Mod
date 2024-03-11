package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RiftSpacebarChargeBar {
    private static final ResourceLocation chargeBarHud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final int textureXSize = 182;
    private static final int textureYSize = 5;
    private int fill = 0;
    private boolean spacebarUsed = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Entity entity = player.getRidingEntity();
        GameSettings settings = Minecraft.getMinecraft().gameSettings;

        if (entity instanceof RiftCreature) {
            if (((RiftCreature)entity).hasSpacebarChargeBar()) {
                if (event.getType() == RenderGameOverlayEvent.ElementType.AIR) {
                    RiftCreature creature = (RiftCreature) entity;
                    ScaledResolution resolution = event.getResolution();

                    Minecraft.getMinecraft().getTextureManager().bindTexture(chargeBarHud);
                    renderSpacebarChargeHud(creature, resolution.getScaledWidth(), resolution.getScaledHeight());
                    reduceUnusedChargeBar(creature, creature.isUsingSpacebar());
                    Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
                }
            }
        }
    }

    @SubscribeEvent
    public static void overlayEvent(RenderGameOverlayEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        World world = player.world;
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;
        if (world != null) {
            if (player.getRidingEntity() instanceof RiftCreature) event.setCanceled(true);
        }
    }

    private void reduceUnusedChargeBar(RiftCreature creature, boolean usingSpacebar) {
        if (usingSpacebar) fill = creature.getSpacebarUse();
        else fill = creature.getSpacebarCooldown() / 2;
    }

    private void renderSpacebarChargeHud(RiftCreature creature, int xSize, int ySize) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int left = xSize / 2 - 91;
        int top = ySize - 32 + (creature.hasRightClickChargeBar() ? 5 : 3);
        float fillUpBar = (float)textureXSize / 20f * fill;
        RiftUtil.drawTexturedModalRect(left, top, 0, 19, textureXSize, textureYSize);
        RiftUtil.drawTexturedModalRect(left, top, 0, 24, Math.min((int)fillUpBar, textureXSize), textureYSize);
        GlStateManager.disableBlend();
    }
}
