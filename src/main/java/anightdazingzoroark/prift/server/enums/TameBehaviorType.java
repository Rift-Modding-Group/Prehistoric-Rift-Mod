package anightdazingzoroark.prift.server.enums;

import net.minecraft.client.resources.I18n;

public enum TameBehaviorType {
    ASSIST,
    NEUTRAL,
    AGGRESSIVE,
    PASSIVE,
    TURRET;

    public String getTranslatedName() {
        return I18n.format("behavior_type."+this.name().toLowerCase());
    }
}
