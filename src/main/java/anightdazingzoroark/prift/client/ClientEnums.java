package anightdazingzoroark.prift.client;

import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * a bunch of enums meant for use on client
 * */
public class ClientEnums {
    public enum StaminaUse {
        NONE,
        LIGHT,
        MEDIUM,
        HEAVY;

        @NotNull
        public String getTranslatedName() {
            return I18n.format("gui.prift.move_stamina_use."+this.toString().toLowerCase(Locale.ROOT));
        }

        @NotNull
        public static StaminaUse getUse(float usage) {
            if (usage <= 0) return NONE;
            else if (usage > 0 && usage <= 0.05f) return LIGHT;
            else if (usage > 0.05f && usage <= 0.1f) return MEDIUM;
            else return HEAVY;
        }
    }
}
