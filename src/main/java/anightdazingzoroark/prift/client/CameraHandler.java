package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.client.hud.RidingCreatureHUD;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftRidingAimMessage;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handler for the custom camera
 * */
@SuppressWarnings("SuspiciousNameCombination")
@SideOnly(Side.CLIENT)
public class CameraHandler {
    public static float RIDING_CAMERA_VERTICAL_OFFSET = 0f;
    public static double RIDING_CAMERA_TRANSITION_SPEED = 0.5D;
    public static double RIDING_AIM_CAMERA_DISTANCE = 3D;
    public static float RIDING_AIM_CAMERA_HORIZONTAL_OFFSET = -2f;
    public static double RIDING_AIM_CAMERA_BODY_MARGIN = 0.25D;
    public static double RIDING_AIM_DISTANCE = 64D;
    public static float RIDING_AIM_FOV_MULTIPLIER = 0.72f;
    public static float RIDING_MELEE_AIM_FOV_MULTIPLIER = 0.9f;
    private static final double RENDER_DISTANCE_OFFSET = 0D;

    @NotNull
    private final RidingCreatureHUD ridingCreatureHUD;
    private boolean enabled;
    private boolean ridingAimActive;
    private int previousThirdPersonView = -1;
    private double startingCameraDistance;
    private double previousCameraTransition;
    private double cameraTransition;
    private boolean ridingMeleeAim;
    private double ridingMeleeAimReach;
    private int cameraCreatureId = -1;
    private double creatureCameraSideExtent;

    public CameraHandler(@NotNull RidingCreatureHUD ridingCreatureHUD) {
        this.ridingCreatureHUD = ridingCreatureHUD;
    }

    /**
     * tick the camera handler
     * */
    public void update(
            @NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature,
            boolean moveHotbarActive, boolean ridingAimActive
    ) {
        this.ridingAimActive = ridingAimActive;
        boolean ridingCreatureHUDEnabled = this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature);
        boolean shouldEnable = ridingCreatureHUDEnabled && moveHotbarActive && ridingAimActive;

        //restore on disable
        if (!ridingCreatureHUDEnabled) {
            this.restorePreviousCamera(minecraft);
            return;
        }

