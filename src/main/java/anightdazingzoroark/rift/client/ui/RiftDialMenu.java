package anightdazingzoroark.rift.client.ui;

import anightdazingzoroark.rift.client.ClientProxy;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RiftDialMenu extends GuiScreen {
    private int selectedItem = -1;

    public RiftDialMenu()  {
        super();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        RiftCreature creature = (RiftCreature) ClientProxy.CREATURE;
        if (creature.isDead) {
            this.mc.player.closeScreen();
        }
    }

    @SubscribeEvent
    public static void overlayEvent(RenderGameOverlayEvent event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS && event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }
        if (Minecraft.getMinecraft().currentScreen instanceof RiftDialMenu) {
            event.setCanceled(true);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int numItems = 4;
        RiftCreature creature = (RiftCreature) ClientProxy.CREATURE;

        String creatureTypeName = "entity."+creature.creatureType.name().toLowerCase()+".name";
        String creatureName = creature.hasCustomName() ? creature.getName()+"\n" : "";
        String string = creatureName + I18n.format(creatureTypeName);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75f, 0.75f, 0.75f);
        this.fontRenderer.drawSplitString(string, (int)((width / 0.75 / 2) - (this.fontRenderer.getStringWidth(string) / 2)), (int)(((height / 0.75) - this.fontRenderer.FONT_HEIGHT) / 2), 80, 0xFFFFFFFF);
        GlStateManager.popMatrix();

        float radiusIn = 40f;
        float radiusOut = radiusIn * 3;

        int x = width / 2;
        int y = height / 2;

        double a = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x));
        double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
        float s0 = (((0 - 0.5f) / (float) numItems) + 0.25f) * 360;
        if (a < s0) a += 360;

        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate(0, 0, 0);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        selectedItem = -1;
        for (int i = 0; i < numItems; i++) {
            float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
            float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
            if (a >= s && a < e && d >= radiusIn && d < radiusOut) {
                if (a >= s && a < e && d >= radiusIn) {
                    selectedItem = i;
                    break;
                }
            }
        }

        for (int i = 0; i < numItems; i++) {
            float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
            float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
            if (selectedItem == i) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 255, 255, 255, 64);
            }
            else {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
        }

        double scaledX = Mouse.getX() - (mc.displayWidth / 2.0f);
        double scaledY = Mouse.getY() - (mc.displayHeight / 2.0f);
        double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
        double radius = 90.0 * (mc.displayWidth / width);
        if (distance > radius) {
            double fixedX = scaledX * radius / distance;
            double fixedY = scaledY * radius / distance;

            Mouse.setCursorPosition((int) (mc.displayWidth / 2 + fixedX), (int) (mc.displayHeight / 2 + fixedY));
        }

        tessellator.draw();
        GlStateManager.enableTexture2D();

        RenderHelper.enableGUIStandardItemLighting();
        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    private void drawPieArc(BufferBuilder buffer, float x, float y, float z, float radiusIn, float radiusOut, float startAngle, float endAngle, int r, int g, int b, int a) {
        float angle = endAngle - startAngle;
        int sections = Math.max(1, MathHelper.ceil(angle / 5));

        startAngle = (float) Math.toRadians(startAngle);
        endAngle = (float) Math.toRadians(endAngle);
        angle = endAngle - startAngle;

        for (int i = 0; i < sections; i++) {
            float angle1 = startAngle + (i / (float) sections) * angle;
            float angle2 = startAngle + ((i + 1) / (float) sections) * angle;

            float pos1InX = x + radiusIn * (float) Math.cos(angle1);
            float pos1InY = y + radiusIn * (float) Math.sin(angle1);
            float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
            float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
            float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
            float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
            float pos2InX = x + radiusIn * (float) Math.cos(angle2);
            float pos2InY = y + radiusIn * (float) Math.sin(angle2);

            buffer.pos(pos1OutX, pos1OutY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos1InX, pos1InY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos2InX, pos2InY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos2OutX, pos2OutY, z).color(r, g, b, a).endVertex();
        }
    }
}
