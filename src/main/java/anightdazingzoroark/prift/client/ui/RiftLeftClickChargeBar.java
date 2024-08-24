package anightdazingzoroark.prift.client.ui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.RiftLargeWeaponType;
import anightdazingzoroark.prift.server.entity.creature.Apatosaurus;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftCatapult;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftMortar;
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

import java.util.Objects;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RiftLeftClickChargeBar {
    private static final ResourceLocation chargeBarHud = new ResourceLocation(RiftInitialize.MODID, "textures/ui/hud_icons.png");
    private static final int textureXSize = 182;
    private static final int textureYSize = 5;
    private int fill = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        Entity entity = player.getRidingEntity();
        GameSettings settings = Minecraft.getMinecraft().gameSettings;

        if (entity instanceof RiftCreature) {
            if (((RiftCreature)entity).hasLeftClickChargeBar()) {
                if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                    RiftCreature creature = (RiftCreature) entity;
                    ScaledResolution resolution = event.getResolution();

                    Minecraft.getMinecraft().getTextureManager().bindTexture(chargeBarHud);
                    renderLeftClickChargeHud(creature, resolution.getScaledWidth(), resolution.getScaledHeight());
                    reduceUnusedChargeBar(creature, creature.isUsingLeftClick());
                    Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
                }
            }
        }
        if (entity instanceof RiftLargeWeapon) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                RiftLargeWeapon weapon = (RiftLargeWeapon) entity;
                ScaledResolution resolution = event.getResolution();

                Minecraft.getMinecraft().getTextureManager().bindTexture(chargeBarHud);
                renderLeftClickChargeHud(weapon, resolution.getScaledWidth(), resolution.getScaledHeight());
                reduceUnusedChargeBar(weapon, weapon.isUsingLeftClick());
                Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
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
            if (player.getRidingEntity() instanceof RiftCatapult) event.setCanceled(true);
        }
    }

    //manage charge bar for creatures
    private void reduceUnusedChargeBar(RiftCreature creature, boolean usingLeftClick) {
        if (creature instanceof Apatosaurus) {
            Apatosaurus apatosaurus = (Apatosaurus) creature;
            if (usingLeftClick && apatosaurus.getLeftClickCooldown() == 0) fill = apatosaurus.getLeftClickUse();
            else fill = apatosaurus.getLeftClickCooldown() / 2;
        }
        else {
            if (usingLeftClick) fill = creature.getLeftClickUse();
            else fill = creature.getLeftClickCooldown() / 2;
        }
    }

    //manage charge bar for weapons
    private void reduceUnusedChargeBar(RiftLargeWeapon weapon, boolean usingLeftClick) {
        if (weapon instanceof RiftCatapult) {
            RiftCatapult catapult = (RiftCatapult) weapon;
            if (usingLeftClick) fill = catapult.getLeftClickUse();
            else fill = catapult.getLeftClickCooldown() / 2;
        }
        else if (weapon instanceof RiftMortar) {
            RiftMortar mortar = (RiftMortar) weapon;
            if (usingLeftClick) fill = mortar.getLeftClickUse();
            else fill = mortar.getLeftClickCooldown() / 2;
        }
        else fill = weapon.getLeftClickCooldown();
    }

    //render charge bar for creatures
    private void renderLeftClickChargeHud(RiftCreature creature, int xSize, int ySize) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int left = xSize / 2 - 91;
        int top = ySize - 32 + (creature.hasRightClickChargeBar() ? 5 : 3);

        float fillUpBar;
        if (creature instanceof Apatosaurus) {
            Apatosaurus apatosaurus = (Apatosaurus) creature;
            if (apatosaurus.getWeapon().equals(RiftLargeWeaponType.CANNON)) {
                fillUpBar = (float) textureXSize / 30f * fill;
            }
            else {
                fillUpBar = (float) textureXSize / 100f * fill;
            }
        }
        else fillUpBar = (float)textureXSize / 100f * fill;

        RiftUtil.drawTexturedModalRect(left, top, 0, 19, textureXSize, textureYSize);
        RiftUtil.drawTexturedModalRect(left, top, 0, 24, Math.min((int)fillUpBar, textureXSize), textureYSize);
        GlStateManager.disableBlend();
    }

    //render charge bar for weapons
    private void renderLeftClickChargeHud(RiftLargeWeapon weapon, int xSize, int ySize) {
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        int left = xSize / 2 - 91;
        int top = ySize - 29;
        float fillUpBar = (float)textureXSize / (weapon.maxCooldown()) * fill;
        RiftUtil.drawTexturedModalRect(left, top, 0, 19, textureXSize, textureYSize);
        RiftUtil.drawTexturedModalRect(left, top, 0, 24, Math.min((int)fillUpBar, textureXSize), textureYSize);
        GlStateManager.disableBlend();
    }
}
