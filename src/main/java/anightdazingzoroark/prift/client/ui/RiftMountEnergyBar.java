package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;

public class RiftMountEnergyBar {
    private static final ResourceLocation energyHud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");

    private static final int posX = 0;
    private static final int posY = 0;
    private static final int textureXSize = 9;
    private static final int textureYSize = 9;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Entity entity = player.getRidingEntity();

        if (entity instanceof RiftCreature) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                RiftCreature creature = (RiftCreature) entity;
                int energy = creature.getEnergy();
                ScaledResolution resolution = event.getResolution();

                Minecraft.getMinecraft().getTextureManager().bindTexture(energyHud);
                renderEnergy(creature, resolution.getScaledWidth(), resolution.getScaledHeight(), energy);
                Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
                GuiIngameForge.right_height += 10;
            }
        }
    }

    private void renderEnergy(RiftCreature creature, int xSize, int ySize, int energy) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int left = xSize / 2 + 82;
        int top = ySize - GuiIngameForge.right_height;

        int renderableEnergy = Math.min(creature.getMaxEnergy(), 60) / 2;
        for (int i = 0; i < renderableEnergy; i++) {
            int halfIcon = i * 2 + 1;

            float xOffset = left - (i % 10 * 8);
            float yOffset = top - ((float)Math.floor(i / 10f) * 8) - (4f * (float) Math.floor(i / 10f));

            RiftUtil.drawTexturedModalRect(xOffset, yOffset, 0, 0, textureXSize, textureYSize);

            if (halfIcon < energy) {
                RiftUtil.drawTexturedModalRect(xOffset, yOffset, 9, posY, textureXSize, textureYSize);
            }
            else if (halfIcon == energy) {
                RiftUtil.drawTexturedModalRect(xOffset, yOffset, 18, posY, textureXSize, textureYSize);
            }
        }
        GlStateManager.disableBlend();
    }
}
