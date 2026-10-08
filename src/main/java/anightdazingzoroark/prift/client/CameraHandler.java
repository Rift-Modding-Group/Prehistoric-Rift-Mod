package anightdazingzoroark.prift.client;

import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.client.hud.RidingCreatureHUD;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftRidingAimMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CameraHandler {
    public static double RIDING_CAMERA_DISTANCE = 4D;
    public static float RIDING_CAMERA_HORIZONTAL_OFFSET = -2.5f;
    public static float RIDING_CAMERA_VERTICAL_OFFSET = 1f;
    public static double RIDING_CAMERA_ANCHOR_HEIGHT_MULTIPLIER = 1D;
    public static double RIDING_CAMERA_TRANSITION_SPEED = 0.2D;
    public static float RIDING_AIM_FOV_MULTIPLIER = 0.72f;
    public static double RIDING_AIM_DISTANCE = 64D;

    private static final double VANILLA_THIRD_PERSON_DISTANCE = 4D;

    @NotNull
    private final RidingCreatureHUD ridingCreatureHUD;
    private boolean enabled;
    private boolean ridingAimActive;
    private int previousThirdPersonView = -1;
    private double startingCameraDistance = VANILLA_THIRD_PERSON_DISTANCE;
    private double previousCameraTransition;
    private double cameraTransition;
    private double previousAimTransition;
    private double aimTransition;

    public CameraHandler(@NotNull RidingCreatureHUD ridingCreatureHUD) {
        this.ridingCreatureHUD = ridingCreatureHUD;
    }

    public void update(
            @NotNull Minecraft minecraft, @Nullable RiftCreature riddenCreature,
            boolean moveHotbarActive, boolean ridingAimActive
    ) {
        this.ridingAimActive = ridingAimActive;
        boolean ridingCreatureHUDEnabled = this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature);
        boolean shouldEnable = ridingCreatureHUDEnabled && moveHotbarActive;

        if (!ridingCreatureHUDEnabled) {
            this.restorePreviousCamera(minecraft);
            return;
        }

        if (shouldEnable && !this.enabled) {
            this.enabled = true;
            this.previousThirdPersonView = minecraft.gameSettings.thirdPersonView;
            this.startingCameraDistance = this.previousThirdPersonView == 1 ? VANILLA_THIRD_PERSON_DISTANCE : 0D;
            this.previousCameraTransition = 0D;
            this.cameraTransition = 0D;
            this.previousAimTransition = 0D;
            this.aimTransition = 0D;
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

        this.previousAimTransition = this.aimTransition;
        this.aimTransition = MathUtil.slopeResult(
                RIDING_CAMERA_TRANSITION_SPEED, true,
                0D, 1D, this.aimTransition, shouldEnable && ridingAimActive ? 1D : 0D
        );
        if (this.aimTransition < 0.001D) this.aimTransition = 0D;
        else if (1D - this.aimTransition < 0.001D) this.aimTransition = 1D;

        if (!shouldEnable && this.cameraTransition == 0D) this.restorePreviousCamera(minecraft);
    }

    public void sendRidingAimUpdate(
            @NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature, int ridingAimMove
    ) {
        Vec3d lookVector = minecraft.player.getLook(1f).normalize();
        Vec3d cameraRight = this.getCameraRight(minecraft.player, lookVector, 1D);
        Vec3d cameraUp = cameraRight.crossProduct(lookVector).normalize();
        Vec3d cameraAnchor = this.getCameraAnchor(minecraft, riddenCreature, 1D, this.cameraTransition);
        double cameraDistance = MathUtil.slopeResult(
                this.cameraTransition, true, 0D, 1D,
                this.startingCameraDistance, RIDING_CAMERA_DISTANCE
        );
        double horizontalOffset = MathUtil.slopeResult(
                this.cameraTransition, true, 0D, 1D,
                0D, RIDING_CAMERA_HORIZONTAL_OFFSET - riddenCreature.width
        );
        double verticalOffset = MathUtil.slopeResult(
                this.cameraTransition, true, 0D, 1D,
                0D, RIDING_CAMERA_VERTICAL_OFFSET
        );
        Vec3d desiredCameraPosition = cameraAnchor
                .subtract(lookVector.scale(cameraDistance))
                .add(cameraRight.scale(-horizontalOffset))
                .add(cameraUp.scale(-verticalOffset));
        RayTraceResult cameraCollision = minecraft.world.rayTraceBlocks(
                cameraAnchor, desiredCameraPosition, false, true, false
        );
        Vec3d aimStart = desiredCameraPosition;
        if (cameraCollision != null) {
            Vec3d towardAnchor = cameraAnchor.subtract(cameraCollision.hitVec).normalize().scale(0.1D);
            aimStart = cameraCollision.hitVec.add(towardAnchor);
        }

        Vec3d aimEnd = aimStart.add(lookVector.scale(RIDING_AIM_DISTANCE));
        RayTraceResult blockHit = minecraft.world.rayTraceBlocks(aimStart, aimEnd, false, true, false);
        Vec3d aimPosition = blockHit == null ? aimEnd : blockHit.hitVec;
        double closestDistanceSquared = aimStart.squareDistanceTo(aimPosition);
        Entity aimedEntity = null;

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

            double hitDistanceSquared = aimStart.squareDistanceTo(hitPosition);
            if (hitDistanceSquared >= closestDistanceSquared) continue;

            closestDistanceSquared = hitDistanceSquared;
            aimedEntity = candidate;
            aimPosition = hitPosition;
        }

        RiftMessages.WRAPPER.sendToServer(new RiftRidingAimMessage(
                true, ridingAimMove, aimedEntity == null ? -1 : aimedEntity.getEntityId(),
                aimPosition.x, aimPosition.y, aimPosition.z
        ));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRidingCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RiftCreature riddenCreature = this.getRiddenCreature(minecraft);
        if (!this.enabled || !this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature)
                || riddenCreature == null || event.getEntity() != minecraft.player
                || minecraft.gameSettings.thirdPersonView != 1) return;

        double partialTicks = event.getRenderPartialTicks();
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
        Vec3d cameraAnchor = this.getCameraAnchor(minecraft, riddenCreature, partialTicks, transition);
        Vec3d anchorDisplacement = cameraAnchor.subtract(playerAnchor);
        Vec3d lookVector = minecraft.player.getLook((float) partialTicks).normalize();
        Vec3d cameraRight = this.getCameraRight(minecraft.player, lookVector, partialTicks);
        Vec3d cameraUp = cameraRight.crossProduct(lookVector).normalize();
        double horizontalOffset = MathUtil.slopeResult(
                transition, true, 0D, 1D,
                0D, RIDING_CAMERA_HORIZONTAL_OFFSET - riddenCreature.width
        );
        double verticalOffset = MathUtil.slopeResult(
                transition, true, 0D, 1D,
                0D, RIDING_CAMERA_VERTICAL_OFFSET
        );
        double cameraDistance = MathUtil.slopeResult(
                transition, true, 0D, 1D,
                this.startingCameraDistance, RIDING_CAMERA_DISTANCE
        );
        double distanceOffset = VANILLA_THIRD_PERSON_DISTANCE - cameraDistance;

        GlStateManager.translate(
                horizontalOffset - anchorDisplacement.dotProduct(cameraRight),
                verticalOffset - anchorDisplacement.dotProduct(cameraUp),
                distanceOffset + anchorDisplacement.dotProduct(lookVector)
        );
    }

    @SubscribeEvent
    public void onRidingCameraFov(EntityViewRenderEvent.FOVModifier event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RiftCreature riddenCreature = this.getRiddenCreature(minecraft);
        if (!this.enabled || !this.ridingCreatureHUD.isEnabled(minecraft, riddenCreature)
                || event.getEntity() != minecraft.player) return;

        double transition = MathUtil.slopeResult(
                event.getRenderPartialTicks(), true, 0D, 1D,
                this.previousAimTransition, this.aimTransition
        );
        double fovMultiplier = MathUtil.slopeResult(
                transition, true, 0D, 1D,
                1D, RIDING_AIM_FOV_MULTIPLIER
        );
        event.setFOV((float) (event.getFOV() * fovMultiplier));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRidingAimCrosshairRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || !this.enabled || !this.ridingAimActive) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (!this.ridingCreatureHUD.isEnabled(minecraft, this.getRiddenCreature(minecraft))) return;

        int crosshairLeft = event.getResolution().getScaledWidth() / 2 - 7;
        int crosshairTop = event.getResolution().getScaledHeight() / 2 - 7;
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
    private Vec3d getCameraAnchor(
            @NotNull Minecraft minecraft, @NotNull RiftCreature riddenCreature,
            double partialTicks, double transition
    ) {
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
        double creatureY = MathUtil.slopeResult(
                partialTicks, true, 0D, 1D,
                riddenCreature.prevPosY, riddenCreature.posY
        ) + riddenCreature.height * RIDING_CAMERA_ANCHOR_HEIGHT_MULTIPLIER;
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
    private Vec3d getCameraRight(
            @NotNull EntityPlayer player, @NotNull Vec3d lookVector, double partialTicks
    ) {
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
        this.previousAimTransition = 0D;
        this.aimTransition = 0D;
    }

    @Nullable
    private RiftCreature getRiddenCreature(@NotNull Minecraft minecraft) {
        return minecraft.player != null
                && minecraft.player.getRidingEntity() instanceof RiftCreature creature
                && creature.getControllingPassenger() == minecraft.player
                ? creature : null;
    }
}
