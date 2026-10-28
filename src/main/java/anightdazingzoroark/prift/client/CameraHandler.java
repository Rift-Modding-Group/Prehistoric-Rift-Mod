package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureNavigationBuilder;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveBuilder.RiddenCrosshairType;
import anightdazingzoroark.prift.api.util.CreatureLookUtil;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.client.camera.RidingAimCrosshair;
import anightdazingzoroark.prift.client.camera.RidingAimTargeting;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.ai.pathfinding.CreatureLeapPlan;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimTarget;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Owns ridden camera transitions and the shared view used for aiming.
 * */
@SideOnly(Side.CLIENT)
public class CameraHandler {
    //camera tuning
    public static float RIDING_CAMERA_VERTICAL_OFFSET = 0f;
    public static double RIDING_CAMERA_TRANSITION_SPEED = 0.5D;
    public static double RIDING_AIM_CAMERA_DISTANCE = 3D;
    public static float RIDING_AIM_CAMERA_HORIZONTAL_OFFSET = -2f;
    public static double RIDING_AIM_DISTANCE = 64D;
    public static float RIDING_AIM_FOV_MULTIPLIER = 0.72f;
    public static float RIDING_MELEE_AIM_FOV_MULTIPLIER = 0.9f;

    //camera lifecycle and interpolation history
    private boolean enabled;
    private int previousThirdPersonView = -1;
    private double startingCameraDistance;
    private double previousCameraTransition;
    private double cameraTransition;
    private float ridingCameraFov = 70f;
    private int cameraCreatureId = -1;
    private double creatureCameraSideExtent;

    //selected move settings are independent of camera transition progress
    @Nullable
    private static RiftCreature aimingCreature;
    private boolean ridingAimActive;
    @NotNull
    private RidingAimSettings aimSettings = new RidingAimSettings(false, RiddenCrosshairType.POINT, null, 0D, false);
    @Nullable
    private CreatureLeapPlan leapPreview;

    //aim lifecycle
    public void beginRidingAim(@NotNull RiftCreature riddenCreature, int ridingAimMove, boolean leapAim) {
        this.ridingAimActive = true;
        this.leapPreview = null;
        if (leapAim) {
            this.aimSettings = new RidingAimSettings(false, RiddenCrosshairType.POINT, null, 0D, true);
            return;
        }
        Map.Entry<String, CreatureMoveBuilder> selectedMove = riddenCreature.getCreatureMoves().getUsableMoves().get(ridingAimMove);
        CreatureMoveBuilder move = selectedMove.getValue();
        boolean melee = riddenCreature.isTargetedMeleeRiddenMove(ridingAimMove);
        double meleeReach = melee ? riddenCreature.calculateRiddenMeleeAimReach(ridingAimMove) : 0D;
        this.ridingAimActive = true;
        this.aimSettings = new RidingAimSettings(melee, move.getRiddenCrosshairType(), selectedMove.getKey(), meleeReach, false);
    }

    public void endRidingAim() {
        aimingCreature = null;
        this.ridingAimActive = false;
        this.aimSettings = new RidingAimSettings(false, RiddenCrosshairType.POINT, null, 0D, false);
        this.leapPreview = null;
    }


