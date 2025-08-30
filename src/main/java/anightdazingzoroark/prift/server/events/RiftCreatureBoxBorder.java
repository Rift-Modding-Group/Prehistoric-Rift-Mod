package anightdazingzoroark.prift.server.events;

import anightdazingzoroark.prift.helper.ChunkPosWithVerticality;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftNewTileEntityCreatureBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RiftCreatureBoxBorder {
    private final List<ChunkPosWithVerticality> chunks;
    private int timer;

    public RiftCreatureBoxBorder(List<ChunkPosWithVerticality> chunks, int displayTime) {
        this.chunks = chunks;
        this.timer = displayTime;
    }

    @SubscribeEvent
    public void renderBorder(RenderWorldLastEvent event) {
        if (this.timer <= 0) {
            MinecraftForge.EVENT_BUS.unregister(this);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        GL11.glPushMatrix();
        GL11.glTranslated(-camX, -camY, -camZ);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.0f);
        GL11.glColor4f(1.0f, 1.0f, 0.0f, 1.0f);

        GL11.glBegin(GL11.GL_LINES);
        //for perimeter around chunk borders drawn horizontally
        int minY = this.yBounds()[0];
        int maxY = this.yBounds()[1];
        for (int y = minY; y <= maxY; y++) this.drawHorizontallySquareFromChunks(y);

        //for perimeter around chunk borders drawn vertically
        int minX = this.xBounds()[0];
        int maxX = this.xBounds()[1];
        for (int x = minX; x <= maxX; x++) this.drawVerticallySquareFromChunks(true, x);

        int minZ = this.zBounds()[0];
        int maxZ = this.zBounds()[1];
        for (int z = minZ; z <= maxZ; z++) this.drawVerticallySquareFromChunks(false, z);

        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();

        this.timer--;
    }

    private void drawLine(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        GL11.glVertex3d(xMin, yMin, zMin);
        GL11.glVertex3d(xMax, yMax, zMax);
    }

    private int[][] horizontalCornersFromChunks() {
        int minX = this.xBounds()[0];
        int maxX = this.xBounds()[1];
        int minZ = this.zBounds()[0];
        int maxZ = this.zBounds()[1];

        return new int[][]{
                {minX, minZ},
                {maxX, minZ},
                {maxX, maxZ},
                {minX, maxZ}
        };
    }

    private void drawHorizontallySquareFromChunks(int y) {
        int[][] corners = this.horizontalCornersFromChunks();

        this.drawLine(
                corners[0][0], y, corners[0][1],
                corners[1][0], y, corners[1][1]
        );

        this.drawLine(
                corners[1][0], y, corners[1][1],
                corners[2][0], y, corners[2][1]
        );

        this.drawLine(
                corners[2][0], y, corners[2][1],
                corners[3][0], y, corners[3][1]
        );

        this.drawLine(
                corners[3][0], y, corners[3][1],
                corners[0][0], y, corners[0][1]
        );
    }

    private int[][] verticalCornersFromChunks(boolean flipped) {
        int minY = Math.max(0, this.chunks.get(0).getYStart());
        int maxY = this.chunks.get(this.chunks.size() - 1).getYEnd() + 1;

        if (flipped) {
            int minZ = this.zBounds()[0];
            int maxZ = this.zBounds()[1];

            return new int[][]{
                    {minY, minZ},
                    {minY, maxZ},
                    {maxY, maxZ},
                    {maxY, minZ}
            };
        }
        else {
            int minX = this.xBounds()[0];
            int maxX = this.xBounds()[1];

            return new int[][]{
                    {minX, minY},
                    {maxX, minY},
                    {maxX, maxY},
                    {minX, maxY}
            };
        }
    }

    private void drawVerticallySquareFromChunks(boolean flipped, int anchorPos) {
        int[][] corners = this.verticalCornersFromChunks(flipped);

        if (flipped) {
            this.drawLine(
                    anchorPos, corners[0][0], corners[0][1],
                    anchorPos, corners[1][0], corners[1][1]
            );

            this.drawLine(
                    anchorPos, corners[1][0], corners[1][1],
                    anchorPos, corners[2][0], corners[2][1]
            );

            this.drawLine(
                    anchorPos, corners[2][0], corners[2][1],
                    anchorPos, corners[3][0], corners[3][1]
            );

            this.drawLine(
                    anchorPos, corners[3][0], corners[3][1],
                    anchorPos, corners[0][0], corners[0][1]
            );
        }
        else {
            this.drawLine(
                    corners[0][0], corners[0][1], anchorPos,
                    corners[1][0], corners[1][1], anchorPos
            );

            this.drawLine(
                    corners[1][0], corners[1][1], anchorPos,
                    corners[2][0], corners[2][1], anchorPos
            );

            this.drawLine(
                    corners[2][0], corners[2][1], anchorPos,
                    corners[3][0], corners[3][1], anchorPos
            );

            this.drawLine(
                    corners[3][0], corners[3][1], anchorPos,
                    corners[0][0], corners[0][1], anchorPos
            );
        }
    }

    private int[] xBounds() {
        int minX = this.chunks.get(0).getXStart();
        int maxX = this.chunks.get(this.chunks.size() - 1).getXEnd() + 1;
        return new int[]{minX, maxX};
    }

    private int[] yBounds() {
        int minY = Math.max(0, this.chunks.get(0).getYStart());
        int maxY = this.chunks.get(this.chunks.size() - 1).getYEnd() + 1;
        return new int[]{minY, maxY};
    }

    private int[] zBounds() {
        int minZ = this.chunks.get(0).getZStart();
        int maxZ = this.chunks.get(this.chunks.size() - 1).getZEnd() + 1;
        return new int[]{minZ, maxZ};
    }

    public static class RiftCreatureBorderHandler {
        @SubscribeEvent
        public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            EntityPlayer player = event.getEntityPlayer();
            IBlockState iblockstate = event.getWorld().getBlockState(event.getPos());

            if (iblockstate.getBlock() instanceof RiftCreatureBox && player.isSneaking() && event.getWorld().isRemote) {
                RiftNewTileEntityCreatureBox tileEntity = (RiftNewTileEntityCreatureBox) event.getWorld().getTileEntity(event.getPos());
                if (tileEntity == null) return;
                RiftCreatureBoxBorder renderer = new RiftCreatureBoxBorder(
                        tileEntity.chunksWithinDeploymentRange(),
                        100
                );
                MinecraftForge.EVENT_BUS.register(renderer);
            }
        }
    }
}
