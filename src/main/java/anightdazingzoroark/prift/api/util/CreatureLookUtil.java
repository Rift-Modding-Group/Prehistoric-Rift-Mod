package anightdazingzoroark.prift.api.util;

import anightdazingzoroark.prift.api.creature.ICreature;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class CreatureLookUtil {
    /**
     * The last evaluated look pose, shared by animation capture and attack facing.
     * */
    @NotNull
    public static LookPose getLookPose(@NotNull ICreature creature) {
        AnimationDataEntityLiving data = creature.getAnimationData();
        float pitch = data.getHeadPitch(1f);
        float yaw = data.getHeadYaw(1f);
        // Steering may already have changed the head fields; prefer the last animated neck pose.
        if (data.getBoneSnapshotCollection().containsKey("neck")) {
            BoneSnapshot neck = data.getBoneSnapshotCollection().get("neck").getRight();
            BoneSnapshot rest = data.getBoneSnapshotCollection().get("neck").getLeft().getInitialSnapshot();
            pitch = (float) -Math.toDegrees(neck.getRotation().x - rest.getRotation().x);
            yaw = (float) -Math.toDegrees(neck.getRotation().y - rest.getRotation().y);
        }
        return new LookPose(Math.clamp(pitch, -15f, 15f), Math.clamp(yaw, -15f, 15f));
    }

    /**
     * Face the target while retaining the local yaw already saved into the attack animation.
     * */
    public static float getLookBasedAttackYaw(@NotNull ICreature creature, @NotNull EntityLivingBase target) {
        return getLookBasedAttackYaw(creature, target, creature.getPositionVector());
    }

    /**
     * Also used to predict facing at a prospective attack position.
     * */
    public static float getLookBasedAttackYaw(@NotNull ICreature creature, @NotNull EntityLivingBase target, @NotNull Vec3d position) {
        float bodyYaw = creature.getAnimationData().getModelYaw();
        Vec3d offset = target.getPositionVector().subtract(position);
        if (offset.x * offset.x + offset.z * offset.z < 1E-4D) return bodyYaw;

        float targetYaw = (float) Math.toDegrees(Math.atan2(-offset.x, offset.z));
        float lookYaw = getLookPose(creature).yaw();
        return bodyYaw + MathHelper.wrapDegrees(targetYaw - lookYaw - bodyYaw);
    }

    public record LookPose(float pitch, float yaw) {}
}