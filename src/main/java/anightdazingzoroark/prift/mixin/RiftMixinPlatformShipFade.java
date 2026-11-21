package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.client.rendering.entity.RiftCreatureRenderer;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import valkyrienwarfare.api.RiftedValkyriumAPI;

import java.util.UUID;

@Pseudo
@Mixin(targets = "org.valkyrienskies.mod.client.render.PhysObjectRenderManager", remap = false)
public class RiftMixinPlatformShipFade {
    @Shadow
    @Final
    public BlockPos offsetPos;
    @Unique
    @NotNull
    private RiftCreatureRenderer.FadeState prift$aimVisibility = new RiftCreatureRenderer.FadeState(false, 1f);
    @Unique
    @Nullable
    private RiftCreatureRenderer.RiderPass prift$platformAimPass;

    @WrapMethod(method = "renderBlockLayer")
    private void fadePlatformBlocks(
            @NotNull BlockRenderLayer layer, double partialTicks, int pass, @NotNull ICamera camera, @NotNull Operation<Void> original
    ) {
        if (layer == BlockRenderLayer.SOLID) {
            this.prift$aimVisibility = new RiftCreatureRenderer.FadeState(false, 1f);
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.player != null && minecraft.player.getRidingEntity() instanceof RiftCreature creature
                    && creature.isPlatformEquipped()
            ) {
                UUID shipId = creature.getPlatformShipId();
                if (shipId != null && RiftedValkyriumAPI.getShipManagingBlock(minecraft.world, this.offsetPos)
                        .filter(ship -> shipId.equals(ship.getUuid())).isPresent()
                ) {
                    this.prift$aimVisibility = RiftCreatureRenderer.getPlatformAimVisibility(minecraft.player);
                }
            }
        }
        if (this.prift$aimVisibility.opacity() >= 1f) {
            original.call(layer, partialTicks, pass, camera);
            return;
        }
        if (layer != BlockRenderLayer.TRANSLUCENT) return;

        this.prift$platformAimPass = RiftCreatureRenderer.renderAimAttachment(
                this.prift$platformAimPass, this.prift$aimVisibility, () -> {
                    boolean depthWrites = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
                    boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
                    boolean alphaTesting = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
                    ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.enableAlpha();
                    for (BlockRenderLayer shipLayer : BlockRenderLayer.values()) {
                        if (shipLayer == BlockRenderLayer.TRANSLUCENT) {
                            GlStateManager.enableBlend();
                            GlStateManager.depthMask(false);
                        }
                        else {
                            GlStateManager.disableBlend();
                            GlStateManager.depthMask(true);
                        }
                        if (shipLayer == BlockRenderLayer.CUTOUT) texture.setBlurMipmap(false, false);
                        original.call(shipLayer, partialTicks, pass, camera);
                        if (shipLayer == BlockRenderLayer.CUTOUT) texture.restoreLastBlurMipmap();
                    }
                    GlStateManager.depthMask(depthWrites);
                    if (blending) GlStateManager.enableBlend();
                    else GlStateManager.disableBlend();
                    if (!alphaTesting) GlStateManager.disableAlpha();
                }
        );
    }
}
