package anightdazingzoroark.rift.server.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.input.*;

public class RiftMouseHoldEvent extends Event {
    private int ticks;
    private int mouseButton;
    private boolean released;

    public RiftMouseHoldEvent(int ticks) {
        this(ticks, false);
    }

    public RiftMouseHoldEvent(int ticks, boolean released) {
        this(-1, ticks, released);
    }

    public RiftMouseHoldEvent(int mouseButton, int ticks, boolean released) {
        this.mouseButton = mouseButton;
        this.ticks = ticks;
        this.released = released;
    }

    public int getMouseButton() {
        return this.mouseButton;
    }

    public int getTicks() {
        return this.ticks;
    }

    public boolean isReleased() {
        return this.released;
    }

    public static class Handler {
        private int leftTicks = 0;
        private int rightTicks = 0;
        private boolean leftIsReleased = false;
        private boolean rightIsReleased = false;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().currentScreen == null) {
                boolean leftButtonDown = Mouse.isButtonDown(0) && !Mouse.isButtonDown(1);
                boolean rightButtonDown = !Mouse.isButtonDown(0) && Mouse.isButtonDown(1);

                if (leftButtonDown) {
                    MinecraftForge.EVENT_BUS.post(new RiftMouseHoldEvent(0, leftTicks++, false));
                    leftIsReleased = false;
                }
                else {
                    if (!leftIsReleased) {
                        MinecraftForge.EVENT_BUS.post(new RiftMouseHoldEvent(0, leftTicks, true));
                        leftTicks = 0;
                        leftIsReleased = true;
                    }
                }

                if (rightButtonDown) {
                    MinecraftForge.EVENT_BUS.post(new RiftMouseHoldEvent(1, rightTicks++, false));
                    rightIsReleased = false;
                }
                else {
                    if (!rightIsReleased) {
                        MinecraftForge.EVENT_BUS.post(new RiftMouseHoldEvent(1, rightTicks, true));
                        rightTicks = 0;
                        rightIsReleased = true;
                    }
                }
            }
        }
    }
}
