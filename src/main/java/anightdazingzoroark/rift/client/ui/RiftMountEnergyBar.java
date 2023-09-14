package anightdazingzoroark.rift.client.ui;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class RiftMountEnergyBar {
    private static final ResourceLocation energyHud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/energy.png");

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

            drawTexturedModalRect(left - i * 8, top, 0, 0, textureXSize, textureYSize);

            if (halfIcon < energy) {
                drawTexturedModalRect(left - i * 8, top, 9, posY, textureXSize, textureYSize);
            }
            else if (halfIcon == energy) {
                drawTexturedModalRect(left - i * 8, top, 18, posY, textureXSize, textureYSize);
            }
        }
        GlStateManager.disableBlend();
    }

    private void drawTexturedModalRect(float x, float y, int texX, int texY, int width, int height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        double z = 0.0D;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, z).tex((texX * f), (texY + height) * f1).endVertex();
        bufferbuilder.pos((x + width), y + height, z).tex((texX + width) * f, (texY + height) * f1).endVertex();
        bufferbuilder.pos((x + width), y, z).tex((texX + width) * f,(texY * f1)).endVertex();
        bufferbuilder.pos(x, y, z).tex((texX * f), (texY * f1)).endVertex();
        tessellator.draw();
    }
}
