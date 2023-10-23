package anightdazingzoroark.rift.client.ui;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RiftMountEnergyBar {
    private static final ResourceLocation energyHud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");

    private static final int posX = 0;
    private static final int posY = 0;
    private static final int textureXSize = 9;
    private static final int textureYSize = 9;

    @SubscribeEvent
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Entity entity = player.getRidingEntity();

        if (entity instanceof RiftCreature) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.AIR) {
                RiftCreature creature = (RiftCreature) entity;
                int energy = creature.getEnergy();
                ScaledResolution resolution = event.getResolution();

                Minecraft.getMinecraft().getTextureManager().bindTexture(energyHud);
                renderEnergy(resolution.getScaledWidth(), resolution.getScaledHeight(), energy);
                Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
                GuiIngameForge.right_height += 10;
            }
        }
    }

    private void renderEnergy(int xSize, int ySize, int energy) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int left = xSize / 2 + 82;
        int top = ySize - GuiIngameForge.right_height;

        for (int i = 0; i < 10; i++) {
            int halfIcon = i * 2 + 1;

            RiftUtil.drawTexturedModalRect(left - i * 8, top, 0, 0, textureXSize, textureYSize);

            if (halfIcon < energy) {
                RiftUtil.drawTexturedModalRect(left - i * 8, top, 9, posY, textureXSize, textureYSize);
            }
            else if (halfIcon == energy) {
                RiftUtil.drawTexturedModalRect(left - i * 8, top, 18, posY, textureXSize, textureYSize);
            }
        }
        GlStateManager.disableBlend();
    }
}
