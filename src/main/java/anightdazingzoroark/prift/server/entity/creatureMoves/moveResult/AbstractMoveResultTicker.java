package anightdazingzoroark.prift.server.entity.creatureMoves.moveResult;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creatureMoves.moveSelection.MoveRuleBuilder;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMoveResultTicker {
    @NotNull
    protected final RiftCreature creature;
    @NotNull
    protected final MoveRuleBuilder moveRuleBuilder;

    //---look direction preservation---
    protected boolean hasLastLookDirection;
    protected float lastRotationYawHead;
    protected float lastPrevRotationYawHead;
    protected float lastRotationPitch;
    protected float lastPrevRotationPitch;

    /**
     * Constructor is also where the the initialization of the ticker takes place
     * */
    public AbstractMoveResultTicker(@NotNull RiftCreature creature, @NotNull MoveRuleBuilder moveRuleBuilder) {
        this.creature = creature;
        this.moveRuleBuilder = moveRuleBuilder;
    }

    public abstract boolean canContinueTicking();

    public abstract void onUpdate();

    public abstract void onEndTicker();

    public abstract boolean isOverridableWhileUsed();

    /**
     * Restores the creature's cached look direction to all yaw and pitch fields used by
     * movement, rendering, and interpolation. This keeps the creature facing its last
     * intended direction while a move is active or after its target disappears.
     */
    protected void preserveLastLookDirection() {
        if (!this.hasLastLookDirection) return;
        this.creature.rotationYaw = this.lastRotationYawHead;
        this.creature.prevRotationYaw = this.lastPrevRotationYawHead;
        this.creature.renderYawOffset = this.lastRotationYawHead;
        this.creature.prevRenderYawOffset = this.lastPrevRotationYawHead;
        this.creature.rotationYawHead = this.lastRotationYawHead;
        this.creature.prevRotationYawHead = this.lastPrevRotationYawHead;
        this.creature.rotationPitch = this.lastRotationPitch;
        this.creature.prevRotationPitch = this.lastPrevRotationPitch;
    }

    protected void setLookDirection(float value) {
        this.creature.rotationYaw = value;
        this.creature.prevRotationYaw = value;
        this.creature.renderYawOffset = value;
        this.creature.prevRenderYawOffset = value;
        this.creature.rotationYawHead = value;
        this.creature.prevRotationYawHead = value;
        this.lastRotationYawHead = value;
        this.lastPrevRotationYawHead = value;
    }
}
