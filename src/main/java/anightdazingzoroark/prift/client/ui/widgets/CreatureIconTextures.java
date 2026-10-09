package anightdazingzoroark.prift.client.ui.widgets;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.IRiftCreature;
import com.cleanroommc.modularui.drawable.UITexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreatureIconTextures {
    @NotNull
    public static UITexture getTextureForCreature(@Nullable IRiftCreature creature) {
        String creatureName = creature == null || creature.getCreatureType() == null
                ? "empty" : creature.getCreatureType().getName();
        return UITexture.builder()
                .location(RiftInitialize.MODID, "icons/" + creatureName + "_icon")
                .imageSize(24, 24)
                .name(creatureName + "_icon")
                .defaultColorType()
                .build();
    }
}
