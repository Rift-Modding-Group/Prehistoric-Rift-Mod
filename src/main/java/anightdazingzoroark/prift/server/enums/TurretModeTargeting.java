package anightdazingzoroark.prift.server.enums;

import net.minecraft.client.resources.I18n;

public enum TurretModeTargeting {
    PLAYERS,
    PLAYERS_AND_OTHER_TAMES,
    HOSTILES,
    ALL;

    public String getTranslatedName() {
        return I18n.format("radial.choice."+this.name().toLowerCase());
    }
}
