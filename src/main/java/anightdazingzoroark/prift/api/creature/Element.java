package anightdazingzoroark.prift.api.creature;

import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * should be obvious to explain lol
 * */
public enum Element {
    //no resistances nor super effectivenesses lol
    NEUTRAL(
            0xFFFFFF,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.DARK);
                }
            },
            Set.of(),
            Set.of()
    ),
    //Fire cannot burn itself; gusts feed flames.
    FIRE(0xFA5700,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.WATER, Element.EARTH);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.GRASS, Element.ICE, Element.WIND);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.FIRE);
                }
            }
    ),
    WATER(0x5D7FE5,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.ELECTRIC, Element.GRASS);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.FIRE, Element.ICE);
                }
            },
            Set.of()
    ),
    ELECTRIC(0xE5E833,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.EARTH);
                }
            },
            Set.of(),
            Set.of()
    ),
    ICE(0x26D8E0,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.FIRE, Element.STEEL, Element.SONIC);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.WIND, Element.ICE);
                }
            },
            Set.of()
    ),
    //like dragon element from MH, or the dragon type from pokemon
    MESOZOIC(0xDB216A,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.ICE, Element.MESOZOIC);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.NEUTRAL, Element.FIRE, Element.WATER, Element.ELECTRIC, Element.GRASS);
                }
            },
            Set.of()
    ),
    //loud sounds
    SONIC(0x21956E,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.EARTH, Element.WIND);
                }
            },
            Set.of(),
            Set.of()
    ),
    EARTH(0x714E2F,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.WATER, Element.GRASS, Element.ICE, Element.STEEL);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.NEUTRAL, Element.POISON, Element.SONIC, Element.FIRE, Element.WIND);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.ELECTRIC);
                }
            }
    ),
    GRASS(0x27B319,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.FIRE, Element.ICE, Element.POISON);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.WATER, Element.ELECTRIC, Element.EARTH, Element.LIGHT);
                }
            },
            Set.of()
    ),
    //Moving air is blocked by earth and steel, and disperses fumes and sound.
    WIND(0x898F96,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.EARTH, Element.STEEL);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.POISON, Element.SONIC);
                }
            },
            Set.of()
    ),
    //Earth absorbs toxins, purifying light breaks them down, and wind disperses fumes.
    POISON(0x068630,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.EARTH, Element.LIGHT, Element.WIND);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.GRASS);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.POISON);
                }
            }
    ),
    //"evil" or "death" or anything dark, like a dark or ghost type pokemon
    //Light and darkness counter one another.
    DARK(0x473E68,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.LIGHT);
                }
            },
            Set.of(),
            Set.of()
    ),
    //bright lights basically
    //Polished steel reflects light.
    LIGHT(0xE216FC,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.DARK, Element.STEEL);
                }
            },
            Set.of(),
            Set.of()
    ),
    //Metal melts, conducts electricity, corrodes, and buckles, but blocks gusts and cannot be poisoned.
    STEEL(0xCDC7DC,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.FIRE);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.NEUTRAL, Element.ICE, Element.GRASS, Element.LIGHT, Element.WIND, Element.ELECTRIC, Element.WATER, Element.MESOZOIC, Element.SONIC, Element.DARK, Element.STEEL, Element.EARTH);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.POISON);
                }
            }
    ),
    //this type has mostly status moves, but creatures can be of this type
    //basically something like defense
    GUARD(0xCC7627,
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.SONIC, Element.POISON, Element.MESOZOIC, Element.DARK);
                }
            },
            new DeferredElementSet() {
                @Override
                @NotNull
                protected Set<Element> elements() {
                    return Set.of(Element.NEUTRAL, Element.STEEL, Element.WIND);
                }
            },
            Set.of()
    );

    public final int color;
    @NotNull
    private final Set<Element> weaknesses;
    @NotNull
    private final Set<Element> resistances;
    @NotNull
    private final Set<Element> immunities;

    Element(
            int color,
            @NotNull Set<Element> weaknesses,
            @NotNull Set<Element> resistances,
            @NotNull Set<Element> immunities
    ) {
        this.color = color;
        this.weaknesses = Collections.unmodifiableSet(weaknesses);
        this.resistances = Collections.unmodifiableSet(resistances);
        this.immunities = Collections.unmodifiableSet(immunities);
    }

    public double damageMultiplierFromAttackElement(@NotNull Element attackElement) {
        if (this.weaknesses.contains(attackElement)) return 2D;
        else if (this.resistances.contains(attackElement)) return 0.5D;
        else if (this.immunities.contains(attackElement)) return 0D;
        return 1;
    }

    @NotNull
    public String getTranslatedName() {
        return I18n.format("move.creature.element." + this.name().toLowerCase());
    }

    /**
     * Resolves enum references when a matchup is queried, after enum construction has completed.
     */
    private abstract static class DeferredElementSet extends AbstractSet<Element> {
        @NotNull
        protected abstract Set<Element> elements();

        @Override
        @NotNull
        public Iterator<Element> iterator() {
            return this.elements().iterator();
        }

        @Override
        public int size() {
            return this.elements().size();
        }
    }
}
