package anightdazingzoroark.prift.server.entity.creature;

import anightdazingzoroark.riftlib.hitbox.EntityHitbox;
import anightdazingzoroark.riftlib.hitbox.HitboxDefinitionList;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

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

    @Override
    public RiftCreatureHitboxed getMultiHitboxUser() {
        return this;
    }

    @Override
    public float multiHitboxUserScale() {
        return this.scale();
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

    @Override
    @Nullable
    public EntityHitbox getHitboxByName(String name) {
        return IMultiHitboxUser.super.getHitboxByName(name);
    }
}
