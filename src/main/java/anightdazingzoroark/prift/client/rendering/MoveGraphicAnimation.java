package anightdazingzoroark.prift.client.rendering;

import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic.Playback;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.model.AnimationPoseSampler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public class MoveGraphicAnimation implements IAnimatable<MoveGraphicAnimation.Data> {
    @NotNull
    public World world;
    @NotNull
    public GeoModel template;
    @NotNull
    public GeoModel model;
    @NotNull
    public Playback playback;
    @Nullable
    private Data data;

    public MoveGraphicAnimation(@NotNull World world, @NotNull Playback playback, @NotNull GeoModel template) {
        this.world = world;
        this.playback = playback;
        this.template = template;
        this.model = template.copy();
        for (GeoBone bone : this.model.getAllBones().values()) {
            bone.saveInitialSnapshot();
            this.getAnimationData().getBoneSnapshotCollection().put(bone.getName(), Pair.of(bone, new BoneSnapshot(bone)));
        }
    }

    public void sample(@NotNull Animation animation, double tick) {
        Data animationData = this.getAnimationData();
        animationData.tick = tick;
        animationData.animTime = tick / 20D;
        animationData.lifeTime = tick / 20D;
        AnimationPoseSampler.sample(animation, animationData, tick).forEach((name, pose) -> {
            GeoBone bone = this.model.getAllBones().get(name);
            bone.getPosition().set(pose.getPosition());
            bone.getRotation().set(pose.getRotation());
            bone.getScale().set(pose.getScale());
        });
    }

    @Override
    @NotNull
    public Data getAnimationData() {
        if (this.data == null) this.data = new Data(this);
        return this.data;
    }

    @Override
    public void initializeAnimationData(@NotNull Data animationData) {
        Vec3d offset = this.playback.destination().subtract(this.playback.origin()).scale(16D);
        animationData.setVariable("variable.graphic_target_x", -offset.x);
        animationData.setVariable("variable.graphic_target_y", offset.y);
        animationData.setVariable("variable.graphic_target_z", offset.z);
    }

    public static class Data extends AbstractAnimationData<MoveGraphicAnimation, Data> {
        public Data(@NotNull MoveGraphicAnimation graphic) {
            super(graphic, graphic);
        }

        @Override
        public void updateOnDataTick() {}

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        @Nullable
        public World getWorld() {
            return this.getHolder().world;
        }
    }
}
