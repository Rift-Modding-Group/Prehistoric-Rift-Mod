package anightdazingzoroark.prift.client.ui.widgets;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public class MoveIconTextures {
    @NotNull
    public static final UITexture PHYSICAL_MOVE = makeUITexture("physical_move", 0, 0);
    @NotNull
    public static final UITexture ELEMENTAL_MOVE = makeUITexture("elemental_move", 16, 0);
    @NotNull
    public static final UITexture STATUS_MOVE = makeUITexture("status_move", 32, 0);
    @NotNull
    public static final UITexture NEUTRAL_ELEMENT = makeUITexture("neutral_element", 0, 16);
    @NotNull
    public static final UITexture FIRE_ELEMENT = makeUITexture("fire_element", 16, 16);
    @NotNull
    public static final UITexture WATER_ELEMENT = makeUITexture("water_element", 32, 16);
    @NotNull
    public static final UITexture ELECTRIC_ELEMENT = makeUITexture("electric_element", 0, 32);
    @NotNull
    public static final UITexture ICE_ELEMENT = makeUITexture("ice_element", 16, 32);
    @NotNull
    public static final UITexture MESOZOIC_ELEMENT = makeUITexture("mesozoic_element", 32, 32);
    @NotNull
    public static final UITexture SOUND_ELEMENT = makeUITexture("sound_element", 0, 48);

    @NotNull
    public static Widget<?> getTextureForMoveType(@NotNull CreatureMoveBuilder.MoveType moveType) {
        return (switch (moveType) {
            case PHYSICAL -> PHYSICAL_MOVE;
            case ELEMENTAL -> ELEMENTAL_MOVE;
            default -> STATUS_MOVE;
        }).asWidget().size(16, 16).addTooltipLine(moveType.getTranslatedName());
    }

    @NotNull
    public static Widget<?> getTextureForElementType(@NotNull Element element, int level) {
        return (switch (element) {
            case FIRE -> FIRE_ELEMENT;
            case WATER -> WATER_ELEMENT;
            case ELECTRIC -> ELECTRIC_ELEMENT;
            case ICE -> ICE_ELEMENT;
            case MESOZOIC -> MESOZOIC_ELEMENT;
            case SOUND -> SOUND_ELEMENT;
            default -> NEUTRAL_ELEMENT;
        }).asWidget().size(16, 16)
                .addTooltipLine(element.getTranslatedName(level))
                .tooltipTextColor(0xFF000000 | element.color);
    }

    @NotNull
    public static Widget<?> getStaminaIcon() {
        UITexture staminaBackground = UITexture.builder()
                .location(RiftInitialize.MODID, "ui/hud_icons")
                .imageSize(256, 256)
                .subAreaXYWH(0, 0, 9, 9)
                .name("stamina")
                .defaultColorType()
                .build();
        UITexture stamina = UITexture.builder()
                .location(RiftInitialize.MODID, "ui/hud_icons")
                .imageSize(256, 256)
                .subAreaXYWH(9, 0, 9, 9)
                .name("stamina")
                .defaultColorType()
                .build();

        return staminaBackground.asWidget().size(9, 9).overlay(stamina);
    }

    @NotNull
    private static UITexture makeUITexture(@NotNull String name, int uvX, int uvY) {
        return UITexture.builder().imageSize(16, 16)
                .location(RiftInitialize.MODID, "ui/move_icons")
                .imageSize(48, 80)
                .subAreaXYWH(uvX, uvY, 16, 16)
                .adaptable(2).tiled()
                .name(name)
                .defaultColorType()
                .build();
    }
}
