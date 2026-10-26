package anightdazingzoroark.prift.client.rendering.block;

import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.creatureBox.CreatureBoxTileEntity;
import anightdazingzoroark.prift.api.creature.RiftCreatureEnums.CreatureDeployment;
import anightdazingzoroark.prift.server.entity.creature.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.CreatureStorage;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public class CreatureBoxRangeRenderer {
    private static final double DISPLAY_SECONDS = 10D;
    private static final int GRID_SPACING = 4;
    @Nullable
    private static BlockPos shownBox;
    @Nullable
    private static World shownWorld;
    private static long shownAtNanos;
    private static final Set<UUID> highlightedCreatures = new HashSet<>();

    public static void toggle(@NotNull BlockPos position) {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        highlightedCreatures.clear();
        if (world == shownWorld && position.equals(shownBox)) {
            shownBox = null;
            shownWorld = null;
        }
        else {
            shownBox = position.toImmutable();
            shownWorld = world;
            shownAtNanos = System.nanoTime();
            refreshHighlightedCreatures();
        }
    }

    private static boolean isActive(@Nullable World world) {
        if (shownBox == null) return false;
        double elapsedSeconds = (System.nanoTime() - shownAtNanos) / 1_000_000_000D;
        if (elapsedSeconds >= DISPLAY_SECONDS || world != shownWorld || shownWorld == null
                || shownWorld.isBlockLoaded(shownBox) && shownWorld.getBlockState(shownBox).getBlock() != RiftBlocks.CREATURE_BOX) {
            shownBox = null;
            shownWorld = null;
            highlightedCreatures.clear();
            return false;
        }
        return true;
    }

    private static void refreshHighlightedCreatures() {
        highlightedCreatures.clear();
        if (shownWorld == null || shownBox == null
                || !(shownWorld.getTileEntity(shownBox) instanceof CreatureBoxTileEntity tile)) return;
        CreatureStorage storage = tile.getDeployedCreatures();
        for (int slot = 0; slot < storage.getSize(); slot++) {
            CreatureNBT creature = storage.getCreature(slot);
            if (!creature.nbtTagCompound().isEmpty()) highlightedCreatures.add(creature.getUniqueID());
        }
    }

    public static boolean isHighlighted(@NotNull RiftCreature creature) {
        return !creature.isDead && creature.getDeploymentType() == CreatureDeployment.BASE
                && isActive(creature.world) && highlightedCreatures.contains(creature.getUniqueID());
    }

    @SubscribeEvent
    public void onClientTick(@NotNull TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isActive(Minecraft.getMinecraft().world)) refreshHighlightedCreatures();
    }

    @SubscribeEvent
    public void renderRange(@NotNull RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        BlockPos position = shownBox;
        if (position == null || !isActive(minecraft.world)) return;
        if (minecraft.getRenderViewEntity() == null) return;
        AxisAlignedBB range = CreatureBoxTileEntity.getRange(position);
        RenderManager renderManager = minecraft.getRenderManager();
        int previousMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        // Match pre-rewrite's border renderer: world-space vertices with one camera translation.
        GlStateManager.translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ);
        // Debug renderers may leave lighting enabled, which replaces the grid's yellow vertex colors.
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        // Let terrain cover the preview without letting the translucent grid write to the depth buffer.
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(1f);
        GlStateManager.resetColor();
        GL11.glBegin(GL11.GL_LINES);
        // Keep the sparse grid, with bright chunk divisions and outer edges.
        for (int offset = 0; offset <= range.maxX - range.minX; offset += GRID_SPACING) {
            float alpha;
            if (offset == 0 || offset == range.maxX - range.minX) alpha = 1f;
            else if (offset % 16 == 0) alpha = 0.65f;
            else alpha = 0.35f;
            GlStateManager.color(1f, 1f, 0f, alpha);
            double gridX = range.minX + offset;
            double gridY = range.minY + offset;
            double gridZ = range.minZ + offset;
            for (int face = 0; face < 2; face++) {
                double faceX = face == 0 ? range.minX : range.maxX;
                double faceY = face == 0 ? range.minY : range.maxY;
                double faceZ = face == 0 ? range.minZ : range.maxZ;

                GL11.glVertex3d(faceX, gridY, range.minZ);
                GL11.glVertex3d(faceX, gridY, range.maxZ);
                GL11.glVertex3d(faceX, range.minY, gridZ);
                GL11.glVertex3d(faceX, range.maxY, gridZ);

                GL11.glVertex3d(gridX, faceY, range.minZ);
                GL11.glVertex3d(gridX, faceY, range.maxZ);
                GL11.glVertex3d(range.minX, faceY, gridZ);
                GL11.glVertex3d(range.maxX, faceY, gridZ);

                GL11.glVertex3d(gridX, range.minY, faceZ);
                GL11.glVertex3d(gridX, range.maxY, faceZ);
                GL11.glVertex3d(range.minX, gridY, faceZ);
                GL11.glVertex3d(range.maxX, gridY, faceZ);
            }
        }
        GL11.glEnd();
        GlStateManager.resetColor();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.glLineWidth(1f);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        if (lightingEnabled) GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(previousMatrixMode);
    }
}
