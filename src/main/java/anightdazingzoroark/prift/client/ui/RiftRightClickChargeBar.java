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
public class RiftRightClickChargeBar {
    private static final ResourceLocation chargeBarHud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final int textureXSize = 182;
    private static final int textureYSize = 5;
    private int fill = 0;
    private boolean mouseUsed = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Entity entity = player.getRidingEntity();
        GameSettings settings = Minecraft.getMinecraft().gameSettings;

        if (entity instanceof RiftCreature) {
            if (((RiftCreature)entity).hasRightClickChargeBar()) {
                if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                    RiftCreature creature = (RiftCreature) entity;
                    ScaledResolution resolution = event.getResolution();

                    Minecraft.getMinecraft().getTextureManager().bindTexture(chargeBarHud);
                    renderRightClickChargeHud(creature, resolution.getScaledWidth(), resolution.getScaledHeight());
                    reduceUnusedChargeBar(creature, creature.alwaysShowRightClickUse() ? creature.getRightClickCooldown() == 0 : creature.isUsingRightClick());
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

    private void reduceUnusedChargeBar(RiftCreature creature, boolean usingRightClick) {
        if (usingRightClick) this.fill = creature.getRightClickUse();
        else this.fill = creature.getRightClickCooldown() / 2;
    }

    private void renderRightClickChargeHud(RiftCreature creature, int xSize, int ySize) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int left = xSize / 2 - 91;
        int top = ySize - 32 + (creature.hasLeftClickChargeBar() || creature.hasSpacebarChargeBar() ? 1 : 3);
        float fillUpBar = (float)textureXSize / creature.maxRightClickCooldown * this.fill;
        RiftUtil.drawTexturedModalRect(left, top, 0, 9, textureXSize, textureYSize);
        RiftUtil.drawTexturedModalRect(left, top, 0, 14, Math.min((int)fillUpBar, textureXSize), textureYSize);
        GlStateManager.disableBlend();
    }
}
