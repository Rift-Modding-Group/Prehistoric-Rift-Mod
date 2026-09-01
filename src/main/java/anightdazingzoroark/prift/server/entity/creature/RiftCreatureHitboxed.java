package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.hitbox.MultiHitboxList;
import anightdazingzoroark.riftlib.hitbox.RiftLibCollisionHitbox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
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