    /**
     * tick the camera handler
     * */
    public void update(
            @NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature,
            boolean moveHotbarActive, boolean ridingAimActive
    ) {
        this.ridingAimActive = ridingAimActive;
        boolean ridingCameraAvailable = this.canUseRidingCamera(minecraft, riddenCreature);
        boolean shouldEnable = ridingCameraAvailable && moveHotbarActive && ridingAimActive;

        //restore on disable
        if (!ridingCameraAvailable) {
            this.restorePreviousCamera(minecraft);
            return;
        }
        if (shouldEnable) CreatureLookUtil.clampRiddenAimPitch(minecraft.player, riddenCreature, this.aimSettings.moveName());

        if (shouldEnable && riddenCreature != null && this.cameraCreatureId != riddenCreature.getEntityId()) {
            this.cameraCreatureId = riddenCreature.getEntityId();
            double yawRadians = Math.toRadians(riddenCreature.rotationYaw);
            double cameraSideX = -Math.cos(yawRadians);
            double cameraSideZ = -Math.sin(yawRadians);
            double maximumSideOffset = riddenCreature.width * 0.5D;
            for (AnimatedBoundingBox animatedBoundingBox : riddenCreature.getAnimationData().getAnimatedBoundingBoxes().values()) {
                if (!animatedBoundingBox.canDoCollisions()) continue;
                AxisAlignedBB bounds = riddenCreature.getAnimationData().getWorldSpaceAABB(animatedBoundingBox.getName());
                if (bounds == null) continue;

                double sideX = cameraSideX >= 0D ? bounds.maxX : bounds.minX;
                double sideZ = cameraSideZ >= 0D ? bounds.maxZ : bounds.minZ;
                double sideOffset = (sideX - riddenCreature.posX) * cameraSideX + (sideZ - riddenCreature.posZ) * cameraSideZ;
                maximumSideOffset = Math.max(maximumSideOffset, sideOffset);
            }
            this.creatureCameraSideExtent = maximumSideOffset;
        }

        if (shouldEnable && !this.enabled) {
            this.enabled = true;
            this.previousThirdPersonView = minecraft.gameSettings.thirdPersonView;
            this.startingCameraDistance = this.previousThirdPersonView == 1 ? minecraft.entityRenderer.thirdPersonDistance : 0D;
            this.previousCameraTransition = 0D;
            this.cameraTransition = 0D;
        }
        if (!this.enabled) return;
        minecraft.gameSettings.thirdPersonView = 1;

        this.previousCameraTransition = this.cameraTransition;
        this.cameraTransition = MathUtil.slopeResult(
                RIDING_CAMERA_TRANSITION_SPEED, true,
                0D, 1D, this.cameraTransition, shouldEnable ? 1D : 0D
        );
        if (this.cameraTransition < 0.001D) this.cameraTransition = 0D;
        else if (1D - this.cameraTransition < 0.001D) this.cameraTransition = 1D;

        if (!shouldEnable && this.cameraTransition == 0D) this.restorePreviousCamera(minecraft);
    }

    // Camera event integration.

