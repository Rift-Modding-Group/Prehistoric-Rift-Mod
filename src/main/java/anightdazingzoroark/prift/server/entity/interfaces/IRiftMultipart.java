package anightdazingzoroark.prift.server.entity.interfaces;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.DamageSource;

public interface IRiftMultipart extends IEntityMultiPart {
    RiftCreature getPartParent();

    default boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
        RiftCreaturePart riftPart = (RiftCreaturePart) part;
        if (damage > 0.0f && !riftPart.isDisabled() && riftPart.testForMeleeImmunity(source) && riftPart.testForProjectileImmunity(source)) {
            float newDamage = riftPart.getDamageMultiplier() * damage;
            return this.getPartParent().attackEntityFrom(source, newDamage);
        }
        return false;
    }
}
