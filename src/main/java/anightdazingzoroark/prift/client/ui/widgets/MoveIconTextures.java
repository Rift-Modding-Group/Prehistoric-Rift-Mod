package anightdazingzoroark.prift.client.ui.widgets;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.Element;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widget.Widget;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

public class MoveIconTextures {
    @NotNull
    public static final UITexture PHYSICAL_MOVE = makeUITexture("physical_move", 0, 0);
    @NotNull
    public static final UITexture SPECIAL_MOVE = makeUITexture("special_move", 16, 0);
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
    public static final UITexture SONIC_ELEMENT = makeUITexture("sonic_element", 0, 48);
    @NotNull
    public static final UITexture EARTH_ELEMENT = makeUITexture("earth_element", 16, 48);
    @NotNull
    public static final UITexture GRASS_ELEMENT = makeUITexture("grass_element", 32, 48);
    @NotNull
    public static final UITexture WIND_ELEMENT = makeUITexture("wind_element", 0, 64);
    @NotNull
    public static final UITexture POISON_ELEMENT = makeUITexture("poison_element", 16, 64);
    @NotNull
    public static final UITexture DARK_ELEMENT = makeUITexture("dark_element", 32, 64);
    @NotNull
    public static final UITexture LIGHT_ELEMENT = makeUITexture("light_element", 0, 80);
    @NotNull
    public static final UITexture STEEL_ELEMENT = makeUITexture("steel_element", 16, 80);
    @NotNull
    public static final UITexture GUARD_ELEMENT = makeUITexture("guard_element", 32, 80);

    @NotNull
    public static Widget<?> getTextureForMoveType(@NotNull CreatureMoveBuilder.MoveType moveType) {
        return (switch (moveType) {
            case PHYSICAL -> PHYSICAL_MOVE;
            case SPECIAL -> SPECIAL_MOVE;
            default -> STATUS_MOVE;
        }).asWidget().size(16, 16).addTooltipLine(moveType.getTranslatedName());
    }

    @NotNull
    public static Widget<?> getTextureForElementType(@NotNull Element element) {
        return getElementTexture(element).asWidget().size(16, 16)
                .tooltipBuilder(tooltip -> {
                    tooltip.add(getElementTexture(element).asIcon().size(16)).space()
                            .addLine(IKey.str(element.getTranslatedName()).color(0xFF000000 | element.color).asTextIcon());
                    ToDoubleFunction<Element> effectiveness = target -> target.damageMultiplierFromAttackElement(element);
                    addElementMatchupSection(tooltip, "gui.prift.element.effective_against", effectiveness, value -> value > 1D);
                    addElementMatchupSection(tooltip, "gui.prift.element.not_effective_against", effectiveness, value -> value > 0D && value < 1D);
                    addElementMatchupSection(tooltip, "gui.prift.element.does_not_affect", effectiveness, value -> value == 0D);
                });
    }

    @NotNull
    public static Widget<?> getCreatureElementWidget(@NotNull Element[] elements) {
        Element[] defendingElements = Arrays.stream(elements).distinct().toArray(Element[]::new);
        return new Widget<>().size(Math.max(0, defendingElements.length * 14 - 2), 12)
                .background((context, x, y, width, height, theme) ->
                        IntStream.range(0, defendingElements.length).forEach(index ->
                                getElementTexture(defendingElements[index]).draw(context, x + index * 14, y, 12, 12, theme)))
                .tooltipBuilder(tooltip -> {
                    Arrays.stream(defendingElements).forEach(element ->
                            tooltip.add(getElementTexture(element).asIcon().size(16)).space()
                                    .addLine(IKey.str(element.getTranslatedName()).color(0xFF000000 | element.color).asTextIcon()));
                    ToDoubleFunction<Element> effectiveness = attack -> Arrays.stream(defendingElements)
                            .mapToDouble(element -> element.damageMultiplierFromAttackElement(attack))
                            .reduce(1D, (combined, multiplier) -> combined * multiplier);
                    addElementMatchupSection(tooltip, "gui.prift.element.weak_to", effectiveness, value -> value > 1D);
                    addElementMatchupSection(tooltip, "gui.prift.element.resistant_to", effectiveness, value -> value > 0D && value < 1D);
                    addElementMatchupSection(tooltip, "gui.prift.element.immune_to", effectiveness, value -> value == 0D);
                });
    }

    private static void addElementMatchupSection(
            @NotNull RichTooltip tooltip, @NotNull String translationKey,
            @NotNull ToDoubleFunction<Element> effectiveness, @NotNull DoublePredicate matches
    ) {
        List<Element> targets = Arrays.stream(Element.values())
                .filter(target -> matches.test(effectiveness.applyAsDouble(target)))
                .toList();
        tooltip.spaceLine().addLine(IKey.lang(translationKey));
        if (targets.isEmpty()) {
            tooltip.addLine(IKey.lang("gui.prift.element.none").color(0xFFAAAAAA).asTextIcon());
        } else {
            targets.forEach(target -> tooltip.add(getElementTexture(target).asIcon().size(16)).space()
                    .addLine(IKey.str(target.getTranslatedName()).color(0xFF000000 | target.color).asTextIcon()));
        }
    }

    @NotNull
    private static UITexture getElementTexture(@NotNull Element element) {
        return switch (element) {
            case NEUTRAL -> NEUTRAL_ELEMENT;
            case FIRE -> FIRE_ELEMENT;
            case WATER -> WATER_ELEMENT;
            case ELECTRIC -> ELECTRIC_ELEMENT;
            case ICE -> ICE_ELEMENT;
            case MESOZOIC -> MESOZOIC_ELEMENT;
            case SONIC -> SONIC_ELEMENT;
            case EARTH -> EARTH_ELEMENT;
            case GRASS -> GRASS_ELEMENT;
            case WIND -> WIND_ELEMENT;
            case POISON -> POISON_ELEMENT;
            case DARK -> DARK_ELEMENT;
            case LIGHT -> LIGHT_ELEMENT;
            case STEEL -> STEEL_ELEMENT;
            case GUARD -> GUARD_ELEMENT;
        };
    }

    public static void drawCreatureElements(@NotNull Element[] elements, float x, float y, int size) {
        IntStream.range(0, elements.length).forEach(index ->
                drawElementIcon(elements[index], x + index * (size + 1), y, size));
    }

    public static void drawElementIcon(@NotNull Element element, float x, float y, int size) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        getElementTexture(element).draw(x, y, size, size);
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
        return UITexture.builder()
                .location(RiftInitialize.MODID, "ui/move_icons")
                .imageSize(48, 96)
                .subAreaXYWH(uvX, uvY, 16, 16)
                .name(name)
                .build();
    }
}
