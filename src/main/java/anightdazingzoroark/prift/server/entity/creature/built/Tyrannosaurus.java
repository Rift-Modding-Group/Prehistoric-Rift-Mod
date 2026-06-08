package anightdazingzoroark.prift.server.entity.creature.built;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class Tyrannosaurus extends RiftCreature implements IRayCreator<Tyrannosaurus> {
    public Tyrannosaurus(World worldIn) {
        super(worldIn, "tyrannosaurus");
    }

    @Override
    public Tyrannosaurus getRayCreator() {
        return this;
    }

    @Override
    public Map<String, RiftLibRayBuilder> getRayBuilders() {
        return this.rayMap;
    }

    @Override
    public void applyRaySegments(String s, BlockPos blockPos, RiftLibRay.RayHitResult rayHitResult) {
        if (this.rayHitEffectMap == null) return;
        this.rayHitEffectMap.get(s).accept(this, blockPos, rayHitResult);
    }
}
