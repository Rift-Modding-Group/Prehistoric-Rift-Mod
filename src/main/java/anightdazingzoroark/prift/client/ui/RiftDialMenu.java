package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IWorkstationUser;
import anightdazingzoroark.prift.server.enums.*;
import anightdazingzoroark.prift.server.message.*;
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
    private int radialChoiceMenu; //0 is main, 1 is state, 2 is options, 3 is behaviors

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
        if (this.creature.isDead) {
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

    @SubscribeEvent
    public static void cancelRenders(RenderGameOverlayEvent.Pre event) {
        if (event.getType()==RenderGameOverlayEvent.ElementType.ALL && Minecraft.getMinecraft().currentScreen instanceof RiftDialMenu)
            event.setCanceled(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int numItems = this.choices.size();

        String creatureTypeName = "entity."+creature.creatureType.name().toLowerCase()+".name";
        String creatureName = creature.getCustomNameTag();
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75f, 0.75f, 0.75f);
        this.fontRenderer.drawString(I18n.format(creatureTypeName), (int)((width / 0.75 / 2) - (this.fontRenderer.getStringWidth(I18n.format(creatureTypeName)) / 2)), (int)(((height / 0.75) - this.fontRenderer.FONT_HEIGHT) / 2) + 8, 0xFFFFFF);
        if (this.creature.hasCustomName()) this.fontRenderer.drawString(I18n.format(creatureName), (int)((width / 0.75 / 2) - (this.fontRenderer.getStringWidth(I18n.format(creatureName)) / 2)), (int)(((height / 0.75) - this.fontRenderer.FONT_HEIGHT) / 2) - 8, 0xFFFFFF);
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
            else if (this.radialChoiceMenu == 3 && ((!this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) && this.creature.getTameBehavior().name().equals(this.choices.get(i).name())) || (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) && this.creature.getTurretTargeting().name().equals(this.choices.get(i).name())))) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 128, 0, 128, 128);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isBaby() && i == 2) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isBaby() && i == 4) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
            else if (this.radialChoiceMenu == 2 && this.creature.isBaby() && i == 3) {
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
            String radialString;
            if (i == 4 && this.radialChoiceMenu == 0) {
                if (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) radialString = I18n.format("radial.choice.turret_targeting");
                else radialString = I18n.format("radial.choice.behavior");
            }
            else if (i == 2 && this.radialChoiceMenu == 2) {
                if (this.creature.getHasHomePos()) radialString = I18n.format("radial.choice.reset_home");
                else radialString = I18n.format("radial.choice.set_home");
            }
            else if (i == 4 && this.radialChoiceMenu == 2) {
                if (!this.creature.isUsingWorkstation()) radialString = I18n.format("radial.choice.set_workstation");
                else radialString = I18n.format("radial.choice.clear_workstation");
            }
            else radialString = I18n.format("radial.choice."+this.choices.get(i).name().toLowerCase());

            if (this.radialChoiceMenu == 0 && this.creature.isRideable && (this.creature.isBaby() || (!this.creature.isBaby() && !this.creature.isSaddled()) || this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) || this.creature.isUsingWorkstation()) && i == 2) radialString = "["+radialString+"]";
            if (this.radialChoiceMenu == 0 && this.creature.isBaby() && i == 4) radialString = "["+radialString+"]";
            if (this.radialChoiceMenu == 2 && this.creature.isBaby() && i == 3) radialString = "["+radialString+"]";

            float angle1 = ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
            float posX = x + 75 + itemRadius * (float) Math.cos(angle1) - (float)(this.fontRenderer.getStringWidth(radialString) / 2);
            float posY = y + 40 + itemRadius * (float) Math.sin(angle1);

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75f, 0.75f, 0.75f);
            this.fontRenderer.drawString(radialString, (int) posX, (int) posY, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }

        //hover text
        if (this.radialChoiceMenu == 0 && this.creature.isBaby() && selectedItem == 2) {
            this.drawHoveringText(I18n.format("radial.note.too_young_saddle"), mouseX, mouseY);
        }
        else if (this.radialChoiceMenu == 0 && (this.creature.isUsingWorkstation() || this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) && selectedItem == 2) {
            this.drawHoveringText(I18n.format("radial.note.too_busy"), mouseX, mouseY);
        }
        else if (this.radialChoiceMenu == 0 && this.creature.isRideable && !this.creature.isSaddled() && selectedItem == 2) {
            this.drawHoveringText(I18n.format("radial.note.need_saddle"), mouseX, mouseY);
        }
        else if (this.radialChoiceMenu == 0 && this.creature.isBaby() && selectedItem == 4) {
            this.drawHoveringText(I18n.format("radial.note.too_young_behavior"), mouseX, mouseY);
        }
        else if (this.radialChoiceMenu == 2 && this.creature.isBaby() && selectedItem == 3) {
            this.drawHoveringText(I18n.format("radial.note.too_young_unclaim"), mouseX, mouseY);
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
            /**
             * how menu works:
             * menu is for when radialChoiceMenu == 0
             * state choices is for when radialChoiceMenu == 1
             * options choices is for when radialChoiceMenu == 2
             * behavior choices is for when radialChoiceMenu == 3
             * choices (clockwise from bottom) r inventory, state, ride, options, behavior
             */
            case 0:
                if (selectedItem == 0) {
                    RiftMessages.WRAPPER.sendToServer(new RiftOpenInventoryFromMenu(this.creature.getEntityId()));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 1) {
                    this.choices = getState();
                    this.radialChoiceMenu = 1;
                }
                else if (selectedItem == 2) {
                    if (this.creature.isRideable && !this.creature.isBaby() && !this.creature.isUsingWorkstation() && !this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) && this.creature.isSaddled()) {
                        this.mc.player.closeScreen();
                        RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this.creature));
                    }
                    else if (!this.creature.isRideable) {
                        this.choices = getOptions();
                        this.radialChoiceMenu = 2;
                    }
                }
                else if (selectedItem == 3) {
                    if (this.creature.isRideable) {
                        this.choices = getOptions();
                        this.radialChoiceMenu = 2;
                    }
                    else {
                        this.choices = getBehavior();
                        this.radialChoiceMenu = 3;
                    }
                }
                else if (selectedItem == 4 && !this.creature.isBaby()) {
                    this.choices = getBehavior();
                    this.radialChoiceMenu = 3;
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
                else if (selectedItem == 4) {
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameStatusType.TURRET_MODE));
                    this.mc.player.closeScreen();
                }
                break;
            case 2:
                if (selectedItem == 0) {
                    this.choices = getMain();
                    this.radialChoiceMenu = 0;
                }
                else if (selectedItem == 1) {
                    ClientProxy.popupFromRadial = PopupFromRadial.CHANGE_NAME;
                    RiftMessages.WRAPPER.sendToServer(new RiftOpenPopupFromRadial(this.creature));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 2) {
                    ClientProxy.popupFromRadial = PopupFromRadial.SET_HOME;
                    RiftMessages.WRAPPER.sendToServer(new RiftChangeHomePosFromMenu(this.creature, !this.creature.getHasHomePos()));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 3 && !this.creature.isBaby()) {
                    ClientProxy.popupFromRadial = PopupFromRadial.UNCLAIM;
                    RiftMessages.WRAPPER.sendToServer(new RiftOpenPopupFromRadial(this.creature));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 4) {
                    RiftMessages.WRAPPER.sendToServer(new RiftSetWorkstation(this.creature, !this.creature.isUsingWorkstation()));
                    this.mc.player.closeScreen();
                }
                break;
            case 3:
                if (selectedItem == 0) {
                    this.choices = getMain();
                    this.radialChoiceMenu = 0;
                }
                else if (selectedItem == 1) {
                    if (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TurretModeTargeting.PLAYERS));
                    else RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.ASSIST));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 2) {
                    if (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TurretModeTargeting.PLAYERS_AND_OTHER_TAMES));
                    else RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.NEUTRAL));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 3) {
                    if (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TurretModeTargeting.HOSTILES));
                    else RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.AGGRESSIVE));
                    this.mc.player.closeScreen();
                }
                else if (selectedItem == 4) {
                    if (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TurretModeTargeting.ALL));
                    else RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.PASSIVE));
                    this.mc.player.closeScreen();
                }
                break;
        }
    }

    private List<RiftTameRadialChoice> getMain() {
        if (!this.creature.isRideable) return Arrays.asList(RiftTameRadialChoice.INVENTORY, RiftTameRadialChoice.STATE, RiftTameRadialChoice.OPTIONS, RiftTameRadialChoice.BEHAVIOR);
        return Arrays.asList(RiftTameRadialChoice.INVENTORY, RiftTameRadialChoice.STATE, RiftTameRadialChoice.RIDE, RiftTameRadialChoice.OPTIONS, RiftTameRadialChoice.BEHAVIOR);
    }

    private List<RiftTameRadialChoice> getState() {
        if (this.creature.canDoTurretMode()) return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.STAND, RiftTameRadialChoice.SIT, RiftTameRadialChoice.WANDER, RiftTameRadialChoice.TURRET_MODE);
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.STAND, RiftTameRadialChoice.SIT, RiftTameRadialChoice.WANDER);
    }

    private List<RiftTameRadialChoice> getBehavior() {
        if (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.PLAYERS, RiftTameRadialChoice.PLAYERS_AND_OTHER_TAMES, RiftTameRadialChoice.HOSTILES, RiftTameRadialChoice.ALL);
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.ASSIST, RiftTameRadialChoice.NEUTRAL, RiftTameRadialChoice.AGGRESSIVE, RiftTameRadialChoice.PASSIVE);
    }

    private List<RiftTameRadialChoice> getOptions() {
        if (this.creature instanceof IWorkstationUser) {
            if (((IWorkstationUser)this.creature).canUseWorkstation()) return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.CHANGE_NAME, RiftTameRadialChoice.SET_HOME, RiftTameRadialChoice.UNCLAIM, RiftTameRadialChoice.SET_WORKSTATION);
        }
        return Arrays.asList(RiftTameRadialChoice.BACK, RiftTameRadialChoice.CHANGE_NAME, RiftTameRadialChoice.SET_HOME, RiftTameRadialChoice.UNCLAIM);
    }
}
