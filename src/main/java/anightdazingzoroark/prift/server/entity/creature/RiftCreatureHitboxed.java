package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.riftlib.hitbox.HitboxDefinitionList;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Any creature that has hitboxes must extend this class
 * */
public abstract class RiftCreatureHitboxed extends RiftCreature implements IMultiHitboxUser<RiftCreatureHitboxed> {
    @NotNull
    private Entity[] hitboxes = {};
    private HitboxDefinitionList hitboxDefinitionList;

    public RiftCreatureHitboxed(World worldIn, String creatureName) {
        super(worldIn, creatureName);
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
    public void applyEntityCollision(Entity entityIn) {
        if (entityIn == null || this.isRidingSameEntity(entityIn) || entityIn.noClip) return;

        double dispX = entityIn.posX - this.posX;
        double dispZ = entityIn.posZ - this.posZ;
        double maxDisp = MathHelper.absMax(dispX, dispZ);

        maxDisp = MathHelper.sqrt(maxDisp);
        dispX /= maxDisp;
        dispZ /= maxDisp;
        double d3 = Math.min(1D / maxDisp, 1D);

        dispX *= d3;
        dispZ *= d3;
        dispX *= 0.05f;
        dispZ *= 0.05f;
        dispX *= 1f - this.entityCollisionReduction;
        dispZ *= 1f - this.entityCollisionReduction;

        if (!entityIn.isBeingRidden()) {
            System.out.println("pushed entity: "+entityIn);
            entityIn.addVelocity(dispX, 0D, dispZ);

            //mark dirty to force push on players
            if (entityIn instanceof EntityPlayer) entityIn.velocityChanged = true;
        }
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {}

    //-----methods from IMultiHitboxUser start here-----
    @Override
    public RiftCreatureHitboxed getMultiHitboxUser() {
        return this;
    }

    @Override
    public float multiHitboxUserScale() {
        return this.scale();
    }

    @Override
    public boolean hitboxCanCollideWithEntities() {
        return true;
    }

    @Override
    public void setParts(Entity[] entities) {
        this.hitboxes = entities;
    }

    @Override
    public Entity[] getParts() {
        return this.hitboxes;
    }

    @Override
    public HitboxDefinitionList getHitboxDefinitionList() {
        return this.hitboxDefinitionList;
    }

    @Override
    public void setHitboxDefinitionList(HitboxDefinitionList hitboxDefinitionList) {
        this.hitboxDefinitionList = hitboxDefinitionList;
    }

    @Override
    public World getWorld() {
        return this.world;
    }
    //-----methods from IMultiHitboxUser end here-----
}
