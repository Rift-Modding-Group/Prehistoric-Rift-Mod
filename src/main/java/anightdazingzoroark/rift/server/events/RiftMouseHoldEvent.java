package anightdazingzoroark.rift.server.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RiftMouseHoldEvent extends Event {
    private int ticks;
    private int mouseButton;

    public RiftMouseHoldEvent(int ticks) {
        this(-1, ticks);
    }

    public RiftMouseHoldEvent(int mouseButton, int ticks) {
        this.mouseButton = mouseButton;
        this.ticks = ticks;
    }

    public int getMouseButton() {
        return this.mouseButton;
    }

    public int getTicks() {
        return this.ticks;
    }

    public static class Handler {
        private int leftTicks = 0;
        private int rightTicks = 0;
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().world != null) {
                // Check the mouse button state at the end of each tick
                boolean leftButtonDown = org.lwjgl.input.Mouse.isButtonDown(0);
                boolean rightButtonDown = org.lwjgl.input.Mouse.isButtonDown(1);

                if (leftButtonDown) {
                    // Left mouse button is being held
                    MinecraftForge.EVENT_BUS.post(new RiftMouseHoldEvent(0, leftTicks++));
                }
                else {
                    leftTicks = 0;
                    MinecraftForge.EVENT_BUS.post(new RiftMouseHoldEvent(0));
                }

                if (rightButtonDown) {
                    MinecraftForge.EVENT_BUS.post(new RiftMouseHoldEvent(1, rightTicks++));
                }
                else {
                    rightTicks = 0;
                    MinecraftForge.EVENT_BUS.post(new RiftMouseHoldEvent(0));
                }
            }
        }
    }
}
