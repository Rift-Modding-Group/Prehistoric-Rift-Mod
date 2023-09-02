package anightdazingzoroark.rift.client.ui;

import anightdazingzoroark.rift.server.entity.*;
import anightdazingzoroark.rift.server.enums.RiftTameRadialChoice;
import anightdazingzoroark.rift.server.enums.TameBehaviorType;
import anightdazingzoroark.rift.server.enums.TameStatusType;
import anightdazingzoroark.rift.server.message.RiftChangeCreatureFromMenu;
import anightdazingzoroark.rift.server.message.RiftCreatureInventoryFromMenu;
import anightdazingzoroark.rift.server.message.RiftMessages;
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

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RiftDialMenu extends GuiScreen {
    private int selectedItem = -1;
    private RiftCreature creature;
    private List<RiftTameRadialChoice> choices;
    private int radialChoiceMenu; //0 is main, 1 is state, 2 is behavior

    public RiftDialMenu(RiftCreature creature)  {
        super();
        this.creature = creature;
        this.choices = getMain();
        this.radialChoiceMenu = 0;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (creature.isDead) {
            this.mc.player.closeScreen();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.choices = getMain();
        this.radialChoiceMenu = 0;
    }

    @SubscribeEvent
    public static void overlayEvent(RenderGameOverlayEvent event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS && event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR && event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE && event.getType() != RenderGameOverlayEvent.ElementType.HEALTH && event.getType() != RenderGameOverlayEvent.ElementType.FOOD && event.getType() != RenderGameOverlayEvent.ElementType.ARMOR) {
            return;
        }
        if (Minecraft.getMinecraft().currentScreen instanceof RiftDialMenu) {
            event.setCanceled(true);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int numItems = this.choices.size();

        String creatureTypeName = "entity."+creature.creatureType.name().toLowerCase()+".name";
        String creatureName = creature.hasCustomName() ? creature.getName()+"\n" : "";
        String string = creatureName + I18n.format(creatureTypeName);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75f, 0.75f, 0.75f);
        this.fontRenderer.drawSplitString(string, (int)((width / 0.75 / 2) - (this.fontRenderer.getStringWidth(string) / 2)), (int)(((height / 0.75) - this.fontRenderer.FONT_HEIGHT) / 2), 80, 0xFFFFFFFF);
        GlStateManager.popMatrix();

        float radiusIn = 40f;
        float radiusOut = radiusIn * 3;
        float itemRadius = (radiusIn + radiusOut) / 1.625f;

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

        //circle
        for (int i = 0; i < numItems; i++) {
            float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
            float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
            if (selectedItem == i) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 255, 255, 255, 128);
            }
            else if (this.radialChoiceMenu == 1 && this.creature.getTameStatus().name().equals(this.choices.get(i).name())) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 128, 0, 128, 128);
            }
            else if (this.radialChoiceMenu == 2 && this.creature.getTameBehavior().name().equals(this.choices.get(i).name())) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 128, 0, 128, 128);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isChild() && i == 2) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
            else {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 128);
            }
        }

        //lock mouse position
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

        //text
        for (int i = 0; i < numItems; i++) {
            String radialString = I18n.format("radial.choice."+this.choices.get(i).name().toLowerCase());
            if (this.radialChoiceMenu == 0 && (this.creature.isChild() || (!this.creature.isChild() && !this.creature.isSaddled())) && i == 2) radialString = "["+radialString+"]";

            float angle1 = ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
            float posX = x + 75 + itemRadius * (float) Math.cos(angle1) - (float)(this.fontRenderer.getStringWidth(radialString) / 2);
            float posY = y + 40 + itemRadius * (float) Math.sin(angle1);

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75f, 0.75f, 0.75f);
            this.fontRenderer.drawString(radialString, (int) posX, (int) posY, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }

        //hover text
        if (this.radialChoiceMenu == 0 && this.creature.isChild() && selectedItem == 2) {
            this.drawHoveringText(I18n.format("radial.note.too_young"), mouseX, mouseY);
        }
        else if (this.radialChoiceMenu == 0 && !this.creature.isSaddled() && selectedItem == 2) {
            this.drawHoveringText(I18n.format("radial.note.need_saddle"), mouseX, mouseY);
        }

        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    private void drawPieArc(BufferBuilder buffer, float x, float y, float z, float radiusIn, float radiusOut, float startAngle, float endAngle, int r, int g, int b, int a) {
        float angle = endAngle - startAngle;
        int sections = Math.max(1, MathHelper.ceil(angle / 5));

        startAngle = (float) Math.toRadians(startAngle + (startAngle > 0 ? 2.5 : -2.5));
        endAngle = (float) Math.toRadians(endAngle + (endAngle > 0 ? -2.5: 2.5));
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

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        switch (this.radialChoiceMenu) {
            case 0:
                if (selectedItem == 0) {
                    RiftMessages.WRAPPER.sendToServer(new RiftCreatureInventoryFromMenu(this.creature.getEntityId()));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 1) {
                    this.choices = getState();
                    this.radialChoiceMenu = 1;
                }
                else if (selectedItem == 3) {
                    this.choices = getBehavior();
                    this.radialChoiceMenu = 2;
                }
                break;
            case 1:
                if (selectedItem == 0) {
                    this.choices = getMain();
                    this.radialChoiceMenu = 0;
                }
                else if (selectedItem == 1) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameStatusType.STAND));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 2) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameStatusType.SIT));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 3) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameStatusType.WANDER));
                    this.mc.player.closeScreen();
                }
                break;
            case 2:
                if (selectedItem == 0) {
                    this.choices = getMain();
                    this.radialChoiceMenu = 0;
                }
                else if (selectedItem == 1) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.ASSIST));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 2) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.NEUTRAL));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 3) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.AGGRESSIVE));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 4) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.PASSIVE));
                    this.mc.player.closeScreen();
                }
                break;
        }
    }

    private static List<RiftTameRadialChoice> getMain() {
        return Arrays.asList(RiftTameRadialChoice.INVENTORY, RiftTameRadialChoice.STATE, RiftTameRadialChoice.RIDE, RiftTameRadialChoice.BEHAVIOR);
    }

    private static List<RiftTameRadialChoice> getMainUnrideable() {
        return Arrays.asList(RiftTameRadialChoice.INVENTORY, RiftTameRadialChoice.STATE, RiftTameRadialChoice.BEHAVIOR);
    }

    private static List<RiftTameRadialChoice> getState() {
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.STAND, RiftTameRadialChoice.SIT, RiftTameRadialChoice.WANDER);
    }

    private static List<RiftTameRadialChoice> getBehavior() {
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.ASSIST, RiftTameRadialChoice.NEUTRAL, RiftTameRadialChoice.AGGRESSIVE, RiftTameRadialChoice.PASSIVE);
    }

    private static List<RiftTameRadialChoice> getBehaviorCanTurret() {
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.ASSIST, RiftTameRadialChoice.NEUTRAL, RiftTameRadialChoice.AGGRESSIVE, RiftTameRadialChoice.PASSIVE, RiftTameRadialChoice.TURRET);
    }

    private static List<RiftTameRadialChoice> getBehaviorTurretOnly() {
        return Arrays.asList(RiftTameRadialChoice.BACK,RiftTameRadialChoice.PASSIVE, RiftTameRadialChoice.TURRET);
    }
}
