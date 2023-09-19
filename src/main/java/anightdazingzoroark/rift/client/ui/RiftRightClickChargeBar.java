package anightdazingzoroark.rift.client.ui;

import anightdazingzoroark.rift.RiftInitialize;
import anightdazingzoroark.rift.RiftUtil;
import anightdazingzoroark.rift.server.entity.RiftCreature;
import anightdazingzoroark.rift.server.events.RiftMouseHoldEvent;
import anightdazingzoroark.rift.server.message.RiftManageCanUseRightClick;
import anightdazingzoroark.rift.server.message.RiftMessages;
import anightdazingzoroark.rift.server.message.RiftMountControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RiftRightClickChargeBar {
    private static final ResourceLocation chargeBarHud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final int textureXSize = 182;
    private static final int textureYSize = 5;
    private int fill = 0;

    @SubscribeEvent
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Entity entity = player.getRidingEntity();

        if (entity instanceof RiftCreature) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.AIR) {
                RiftCreature creature = (RiftCreature) entity;
                ScaledResolution resolution = event.getResolution();

                Minecraft.getMinecraft().getTextureManager().bindTexture(chargeBarHud);
                renderRightClickChargeHud(resolution.getScaledWidth(), resolution.getScaledHeight());
                Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
            }
        }
    }

    @SubscribeEvent
    public static void overlayEvent(RenderGameOverlayEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        World world = player.world;
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }
        if (world != null) {
            if (player.getRidingEntity() instanceof RiftCreature) event.setCanceled(true);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void mouseTest(RiftMouseHoldEvent event) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        EntityPlayer player = Minecraft.getMinecraft().player;
        Item heldItem = player.getHeldItemMainhand().getItem();

        if (player.getRidingEntity() instanceof RiftCreature) {
            RiftCreature creature = (RiftCreature) player.getRidingEntity();
            if (!RiftUtil.checkInMountItemWhitelist(heldItem) && !(heldItem instanceof ItemFood) && event.getMouseButton() == 1 && creature.canUseRightClick()) {
                if (!event.isReleased()) {
                    fill++;
                }
                else {
                    fill = 0;
                }
            }
        }
    }

    private void renderRightClickChargeHud(int xSize, int ySize) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int left = xSize / 2 - 91;
        int top = ySize - 32 + 3;
        float fillUpBar = (float)textureXSize / 100f * fill;
        RiftUtil.drawTexturedModalRect(left, top, 0, 9, textureXSize, textureYSize);
        RiftUtil.drawTexturedModalRect(left, top, 0, 14, Math.min((int)fillUpBar, textureXSize), textureYSize);
        GlStateManager.disableBlend();
    }
}
