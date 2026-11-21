package anightdazingzoroark.prift.mixin;

import anightdazingzoroark.prift.client.rendering.entity.RiftCreatureRenderer;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valkyrienwarfare.api.RiftedValkyriumAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(RenderGlobal.class)
public class RiftMixinPlatformTileFade {
    @Unique
    @Nullable
    private RiftCreatureRenderer.RiderPass prift$platformAimPass;
    @Unique
    @Nullable
    private UUID prift$platformShipId;
    @Unique
    @NotNull
    private RiftCreatureRenderer.FadeState prift$aimVisibility = new RiftCreatureRenderer.FadeState(false, 1f);
    @Unique
    @NotNull
    private List<Runnable> prift$platformTiles = new ArrayList<>();

    @Inject(method = "renderEntities", at = @At("HEAD"))
    private void beginPlatformTileBatch(@NotNull Entity viewer, @NotNull ICamera camera, float partialTicks, @NotNull CallbackInfo callback) {
        this.prift$platformTiles.clear();
        this.prift$platformShipId = null;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || !Loader.isModLoaded("valkyrienskies")) return;
        this.prift$aimVisibility = RiftCreatureRenderer.getPlatformAimVisibility(minecraft.player);
        if (this.prift$aimVisibility.opacity() < 1f && minecraft.player.getRidingEntity() instanceof RiftCreature creature) {
            this.prift$platformShipId = creature.getPlatformShipId();
        }
    }

    @WrapOperation(
            method = "renderEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V")
    )
    private void queuePlatformTile(
            @NotNull TileEntityRendererDispatcher dispatcher, @NotNull TileEntity tile, float partialTicks, int destroyStage,
            @NotNull Operation<Void> original
    ) {
        if (this.prift$platformShipId == null || destroyStage >= 0
                || RiftedValkyriumAPI.getShipManagingBlock(tile.getWorld(), tile.getPos())
                .filter(ship -> this.prift$platformShipId.equals(ship.getUuid())).isEmpty()
        ) {
            original.call(dispatcher, tile, partialTicks, destroyStage);
            return;
        }
        this.prift$platformTiles.add(() -> original.call(dispatcher, tile, partialTicks, destroyStage));
    }

    @WrapOperation(
            method = "renderEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;drawBatch(I)V", remap = false)
    )
    private void fadePlatformTiles(
            @NotNull TileEntityRendererDispatcher dispatcher, int pass, @NotNull Operation<Void> original
    ) {
        original.call(dispatcher, pass);
        if (this.prift$platformTiles.isEmpty()) return;
        // Copy and composite once for the platform, rather than once per tile entity.
        this.prift$platformAimPass = RiftCreatureRenderer.renderAimAttachment(
                this.prift$platformAimPass, this.prift$aimVisibility, () -> {
                    dispatcher.preDrawBatch();
                    this.prift$platformTiles.forEach(Runnable::run);
                    dispatcher.drawBatch(pass);
                }
        );
        this.prift$platformTiles.clear();
    }
}
