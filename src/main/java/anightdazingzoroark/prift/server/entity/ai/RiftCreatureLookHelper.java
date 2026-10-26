package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityLookHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Submits combat look requests during vanilla's look update, after AI and movement requests.
 * */
public class RiftCreatureLookHelper extends EntityLookHelper {
    @NotNull
    private final RiftCreature creature;

    public RiftCreatureLookHelper(@NotNull RiftCreature creature) {
        super(creature);
        this.creature = creature;
    }

    @Override
    public void onUpdateLook() {
        if (!this.creature.isAIDisabled() && this.creature.getNavigationBuilder() != null) {
            EntityLivingBase target = this.creature.getAttackTarget();
            CreatureMoveBuilder move = this.creature.getCreatureMoves().getMoveBuilderCurrentMove();
            CreatureMoveChargeupBuilder charge = move == null ? null : move.getMoveChargeupBuilder();
            boolean canLookWhileUsing = charge != null && charge.getCanLookWhileUsing();
            if (target != null && target.isEntityAlive() && !this.creature.isBeingRidden()
                    && !this.creature.getIsSleeping() && !this.creature.isStaggered()
                    && (this.creature.getCurrentMove().isEmpty() || this.creature.canRotateWhileCharging() || canLookWhileUsing)
            ) {
                this.setLookPositionWithEntity(target, 30f, this.creature.getVerticalFaceSpeed());
            }
        }
        super.onUpdateLook();
    }
}