package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.client.hud.TameProgressHUD;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderHUD(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.gameSettings.hideGUI || minecraft.objectMouseOver == null) return;

        TameProgressHUD.renderTamingProgress(minecraft, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight());
    }
}