        if (shouldEnable && riddenCreature != null && this.cameraCreatureId != riddenCreature.getEntityId()) {
            this.cameraCreatureId = riddenCreature.getEntityId();
            this.creatureCameraSideExtent = this.calculateCreatureCameraSideExtent(riddenCreature);
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

    public void sendRidingAimUpdate(@NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature, int ridingAimMove) {
        Vec3d desiredCameraPosition = this.getDesiredCameraPosition(minecraft, riddenCreature, 1D, this.cameraTransition);
        Vec3d lookVector = this.getAimCameraLookVector(minecraft, riddenCreature, 1D, this.cameraTransition, desiredCameraPosition);
        Vec3d aimStart = this.getRenderedCameraPosition(minecraft, 1D, desiredCameraPosition, lookVector);
        Vec3d aimEnd = aimStart.add(lookVector.scale(RIDING_AIM_DISTANCE));
        RayTraceResult blockHit = minecraft.world.rayTraceBlocks(aimStart, aimEnd, false, true, false);
        Vec3d aimPosition = blockHit == null ? aimEnd : blockHit.hitVec;
        double closestDistanceSquared = aimStart.squareDistanceTo(aimPosition);
        Entity aimedEntity = null;
        Vec3d creatureAimOrigin = new Vec3d(
                riddenCreature.posX,
                riddenCreature.posY + riddenCreature.height * 0.5D,
                riddenCreature.posZ
        );

        //---look for entities hit by crosshair---
        AxisAlignedBB searchBounds = new AxisAlignedBB(aimStart, aimEnd).grow(1D);
        List<Entity> candidates = minecraft.world.getEntitiesInAABBexcluding(
                minecraft.player, searchBounds, entity -> entity != null && entity.canBeCollidedWith()
        );
        for (Entity candidate : candidates) {
            if (candidate.getLowestRidingEntity() == minecraft.player.getLowestRidingEntity()) continue;

            AxisAlignedBB targetBounds = candidate.getEntityBoundingBox().grow(candidate.getCollisionBorderSize());
            RayTraceResult entityHit = targetBounds.calculateIntercept(aimStart, aimEnd);
            Vec3d hitPosition = entityHit == null ? null : entityHit.hitVec;
            if (targetBounds.contains(aimStart)) hitPosition = aimStart;
            if (hitPosition == null) continue;
            if (this.ridingMeleeAim && !this.isWithinMeleeAimReach(targetBounds, creatureAimOrigin)) continue;

            double hitDistanceSquared = aimStart.squareDistanceTo(hitPosition);
            if (hitDistanceSquared >= closestDistanceSquared) continue;

            closestDistanceSquared = hitDistanceSquared;
            aimedEntity = candidate;
            aimPosition = hitPosition;
        }

        //---apply crosshair hit results---
        boolean hasCrosshairTarget = aimedEntity != null || blockHit != null;
        //--melee attack endpoint--
        if (this.ridingMeleeAim && aimedEntity == null && !this.isWithinMeleeAimReach(aimPosition, creatureAimOrigin)) {
            blockHit = null;
            hasCrosshairTarget = false;

            Vec3d centerToAimStart = aimStart.subtract(creatureAimOrigin);
            double directionProjection = centerToAimStart.dotProduct(lookVector);
            double discriminant = directionProjection * directionProjection
                    - centerToAimStart.lengthSquared()
                    + this.ridingMeleeAimReach * this.ridingMeleeAimReach;
            if (discriminant < 0D) {
                aimPosition = creatureAimOrigin.add(lookVector.scale(this.ridingMeleeAimReach));
            }
            else {
                double aimDistance = Math.max(0D, -directionProjection + Math.sqrt(discriminant));
                aimPosition = aimStart.add(lookVector.scale(aimDistance));
            }
        }
        //--ranged attack endpoint--
        else if (!this.ridingMeleeAim && !hasCrosshairTarget) {
            Vec3d creatureLook = riddenCreature.getLookVec();
            Vec3d horizontalCreatureLook = new Vec3d(creatureLook.x, 0D, creatureLook.z);
            if (horizontalCreatureLook.lengthSquared() < 1E-6D) {
                horizontalCreatureLook = new Vec3d(0D, 0D, 1D);
            }
            horizontalCreatureLook = horizontalCreatureLook.normalize();

            double verticalDirection = Math.clamp(lookVector.y, -1D, 1D);
            double horizontalDirection = Math.sqrt(Math.max(0D, 1D - verticalDirection * verticalDirection));
            Vec3d rangedDirection = horizontalCreatureLook.scale(horizontalDirection)
                    .add(new Vec3d(0D, verticalDirection, 0D)).normalize();
            aimPosition = creatureAimOrigin.add(rangedDirection.scale(RIDING_AIM_DISTANCE));
        }
        //--block hit to break blocks--
        if (!this.ridingMeleeAim || hasCrosshairTarget) {
            RayTraceResult creatureBlockHit = minecraft.world.rayTraceBlocks(
                    creatureAimOrigin, aimPosition, false, true, false
            );
            if (creatureBlockHit != null
                    && creatureAimOrigin.squareDistanceTo(creatureBlockHit.hitVec) + 1E-6D < creatureAimOrigin.squareDistanceTo(aimPosition)
            ) {
                aimedEntity = null;
                aimPosition = creatureBlockHit.hitVec;
                blockHit = creatureBlockHit;
            }
        }
        RiftMessages.WRAPPER.sendToServer(new RiftRidingAimMessage(
                true, ridingAimMove, aimedEntity == null ? -1 : aimedEntity.getEntityId(),
                aimPosition.x, aimPosition.y, aimPosition.z,
                aimedEntity == null && blockHit != null ? blockHit.getBlockPos() : null,
                aimedEntity == null && blockHit != null ? blockHit.sideHit : null
        ));
    }

    public void beginRidingAim(@NotNull RiftCreature riddenCreature, int ridingAimMove) {
        this.ridingMeleeAim = riddenCreature.isTargetedMeleeRiddenMove(ridingAimMove);
        this.ridingMeleeAimReach = this.ridingMeleeAim ? riddenCreature.calculateRiddenMeleeAimReach() : 0D;
    }

    public void endRidingAim() {
        this.ridingMeleeAim = false;
        this.ridingMeleeAimReach = 0D;
    }

    private boolean isWithinMeleeAimReach(@NotNull AxisAlignedBB bounds, @NotNull Vec3d creatureCenter) {
        Vec3d nearestPoint = new Vec3d(
                Math.clamp(creatureCenter.x, bounds.minX, bounds.maxX),
                Math.clamp(creatureCenter.y, bounds.minY, bounds.maxY),
                Math.clamp(creatureCenter.z, bounds.minZ, bounds.maxZ)
        );
        return this.isWithinMeleeAimReach(nearestPoint, creatureCenter);
    }

    private boolean isWithinMeleeAimReach(@NotNull Vec3d position, @NotNull Vec3d creatureCenter) {
        return position.squareDistanceTo(creatureCenter) <= this.ridingMeleeAimReach * this.ridingMeleeAimReach;
    }

    @Nullable
    public RidingCameraAngles setupRidingCamera(
            @NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature,
            @NotNull Entity riderEntity, double partialTicks
    ) {
        if (!this.enabled || !this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature)
                || riddenCreature == null || riderEntity != minecraft.player
                || minecraft.gameSettings.thirdPersonView != 1
        ) {
            return null;
        }

        double transition = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                this.previousCameraTransition, this.cameraTransition
        );
        Vec3d playerAnchor = new Vec3d(
                MathUtil.slopeResult(partialTicks, true, 0D, 1D, minecraft.player.prevPosX, minecraft.player.posX),
                MathUtil.slopeResult(partialTicks, true, 0D, 1D, minecraft.player.prevPosY, minecraft.player.posY)
                        + minecraft.player.getEyeHeight(),
                MathUtil.slopeResult(partialTicks, true, 0D, 1D, minecraft.player.prevPosZ, minecraft.player.posZ)
        );
        Vec3d playerLookVector = minecraft.player.getLook((float) partialTicks).normalize();
        Vec3d desiredCameraPosition = this.getDesiredCameraPosition(minecraft, riddenCreature, partialTicks, transition);
        Vec3d cameraLookVector = this.getAimCameraLookVector(minecraft, riddenCreature, partialTicks, transition, desiredCameraPosition);
        Vec3d cameraRight = this.getCameraRight(minecraft.player, cameraLookVector, partialTicks);
        Vec3d cameraUp = cameraRight.crossProduct(cameraLookVector).normalize();
        double vanillaCameraDistance = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                minecraft.entityRenderer.thirdPersonDistancePrev,
                minecraft.entityRenderer.thirdPersonDistance
        );
        Vec3d vanillaCameraPosition = this.resolveCameraCollision(
                minecraft, playerAnchor,
                playerAnchor.subtract(playerLookVector.scale(vanillaCameraDistance))
        );
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
        if (!this.enabled || !this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature) || riderEntity != minecraft.player) return currentFov;

        double transition = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                this.previousCameraTransition, this.cameraTransition
        );
        double fovMultiplier = MathUtil.slopeResult(
                transition, true, 0D, 1D,
                1D, this.ridingMeleeAim ? RIDING_MELEE_AIM_FOV_MULTIPLIER : RIDING_AIM_FOV_MULTIPLIER
        );
        return (float) (currentFov * fovMultiplier);
    }

    public void renderRidingAimCrosshair(
            @NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature,
            int screenWidth, int screenHeight
    ) {
        if (!this.enabled || !this.ridingAimActive) return;
        if (!this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature)) return;

        int crosshairLeft = screenWidth / 2 - 7;
        int crosshairTop = screenHeight / 2 - 7;
        minecraft.getTextureManager().bindTexture(Gui.ICONS);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        Gui.drawModalRectWithCustomSizedTexture(crosshairLeft, crosshairTop, 0, 0, 16, 16, 256, 256);
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
    }

    @NotNull
    private Vec3d getDesiredCameraPosition(
            @NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature,
            double partialTicks, double transition
    ) {
        Vec3d lookVector = minecraft.player.getLook((float) partialTicks).normalize();
        Vec3d cameraRight = this.getCameraRight(minecraft.player, lookVector, partialTicks);
        Vec3d cameraAnchor = this.getCameraAnchor(minecraft, riddenCreature, partialTicks, transition);

        double aimSideDistance = this.getAimCameraSideDistance();
        Vec3d aimCameraOffset = cameraRight.scale(aimSideDistance).subtract(lookVector.scale(RIDING_AIM_CAMERA_DISTANCE));

        Vec3d customCameraOffset = aimCameraOffset.add(lookVector.scale(RENDER_DISTANCE_OFFSET));
        Vec3d startingCameraOffset = lookVector.scale(-this.startingCameraDistance);
        Vec3d cameraOffset = this.interpolateVector(startingCameraOffset, customCameraOffset, transition);
        Vec3d desiredCameraPosition = cameraAnchor.add(cameraOffset);
        return this.resolveCameraCollision(minecraft, cameraAnchor, desiredCameraPosition);
    }

    @NotNull
    private Vec3d getAimCameraLookVector(
            @NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature,
            double partialTicks, double transition, @NotNull Vec3d cameraPosition
    ) {
        Vec3d creatureForward = minecraft.player.getLook((float) partialTicks).normalize();
        Vec3d cameraAnchor = this.getCameraAnchor(minecraft, riddenCreature, partialTicks, transition);
        Vec3d focusPosition = cameraAnchor.add(creatureForward.scale(this.getAimCameraFocusDistance()));
        Vec3d cameraToFocus = focusPosition.subtract(cameraPosition);
        if (cameraToFocus.lengthSquared() < 1E-6D) return creatureForward;
        return cameraToFocus.normalize();
    }

    @NotNull
    private Vec3d getRenderedCameraPosition(
            @NotNull Minecraft minecraft, double partialTicks,
            @NotNull Vec3d desiredCameraPosition, @NotNull Vec3d cameraLookVector
    ) {
        Vec3d playerAnchor = new Vec3d(
                MathUtil.slopeResult(
                        partialTicks, true, 0D, 1D,
                        minecraft.player.prevPosX, minecraft.player.posX
                ),
                MathUtil.slopeResult(
                        partialTicks, true, 0D, 1D,
                        minecraft.player.prevPosY, minecraft.player.posY
                ) + minecraft.player.getEyeHeight(),
                MathUtil.slopeResult(
                        partialTicks, true, 0D, 1D,
                        minecraft.player.prevPosZ, minecraft.player.posZ
                )
        );
        Vec3d playerLookVector = minecraft.player.getLook((float) partialTicks).normalize();
        double vanillaCameraDistance = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                minecraft.entityRenderer.thirdPersonDistancePrev,
                minecraft.entityRenderer.thirdPersonDistance
        );
        Vec3d vanillaCameraPosition = this.resolveCameraCollision(
                minecraft, playerAnchor,
                playerAnchor.subtract(playerLookVector.scale(vanillaCameraDistance))
        );
        Vec3d cameraDisplacement = desiredCameraPosition.subtract(vanillaCameraPosition);
        double resolvedVanillaDistance = this.getVanillaThirdPersonCameraDistance(minecraft, playerAnchor, partialTicks);
        return playerAnchor.subtract(cameraLookVector.scale(resolvedVanillaDistance)).add(cameraDisplacement);
    }

    private double getVanillaThirdPersonCameraDistance(@NotNull Minecraft minecraft, @NotNull Vec3d playerAnchor, double partialTicks) {
        double cameraDistance = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                minecraft.entityRenderer.thirdPersonDistancePrev,
                minecraft.entityRenderer.thirdPersonDistance
        );
        float yaw = minecraft.player.rotationYaw;
        float pitch = minecraft.player.rotationPitch;
        double offsetX = -MathHelper.sin(yaw * (float) (Math.PI / 180D))
                * MathHelper.cos(pitch * (float) (Math.PI / 180D)) * cameraDistance;
        double offsetZ = MathHelper.cos(yaw * (float) (Math.PI / 180D))
                * MathHelper.cos(pitch * (float) (Math.PI / 180D)) * cameraDistance;
        double offsetY = -MathHelper.sin(pitch * (float) (Math.PI / 180D)) * cameraDistance;

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
            if (collision == null) continue;
            cameraDistance = Math.min(cameraDistance, playerAnchor.distanceTo(collision.hitVec));
        }
        return cameraDistance;
    }

    private double getAimCameraSideDistance() {
        return this.creatureCameraSideExtent + Math.abs(RIDING_AIM_CAMERA_HORIZONTAL_OFFSET);
    }

    private double getAimCameraFocusDistance() {
        double bodyClearance = this.creatureCameraSideExtent + RIDING_AIM_CAMERA_BODY_MARGIN;
        double convergenceWidth = this.getAimCameraSideDistance() - bodyClearance;
        convergenceWidth = Math.max(0.1D, convergenceWidth);
        return bodyClearance * RIDING_AIM_CAMERA_DISTANCE / convergenceWidth;
    }

    @NotNull
    private Vec3d interpolateVector(@NotNull Vec3d start, @NotNull Vec3d end, double amount) {
        return new Vec3d(
                MathUtil.slopeResult(amount, true, 0D, 1D, start.x, end.x),
                MathUtil.slopeResult(amount, true, 0D, 1D, start.y, end.y),
                MathUtil.slopeResult(amount, true, 0D, 1D, start.z, end.z)
        );
    }

    @NotNull
    private Vec3d resolveCameraCollision(@NotNull Minecraft minecraft, @NotNull Vec3d cameraAnchor, @NotNull Vec3d desiredCameraPosition) {
        Vec3d cameraPath = desiredCameraPosition.subtract(cameraAnchor);
        double desiredDistance = Math.sqrt(cameraPath.lengthSquared());
        if (desiredDistance < 1E-6D) return cameraAnchor;

        double unobstructedDistance = desiredDistance;
        for (int sample = -1; sample < 8; sample++) {
            Vec3d sampleOffset = sample < 0 ? Vec3d.ZERO : new Vec3d(
                    ((sample & 1) * 2 - 1) * 0.1D,
                    ((sample >> 1 & 1) * 2 - 1) * 0.1D,
                    ((sample >> 2 & 1) * 2 - 1) * 0.1D
            );
            Vec3d sampleStart = cameraAnchor.add(sampleOffset);
            Vec3d sampleEnd = desiredCameraPosition.add(sampleOffset);
            RayTraceResult collision = minecraft.world.rayTraceBlocks(sampleStart, sampleEnd, false, true, false);
            if (collision == null) continue;
            unobstructedDistance = Math.min(
                    unobstructedDistance,
                    sampleStart.distanceTo(collision.hitVec)
            );
        }
        return cameraAnchor.add(cameraPath.scale(unobstructedDistance / desiredDistance));
    }

    private double calculateCreatureCameraSideExtent(@NotNull RiftCreature riddenCreature) {
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
        return maximumSideOffset;
    }

    @NotNull
    private Vec3d getCameraAnchor(@NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature, double partialTicks, double transition) {
        double playerX = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                minecraft.player.prevPosX, minecraft.player.posX
        );
        double playerY = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                minecraft.player.prevPosY, minecraft.player.posY
        ) + minecraft.player.getEyeHeight();
        double playerZ = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                minecraft.player.prevPosZ, minecraft.player.posZ
        );
        double creatureX = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                riddenCreature.prevPosX, riddenCreature.posX
        );
        double playerTop = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                minecraft.player.prevPosY, minecraft.player.posY
        ) + minecraft.player.height;
        double creatureBottom = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                riddenCreature.prevPosY, riddenCreature.posY
        );
        double creatureY = (playerTop + creatureBottom) * 0.5D + RIDING_CAMERA_VERTICAL_OFFSET;
        double creatureZ = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                riddenCreature.prevPosZ, riddenCreature.posZ
        );
        return new Vec3d(
                MathUtil.slopeResult(transition, true, 0D, 1D, playerX, creatureX),
                MathUtil.slopeResult(transition, true, 0D, 1D, playerY, creatureY),
                MathUtil.slopeResult(transition, true, 0D, 1D, playerZ, creatureZ)
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

    private void restorePreviousCamera(@NotNull Minecraft minecraft) {
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

    public record RidingCameraAngles(float yaw, float pitch) {}
}
