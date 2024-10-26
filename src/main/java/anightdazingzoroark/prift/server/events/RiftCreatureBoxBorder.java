package anightdazingzoroark.prift.server.events;

import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityCreatureBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class RiftCreatureBoxBorder {
    private final double minX, maxX, minY, maxY, minZ, maxZ;
    private int timer;

    public RiftCreatureBoxBorder(double minX, double maxX, double minY, double maxY, double minZ, double maxZ, int displayTime) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
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
        this.drawLine(this.minX, this.minZ, this.maxX, this.minZ); // Bottom border
        this.drawLine(this.maxX, this.minZ, this.maxX, this.maxZ); // Right border
        this.drawLine(this.maxX, this.maxZ, this.minX, this.maxZ); // Top border
        this.drawLine(this.minX, this.maxZ, this.minX, this.minZ); // Left border
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();

        this.timer--;
    }

    private void drawLine(double x1, double z1, double x2, double z2) {
        for (int y = this.minY < 0 ? 0 : (int)this.minY; y <= this.maxY; y++ ) {
            GL11.glVertex3d(x1, y, z1);
            GL11.glVertex3d(x2, y, z2);
        }
    }

    public static class RiftCreatureBorderHandler {
        @SubscribeEvent
        public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            EntityPlayer player = event.getEntityPlayer();
            IBlockState iblockstate = event.getWorld().getBlockState(event.getPos());

            if (iblockstate.getBlock() instanceof RiftCreatureBox && player.isSneaking() && event.getWorld().isRemote) {
                RiftTileEntityCreatureBox tileEntity = (RiftTileEntityCreatureBox) event.getWorld().getTileEntity(event.getPos());
                double minX = event.getPos().getX() - tileEntity.getWanderRange();
                double maxX = event.getPos().getX() + tileEntity.getWanderRange();
                double minY = event.getPos().getY() - tileEntity.getWanderRange();
                double maxY = event.getPos().getY() + tileEntity.getWanderRange();
                double minZ = event.getPos().getZ() - tileEntity.getWanderRange();
                double maxZ = event.getPos().getZ() + tileEntity.getWanderRange();

                // Start rendering the world border
                RiftCreatureBoxBorder renderer = new RiftCreatureBoxBorder(minX, maxX, minY, maxY, minZ, maxZ, 100);
                MinecraftForge.EVENT_BUS.register(renderer);
            }
        }
    }
}
