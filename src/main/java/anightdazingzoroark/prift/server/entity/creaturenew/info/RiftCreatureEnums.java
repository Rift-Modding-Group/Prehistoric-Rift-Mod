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
    //boosts one stat but lowers another, and some natures do nothing
    //soon this will be used to affect creature ai when tamed or wild
    public enum Nature {
        STEADY(Stats.HEALTH, Stats.HEALTH),
        COMPOSED(Stats.MELEE_DAMAGE, Stats.MELEE_DAMAGE),
        TEMPERATE(Stats.ELEMENTAL_DAMAGE, Stats.ELEMENTAL_DAMAGE),
        PATIENT(Stats.STAMINA, Stats.STAMINA),
        ADAPTABLE(Stats.SPEED, Stats.SPEED),

        GUARDED(Stats.HEALTH, Stats.MELEE_DAMAGE),
        GROUNDED(Stats.HEALTH, Stats.ELEMENTAL_DAMAGE),
        STOUT(Stats.HEALTH, Stats.STAMINA),
        CAREFUL(Stats.HEALTH, Stats.SPEED),

        RECKLESS(Stats.MELEE_DAMAGE, Stats.HEALTH),
        BLUNT(Stats.MELEE_DAMAGE, Stats.ELEMENTAL_DAMAGE),
        IMPETUOUS(Stats.MELEE_DAMAGE, Stats.STAMINA),
        BRUTISH(Stats.MELEE_DAMAGE, Stats.SPEED),

        FERVID(Stats.ELEMENTAL_DAMAGE, Stats.HEALTH),
        SPIRITUAL(Stats.ELEMENTAL_DAMAGE, Stats.MELEE_DAMAGE),
        INTENSE(Stats.ELEMENTAL_DAMAGE, Stats.STAMINA),
        FOCUSED(Stats.ELEMENTAL_DAMAGE, Stats.SPEED),

        TENACIOUS(Stats.STAMINA, Stats.HEALTH),
        DISCIPLINED(Stats.STAMINA, Stats.MELEE_DAMAGE),
        RESERVED(Stats.STAMINA, Stats.ELEMENTAL_DAMAGE),
        CALM(Stats.STAMINA, Stats.SPEED),

        SKITTISH(Stats.SPEED, Stats.HEALTH),
        NIMBLE(Stats.SPEED, Stats.MELEE_DAMAGE),
        RESTLESS(Stats.SPEED, Stats.ELEMENTAL_DAMAGE),
        HASTY(Stats.SPEED, Stats.STAMINA);

        private final Stats statToBoost;
        private final Stats statToWeaken;

        Nature(Stats statToBoost, Stats statToWeaken) {
            this.statToBoost = statToBoost;
            this.statToWeaken = statToWeaken;
        }

        public Stats getStatToBoost() {
            return this.statToBoost;
        }

        public Stats getStatToWeaken() {
            return this.statToWeaken;
        }

        public boolean isNeutral() {
            return this.statToBoost.equals(this.statToWeaken);
        }

        public boolean boostsStat(Stats stat) {
            return !this.isNeutral() && this.statToBoost.equals(stat);
        }

        public boolean weakensStat(Stats stat) {
            return !this.isNeutral() && this.statToWeaken.equals(stat);
        }

        public String getTranslatedName() {
            return I18n.format("nature.creature."+this.name().toLowerCase());
        }
    }
}
