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
    private final double centerX, centerY, centerZ, radius;
    private int timer;

    public RiftCreatureBoxBorder(double centerX, double centerY, double centerZ, double radius, int displayTime) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
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
        drawSphereLines(this.centerX, this.centerY, this.centerZ, this.radius);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();

        this.timer--;
    }

    private void drawSphereLines(double centerX, double centerY, double centerZ, double radius) {
        int segments = 36; // Number of segments to approximate the sphere

        // Draw latitude lines
        for (int i = 0; i <= segments; i++) {
            double theta1 = Math.PI * i / segments;
            double theta2 = Math.PI * (i + 1) / segments;

            for (int j = 0; j < segments; j++) {
                double phi1 = 2 * Math.PI * j / segments;
                double phi2 = 2 * Math.PI * (j + 1) / segments;

                double x1 = centerX + radius * Math.sin(theta1) * Math.cos(phi1);
                double y1 = centerY + radius * Math.cos(theta1);
                double z1 = centerZ + radius * Math.sin(theta1) * Math.sin(phi1);

                double x2 = centerX + radius * Math.sin(theta1) * Math.cos(phi2);
                double y2 = centerY + radius * Math.cos(theta1);
                double z2 = centerZ + radius * Math.sin(theta1) * Math.sin(phi2);

                double x3 = centerX + radius * Math.sin(theta2) * Math.cos(phi1);
                double y3 = centerY + radius * Math.cos(theta2);
                double z3 = centerZ + radius * Math.sin(theta2) * Math.sin(phi1);

                // Draw lines
                GL11.glVertex3d(x1, y1, z1);
                GL11.glVertex3d(x2, y2, z2);

                GL11.glVertex3d(x1, y1, z1);
                GL11.glVertex3d(x3, y3, z3);
            }
        }
    }


    public static class RiftCreatureBorderHandler {
        @SubscribeEvent
        public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            EntityPlayer player = event.getEntityPlayer();
            IBlockState iblockstate = event.getWorld().getBlockState(event.getPos());

            if (iblockstate.getBlock() instanceof RiftCreatureBox && player.isSneaking() && event.getWorld().isRemote) {
                RiftTileEntityCreatureBox tileEntity = (RiftTileEntityCreatureBox) event.getWorld().getTileEntity(event.getPos());
                RiftCreatureBoxBorder renderer = new RiftCreatureBoxBorder(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), tileEntity.getWanderRange(),  100);
                MinecraftForge.EVENT_BUS.register(renderer);
            }
        }
    }
}
