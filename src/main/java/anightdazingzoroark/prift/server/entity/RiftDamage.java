package anightdazingzoroark.prift.server.entity;

import anightdazingzoroark.prift.api.creature.Element;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RiftDamage {
    public static DamageSource RIFT_BLEED = new DamageSource("prift_bleed").setDamageBypassesArmor();

    public static RiftElementDamageSource getElementDamageSource(
            @NotNull String damageType, @NotNull Entity immediateSource,
            @Nullable Entity trueSource, @NotNull Element element
    ) {
        return new RiftElementDamageSource(damageType, immediateSource, trueSource, element);
    }

    public static class RiftElementDamageSource extends EntityDamageSourceIndirect {
        @NotNull
        public final Element element;

        private RiftElementDamageSource(
                @NotNull String damageType, @NotNull Entity immediateSource,
                @Nullable Entity trueSource, @NotNull Element element
        ) {
            super(damageType, immediateSource, trueSource);
            this.element = element;
            if (element == Element.FIRE) this.setFireDamage();
        }
    }
}