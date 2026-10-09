package anightdazingzoroark.prift.api.creature;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * elemental damage is too obvious to explain
 * they also have special effects attached to them
 * */
public enum Element {
    NEUTRAL(0xFFFFFF, (target, strength) -> {}),
    FIRE(0xFA5700, (target, strength) -> {
        target.setFire((strength + 1) * 5 * 20);
    }),
    WATER(0x5D7FE5, (target, strength) -> {}),
    ELECTRIC(0xE5E833, (target, strength) -> {}),
    ICE(0x26D8E0, (target, strength) -> {}),
    //like dragon element from MH
    MESOZOIC(0x26D8E0, (target, strength) -> {}),
    WIND(0x000000, (target, strength) -> {}),
    POISON(0x000000, (target, strength) -> {}),
    //"evil" or "death" or anything dark, like a dark or ghost type pokemon
    SHADOW(0x000000, (target, strength) -> {}),
    //bright lights basically
    LIGHT(0x000000, (target, strength) -> {});

    public final int color;
    @NotNull
    public final BiConsumer<Entity, Integer> applyElementEffect;

    Element(int color, @NotNull BiConsumer<Entity, Integer> applyElementEffect) {
        this.color = color;
        this.applyElementEffect = applyElementEffect;
    }

    @NotNull
    public String getTranslatedName(int level) {
        String translatedName = I18n.format("move.creature.element." + this.name().toLowerCase());
        if (this == NEUTRAL) return translatedName;
        return translatedName + (level >= 0 ? " " + I18n.format("enchantment.level." + (level + 1)) : "");
    }

    @NotNull
    public String getTranslatedDescription() {
        return I18n.format("move.creature.element." + this.name().toLowerCase() + ".description");
    }
}
