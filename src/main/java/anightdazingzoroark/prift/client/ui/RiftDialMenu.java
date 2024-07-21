package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ClientProxy;
import anightdazingzoroark.prift.server.ServerProxy;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.interfaces.IHarvestWhenWandering;
import anightdazingzoroark.prift.server.entity.interfaces.IImpregnable;
import anightdazingzoroark.prift.server.entity.interfaces.ILeadWorkstationUser;
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
import net.minecraft.util.text.TextComponentTranslation;
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
        this.choices = creature.mainRadialChoices();
        this.radialChoiceMenu = 0;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.creature.isDead) this.mc.player.closeScreen();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.choices = creature.mainRadialChoices();
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

        //for items in center
        String creatureTypeName = I18n.format("entity."+creature.creatureType.name().toLowerCase()+".name");
        String creatureName = creature.getCustomNameTag();
        String level = I18n.format("tametrait.level", this.creature.getLevel());
        String xp = I18n.format(this.creature.getXP()+"/"+this.creature.getMaxXP()+" XP");
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.75f, 0.75f, 0.75f);

        this.fontRenderer.drawString(xp, (int)((width / 0.75 / 2) - (this.fontRenderer.getStringWidth(xp) / 2)), (int)(((height / 0.75) - this.fontRenderer.FONT_HEIGHT) / 2) + (this.creature.hasCustomName() ? 18 : 12), 0xFFFFFF);
        this.fontRenderer.drawString(level, (int)((width / 0.75 / 2) - (this.fontRenderer.getStringWidth(level) / 2)), (int)(((height / 0.75) - this.fontRenderer.FONT_HEIGHT) / 2) + (this.creature.hasCustomName() ? 6 : 0), 0xFFFFFF);
        this.fontRenderer.drawString(creatureTypeName, (int)((width / 0.75 / 2) - (this.fontRenderer.getStringWidth(creatureTypeName) / 2)), (int)(((height / 0.75) - this.fontRenderer.FONT_HEIGHT) / 2) + (this.creature.hasCustomName() ? -6 : -12), 0xFFFFFF);
        if (this.creature.hasCustomName()) this.fontRenderer.drawString(I18n.format(creatureName), (int)((width / 0.75 / 2) - (this.fontRenderer.getStringWidth(I18n.format(creatureName)) / 2)), (int)(((height / 0.75) - this.fontRenderer.FONT_HEIGHT) / 2) - 18, 0xFFFFFF);
        GlStateManager.popMatrix();

        float radiusIn = 40f;
        float radiusOut = radiusIn * 3;

        int x = this.width / 2;
        int y = this.height / 2;

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

        this.selectedItem = -1;
        for (int i = 0; i < numItems; i++) {
            float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
            float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
            if (a >= s && a < e && d >= radiusIn && d < radiusOut) {
                if (a >= s && a < e && d >= radiusIn) {
                    this.selectedItem = i;
                    break;
                }
            }
        }

        //circle
        for (int i = 0; i < numItems; i++) {
            float s = (((i - 0.5f) / (float) numItems) + 0.25f) * 360;
            float e = (((i + 0.5f) / (float) numItems) + 0.25f) * 360;
            boolean onlySelected = this.selectedItem > -1 && this.selectedItem < numItems;
            if (this.selectedItem == i) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 255, 255, 255, 128);
            }
            else if (this.radialChoiceMenu == 1 && this.creature.getTameStatus().name().equals(this.choices.get(i).name())) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 128, 0, 128, 128);
            }
            else if (this.radialChoiceMenu == 3 && ((!this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) && this.creature.getTameBehavior().name().equals(this.choices.get(i).name())) || (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) && this.creature.getTurretTargeting().name().equals(this.choices.get(i).name())))) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 128, 0, 128, 128);
            }
            else if (this.radialChoiceMenu == 0 && (this.creature.isBaby() || this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) || this.creature.isUsingWorkstation() || this.creature.isSleeping()) && this.choices.get(i) == RiftTameRadialChoice.RIDE) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
            else if (this.radialChoiceMenu == 0 && onlySelected && (this.choices.get(this.selectedItem) == RiftTameRadialChoice.RIDE || this.choices.get(this.selectedItem) == RiftTameRadialChoice.STATE || this.choices.get(this.selectedItem) == RiftTameRadialChoice.BEHAVIOR) && this.creature instanceof IImpregnable) {
                IImpregnable impregnable = (IImpregnable)this.creature;
                if (impregnable.isPregnant()) drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
                else drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 128);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isBaby() && onlySelected && this.choices.get(i) == RiftTameRadialChoice.BEHAVIOR) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
            else if (this.radialChoiceMenu == 2 && this.creature.isBaby() && onlySelected && this.choices.get(i) == RiftTameRadialChoice.UNCLAIM) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
            else if (this.radialChoiceMenu == 1 && onlySelected && this.choices.get(i) == RiftTameRadialChoice.TURRET_MODE && this.creature.isUsingWorkstation()) {
                drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            }
            else if (this.radialChoiceMenu == 0 && onlySelected && this.choices.get(i) == RiftTameRadialChoice.OPTIONS) {
                if (this.creature instanceof ILeadWorkstationUser) {
                    ILeadWorkstationUser user = (ILeadWorkstationUser) this.creature;
                    if (user.isAttachableForWork(this.creature.getWorkstationPos())) {
                        drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
                    }
                    else drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 128);
                }
                else drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 128);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isUsingWorkstation() && onlySelected && (this.choices.get(i) == RiftTameRadialChoice.STATE || this.choices.get(i) == RiftTameRadialChoice.BEHAVIOR)) drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            else if (this.radialChoiceMenu == 2 && this.creature.isUsingWorkstation() && onlySelected && (this.choices.get(i) != RiftTameRadialChoice.BACK && this.choices.get(i) != RiftTameRadialChoice.SET_WORKSTATION)) drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 64);
            else drawPieArc(buffer, x, y, zLevel, radiusIn, radiusOut, s, e, 0, 0, 0, 128);
        }

        //lock mouse position
        double scaledX = Mouse.getX() - (mc.displayWidth / 2.0f);
        double scaledY = Mouse.getY() - (mc.displayHeight / 2.0f);
        double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
        double radius = 90.0 * ((double) mc.displayWidth / width);
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
            if (this.radialChoiceMenu == 0 && this.choices.get(i) == RiftTameRadialChoice.BEHAVIOR) {
                if (this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) radialString = I18n.format("radial.choice.turret_targeting");
                else radialString = I18n.format("radial.choice.behavior");
            }
            else if (this.radialChoiceMenu == 2 && this.choices.get(i) == RiftTameRadialChoice.SET_HOME) {
                if (this.creature.getHasHomePos()) radialString = I18n.format("radial.choice.reset_home");
                else radialString = I18n.format("radial.choice.set_home");
            }
            else if (this.radialChoiceMenu == 2 && this.choices.get(i) == RiftTameRadialChoice.SET_WORKSTATION) {
                if (!this.creature.isUsingWorkstation()) radialString = I18n.format("radial.choice.set_workstation");
                else radialString = I18n.format("radial.choice.clear_workstation");
            }
            else if (this.radialChoiceMenu == 2 && this.choices.get(i) == RiftTameRadialChoice.SET_WANDER_HARVEST) {
                boolean isHarvestWandering = this.creature instanceof IHarvestWhenWandering && ((IHarvestWhenWandering) this.creature).canHarvest();
                if (!isHarvestWandering) radialString = I18n.format("radial.choice.set_wander_harvest");
                else radialString = I18n.format("radial.choice.clear_wander_harvest");
            }
            else radialString = I18n.format("radial.choice."+this.choices.get(i).name().toLowerCase());

            //additional disablin text
            //radialString.charAt(0) != '['
            if (this.radialChoiceMenu == 0 && this.creature.isRideable && this.choices.get(i) == RiftTameRadialChoice.RIDE && (this.creature.isBaby() || (!this.creature.isBaby() && !this.creature.isSaddled()) || this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) || this.creature.isUsingWorkstation() || this.creature.isSleeping())) radialString = "["+radialString+"]";
            if (this.radialChoiceMenu == 0 && this.choices.get(i) == RiftTameRadialChoice.BEHAVIOR && this.creature.isBaby()) radialString = "["+radialString+"]";
            if (this.radialChoiceMenu == 2 && this.choices.get(i) == RiftTameRadialChoice.UNCLAIM && this.creature.isBaby()) radialString = "["+radialString+"]";
            if (this.radialChoiceMenu == 1 && this.choices.get(i) == RiftTameRadialChoice.TURRET_MODE && this.creature.isUsingWorkstation()) radialString = "["+radialString+"]";
            if (this.radialChoiceMenu == 0 && this.choices.get(i) == RiftTameRadialChoice.OPTIONS) {
                if (this.creature instanceof ILeadWorkstationUser) {
                    ILeadWorkstationUser user = (ILeadWorkstationUser) this.creature;
                    if (user.isAttachableForWork(this.creature.getWorkstationPos())) {
                        radialString = "["+radialString+"]";
                    }
                }
            }
            if (this.radialChoiceMenu == 0 && (this.choices.get(i) == RiftTameRadialChoice.RIDE || this.choices.get(i) == RiftTameRadialChoice.STATE || this.choices.get(i) == RiftTameRadialChoice.BEHAVIOR)) {
                if (this.creature instanceof IImpregnable) {
                    IImpregnable impregnable = (IImpregnable)this.creature;
                    if (impregnable.isPregnant()) radialString = "["+radialString+"]";
                }
            }
            if (this.radialChoiceMenu == 0 && this.creature.isUsingWorkstation() && (this.choices.get(i) == RiftTameRadialChoice.STATE || this.choices.get(i) == RiftTameRadialChoice.BEHAVIOR)) radialString = "["+radialString+"]";
            if (this.radialChoiceMenu == 2 && this.creature.isUsingWorkstation() && (this.choices.get(i) != RiftTameRadialChoice.BACK && this.choices.get(i) != RiftTameRadialChoice.SET_WORKSTATION)) radialString = "["+radialString+"]";

            //create text
            float angle1 = ((i / (float) numItems) + 0.25f) * 2 * (float) Math.PI;
            float posX = ((float)this.width / 0.75f  - (float)this.fontRenderer.getStringWidth(radialString)) / 2 + 100 * (float)Math.cos(angle1);
            float posY = (y / 0.75f) + 100 * (float)Math.sin(angle1);

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.75f, 0.75f, 0.75f);
            this.fontRenderer.drawString(radialString, (int) posX, (int) posY, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }

        //hover text
        if (this.selectedItem > -1 && this.selectedItem < this.choices.size()) {
            if (this.radialChoiceMenu == 0 && this.creature.isBaby() && this.choices.get(this.selectedItem) == RiftTameRadialChoice.RIDE) {
                this.drawHoveringText(I18n.format("radial.note.too_young_saddle"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 0 && (this.creature.isUsingWorkstation() || this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE)) && this.choices.get(this.selectedItem) == RiftTameRadialChoice.RIDE) {
                this.drawHoveringText(I18n.format("radial.note.too_busy"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 0 && (this.choices.get(this.selectedItem) == RiftTameRadialChoice.RIDE || this.choices.get(this.selectedItem) == RiftTameRadialChoice.STATE || this.choices.get(this.selectedItem) == RiftTameRadialChoice.BEHAVIOR) && this.creature instanceof IImpregnable) {
                IImpregnable impregnable = (IImpregnable) this.creature;
                if (impregnable.isPregnant()) this.drawHoveringText(I18n.format("radial.note.is_pregnant"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isSleeping() && this.choices.get(this.selectedItem) == RiftTameRadialChoice.RIDE) {
                this.drawHoveringText(I18n.format("radial.note.is_asleep"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isRideable && !this.creature.isSaddled() && this.choices.get(this.selectedItem) == RiftTameRadialChoice.RIDE) {
                this.drawHoveringText(I18n.format("radial.note.need_saddle"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isBaby() && this.choices.get(this.selectedItem) == RiftTameRadialChoice.BEHAVIOR) {
                this.drawHoveringText(I18n.format("radial.note.too_young_behavior"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 1 && this.creature.isUsingWorkstation() && this.choices.get(this.selectedItem) == RiftTameRadialChoice.TURRET_MODE) {
                this.drawHoveringText(I18n.format("radial.note.too_busy"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isUsingWorkstation() && this.choices.get(this.selectedItem) == RiftTameRadialChoice.STATE) {
                this.drawHoveringText(I18n.format("radial.note.too_busy"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 0 && this.creature.isUsingWorkstation() && this.choices.get(this.selectedItem) == RiftTameRadialChoice.BEHAVIOR) {
                this.drawHoveringText(I18n.format("radial.note.too_busy"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 2 && this.creature.isUsingWorkstation() && this.choices.get(this.selectedItem) != RiftTameRadialChoice.BACK && this.choices.get(this.selectedItem) == RiftTameRadialChoice.SET_WORKSTATION) {
                this.drawHoveringText(I18n.format("radial.note.too_busy"), mouseX, mouseY);
            }
            else if (this.radialChoiceMenu == 0 && this.choices.get(this.selectedItem) == RiftTameRadialChoice.OPTIONS) {
                if (this.creature instanceof ILeadWorkstationUser) {
                    ILeadWorkstationUser user = (ILeadWorkstationUser) this.creature;
                    if (user.isAttachableForWork(this.creature.getWorkstationPos())) {
                        this.drawHoveringText(I18n.format("radial.note.too_busy"), mouseX, mouseY);
                    }
                }
            }
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
                if (this.selectedItem > -1 && this.selectedItem < this.creature.mainRadialChoices().size()) {
                    if (this.creature.mainRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.INVENTORY) {
                        RiftMessages.WRAPPER.sendToServer(new RiftOpenInventoryFromMenu(this.creature.getEntityId()));
                        this.mc.player.closeScreen();
                    }
                    else if (this.creature.mainRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.STATE) {
                        boolean isNotPregnant = !(this.creature instanceof IImpregnable) || !((IImpregnable) this.creature).isPregnant();
                        if (isNotPregnant && !this.creature.isUsingWorkstation()) {
                            this.choices = this.creature.stateRadialChoices();
                            this.radialChoiceMenu = 1;
                        }
                    }
                    else if (this.creature.mainRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.RIDE) {
                        boolean isNotPregnant = !(this.creature instanceof IImpregnable) || !((IImpregnable) this.creature).isPregnant();
                        if (isNotPregnant && !this.creature.isSleeping() && !this.creature.isBaby() && !this.creature.isUsingWorkstation() && !this.creature.getTameStatus().equals(TameStatusType.TURRET_MODE) && this.creature.isSaddled()) {
                            this.mc.player.closeScreen();
                            RiftMessages.WRAPPER.sendToServer(new RiftStartRiding(this.creature));
                        }
                    }
                    else if (this.creature.mainRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.OPTIONS) {
                        boolean isNotWorkedLead = !(this.creature instanceof ILeadWorkstationUser) || !((ILeadWorkstationUser) this.creature).isAttachableForWork(this.creature.getWorkstationPos());
                        if (isNotWorkedLead) {
                            this.choices = this.creature.optionsRadialChoices();
                            this.radialChoiceMenu = 2;
                        }
                    }
                    else if (this.creature.mainRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.BEHAVIOR) {
                        boolean isNotPregnant = !(this.creature instanceof IImpregnable) || !((IImpregnable) this.creature).isPregnant();
                        if (!this.creature.isBaby() && isNotPregnant && !this.creature.isUsingWorkstation()) {
                            this.choices = (this.creature.getTameStatus() == TameStatusType.TURRET_MODE) ? this.creature.turretRadialChoices() : this.creature.behaviorRadialChoices();
                            this.radialChoiceMenu = 3;
                        }
                    }
                }
                break;
            case 1:
                if (this.selectedItem > -1 && this.selectedItem < this.creature.stateRadialChoices().size()) {
                    if (this.creature.stateRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.BACK) {
                        this.choices = this.creature.mainRadialChoices();
                        this.radialChoiceMenu = 0;
                    }
                    else if (this.creature.stateRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.STAND) {
                        RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameStatusType.STAND));
                        this.mc.player.closeScreen();
                    }
                    else if (this.creature.stateRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.SIT) {
                        RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameStatusType.SIT));
                        this.mc.player.closeScreen();
                    }
                    else if (this.creature.stateRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.WANDER) {
                        RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameStatusType.WANDER));
                        this.mc.player.closeScreen();
                    }
                    else if (this.creature.stateRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.TURRET_MODE && !this.creature.isUsingWorkstation()) {
                        RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameStatusType.TURRET_MODE));
                        this.mc.player.closeScreen();
                    }
                }
                break;
            case 2:
                if (this.selectedItem > -1 && this.selectedItem < this.creature.optionsRadialChoices().size()) {
                    if (this.creature.optionsRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.BACK) {
                        this.choices = this.creature.mainRadialChoices();
                        this.radialChoiceMenu = 0;
                    }
                    else if (this.creature.optionsRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.CHANGE_NAME) {
                        if (!this.creature.isUsingWorkstation()) {
                            ClientProxy.popupFromRadial = PopupFromRadial.CHANGE_NAME;
                            this.mc.player.closeScreen();
                            this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_RADIAL, this.mc.player.world, this.creature.getEntityId(), 0, 0);
                        }
                    }
                    else if (this.creature.optionsRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.SET_HOME) {
                        if (!this.creature.isUsingWorkstation()) {
                            ClientProxy.popupFromRadial = PopupFromRadial.SET_HOME;
                            this.mc.player.closeScreen();
                            if (!this.creature.getHasHomePos()) RiftMessages.WRAPPER.sendToServer(new RiftChangeHomePosFromMenu(this.creature));
                            else this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_RADIAL, this.mc.player.world, this.creature.getEntityId(), 0, 0);
                        }
                    }
                    else if (this.creature.optionsRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.UNCLAIM) {
                        if (!this.creature.isUsingWorkstation() && !this.creature.isBaby()) {
                            ClientProxy.popupFromRadial = PopupFromRadial.UNCLAIM;
                            this.mc.player.closeScreen();
                            this.mc.player.openGui(RiftInitialize.instance, ServerProxy.GUI_MENU_FROM_RADIAL, this.mc.player.world, this.creature.getEntityId(), 0, 0);
                        }
                    }
                    else if (this.creature.optionsRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.SET_WORKSTATION) {
                        if (!this.creature.isBaby()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftSetWorkstation(this.creature, !this.creature.isUsingWorkstation()));
                            this.mc.player.closeScreen();
                        }
                    }
                    else if (this.creature.optionsRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.SET_WANDER_HARVEST) {
                        if (!this.creature.isBaby()) {
                            RiftMessages.WRAPPER.sendToServer(new RiftSetCanWanderHarvest(this.creature, !((IHarvestWhenWandering)this.creature).canHarvest()));
                            this.mc.player.closeScreen();
                        }
                    }
                }
                break;
            case 3:
                if (this.creature.getTameStatus() == TameStatusType.TURRET_MODE) {
                    if (this.selectedItem > -1 && this.selectedItem < this.creature.turretRadialChoices().size()) {
                        if (this.creature.turretRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.BACK) {
                            this.choices = this.creature.mainRadialChoices();
                            this.radialChoiceMenu = 0;
                        }
                        else if (this.creature.turretRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.PLAYERS) {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TurretModeTargeting.PLAYERS));
                            this.mc.player.closeScreen();
                        }
                        else if (this.creature.turretRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.PLAYERS_AND_OTHER_TAMES) {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TurretModeTargeting.PLAYERS_AND_OTHER_TAMES));
                            this.mc.player.closeScreen();
                        }
                        else if (this.creature.turretRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.HOSTILES) {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TurretModeTargeting.HOSTILES));
                            this.mc.player.closeScreen();
                        }
                        else if (this.creature.turretRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.ALL) {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TurretModeTargeting.ALL));
                            this.mc.player.closeScreen();
                        }
                    }
                }
                else {
                    if (this.selectedItem > -1 && this.selectedItem < this.creature.behaviorRadialChoices().size()) {
                        if (this.creature.behaviorRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.BACK) {
                            this.choices = this.creature.mainRadialChoices();
                            this.radialChoiceMenu = 0;
                        }
                        else if (this.creature.behaviorRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.ASSIST) {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.ASSIST));
                            this.mc.player.closeScreen();
                        }
                        else if (this.creature.behaviorRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.NEUTRAL) {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.NEUTRAL));
                            this.mc.player.closeScreen();
                        }
                        else if (this.creature.behaviorRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.AGGRESSIVE) {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.AGGRESSIVE));
                            this.mc.player.closeScreen();
                        }
                        else if (this.creature.behaviorRadialChoices().get(this.selectedItem) == RiftTameRadialChoice.PASSIVE) {
                            RiftMessages.WRAPPER.sendToServer(new RiftChangeCreatureFromMenu(this.creature, TameBehaviorType.PASSIVE));
                            this.mc.player.closeScreen();
                        }
                    }
                }
                break;
        }
    }
}