    @Nullable
    public RidingCameraAngles setupRidingCamera(
            @NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature,
            @NotNull Entity riderEntity, double partialTicks
    ) {
        aimingCreature = null;
        if (!this.enabled || !this.canUseRidingCamera(minecraft, riddenCreature)
                || riddenCreature == null || riderEntity != minecraft.player
                || minecraft.gameSettings.thirdPersonView != 1
        ) {
            return null;
        }

        //vanilla has already probed camera collisions using these input angles.
        float yaw = minecraft.player.rotationYaw;
        float pitch = minecraft.player.rotationPitch;
        if (this.ridingAimActive) CreatureLookUtil.clampRiddenAimPitch(minecraft.player, riddenCreature, this.aimSettings.moveName());

        double transition = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                this.previousCameraTransition, this.cameraTransition
        );
        Vec3d playerAnchor = this.getInterpolatedEyePosition(minecraft.player, partialTicks);
        CameraFrame frame = this.getCameraFrame(minecraft, riddenCreature, partialTicks, transition);
        Vec3d desiredCameraPosition = frame.position();
        Vec3d cameraLookVector = frame.lookVector();
        if (this.ridingAimActive && this.aimSettings.crosshairType() == RiddenCrosshairType.WIDE) {
            aimingCreature = riddenCreature;
        }
        Vec3d cameraRight = this.getCameraRight(minecraft.player, cameraLookVector, partialTicks);
        Vec3d cameraUp = cameraRight.crossProduct(cameraLookVector).normalize();
        double cameraDistance = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                minecraft.entityRenderer.thirdPersonDistancePrev,
                minecraft.entityRenderer.thirdPersonDistance
        );
        double offsetX = -MathHelper.sin(yaw * (float) (Math.PI / 180D))
                * MathHelper.cos(pitch * (float) (Math.PI / 180D)) * cameraDistance;
        double offsetZ = MathHelper.cos(yaw * (float) (Math.PI / 180D))
                * MathHelper.cos(pitch * (float) (Math.PI / 180D)) * cameraDistance;
        double offsetY = -MathHelper.sin(pitch * (float) (Math.PI / 180D)) * cameraDistance;
        // Match vanilla's collision probes, then displace its camera in the returned camera basis.
        for (int sample = 0; sample < 8; sample++) {
            double sampleX = ((sample & 1) * 2 - 1) * 0.1D;
            double sampleY = ((sample >> 1 & 1) * 2 - 1) * 0.1D;
            double sampleZ = ((sample >> 2 & 1) * 2 - 1) * 0.1D;
            Vec3d sampleStart = playerAnchor.add(sampleX, sampleY, sampleZ);
            Vec3d sampleEnd = new Vec3d(
                    playerAnchor.x - offsetX + sampleX + sampleZ,
                    playerAnchor.y - offsetY + sampleY,
                    playerAnchor.z - offsetZ + sampleZ
            );
            RayTraceResult collision = minecraft.world.rayTraceBlocks(sampleStart, sampleEnd);
            if (collision != null) cameraDistance = Math.min(cameraDistance, playerAnchor.distanceTo(collision.hitVec));
        }
        Vec3d vanillaCameraPosition = playerAnchor.subtract(cameraLookVector.scale(cameraDistance));
        Vec3d cameraDisplacement = desiredCameraPosition.subtract(vanillaCameraPosition);

        GlStateManager.translate(
                -cameraDisplacement.dotProduct(cameraRight),
                -cameraDisplacement.dotProduct(cameraUp),
                cameraDisplacement.dotProduct(cameraLookVector)
        );

        double horizontalLookDistance = Math.sqrt(cameraLookVector.x * cameraLookVector.x + cameraLookVector.z * cameraLookVector.z);
        float cameraYaw = (float) Math.toDegrees(Math.atan2(cameraLookVector.z, cameraLookVector.x)) - 90f;
        float cameraPitch = (float) -Math.toDegrees(Math.atan2(cameraLookVector.y, horizontalLookDistance));
        return new RidingCameraAngles(cameraYaw + 180f, cameraPitch);
    }

    public float modifyRidingCameraFov(
            @NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature,
            @NotNull Entity riderEntity, double partialTicks, float currentFov
    ) {
        if (!this.enabled || !this.canUseRidingCamera(minecraft, riddenCreature) || riderEntity != minecraft.player) return currentFov;

        double transition = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                this.previousCameraTransition, this.cameraTransition
        );
        double fovMultiplier = MathUtil.slopeResult(
                transition, true, 0D, 1D,
                1D, this.aimSettings.melee() ? RIDING_MELEE_AIM_FOV_MULTIPLIER : RIDING_AIM_FOV_MULTIPLIER
        );
        this.ridingCameraFov = (float) (currentFov * fovMultiplier);
        return this.ridingCameraFov;
    }

    //target selection and HUD drawing use the same camera and move settings.
    @NotNull
    public RiddenAimTarget sendRidingAimUpdate(@NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature, int ridingAimMove) {
        CreatureLookUtil.clampRiddenAimPitch(minecraft.player, riddenCreature, this.aimSettings.moveName());
        CameraFrame frame = this.getCameraFrame(minecraft, riddenCreature, 1D, this.cameraTransition);
        RiddenAimTarget target = RidingAimTargeting.sendUpdate(
                minecraft, riddenCreature, ridingAimMove, frame, this.aimSettings,
                this.ridingCameraFov, RIDING_AIM_DISTANCE
        );
        this.leapPreview = this.aimSettings.leap() ? CreatureLeapPlan.create(riddenCreature, target) : null;
        return target;
    }

    public void renderRidingAimCrosshair(
            @NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature,
            boolean leapFullyCharged, int screenWidth, int screenHeight
    ) {
        if (!this.enabled || !this.ridingAimActive || minecraft.gameSettings.hideGUI
                || !this.canUseRidingCamera(minecraft, riddenCreature)
        ) {
            return;
        }
        if (this.aimSettings.leap()) {
            if (riddenCreature == null || riddenCreature.getCreatureMoveHelper().getLeapHelper().isRiddenLeapPendingOrActive()) return;
            RidingAimCrosshair.renderLeapCrosshair(minecraft, this.leapPreview != null, leapFullyCharged, screenWidth, screenHeight);
            return;
        }
        RidingAimCrosshair.renderWideCrosshair(minecraft, this.aimSettings.crosshairType(), screenWidth, screenHeight);
    }

    public static boolean isRidingJawAim(@NotNull RiftCreature creature, @Nullable Entity viewer) {
        return viewer != null && creature == aimingCreature && creature.getControllingPassenger() == viewer;
    }

    // Shared camera geometry.

    @NotNull
    private CameraFrame getCameraFrame(
            @NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature, double partialTicks, double transition
    ) {
        Vec3d forward = minecraft.player.getLook((float) partialTicks).normalize();
        Vec3d cameraRight = this.getCameraRight(minecraft.player, forward, partialTicks);
        Vec3d cameraAnchor = this.interpolateVector(
                this.getInterpolatedEyePosition(minecraft.player, partialTicks),
                this.getInterpolatedEyePosition(riddenCreature, partialTicks).add(0D, RIDING_CAMERA_VERTICAL_OFFSET, 0D),
                transition
        );

        // Blend from the previous view into the shoulder camera, then clip against terrain.
        double sideDistance = this.creatureCameraSideExtent + Math.abs(RIDING_AIM_CAMERA_HORIZONTAL_OFFSET);
        Vec3d aimOffset = cameraRight.scale(sideDistance).subtract(forward.scale(RIDING_AIM_CAMERA_DISTANCE));
        Vec3d cameraPosition = cameraAnchor.add(this.interpolateVector(forward.scale(-this.startingCameraDistance), aimOffset, transition));
        Vec3d cameraPath = cameraPosition.subtract(cameraAnchor);
        double desiredDistance = Math.sqrt(cameraPath.lengthSquared());
        if (desiredDistance < 1E-6D) cameraPosition = cameraAnchor;
        else {
            double unobstructedDistance = desiredDistance;
            for (int sample = -1; sample < 8; sample++) {
                Vec3d sampleOffset = sample < 0 ? Vec3d.ZERO : new Vec3d(
                        ((sample & 1) * 2 - 1) * 0.1D,
                        ((sample >> 1 & 1) * 2 - 1) * 0.1D,
                        ((sample >> 2 & 1) * 2 - 1) * 0.1D
                );
                Vec3d sampleStart = cameraAnchor.add(sampleOffset);
                RayTraceResult collision = minecraft.world.rayTraceBlocks(sampleStart, cameraPosition.add(sampleOffset), false, true, false);
                if (collision != null) unobstructedDistance = Math.min(unobstructedDistance, sampleStart.distanceTo(collision.hitVec));
            }
            cameraPosition = cameraAnchor.add(cameraPath.scale(unobstructedDistance / desiredDistance));
        }

        // Head-driven attacks derive their focus from their existing animation hitboxes.
        double focusDistance = this.aimSettings.melee() ? Math.max(1D, this.aimSettings.meleeReach()) : RIDING_AIM_DISTANCE;
        Vec3d focusPosition = cameraAnchor.add(forward.scale(focusDistance));
        if (this.aimSettings.leap()) {
            focusPosition = getLeapAimFocusPosition(riddenCreature, this.getInterpolatedEyePosition(riddenCreature, partialTicks), forward);
        }
        if (this.aimSettings.melee() && this.aimSettings.moveName() != null) {
            float headPitch = (float) MathUtil.slopeResult(partialTicks, true, 0D, 1D,
                    minecraft.player.prevRotationPitch, minecraft.player.rotationPitch);
            float bodyYaw = (float) Math.toDegrees(Math.atan2(-forward.x, forward.z));
            Vec3d strikePosition = CreatureLookUtil.getRiddenAimHitboxPosition(
                    riddenCreature, this.aimSettings.moveName(), headPitch, bodyYaw, partialTicks);
            if (strikePosition != null) focusPosition = strikePosition;
        }
        Vec3d capturedAim = riddenCreature.getRiddenAimPosition();
        if (this.ridingAimActive && riddenCreature.isRiddenAimLocked() && capturedAim != null) focusPosition = capturedAim;
        return new CameraFrame(cameraPosition, focusPosition,
                getAimCameraLookVector(cameraPosition, focusPosition, minecraft.player.getLook((float) partialTicks)));
    }

    @NotNull
    @SuppressWarnings("SuspiciousNameCombination")
    private Vec3d getInterpolatedEyePosition(@NotNull Entity entity, double partialTicks) {
        return new Vec3d(
                MathUtil.slopeResult(partialTicks, true, 0D, 1D, entity.prevPosX, entity.posX),
                MathUtil.slopeResult(partialTicks, true, 0D, 1D, entity.prevPosY, entity.posY) + entity.getEyeHeight(),
                MathUtil.slopeResult(partialTicks, true, 0D, 1D, entity.prevPosZ, entity.posZ)
        );
    }

    @NotNull
    @SuppressWarnings("SuspiciousNameCombination")
    private Vec3d interpolateVector(@NotNull Vec3d start, @NotNull Vec3d end, double amount) {
        return new Vec3d(
                MathUtil.slopeResult(amount, true, 0D, 1D, start.x, end.x),
                MathUtil.slopeResult(amount, true, 0D, 1D, start.y, end.y),
                MathUtil.slopeResult(amount, true, 0D, 1D, start.z, end.z)
        );
    }

    @NotNull
    private Vec3d getCameraRight(@NotNull EntityPlayer player, @NotNull Vec3d lookVector, double partialTicks) {
        Vec3d cameraRight = lookVector.crossProduct(new Vec3d(0D, 1D, 0D));
        if (cameraRight.lengthSquared() >= 1E-6D) return cameraRight.normalize();

        float yawDifference = MathHelper.wrapDegrees(player.rotationYaw - player.prevRotationYaw);
        double interpolatedYaw = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                player.prevRotationYaw, player.prevRotationYaw + yawDifference
        );
        double yawRadians = Math.toRadians(interpolatedYaw);
        return new Vec3d(-Math.cos(yawRadians), 0D, -Math.sin(yawRadians));
    }

    @NotNull
    public static Vec3d getLeapAimFocusPosition(@NotNull RiftCreature creature, @NotNull Vec3d creatureEyes, @NotNull Vec3d forward) {
        CreatureNavigationBuilder navigation = creature.getNavigationBuilder();
        if (navigation == null || !navigation.getCanLeap()) return creatureEyes.add(forward.scale(RIDING_AIM_DISTANCE));
        // Use the existing clamped input to focus across landing heights at leap range, rather than the projectile horizon.
        double pitch = Math.toDegrees(Math.atan2(-forward.y, Math.hypot(forward.x, forward.z)));
        double focusHeight = MathUtil.slopeResult(pitch, true, CreatureLookUtil.HEAD_PITCH_MIN, CreatureLookUtil.HEAD_PITCH_MAX,
                navigation.getLeapHeight(), -Math.max(navigation.getLeapHeight(), creature.getMaxFallHeight()));
        Vec3d horizontalDirection = new Vec3d(forward.x, 0D, forward.z).normalize();
        double focusDistance = Math.max(0D, navigation.getLeapDistance() - 0.05D);
        return creatureEyes.add(0D, focusHeight - creature.getEyeHeight(), 0D).add(horizontalDirection.scale(focusDistance));
    }

    @NotNull
    public static Vec3d getAimCameraLookVector(@NotNull Vec3d cameraPosition, @NotNull Vec3d focusPosition, @NotNull Vec3d fallback) {
        Vec3d cameraToFocus = focusPosition.subtract(cameraPosition);
        if (cameraToFocus.lengthSquared() < 1E-6D) return fallback.normalize();
        return cameraToFocus.normalize();
    }

    private boolean canUseRidingCamera(@NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature) {
        return minecraft.player != null && riddenCreature != null && riddenCreature.getControllingPassenger() == minecraft.player;
    }

    private void restorePreviousCamera(@NotNull Minecraft minecraft) {
        aimingCreature = null;
        if (this.enabled && this.previousThirdPersonView >= 0) {
            minecraft.gameSettings.thirdPersonView = this.previousThirdPersonView;
        }
        this.enabled = false;
        this.previousThirdPersonView = -1;
        this.previousCameraTransition = 0D;
        this.cameraTransition = 0D;
        this.cameraCreatureId = -1;
        this.creatureCameraSideExtent = 0D;
    }

    public record RidingAimSettings(
            boolean melee, @NotNull RiddenCrosshairType crosshairType,
            @Nullable String moveName, double meleeReach, boolean leap
    ) {}

    public record CameraFrame(@NotNull Vec3d position, @NotNull Vec3d focusPosition, @NotNull Vec3d lookVector) {}

    public record RidingCameraAngles(float yaw, float pitch) {}
}
