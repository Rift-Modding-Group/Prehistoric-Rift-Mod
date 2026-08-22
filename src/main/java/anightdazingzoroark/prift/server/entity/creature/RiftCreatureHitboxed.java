package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.hitbox.MultiHitboxList;
import anightdazingzoroark.riftlib.hitbox.RiftLibCollisionHitbox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

/**
 * Any creature that has hitboxes must extend this class
 * */
public class RiftCreatureHitboxed extends RiftCreature implements IMultiHitboxUser<RiftCreatureHitboxed> {
    @NotNull
    private MultiHitboxList<RiftCreatureHitboxed> multiHitboxList;

    public RiftCreatureHitboxed(World worldIn) {
        this(worldIn, RiftCreatureRegistry.DEFAULT_CREATURE);
    }

    public RiftCreatureHitboxed(World worldIn, String creatureName) {
        super(worldIn, creatureName);
        this.multiHitboxList = new MultiHitboxList<>(this, this.getAnimationData());
    }

    @Override
    protected void onCreatureTypeChanged() {
        this.multiHitboxList = new MultiHitboxList<>(this, this.getAnimationData());
    }

    /**
     * dumass override
     * */
    @Override
    public void applyEntityCollision(Entity entityIn) {}

    /**
     * only push back whatever is colliding with this entity
     * */
    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (entityIn == null || entityIn.equals(this) || this.isRidingSameEntity(entityIn) || entityIn.noClip) return;

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

        entityIn.addVelocity(dispX, 0D, dispZ);

        //mark dirty to force push on players
        if (entityIn instanceof EntityPlayer) entityIn.velocityChanged = true;
    }

    /**
     * hitboxed creatures use their main body hitbox
     * */
    @Override
    public boolean bodyTouchingLiquid() {
        if (!this.multiHitboxList.hasCollisionHitboxes()) return false;
        RiftLibCollisionHitbox<RiftCreatureHitboxed> bodyHitbox = this.multiHitboxList.getCollisionHitboxByName("body");
        return bodyHitbox.isInWater() || bodyHitbox.isInLava();
    }

    //-----methods from IMultiHitboxUser start here-----
    @Override
    @NotNull
    public RiftCreatureHitboxed getMultiHitboxUser() {
        return this;
    }

    @Override
    @NotNull
    public MultiHitboxList<RiftCreatureHitboxed> getMultiHitboxList() {
        return this.multiHitboxList;
    }

    @Override
    public float hitboxDamageMultiplier(RiftLibCollisionHitbox<RiftCreatureHitboxed> collisionHitbox, DamageSource source) {
        Map<String, Function<ICreature, Double>> hitboxHitInfo = this.getCreatureType().getHitboxInformation();
        if (hitboxHitInfo == null) return 1f;
        for (Map.Entry<String, Function<ICreature, Double>> hitEntry : hitboxHitInfo.entrySet()) {
            if (!collisionHitbox.hasHitboxTag(hitEntry.getKey())) continue;
            return hitEntry.getValue().apply(this).floatValue();
        }
        return 1f;
    }

    @Override
    public boolean hitboxCanCollideWithEntities() {
        return false;
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
