package anightdazingzoroark.prift.server.enums;

import net.minecraft.client.resources.I18n;

public enum CreatureCategory {
    ALL,
    DINOSAUR,
    MAMMAL,
    REPTILE,
    BIRD,
    FISH,
    INVERTEBRATE;

    public String getTranslatedName(boolean plural) {
        String pluralAdd = (plural && !this.equals(ALL)) ? "_plural" : "";
        return I18n.format("type.creature."+this.name().toLowerCase()+pluralAdd);
    }

    public static CreatureCategory safeValOf(String string) {
        for (CreatureCategory category : CreatureCategory.values()) {
            if (category.name().equalsIgnoreCase(string)) {
                return category;
            }
        }
        return null;
    }
}
