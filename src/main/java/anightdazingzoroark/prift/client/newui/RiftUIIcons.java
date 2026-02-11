package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.RiftInitialize;
import com.cleanroommc.modularui.drawable.UITexture;
import net.minecraft.util.ResourceLocation;

public class RiftUIIcons {
    public static final UITexture BACK = icon("back", 0, 0);

    private static UITexture icon(String name, int x, int y) {
        return UITexture.builder()
                .location(new ResourceLocation(RiftInitialize.MODID, "textures/ui/ui_icons.png"))
                .imageSize(32, 32)
                .subAreaXYWH(x, y, 16, 16)
                .iconColorType()
                .name(name)
                .build();
    }
}
