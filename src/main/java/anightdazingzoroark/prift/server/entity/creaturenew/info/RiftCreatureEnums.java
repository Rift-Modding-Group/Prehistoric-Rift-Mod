package anightdazingzoroark.prift.server.entity.creaturenew.info;

import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import net.minecraft.client.resources.I18n;

public class RiftCreatureEnums {
    public static enum InventoryGearType {
        SADDLE,
        LARGE_WEAPON
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

    //Stats are to be on a scale of 0.5-10 with steps of 0.5
    //and will be represented as stars on most UIs
    public enum Stats {
        //health is well health
        HEALTH,
        //melee damage is damage from charge attacks, physical moves, and physical projectiles
        MELEE_DAMAGE,
        //elemental damage is damage from attacks with elemental properties, like breathing fire or exploding
        ELEMENTAL_DAMAGE,
        //stamina is required to perform actions (movement, using moves) and recharges at the same rate for each creature
        STAMINA,
        //speed is movement speed, specifically on land. in water and air movement,
        //all creatures have different movement speeds that factor this in and their own individual fly and swim multipliers.
        //its unique among other stats in that it is on a scale of 1-5, has steps of 1, and is unaffected by leveling
        SPEED;
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

    //just like how it works in pokemon, some creatures have a nature that
    //boosts one stat bu lowers another, and some natures do nothing
    public enum Nature {}
}
