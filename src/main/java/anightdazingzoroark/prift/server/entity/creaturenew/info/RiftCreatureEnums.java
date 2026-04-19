package anightdazingzoroark.prift.server.entity.creaturenew.info;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.resources.I18n;

public class RiftCreatureEnums {
    public static enum InventoryGearType {
        SADDLE,
        LARGE_WEAPON
    }

    public static enum EnergyRechargeCategory {
        FAST,
        SLOW;
    }

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

        public static RiftCreatureType.CreatureCategory safeValOf(String string) {
            for (RiftCreatureType.CreatureCategory category : RiftCreatureType.CreatureCategory.values()) {
                if (category.name().equalsIgnoreCase(string)) {
                    return category;
                }
            }
            return null;
        }
    }

    public enum CreatureDiet {
        HERBIVORE,
        FUNGIVORE,
        CARNIVORE,
        PISCIVORE,
        INSECTIVORE,
        OMNIVORE,
        SAXUMAVORE;

        public String getTranslatedName() {
            return I18n.format("diet.creature."+this.name().toLowerCase());
        }
    }

    //additional movement options in addition to walking
    public enum Movement {
        JUMP,
        CLIMB,
        SWIM,
        FLY,
        SLOW_FALL,
        BURROW
    }

    public static enum LevelupRate {
        VERY_SLOW(1.6D),
        SLOW(1.4D),
        NORMAL(1.2D),
        FAST(1D),
        VERY_FAST(0.8D);

        private final double rate;

        LevelupRate(double rate) {
            this.rate = rate;
        }

        public double getRate() {
            return this.rate;
        }

        public String getTranslatedName() {
            return I18n.format("levelup_rate.creature."+this.name().toLowerCase());
        }
    }

    //these mostly influence wild only behaviors
    public static enum Behavior {
        HERDER, //will use herding or pack hunting behaviors
        DOCILE, //will retaliate when attacked
        BLOCK_BREAKER, //will break blocks in front when pursuing a target
        NOCTURNAL; //will be active at night and sleep at day

        public String getTranslatedName() {
            return I18n.format("behavior."+this.name().toLowerCase());
        }
    }
}
