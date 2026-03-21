package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.cleanroommc.modularui.drawable.UITexture;
import net.minecraft.util.ResourceLocation;

public class RiftUIIcons {
    public static final UITexture BACK = icon("back", 0, 0);
    public static final UITexture LEFT_ARROW = icon("leftArrow", 16, 0);
    public static final UITexture RIGHT_ARROW = icon("rightArrow", 32, 0);
    public static final UITexture LEFT_ARROW_SELECTED = icon("leftArrowSelected", 0, 16);
    public static final UITexture RIGHT_ARROW_SELECTED = icon("rightArrowSelected", 16, 16);
    public static final UITexture BABY = icon("baby", 32, 16);

    private static UITexture icon(String name, int x, int y) {
        return UITexture.builder()
                .location(new ResourceLocation(RiftInitialize.MODID, "textures/ui/ui_icons.png"))
                .imageSize(48, 32)
                .subAreaXYWH(x, y, 16, 16)
                .iconColorType()
                .name(name)
                .build();
    }

    public static UITexture creatureIcon(RiftCreatureType creatureType) {
        return UITexture.builder()
                .location(new ResourceLocation(RiftInitialize.MODID, "textures/icons/"+creatureType.toString().toLowerCase()+"_icon.png"))
                .imageSize(24, 24)
                .subAreaXYWH(0, 0, 24, 24)
                .iconColorType()
                .name(creatureType.friendlyName)
                .build();
    }

    public static UITexture creatureIllustration(RiftCreatureType creatureType) {
        return UITexture.builder()
                .location(new ResourceLocation(RiftInitialize.MODID, "textures/journal/"+creatureType.toString().toLowerCase()+"_journal.png"))
                .imageSize(240, 180)
                .subAreaXYWH(0, 0, 240, 180)
                .iconColorType()
                .name(creatureType.friendlyName)
                .build();
    }
}
