package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.hitbox.MultiHitboxList;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Any creature that has hitboxes must extend this class
 * */
public abstract class RiftCreatureHitboxed extends RiftCreature implements IMultiHitboxUser<RiftCreatureHitboxed> {
    @NotNull
    private final MultiHitboxList<RiftCreatureHitboxed> multiHitboxList;

    public RiftCreatureHitboxed(World worldIn, String creatureName) {
        super(worldIn, creatureName);
        this.multiHitboxList = new MultiHitboxList<>(this, this.getAnimationData());
    }

    /**
     * disable vanilla raytraces and entity collisions
     * hitboxes will take care of those instead
     * */
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    /**
     * This override just makes it so that this creature doesn't get dislocated when pushed
     * */
    @Override
    public void applyEntityCollision(Entity entityIn) {}

    @Override
    protected void collideWithEntity(Entity entityIn) {}

    //-----methods from IMultiHitboxUser start here-----
    @Override
    @NotNull
    public RiftCreatureHitboxed getMultiHitboxUser() {
        return this;
    }

    @Override
    public float multiHitboxUserScale() {
        return this.scale();
    }

    @Override
    @NotNull
    public MultiHitboxList<RiftCreatureHitboxed> getMultiHitboxList() {
        return this.multiHitboxList;
    }

    @Override
    public boolean hitboxCanCollideWithEntities() {
        return true;
    }

    @Override
    public Entity[] getParts() {
        return this.multiHitboxList.getHitboxesAsArray();
    }

    @Override
    public World getWorld() {
        return this.world;
    }
    //-----methods from IMultiHitboxUser end here-----
}
